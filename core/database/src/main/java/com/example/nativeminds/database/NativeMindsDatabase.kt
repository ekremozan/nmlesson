package com.example.nativeminds.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Construction and lifetime are owned by
 * [com.example.nativeminds.database.di.DatabaseModule] — the class deliberately has no
 * `getInstance()`, so there is exactly one way to obtain it and Hilt guarantees it is a singleton.
 */
@Database(entities = [StoryEntity::class], version = 1, exportSchema = true)
abstract class NativeMindsDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
}
