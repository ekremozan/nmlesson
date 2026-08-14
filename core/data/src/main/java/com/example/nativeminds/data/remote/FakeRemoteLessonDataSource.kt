package com.example.nativeminds.data.remote

import com.example.nativeminds.data.local.DummyLessonContentSeed
import com.example.nativeminds.data.local.DummyLessonSeed
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.delay

private const val SIMULATED_ROUND_TRIP_MS = 300L

/**
 * Stand-in for the real backend — returns the same seed catalog after a short simulated
 * round-trip. Cut corner: no backend exists yet; swapping this for a Retrofit/Ktor implementation
 * later changes nothing on [com.example.nativeminds.data.RoomLessonRepository]'s side.
 */
class FakeRemoteLessonDataSource @Inject constructor() : RemoteLessonDataSource {
    override suspend fun fetchLessons(): List<Lesson> {
        delay(SIMULATED_ROUND_TRIP_MS)
        return DummyLessonSeed.lessons
    }

    override suspend fun fetchContent(lessonId: Long): LessonContent {
        delay(SIMULATED_ROUND_TRIP_MS)
        return DummyLessonContentSeed.content.firstOrNull { it.lessonId == lessonId }
            ?: throw IOException("No content for lesson $lessonId")
    }
}
