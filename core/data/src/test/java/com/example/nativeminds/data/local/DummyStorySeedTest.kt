package com.example.nativeminds.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

private const val EXPECTED_STORY_COUNT = 100
private const val EXPECTED_STORIES_PER_COVER = 10
private const val EXPECTED_STORIES_PER_CATEGORY = 25

class DummyStorySeedTest {
    @Test
    fun generatesExactlyOneHundredStoriesWithUniqueIds() {
        val ids = DummyStorySeed.stories.map { it.id }
        assertEquals(EXPECTED_STORY_COUNT, ids.size)
        assertEquals(EXPECTED_STORY_COUNT, ids.toSet().size)
    }

    @Test
    fun distributesCoverImagesEvenly() {
        val storiesPerCover = DummyStorySeed.stories.groupingBy { it.image }.eachCount()
        assertEquals(StorySeedBases.COVER_KEYS.toSet(), storiesPerCover.keys)
        storiesPerCover.values.forEach { count -> assertEquals(EXPECTED_STORIES_PER_COVER, count) }
    }

    @Test
    fun distributesCategoriesEvenly() {
        val storiesPerCategory = DummyStorySeed.stories.groupingBy { it.category }.eachCount()
        assertEquals(StorySeedBases.CATEGORIES.toSet(), storiesPerCategory.keys)
        storiesPerCategory.values.forEach { count -> assertEquals(EXPECTED_STORIES_PER_CATEGORY, count) }
    }

    @Test
    fun everyStoryHasMatchingContent() {
        val storyIds = DummyStorySeed.stories.map { it.id }.toSet()
        val contentIds = DummyStoryContentSeed.content.map { it.storyId }.toSet()
        assertEquals(storyIds, contentIds)
    }
}
