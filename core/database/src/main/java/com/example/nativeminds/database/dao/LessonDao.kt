package com.example.nativeminds.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nativeminds.database.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    /**
     * [subject] `null` means "no subject filter" — what the UI draws as the "All" chip. `null`
     * is the representation the whole stack uses; no layer translates a label into it.
     */
    @Query(
        """
        SELECT * FROM lessons
        WHERE (:subject IS NULL OR subject = :subject)
        AND (:query = '' OR title LIKE '%' || :query || '%' OR teaser LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%')
        ORDER BY id
        """,
    )
    fun pagingSource(subject: String?, query: String): PagingSource<Int, LessonEntity>

    /**
     * The subjects that actually have content, most-populated first. `GROUP BY` rather than
     * `DISTINCT` so `COUNT(*)` is available to order by; the alphabetical tie-break keeps the order
     * stable when two subjects hold the same number of lessons.
     *
     * A `Flow` because Room observes the table: a sync that introduces a new subject updates the
     * filter row on its own, with nothing to keep in step by hand.
     */
    @Query("SELECT subject FROM lessons GROUP BY subject ORDER BY COUNT(*) DESC, subject ASC")
    fun subjects(): Flow<List<String>>

    /** Emits `null` when the row is gone — a lesson deleted by a sync while the reader is open. */
    @Query("SELECT * FROM lessons WHERE id = :id")
    fun observeLesson(id: Long): Flow<LessonEntity?>

    @Upsert
    suspend fun upsertAll(lessons: List<LessonEntity>)

    /**
     * Removes every lesson whose id is not in [ids] — the other half of a sync's replace, paired
     * with [upsertAll]. Cascades to that lesson's `lesson_content` row via the existing foreign
     * key, but only for lessons actually gone from the remote catalog; lessons still present keep
     * whatever content is already cached for them.
     */
    @Query("DELETE FROM lessons WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun count(): Int

    /**
     * Wipes the whole catalog, cascading to `lesson_content` via the existing foreign key. Used
     * when the content language changes: the cached rows are for the wrong language entirely, so
     * there is nothing in them worth keeping until the next sync repopulates the table.
     */
    @Query("DELETE FROM lessons")
    suspend fun clearAll()
}
