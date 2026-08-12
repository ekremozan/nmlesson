package com.example.nativeminds.data.local

import com.example.nativeminds.model.Story

/**
 * Bootstrap content written to Room the first time the app runs with an empty database, so
 * offline-first holds even before the first successful sync — see [com.example.nativeminds.data.RoomStoryRepository.syncIfNeeded].
 *
 * Cut corner: hand-seeded content standing in for a real catalog until a backend exists.
 */
object DummyStorySeed {
    val stories = listOf(
        Story(
            id = 1,
            category = "Fiction",
            title = "The Lighthouse Keeper's Last Letter",
            teaser = "Forty years of weather notes, and one page he never sent.",
            minutes = 6,
            hasAudio = true,
            isLocked = false,
        ),
        Story(
            id = 2,
            category = "Science",
            title = "Why Bread Rises",
            teaser = "A single-celled organism, quietly doing all the work.",
            minutes = 4,
            hasAudio = true,
            isLocked = false,
        ),
        Story(
            id = 3,
            category = "History",
            title = "The Cartographer of Missing Islands",
            teaser = "For a century, the map showed land that was never there.",
            minutes = 8,
            hasAudio = false,
            isLocked = true,
        ),
        Story(
            id = 4,
            category = "Essays",
            title = "On Walking Slowly",
            teaser = "What a city gives back when you stop trying to cross it.",
            minutes = 5,
            hasAudio = true,
            isLocked = false,
        ),
        Story(
            id = 5,
            category = "Fiction",
            title = "Salt, and Other Things We Keep",
            teaser = "Her grandmother's pantry held one jar nobody opened.",
            minutes = 7,
            hasAudio = true,
            isLocked = true,
        ),
        Story(
            id = 6,
            category = "Science",
            title = "The Bird That Sleeps Mid-Flight",
            teaser = "Ten months aloft, half a brain awake at a time.",
            minutes = 3,
            hasAudio = false,
            isLocked = false,
        ),
    )
}
