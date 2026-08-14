package com.example.nativeminds.domain.model

/**
 * The stretch of the current sentence a speech engine is saying right now, as character indices
 * into that sentence ([end] exclusive).
 *
 * Null means the narrator does not know yet — the gap between handing a sentence to the engine and
 * the engine reporting its first word. Callers mark nothing across it. A narrator on a platform
 * that cannot report words at all (`onRangeStart` only exists from API 26) publishes the whole
 * sentence as the range instead, so "coarse" is something the narrator states rather than
 * something every caller has to guess at.
 */
data class SpokenRange(val start: Int, val end: Int)

/**
 * What a [com.example.nativeminds.domain.narration.StoryNarrator] is doing right now.
 *
 * [Playing] and [Paused] both carry the sentence position because that is the unit narration can
 * actually resume at — a [StoryNarrator] speaks one sentence per utterance, so "resume" means
 * "re-speak from this index". [SpokenRange] is finer than that on purpose: it is precise enough to
 * follow along with a highlight, but it is never what a resume seeks to, because the engine can
 * only be asked to speak text, not to seek within it.
 */
sealed interface NarrationState {
    data object Idle : NarrationState

    data class Playing(
        val storyId: Long,
        val sentenceIndex: Int,
        val totalSentences: Int,
        val spokenRange: SpokenRange? = null,
    ) : NarrationState

    data class Paused(
        val storyId: Long,
        val sentenceIndex: Int,
        val totalSentences: Int,
        val spokenRange: SpokenRange? = null,
    ) : NarrationState

    data class Unavailable(val storyId: Long, val reason: NarrationUnavailableReason) :
        NarrationState
}

/**
 * Scopes a narrator-wide state to one screen's story: a [Playing]/[Paused]/[Unavailable] that
 * belongs to a *different* story reads as [NarrationState.Idle] here, so opening story B's
 * detail screen while story A is still narrating in the background never makes B's control show
 * a Pause/Resume affordance for audio it doesn't own.
 */
fun NarrationState.forStory(storyId: Long): NarrationState = when (this) {
    is NarrationState.Playing -> if (this.storyId == storyId) this else NarrationState.Idle
    is NarrationState.Paused -> if (this.storyId == storyId) this else NarrationState.Idle
    is NarrationState.Unavailable -> if (this.storyId == storyId) this else NarrationState.Idle
    NarrationState.Idle -> NarrationState.Idle
}
