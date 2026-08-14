package com.example.nativeminds.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonContentDaoTest {
    private lateinit var database: NativeMindsDatabase
    private lateinit var lessonDao: LessonDao
    private lateinit var contentDao: LessonContentDao

    private val lesson = LessonEntity(
        id = 1,
        subject = "Biyoloji",
        title = "Hücre Yapısı ve Organeller",
        teaser = "Zarın içindeki küçük fabrika.",
        minutes = 6,
        hasAudio = true,
        isLocked = false,
        image = "subject_biology",
    )

    private val content = LessonContentEntity(
        lessonId = 1,
        author = "Marguerite Halloran",
        body = "Forty winters he kept the log.\n\nThe keeper's handwriting changed twice.",
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NativeMindsDatabase::class.java).build()
        lessonDao = database.lessonDao()
        contentDao = database.lessonContentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun deleteEveryLesson() {
        database.openHelper.writableDatabase.execSQL("DELETE FROM lessons")
    }

    @Test
    fun contentIsAbsentUntilItIsStored() = runTest {
        lessonDao.upsertAll(listOf(lesson))

        assertNull(contentDao.observeContent(lesson.id).first())
    }

    @Test
    fun storingContentMakesItReadable() = runTest {
        lessonDao.upsertAll(listOf(lesson))

        contentDao.upsert(content)

        assertEquals(content, contentDao.observeContent(lesson.id).first())
    }

    @Test
    fun deletingTheLessonTakesItsContentWithIt() = runTest {
        lessonDao.upsertAll(listOf(lesson))
        contentDao.upsert(content)

        deleteEveryLesson()

        assertNull(contentDao.observeContent(lesson.id).first())
    }

    @Test
    fun observingALessonEmitsItAndThenNullOnceItIsGone() = runTest {
        lessonDao.upsertAll(listOf(lesson))
        assertEquals(lesson, lessonDao.observeLesson(lesson.id).first())

        deleteEveryLesson()

        assertNull(lessonDao.observeLesson(lesson.id).first())
    }
}
