package com.example.nativeminds.data.remote

import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent

/** The lesson catalog's remote source. [com.example.nativeminds.data.remote.SupabaseRemoteLessonDataSource] is the only implementation. */
interface RemoteLessonDataSource {
    /** [language] is a Supabase `language` column value, e.g. `"tr"` or `"en"`. */
    suspend fun fetchLessons(language: String): List<Lesson>

    /**
     * The text of one lesson. Used as a fallback for a lesson [fetchAllContent] missed — an
     * interrupted sync, or one added remotely since.
     *
     * Throws when the lesson is unknown to the source — the caller turns that into something the
     * reader can see.
     */
    suspend fun fetchContent(lessonId: Long, language: String): LessonContent

    /**
     * Every lesson's text for [language], fetched in the same pass as [fetchLessons] so offline
     * reading works before a lesson has ever been opened online.
     */
    suspend fun fetchAllContent(language: String): List<LessonContent>
}
