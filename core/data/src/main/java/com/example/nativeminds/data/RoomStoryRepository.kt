package com.example.nativeminds.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.nativeminds.data.local.DummyStorySeed
import com.example.nativeminds.data.mapper.toDomain
import com.example.nativeminds.data.mapper.toEntity
import com.example.nativeminds.data.remote.RemoteStoryDataSource
import com.example.nativeminds.database.StoryDao
import com.example.nativeminds.model.Story
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PAGE_SIZE = 20

class RoomStoryRepository(
    private val dao: StoryDao,
    private val remote: RemoteStoryDataSource,
    private val networkMonitor: NetworkMonitor,
) : StoryRepository {

    override fun pagedStories(category: String?, query: String): Flow<PagingData<Story>> =
        Pager(PagingConfig(pageSize = PAGE_SIZE)) {
            dao.pagingSource(category, query)
        }.flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override suspend fun syncIfNeeded() {
        if (dao.count() == 0) {
            dao.upsertAll(DummyStorySeed.stories.map { it.toEntity() })
        }
        if (networkMonitor.isOnline()) {
            // Failures are swallowed on purpose: the DB (seeded or previously synced) stays the
            // source of truth and the UI keeps working offline. Once crash reporting is wired up
            // (see README "Cut Corners"), this is where it gets reported.
            runCatching { remote.fetchStories() }
                .onSuccess { stories -> dao.upsertAll(stories.map { it.toEntity() }) }
        }
    }
}
