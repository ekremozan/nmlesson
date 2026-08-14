package com.example.nativeminds.data.remote

import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent

/** The future real API. [FakeRemoteLessonDataSource] is the only implementation until it exists. */
interface RemoteLessonDataSource {
    suspend fun fetchLessons(): List<Lesson>

    /**
     * The text of one lesson. Fetched on demand rather than with the catalog, because a list of
     * cards has no use for six full lessons.
     *
     * Throws when the lesson is unknown to the source — the caller turns that into something the
     * reader can see.
     */
    suspend fun fetchContent(lessonId: Long): LessonContent
}
