package com.example.nativeminds.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StoryEntity::class], version = 1, exportSchema = true)
abstract class NativeMindsDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao

    companion object {
        private const val DATABASE_NAME = "nativeminds.db"

        @Volatile
        private var instance: NativeMindsDatabase? = null

        fun getInstance(context: Context): NativeMindsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NativeMindsDatabase::class.java,
                    DATABASE_NAME,
                ).build().also { instance = it }
            }
    }
}
