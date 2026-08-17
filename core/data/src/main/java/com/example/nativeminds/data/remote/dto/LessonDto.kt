package com.example.nativeminds.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `lessons` table's wire shape, as Supabase's Postgrest client returns it — never passed
 * outside `:core:data`. See `specs/004-remote-lesson-content/contracts/supabase-schema.md`.
 */
@Serializable
data class LessonDto(
    val id: Long,
    val subject: String,
    val title: String,
    val teaser: String,
    val minutes: Int,
    @SerialName("has_audio") val hasAudio: Boolean,
    @SerialName("is_locked") val isLocked: Boolean,
    val image: String,
    val language: String,
)
