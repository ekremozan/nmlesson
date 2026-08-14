package com.example.nativeminds.data.local

import com.example.nativeminds.model.Story

/**
 * Bootstrap content written to Room the first time the app runs with an empty database, so
 * offline-first holds even before the first successful sync — see [com.example.nativeminds.data.RoomStoryRepository.syncIfNeeded].
 *
 * Each of [StorySeedBases.all]'s 20 pieces is published under its 5 titles, so this list holds
 * 100 rows. Category and cover image are assigned by rotating through [StorySeedBases.CATEGORIES]
 * and [StorySeedBases.COVER_KEYS] on the flat position of each row — since 100 divides evenly by
 * both 4 and 10, every category ends up with exactly 25 stories and every cover with exactly 10,
 * with no bookkeeping needed beyond the rotation itself.
 *
 * Cut corner: hand-seeded content standing in for a real catalog until a backend exists.
 */
object DummyStorySeed {
    val stories: List<Story> = StorySeedBases.all.flatMapIndexed { baseIndex, base ->
        base.titles.mapIndexed { variantIndex, title ->
            val flatIndex = baseIndex * StorySeedBases.VARIANTS_PER_BASE + variantIndex
            Story(
                id = (flatIndex + 1).toLong(),
                category = StorySeedBases.CATEGORIES[flatIndex % StorySeedBases.CATEGORIES.size],
                title = title,
                teaser = base.teaser,
                minutes = base.minutes,
                hasAudio = base.hasAudio,
                isLocked = base.isLocked,
                image = StorySeedBases.COVER_KEYS[flatIndex % StorySeedBases.COVER_KEYS.size],
            )
        }
    }
}
