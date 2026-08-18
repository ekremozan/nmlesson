package com.example.nativeminds.data.remote

import com.example.nativeminds.data.remote.dto.LessonContentDto
import com.example.nativeminds.data.remote.dto.LessonDto
import com.example.nativeminds.data.remote.mapper.toDomain
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import java.io.IOException
import javax.inject.Inject

private const val LESSONS_TABLE = "lessons"
private const val LESSON_CONTENT_TABLE = "lesson_content"

/**
 * The real backend: Supabase Postgrest over the `lessons`/`lesson_content` tables, read-only, per
 * `specs/004-remote-lesson-content/contracts/supabase-schema.md`.
 */
class SupabaseRemoteLessonDataSource @Inject constructor(
    private val supabase: SupabaseClient,
) : RemoteLessonDataSource {
    override suspend fun fetchLessons(language: String): List<Lesson> =
        supabase.postgrest.from(LESSONS_TABLE).select {
            filter { eq("language", language) }
        }.decodeList<LessonDto>()
            .map { it.toDomain() }

    override suspend fun fetchContent(lessonId: Long, language: String): LessonContent {
        val rows = supabase.postgrest.from(LESSON_CONTENT_TABLE).select {
            filter {
                eq("lesson_id", lessonId)
                eq("language", language)
            }
        }.decodeList<LessonContentDto>()
        return rows.firstOrNull()?.toDomain()
            ?: throw IOException("No content for lesson $lessonId ($language)")
    }

    override suspend fun fetchAllContent(language: String): List<LessonContent> =
        supabase.postgrest.from(LESSON_CONTENT_TABLE).select {
            filter { eq("language", language) }
        }.decodeList<LessonContentDto>()
            .map { it.toDomain() }
}
