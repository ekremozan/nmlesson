@file:androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])

package com.example.nativeminds.audio

import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import com.example.nativeminds.domain.model.NarrationState
import com.example.nativeminds.domain.narration.StoryNarrator
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private val AVAILABLE_COMMANDS = Player.Commands.Builder()
    .add(Player.COMMAND_PLAY_PAUSE)
    .add(Player.COMMAND_STOP)
    .add(Player.COMMAND_GET_METADATA)
    .build()

/**
 * Exposes [StoryNarrator] as a [SimpleBasePlayer] so [NarrationSessionService] can host it behind
 * a [androidx.media3.session.MediaSession] — the system media notification, its play/pause
 * action, and audio-focus handling all come from that adaptation rather than hand-written
 * notification code.
 *
 * Position isn't real audio time: [NarrationState.Playing.sentenceIndex] stands in for it so the
 * session has something monotonic to report, matching the sentence-level granularity narration
 * actually resumes at.
 */
internal class NarrationPlayer(
    private val narrator: StoryNarrator,
    private val notificationTitle: String,
) : SimpleBasePlayer(Looper.getMainLooper()) {

    private var released = false

    /**
     * Only the part of narration the session can actually show is worth reacting to. The narrator
     * republishes on every word the engine speaks so the reader can follow along; rebuilding the
     * media notification at that rate would be pure waste, since nothing it displays changes
     * between two words of the same sentence.
     */
    fun startObserving(scope: CoroutineScope) {
        narrator.state
            .map { it.sessionView() }
            .distinctUntilChanged()
            .onEach { invalidateState() }
            .launchIn(scope)
    }

    override fun getState(): State {
        val narration = narrator.state.value
        val storyId = narration.activeStoryIdOrNull()
            ?: return State.Builder()
                .setAvailableCommands(AVAILABLE_COMMANDS)
                .setPlaybackState(Player.STATE_IDLE)
                .build()

        return State.Builder()
            .setAvailableCommands(AVAILABLE_COMMANDS)
            .setPlaybackState(Player.STATE_READY)
            .setPlayWhenReady(
                narration is NarrationState.Playing,
                Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST,
            )
            .setPlaylist(
                listOf(
                    MediaItemData.Builder(storyId)
                        .setMediaItem(
                            MediaItem.Builder()
                                .setMediaId(storyId.toString())
                                .setMediaMetadata(
                                    MediaMetadata.Builder().setTitle(notificationTitle).build(),
                                )
                                .build(),
                        )
                        .build(),
                ),
            )
            .setCurrentMediaItemIndex(0)
            .setContentPositionMs(narration.sentenceIndexOrZero().toLong())
            .build()
    }

    override fun handleSetPlayWhenReady(playWhenReady: Boolean): ListenableFuture<*> {
        if (released) return Futures.immediateVoidFuture()
        if (playWhenReady) narrator.resume() else narrator.pause()
        return Futures.immediateVoidFuture()
    }

    override fun handleStop(): ListenableFuture<*> {
        if (released) return Futures.immediateVoidFuture()
        narrator.stop()
        return Futures.immediateVoidFuture()
    }

    override fun handlePrepare(): ListenableFuture<*> = Futures.immediateVoidFuture()

    /**
     * Narration outlives this player: the session is a control surface over the app-scoped
     * narrator, never its owner. Once released, commands media3 issues while tearing the service
     * down MUST NOT reach the narrator — a `stop()` arriving from that teardown would end
     * narration the reader had merely paused, and the next tap would start the story over.
     */
    override fun handleRelease(): ListenableFuture<*> {
        released = true
        return Futures.immediateVoidFuture()
    }
}

private data class SessionView(
    val storyId: Long?,
    val isPlaying: Boolean,
    val sentenceIndex: Int,
)

private fun NarrationState.sessionView(): SessionView = SessionView(
    storyId = activeStoryIdOrNull(),
    isPlaying = this is NarrationState.Playing,
    sentenceIndex = sentenceIndexOrZero(),
)

private fun NarrationState.activeStoryIdOrNull(): Long? = when (this) {
    is NarrationState.Playing -> storyId
    is NarrationState.Paused -> storyId
    else -> null
}

private fun NarrationState.sentenceIndexOrZero(): Int = when (this) {
    is NarrationState.Playing -> sentenceIndex
    is NarrationState.Paused -> sentenceIndex
    else -> 0
}
