package com.example.nativeminds.domain.narration

import com.example.nativeminds.domain.model.NarrationState
import kotlinx.coroutines.flow.StateFlow

/**
 * On-device narration of one story at a time, offline by construction — implementations must not
 * require a network call to speak.
 *
 * There is exactly one narrator for the whole app (single active session, per the feature spec):
 * calling [start] for a different [com.example.nativeminds.domain.model.NarrationState.Playing]
 * or [com.example.nativeminds.domain.model.NarrationState.Paused] story implicitly stops the
 * previous one first.
 */
interface StoryNarrator {
    val state: StateFlow<NarrationState>

    /** Starts narrating [paragraphs] for [storyId] from the beginning. */
    fun start(storyId: Long, paragraphs: List<String>)

    /** No-op unless [state] is [NarrationState.Playing]. */
    fun pause()

    /** No-op unless [state] is [NarrationState.Paused]. */
    fun resume()

    /** Always returns [state] to [NarrationState.Idle] and discards the current position. */
    fun stop()
}
