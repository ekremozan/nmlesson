package com.example.nativeminds.di

import android.content.Context
import androidx.room.Room
import com.example.nativeminds.database.NativeMindsDatabase
import com.example.nativeminds.database.StoryDao
import com.example.nativeminds.database.di.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Swaps the on-disk `nativeminds.db` for an in-memory one so each test run starts from an empty
 * database and never touches the user's data. This is the payoff of `@Provides`-ing the database
 * instead of hiding it behind a `getInstance()` singleton — the replacement needs no test hooks in
 * production code.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun nativeMindsDatabase(@ApplicationContext context: Context): NativeMindsDatabase =
        Room.inMemoryDatabaseBuilder(context, NativeMindsDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun storyDao(database: NativeMindsDatabase): StoryDao = database.storyDao()
}
