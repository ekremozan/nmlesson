package com.example.nativeminds.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.nativeminds.common.di.IoDispatcher
import com.example.nativeminds.data.local.DummyLessonContentSeed
import com.example.nativeminds.data.local.DummyLessonSeed
import com.example.nativeminds.data.mapper.toDomain
import com.example.nativeminds.data.mapper.toEntity
import com.example.nativeminds.data.remote.RemoteLessonDataSource
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.LessonContentDao
import com.example.nativeminds.database.LessonDao
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 20

/**
 * The database is injected alongside its DAOs for one reason: seeding writes two tables that must
 * land together, and `withTransaction` is the only place that guarantee can be made. A crash
 * between the two writes would otherwise leave a catalog whose lessons can never be opened,
 * because `count()` would never be zero again.
 */
class RoomLessonRepository @Inject constructor(
    private val database: NativeMindsDatabase,
    private val dao: LessonDao,
    private val contentDao: LessonContentDao,
    private val remote: RemoteLessonDataSource,
    private val networkMonitor: NetworkMonitor,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LessonRepository {
    override fun pagedLessons(subject: String?, query: String): Flow<PagingData<Lesson>> =
        Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            dao.pagingSource(subject, query)
        }.flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun subjects(): Flow<List<String>> = dao.subjects()

    override fun lesson(id: Long): Flow<Lesson?> = dao.observeLesson(id).map { it?.toDomain() }

    override fun lessonContent(id: Long): Flow<LessonContent?> =
        contentDao.observeContent(id).map { it?.toDomain() }

    override suspend fun refreshContent(id: Long) = withContext(ioDispatcher) {
        if (!networkMonitor.isOnline()) {
            throw OfflineException("Cannot fetch content for lesson $id while offline")
        }
        contentDao.upsert(remote.fetchContent(id).toEntity())
    }

    override suspend fun syncIfNeeded() = withContext(ioDispatcher) {
        if (dao.count() == 0) {
            database.withTransaction {
                dao.upsertAll(DummyLessonSeed.lessons.map { it.toEntity() })
                contentDao.upsertAll(DummyLessonContentSeed.content.map { it.toEntity() })
            }
        }
        if (networkMonitor.isOnline()) {
            dao.upsertAll(remote.fetchLessons().map { it.toEntity() })
        }
        Unit
    }
}
