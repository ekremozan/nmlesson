package com.example.nativeminds.database

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoryDaoTest {

    private lateinit var database: NativeMindsDatabase
    private lateinit var dao: StoryDao

    private val stories = listOf(
        StoryEntity(1, "Fiction", "The Lighthouse Keeper's Last Letter", "Weather notes and one page.", 6, hasAudio = true, isLocked = false),
        StoryEntity(2, "Science", "Why Bread Rises", "A single-celled organism at work.", 4, hasAudio = true, isLocked = false),
        StoryEntity(3, "History", "The Cartographer of Missing Islands", "A map that showed land that was never there.", 8, hasAudio = false, isLocked = true),
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NativeMindsDatabase::class.java).build()
        dao = database.storyDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun load(category: String?, query: String): List<StoryEntity> {
        val result = dao.pagingSource(category, query).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page
        return result.data
    }

    @Test
    fun noFilterReturnsEverything() = runTest {
        dao.upsertAll(stories)

        val result = load(category = null, query = "")

        assertEquals(3, result.size)
    }

    @Test
    fun categoryFilterMatchesOnlyThatCategory() = runTest {
        dao.upsertAll(stories)

        val result = load(category = "Science", query = "")

        assertEquals(1, result.size)
        assertEquals("Why Bread Rises", result.first().title)
    }

    @Test
    fun queryMatchesTitleCaseInsensitively() = runTest {
        dao.upsertAll(stories)

        val result = load(category = null, query = "lighthouse")

        assertEquals(1, result.size)
    }

    @Test
    fun queryMatchesTeaserAndCategoryToo() = runTest {
        dao.upsertAll(stories)

        val byTeaser = load(category = null, query = "single-celled")
        val byCategory = load(category = null, query = "history")

        assertEquals(1, byTeaser.size)
        assertEquals(1, byCategory.size)
    }

    @Test
    fun queryWithNoMatchesReturnsEmpty() = runTest {
        dao.upsertAll(stories)

        val result = load(category = null, query = "quantum lullabies")

        assertTrue(result.isEmpty())
    }

    @Test
    fun categoriesAreOrderedByStoryCountThenAlphabetically() = runTest {
        // Fiction: 3, Science: 2, then History and Essays tie at 1 and fall back to alphabetical.
        dao.upsertAll(
            stories + listOf(
                StoryEntity(4, "Fiction", "Second Fiction", "", 3, hasAudio = false, isLocked = false),
                StoryEntity(5, "Fiction", "Third Fiction", "", 3, hasAudio = false, isLocked = false),
                StoryEntity(6, "Science", "Second Science", "", 3, hasAudio = false, isLocked = false),
                StoryEntity(7, "Essays", "An Essay", "", 3, hasAudio = false, isLocked = false),
            ),
        )

        val result = dao.categories().first()

        assertEquals(listOf("Fiction", "Science", "Essays", "History"), result)
    }

    @Test
    fun categoriesIsEmptyWhenThereAreNoStories() = runTest {
        assertTrue(dao.categories().first().isEmpty())
    }

    @Test
    fun upsertUpdatesExistingRowInsteadOfDuplicating() = runTest {
        dao.upsertAll(stories)

        dao.upsertAll(listOf(stories[0].copy(title = "Updated Title")))

        assertEquals(3, dao.count())
        val result = load(category = null, query = "")
        assertEquals("Updated Title", result.first { it.id == 1L }.title)
    }
}
