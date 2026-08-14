package com.example.nativeminds.domain.model

/**
 * Everything the reader screen can be showing, as values rather than as an exception crossing
 * layers — a failure that is part of the emitted type cannot be swallowed by a `catch` on the way
 * up, and the screen is forced to have a branch for it.
 */
sealed interface ReaderDetail {
    data object Loading : ReaderDetail

    data class Available(val access: ReaderAccess) : ReaderDetail

    data class Unavailable(val reason: UnavailableReason) : ReaderDetail
}

/** Why a story cannot be shown — each maps to its own message and its own recovery. */
enum class UnavailableReason {
    /** The text has never been stored and there is no network to fetch it with. */
    OFFLINE,

    /** A fetch was attempted and failed for some other reason. */
    ERROR,

    /** The story itself is gone — deleted by a sync between the list and the tap. */
    STORY_MISSING,
}
