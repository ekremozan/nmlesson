@file:androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])

package com.example.nativeminds.audio

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.nativeminds.domain.narration.LessonNarrator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Hosts [NarrationPlayer] behind a [MediaSession] so narration keeps playing and stays
 * controllable (play/pause) from the system media notification while the app is backgrounded or
 * the screen is locked.
 *
 * Narration itself lives in the app-scoped [LessonNarrator], not in this service — this service
 * only keeps the process a foreground one and republishes [LessonNarrator]'s state as a
 * [androidx.media3.common.Player] for the session to expose. It stops with the rest of the app's
 * in-memory state if the process is killed, same as the narrator itself.
 */
@AndroidEntryPoint
class NarrationSessionService : MediaSessionService() {
    @Inject
    lateinit var lessonNarrator: LessonNarrator

    /**
     * Main dispatcher, not the default one: media3 requires every [androidx.media3.common.Player]
     * call — including the `invalidateState()` this scope drives — to happen on the looper the
     * player was built with, and [NarrationPlayer] uses the main looper.
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = NarrationPlayer(
            narrator = lessonNarrator,
            notificationTitle = getString(R.string.narration_notification_title),
        )
        player.startObserving(serviceScope)
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.let { session ->
            session.player.release()
            session.release()
        }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }
}
