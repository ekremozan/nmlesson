package com.example.nativeminds.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `lesson_content` table's wire shape, as Supabase's Postgrest client returns it — never
 * passed outside `:core:data`. [body] holds the whole lesson with a blank line between paragraphs,
 * the same on-the-wire shape `LessonContentEntity.body` already stores locally.
 */
@Serializable
data class LessonContentDto(
    @SerialName("lesson_id") val lessonId: Long,
    val author: String,
    val body: String,
    val language: String,
)
