package com.example.nativeminds.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Construction and lifetime are owned by
 * [com.example.nativeminds.database.di.DatabaseModule] — the class deliberately has no
 * `getInstance()`, so there is exactly one way to obtain it and Hilt guarantees it is a singleton.
 */
@Database(
    entities = [StoryEntity::class, StoryContentEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class NativeMindsDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao

    abstract fun storyContentDao(): StoryContentDao
}

/**
 * Adds the story-content table. `stories` is untouched, so an existing install keeps every row and
 * simply has no text until the seed or a refresh fills it — which is the state the reader already
 * has to handle for a story it has never opened.
 *
 * Written by hand rather than left to a destructive fallback: dropping a reader's library on an
 * upgrade is the kind of failure that is invisible in development and unforgivable in production.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `story_content` (
                `storyId` INTEGER NOT NULL,
                `author` TEXT NOT NULL,
                `body` TEXT NOT NULL,
                PRIMARY KEY(`storyId`),
                FOREIGN KEY(`storyId`) REFERENCES `stories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
    }
}

/**
 * Adds the cover-art key each story resolves to a bundled drawable through
 * `StoryCoverAssets` — a plain default keeps existing rows valid until the next sync
 * refills them from the (also updated) seed/remote catalog.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `stories` ADD COLUMN `image` TEXT NOT NULL DEFAULT ''")
    }
}
