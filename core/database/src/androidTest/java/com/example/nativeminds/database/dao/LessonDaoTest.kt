package com.example.nativeminds.database.dao

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.entity.LessonEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonDaoTest {
    private lateinit var database: NativeMindsDatabase
    private lateinit var dao: LessonDao

    private val lessons = listOf(
        LessonEntity(1, "Biyoloji", "Hücre Yapısı ve Organeller", "Zarın içindeki küçük fabrika.", 6, hasAudio = true, isLocked = false),
        LessonEntity(2, "Kimya", "Maddenin Yapısı ve Atom Modelleri", "Her şeyin en küçük yapı taşı.", 4, hasAudio = true, isLocked = false),
        LessonEntity(3, "Tarih", "İstanbul'un Fethi ve Sonuçları", "Bir çağın kapanıp diğerinin açılması.", 8, hasAudio = false, isLocked = true),
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, NativeMindsDatabase::class.java).build()
        dao = database.lessonDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun load(subject: String?, query: String): List<LessonEntity> {
        val result = dao.pagingSource(subject, query).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page
        return result.data
    }

    @Test
    fun noFilterReturnsEverything() = runTest {
        dao.upsertAll(lessons)

        val result = load(subject = null, query = "")

        assertEquals(3, result.size)
    }

    @Test
    fun subjectFilterMatchesOnlyThatSubject() = runTest {
        dao.upsertAll(lessons)

        val result = load(subject = "Kimya", query = "")

        assertEquals(1, result.size)
        assertEquals("Maddenin Yapısı ve Atom Modelleri", result.first().title)
    }

    @Test
    fun queryMatchesTitleCaseInsensitively() = runTest {
        dao.upsertAll(lessons)

        val result = load(subject = null, query = "hücre")

        assertEquals(1, result.size)
    }

    @Test
    fun queryMatchesTeaserAndSubjectToo() = runTest {
        dao.upsertAll(lessons)

        val byTeaser = load(subject = null, query = "fabrika")
        val bySubject = load(subject = null, query = "tarih")

        assertEquals(1, byTeaser.size)
        assertEquals(1, bySubject.size)
    }

    @Test
    fun queryWithNoMatchesReturnsEmpty() = runTest {
        dao.upsertAll(lessons)

        val result = load(subject = null, query = "quantum lullabies")

        assertTrue(result.isEmpty())
    }

    @Test
    fun subjectsAreOrderedByLessonCountThenAlphabetically() = runTest {
        dao.upsertAll(
            lessons + listOf(
                LessonEntity(4, "Biyoloji", "Kalıtım ve Mendel Genetiği", "", 3, hasAudio = false, isLocked = false),
                LessonEntity(5, "Biyoloji", "DNA ve Protein Sentezi", "", 3, hasAudio = false, isLocked = false),
                LessonEntity(6, "Kimya", "Periyodik Sistem", "", 3, hasAudio = false, isLocked = false),
                LessonEntity(7, "Coğrafya", "Türkiye'nin Coğrafi Konumu", "", 3, hasAudio = false, isLocked = false),
            ),
        )

        val result = dao.subjects().first()

        assertEquals(listOf("Biyoloji", "Kimya", "Coğrafya", "Tarih"), result)
    }

    @Test
    fun subjectsIsEmptyWhenThereAreNoLessons() = runTest {
        assertTrue(dao.subjects().first().isEmpty())
    }

    @Test
    fun upsertUpdatesExistingRowInsteadOfDuplicating() = runTest {
        dao.upsertAll(lessons)

        dao.upsertAll(listOf(lessons[0].copy(title = "Updated Title")))

        assertEquals(3, dao.count())
        val result = load(subject = null, query = "")
        assertEquals("Updated Title", result.first { it.id == 1L }.title)
    }
}
