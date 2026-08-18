package com.example.nativeminds.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.model.NarrationUnavailableReason
import com.example.nativeminds.domain.narration.LessonNarrator
import com.example.nativeminds.domain.observability.ErrorReporter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ERROR_CONTEXT = "TextToSpeechNarrator"

/**
 * [LessonNarrator] backed by the platform [TextToSpeech] engine, speaking one sentence per utterance
 * so pausing has a real point to resume from. Where that point is lives in [NarrationQueue]; this
 * class only carries the queue's decisions out to the engine, the audio focus, and the media
 * session service.
 *
 * **Everything runs on the main thread.** The engine delivers its completion callbacks on a binder
 * thread, so without this confinement a finished sentence would race a tap over the same queue —
 * and a queue read from the wrong thread has no guarantee of seeing the sentences the other thread
 * wrote, which is exactly how narration silently falls back to "start over" instead of resuming.
 * Media3 also requires [NarrationPlayer] to be invalidated on this looper.
 */
@Singleton
class TextToSpeechNarrator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val errorReporter: ErrorReporter,
) : LessonNarrator {
    private val _state = MutableStateFlow<NarrationState>(NarrationState.Idle)
    override val state: StateFlow<NarrationState> = _state.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val queue = NarrationQueue(
        reportsWordRanges = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O,
    )

    private var engineReady = false
    private var pendingStart: (() -> Unit)? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    /**
     * Reacts to a transient or permanent loss the same way — by pausing — which is what leaves
     * narration resumable (FR-012) instead of continuing to speak over, or silently losing, an
     * interruption like an incoming call.
     */
    private val onAudioFocusChange = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            -> pause()
        }
    }

    private val engine: TextToSpeech = TextToSpeech(context) { status ->
        onMainThread {
            engineReady = status == TextToSpeech.SUCCESS
            if (engineReady) {
                pendingStart?.invoke()
            } else {
                errorReporter.report(
                    IllegalStateException("TextToSpeech init failed: status=$status"),
                    ERROR_CONTEXT,
                )
            }
            pendingStart = null
        }
    }.apply {
        setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onMainThread {
                        queue.onUtteranceStarted(utteranceId)
                        publish()
                    }
                }

                override fun onRangeStart(
                    utteranceId: String?,
                    start: Int,
                    end: Int,
                    frame: Int,
                ) {
                    onMainThread {
                        queue.onRangeStart(utteranceId, start, end)
                        publish()
                    }
                }

                override fun onDone(utteranceId: String?) {
                    onMainThread {
                        queue.onUtteranceFinished(utteranceId)
                        publish()
                        if (queue.state == NarrationState.Idle) {
                            abandonAudioFocus()
                            stopSessionService()
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) = Unit

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onMainThread {
                        errorReporter.report(
                            IllegalStateException(
                                "TextToSpeech utterance failed: id=$utteranceId, errorCode=$errorCode",
                            ),
                            ERROR_CONTEXT,
                        )
                        queue.onUtteranceFinished(utteranceId)
                        publish()
                        if (queue.state == NarrationState.Idle) {
                            abandonAudioFocus()
                            stopSessionService()
                        }
                    }
                }
            },
        )
    }

    override fun start(lessonId: Long, paragraphs: List<String>) {
        onMainThread {
            if (!engineReady) {
                pendingStart = { start(lessonId, paragraphs) }
                return@onMainThread
            }
            if (engine.isLanguageAvailable(Locale.getDefault()) < TextToSpeech.LANG_AVAILABLE) {
                errorReporter.report(
                    IllegalStateException("No TTS voice installed for ${Locale.getDefault()}"),
                    ERROR_CONTEXT,
                )
                queue.markUnavailable(lessonId, NarrationUnavailableReason.LANGUAGE_UNSUPPORTED)
                publish()
                return@onMainThread
            }
            requestAudioFocus()
            startSessionService()
            enqueue(queue.start(lessonId, paragraphs))
        }
    }

    override fun pause() {
        onMainThread {
            if (queue.pause()) {
                engine.stop()
                publish()
            }
        }
    }

    override fun resume() {
        onMainThread {
            val remaining = queue.resume()
            if (remaining.isEmpty()) return@onMainThread
            requestAudioFocus()
            startSessionService()
            enqueue(remaining)
        }
    }

    override fun stop() {
        onMainThread {
            queue.stop()
            engine.stop()
            publish()
            abandonAudioFocus()
            stopSessionService()
        }
    }

    /**
     * Hands the rest of the lesson to the engine in one go, so playback continues on the engine's
     * own queue rather than waiting for this app to be told each sentence has finished.
     *
     * The first utterance flushes whatever the engine was holding — that is what makes a resume
     * replace the abandoned tail rather than queue up behind it.
     */
    private fun enqueue(utterances: List<NarrationQueue.Utterance>) {
        publish()
        utterances.forEachIndexed { position, utterance ->
            val queueMode =
                if (position == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            engine.speak(utterance.text, queueMode, null, utterance.id)
        }
    }

    private fun publish() {
        _state.value = queue.state
    }

    /**
     * Runs [block] now when already on the main thread so a tap takes effect before the caller's
     * next line reads [state], and posts otherwise — which is the case for the engine's binder
     * thread callbacks.
     */
    private fun onMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    /**
     * Media3 promotes [NarrationSessionService] to the foreground and posts its media notification
     * once the session has a playing player, which is what keeps narration alive — and controllable
     * — after the app leaves the foreground (FR-013). Without this the service is only ever
     * declared, never running, and the process is free to be frozen with the narration in it.
     */
    private fun startSessionService() {
        runCatching {
            context.startService(Intent(context, NarrationSessionService::class.java))
        }.exceptionOrNull()?.let { errorReporter.report(it, ERROR_CONTEXT) }
    }

    private fun stopSessionService() {
        context.stopService(Intent(context, NarrationSessionService::class.java))
    }

    private fun requestAudioFocus() {
        if (audioFocusRequest != null) return
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .setOnAudioFocusChangeListener(onAudioFocusChange)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                onAudioFocusChange,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            errorReporter.report(
                IllegalStateException("Audio focus request denied: result=$result"),
                ERROR_CONTEXT,
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(onAudioFocusChange)
        }
    }
}
