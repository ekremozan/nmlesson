package com.example.nativeminds.data.di

import com.example.nativeminds.data.AndroidNetworkMonitor
import com.example.nativeminds.data.MockEntitlementRepository
import com.example.nativeminds.data.NetworkMonitor
import com.example.nativeminds.data.RoomStoryRepository
import com.example.nativeminds.data.observability.LogcatErrorReporter
import com.example.nativeminds.data.remote.FakeRemoteStoryDataSource
import com.example.nativeminds.data.remote.RemoteStoryDataSource
import com.example.nativeminds.domain.observability.ErrorReporter
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.repository.StoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Where the domain contracts meet their implementations. This is the only place in the app that
 * names a concrete data class — feature modules see nothing but the interfaces from `:core:domain`.
 *
 * `@Binds` (not `@Provides`) because both implementations have `@Inject` constructors, so Dagger
 * already knows how to build them; this only tells it which interface they answer to.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun storyRepository(impl: RoomStoryRepository): StoryRepository

    /**
     * Scoped, unlike the other bindings here: the mock holds the entitlement in memory, so a
     * second instance would be a second source of truth — exactly what this interface exists to
     * prevent.
     */
    @Binds
    @Singleton
    abstract fun entitlementRepository(impl: MockEntitlementRepository): EntitlementRepository

    /** Unscoped: it is stateless, so a new instance per injection point costs nothing. */
    @Binds
    abstract fun remoteStoryDataSource(impl: FakeRemoteStoryDataSource): RemoteStoryDataSource

    @Binds
    abstract fun errorReporter(impl: LogcatErrorReporter): ErrorReporter

    @Binds
    abstract fun networkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor
}
