package com.example.nativeminds.feature.paywall.ui.success

import com.example.nativeminds.feature.paywall.ui.paywall.PurchasePlan
import com.example.nativeminds.model.Story
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val story = Story(
    id = 3,
    category = "History",
    title = "The Cartographer of Missing Islands",
    teaser = "One page he never sent.",
    minutes = 8,
    hasAudio = false,
    isLocked = true,
    image = "cover_03",
)

private fun initialState() =
    PurchaseSuccessUiState(storyId = story.id, progressPercent = 30, plan = PurchasePlan.YEARLY)

class PurchaseSuccessReducerTest {
    @Test
    fun startsWithNoStoryLoaded() {
        assertNull(initialState().story)
    }

    @Test
    fun storyArrivingFillsTheResumeCard() {
        val state = initialState().reduce(PurchaseSuccessIntent.StoryChanged(story))

        assertEquals(story, state.story)
    }

    @Test
    fun theStoryDisappearingClearsTheResumeCard() {
        val withStory = initialState().reduce(PurchaseSuccessIntent.StoryChanged(story))

        val state = withStory.reduce(PurchaseSuccessIntent.StoryChanged(null))

        assertNull(state.story)
    }
}
