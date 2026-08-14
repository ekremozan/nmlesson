package com.example.nativeminds

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

/**
 * End-to-end proof that the Hilt graph actually resolves at runtime: launching [MainActivity]
 * forces Hilt to build `HomeViewModel` → use cases → `StoryRepository` → `StoryDao` (in-memory,
 * see `TestDatabaseModule`) → seed content on screen. A missing binding fails the build, but a
 * misconfigured `@AndroidEntryPoint` or entry point only shows up here.
 */
@HiltAndroidTest
class HomeGraphTest {
    private val hiltRule = HiltAndroidRule(this)
    private val composeRule = createAndroidComposeRule<MainActivity>()

    /** Hilt must be injected before the Activity launches, so the order is fixed explicitly. */
    @get:Rule
    val rule: RuleChain = RuleChain.outerRule(hiltRule).around(composeRule)

    @Test
    fun homeScreenRendersSeededStoriesThroughTheInjectedGraph() {
        val firstSeededTitle = "The Lighthouse Keeper's Last Letter"

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTextCount(firstSeededTitle) > 0
        }

        composeRule.onNodeWithText(firstSeededTitle).assertIsDisplayed()
    }
}
