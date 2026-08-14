package com.example.nativeminds.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.example.nativeminds.common.di.IoDispatcher
import com.example.nativeminds.data.local.DummyStoryContentSeed
import com.example.nativeminds.data.local.DummyStorySeed
import com.example.nativeminds.data.mapper.toDomain
import com.example.nativeminds.data.mapper.toEntity
import com.example.nativeminds.data.remote.RemoteStoryDataSource
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.StoryContentDao
import com.example.nativeminds.database.StoryDao
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.domain.repository.StoryRepository
import com.example.nativeminds.model.Story
import com.example.nativeminds.model.StoryContent
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 20

/**
 * The database is injected alongside its DAOs for one reason: seeding writes two tables that must
 * land together, and `withTransaction` is the only place that guarantee can be made. A crash
 * between the two writes would otherwise leave a catalog whose stories can never be opened,
 * because `count()` would never be zero again.
 */
class RoomStoryRepository @Inject constructor(
    private val database: NativeMindsDatabase,
    private val dao: StoryDao,
    private val contentDao: StoryContentDao,
    private val remote: RemoteStoryDataSource,
    private val networkMonitor: NetworkMonitor,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : StoryRepository {
    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> =
        Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            dao.pagingSource(category, query)
        }.flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun categories(): Flow<List<String>> = dao.categories()

    override fun story(id: Long): Flow<Story?> = dao.observeStory(id).map { it?.toDomain() }

    override fun storyContent(id: Long): Flow<StoryContent?> =
        contentDao.observeContent(id).map { it?.toDomain() }

    override suspend fun refreshContent(id: Long) = withContext(ioDispatcher) {
        if (!networkMonitor.isOnline()) {
            throw OfflineException("Cannot fetch content for story $id while offline")
        }
        contentDao.upsert(remote.fetchContent(id).toEntity())
    }

    override suspend fun syncIfNeeded() = withContext(ioDispatcher) {
        if (dao.count() == 0) {
            database.withTransaction {
                dao.upsertAll(DummyStorySeed.stories.map { it.toEntity() })
                contentDao.upsertAll(DummyStoryContentSeed.content.map { it.toEntity() })
            }
        }
        if (networkMonitor.isOnline()) {
            dao.upsertAll(remote.fetchStories().map { it.toEntity() })
        }
        Unit
    }
}
