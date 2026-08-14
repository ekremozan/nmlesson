package com.example.nativeminds.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DATABASE = "migration-test.db"

/**
 * A reader's stored library must survive an upgrade. This opens a real version-1 database, writes
 * a row, runs [MIGRATION_1_2], and checks the row is still there — which is the part a destructive
 * fallback would silently get wrong.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NativeMindsDatabase::class.java,
    )

    @Test
    fun migratingFromOneToTwoKeepsStoriesAndAddsContent() {
        helper.createDatabase(TEST_DATABASE, 1).use { database ->
            database.execSQL(
                """
                INSERT INTO stories (id, category, title, teaser, minutes, hasAudio, isLocked)
                VALUES (1, 'Fiction', 'The Lighthouse Keeper''s Last Letter', 'One page he never sent.', 6, 1, 0)
                """.trimIndent(),
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 2, true, MIGRATION_1_2)

        migrated.query("SELECT title FROM stories").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("The Lighthouse Keeper's Last Letter", cursor.getString(0))
        }
        migrated.query("SELECT COUNT(*) FROM story_content").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }
}
