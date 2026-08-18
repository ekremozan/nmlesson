package com.example.nativeminds.data.di

import com.example.nativeminds.data.AndroidNetworkMonitor
import com.example.nativeminds.data.ContentLanguageProvider
import com.example.nativeminds.data.DeviceContentLanguageProvider
import com.example.nativeminds.data.LastSyncedLanguageStore
import com.example.nativeminds.data.MockThemeRepository
import com.example.nativeminds.data.NetworkMonitor
import com.example.nativeminds.data.RoomLessonRepository
import com.example.nativeminds.data.SharedPreferencesEntitlementRepository
import com.example.nativeminds.data.SharedPreferencesLastSyncedLanguageStore
import com.example.nativeminds.data.remote.RemoteLessonDataSource
import com.example.nativeminds.data.remote.SupabaseRemoteLessonDataSource
import com.example.nativeminds.data.remote.quiz.GeminiQuizDataSource
import com.example.nativeminds.data.remote.quiz.GeminiRemoteQuizDataSource
import com.example.nativeminds.data.repository.QuizRepositoryImpl
import com.example.nativeminds.domain.repository.EntitlementRepository
import com.example.nativeminds.domain.repository.LessonRepository
import com.example.nativeminds.domain.repository.QuizRepository
import com.example.nativeminds.domain.repository.ThemeRepository
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
    abstract fun lessonRepository(impl: RoomLessonRepository): LessonRepository

    /**
     * Scoped, unlike the other bindings here: the entitlement lives in a single
     * `SharedPreferences`-backed instance, so a second instance would be a second source of
     * truth — exactly what this interface exists to prevent.
     */
    @Binds
    @Singleton
    abstract fun entitlementRepository(
        impl: SharedPreferencesEntitlementRepository,
    ): EntitlementRepository

    /** Scoped for the same reason as [entitlementRepository]: the mock's state lives in memory. */
    @Binds
    @Singleton
    abstract fun themeRepository(impl: MockThemeRepository): ThemeRepository

    /** Unscoped: it is stateless, so a new instance per injection point costs nothing. */
    @Binds
    abstract fun remoteLessonDataSource(impl: SupabaseRemoteLessonDataSource): RemoteLessonDataSource

    @Binds
    abstract fun networkMonitor(impl: AndroidNetworkMonitor): NetworkMonitor

    /** Unscoped: it is stateless, so a new instance per injection point costs nothing. */
    @Binds
    abstract fun contentLanguageProvider(impl: DeviceContentLanguageProvider): ContentLanguageProvider

    @Binds
    @Singleton
    abstract fun lastSyncedLanguageStore(
        impl: SharedPreferencesLastSyncedLanguageStore,
    ): LastSyncedLanguageStore

    @Binds
    abstract fun geminiQuizDataSource(impl: GeminiRemoteQuizDataSource): GeminiQuizDataSource

    @Binds
    abstract fun quizRepository(impl: QuizRepositoryImpl): QuizRepository
}
