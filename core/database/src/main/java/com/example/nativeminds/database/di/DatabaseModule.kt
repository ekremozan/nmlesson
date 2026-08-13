package com.example.nativeminds.database.di

import android.content.Context
import androidx.room.Room
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.StoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "nativeminds.db"

/**
 * `@Provides` rather than `@Binds` because Room generates the implementation — there is no
 * constructor for Dagger to call. Instrumented tests replace this whole module with an in-memory
 * one via `@TestInstallIn`.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun nativeMindsDatabase(@ApplicationContext context: Context): NativeMindsDatabase =
        Room.databaseBuilder(context, NativeMindsDatabase::class.java, DATABASE_NAME).build()

    /**
     * Not scoped: the DAO is a cheap accessor on the already-singleton database, so there is
     * nothing to cache.
     */
    @Provides
    fun storyDao(database: NativeMindsDatabase): StoryDao = database.storyDao()
}
