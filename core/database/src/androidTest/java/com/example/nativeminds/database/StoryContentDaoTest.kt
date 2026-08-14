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
class StoryContentDaoTest {
    private lateinit var database: NativeMindsDatabase
    private lateinit var storyDao: StoryDao
    private lateinit var contentDao: StoryContentDao

    private val story = StoryEntity(
        id = 1,
        category = "Fiction",
        title = "The Lighthouse Keeper's Last Letter",
        teaser = "Weather notes and one page.",
        minutes = 6,
        hasAudio = true,
        isLocked = false,
        image = "cover_01",
    )

    private val content = StoryContentEntity(
        storyId = 1,
        author = "Marguerite Halloran",
        body = "Forty winters he kept the log.\n\nThe keeper's handwriting changed twice.",
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NativeMindsDatabase::class.java).build()
        storyDao = database.storyDao()
        contentDao = database.storyContentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun deleteEveryStory() {
        database.openHelper.writableDatabase.execSQL("DELETE FROM stories")
    }

    @Test
    fun contentIsAbsentUntilItIsStored() = runTest {
        storyDao.upsertAll(listOf(story))

        assertNull(contentDao.observeContent(story.id).first())
    }

    @Test
    fun storingContentMakesItReadable() = runTest {
        storyDao.upsertAll(listOf(story))

        contentDao.upsert(content)

        assertEquals(content, contentDao.observeContent(story.id).first())
    }

    @Test
    fun deletingTheStoryTakesItsContentWithIt() = runTest {
        storyDao.upsertAll(listOf(story))
        contentDao.upsert(content)

        deleteEveryStory()

        assertNull(contentDao.observeContent(story.id).first())
    }

    @Test
    fun observingAStoryEmitsItAndThenNullOnceItIsGone() = runTest {
        storyDao.upsertAll(listOf(story))
        assertEquals(story, storyDao.observeStory(story.id).first())

        deleteEveryStory()

        assertNull(storyDao.observeStory(story.id).first())
    }
}
