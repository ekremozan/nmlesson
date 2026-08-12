package com.example.nativeminds.data

import android.content.Context
import com.example.nativeminds.data.remote.FakeRemoteStoryDataSource
import com.example.nativeminds.database.NativeMindsDatabase

/**
 * The single place that knows how to build a [StoryRepository]. Callers (feature modules) only
 * ever see the [StoryRepository] interface — `core:database`'s Room types never leak past here.
 */
object StoryRepositoryProvider {
    fun create(context: Context): StoryRepository {
        val dao = NativeMindsDatabase.getInstance(context).storyDao()
        return RoomStoryRepository(
            dao = dao,
            remote = FakeRemoteStoryDataSource(),
            networkMonitor = NetworkMonitor(context),
        )
    }
}
