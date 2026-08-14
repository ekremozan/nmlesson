package com.example.nativeminds.model

/**
 * A short lesson, as known by the domain layer — no UI formatting, no Android dependency.
 *
 * Shared by every feature that needs a lesson (home, reader, library): the single definition here
 * is what mappers in each feature convert into their own UI model.
 */
data class Lesson(
    val id: Long,
    val subject: String,
    val title: String,
    val teaser: String,
    val minutes: Int,
    val hasAudio: Boolean,
    val isLocked: Boolean,
    val image: String,
)
