package com.example.nativeminds.database.di

import android.content.Context
import androidx.room.Room
import com.example.nativeminds.database.LessonContentDao
import com.example.nativeminds.database.LessonDao
import com.example.nativeminds.database.MIGRATION_1_2
import com.example.nativeminds.database.MIGRATION_2_3
import com.example.nativeminds.database.MIGRATION_3_4
import com.example.nativeminds.database.NativeMindsDatabase
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
        Room.databaseBuilder(context, NativeMindsDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    /**
     * Not scoped: the DAO is a cheap accessor on the already-singleton database, so there is
     * nothing to cache.
     */
    @Provides
    fun lessonDao(database: NativeMindsDatabase): LessonDao = database.lessonDao()

    @Provides
    fun lessonContentDao(database: NativeMindsDatabase): LessonContentDao = database.lessonContentDao()
}
