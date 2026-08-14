package com.example.nativeminds

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import com.example.nativeminds.data.MockEntitlementRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

private const val FREE_STORY_TITLE = "The Lighthouse Keeper's Last Letter"
private const val FREE_STORY_AUTHOR = "by Marguerite Halloran"
private const val FREE_STORY_OPENING = "Forty winters he kept the log"

private const val PREMIUM_STORY_TITLE = "The Cartographer of Missing Islands"
private const val PREMIUM_STORY_OPENING = "For ninety-one years"
private const val PREMIUM_STORY_WITHHELD = "Cartographers copy each other"

private const val UNLOCK_HEADING = "Unlock the full story"
private const val SUBSCRIBE_ACTION = "Start 7 days free"
private const val SUBSCRIPTION_UNAVAILABLE = "Subscriptions are not available yet."

/**
 * The journeys that only exist once the graph, the database and the screens are wired together:
 * that a tap on a card lands on the right story, that back leaves home as it was, and that a
 * premium story shows a taste rather than the whole thing.
 */
@HiltAndroidTest
class ReaderJourneyTest {
    private val hiltRule = HiltAndroidRule(this)
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rule: RuleChain = RuleChain.outerRule(hiltRule).around(composeRule)

    @Inject
    lateinit var entitlements: MockEntitlementRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun openStory(title: String) {
        composeRule.waitUntilExactlyOneExists(title)
        composeRule.onNodeWithText(title).performClick()
    }

    @Test
    fun tappingAStoryOpensItAndFillsItWithItsOwnContent() {
        openStory(FREE_STORY_TITLE)

        composeRule.waitUntilExactlyOneExists(FREE_STORY_AUTHOR)
        composeRule.onNodeWithText(FREE_STORY_AUTHOR).assertIsDisplayed()
        composeRule.onNodeWithText(FREE_STORY_OPENING, substring = true).assertIsDisplayed()
    }

    @Test
    fun goingBackLeavesHomeExactlyAsItWas() {
        composeRule.waitUntilExactlyOneExists(FREE_STORY_TITLE)
        composeRule.onNode(hasSetTextAction()).performTextInput("Lighthouse")
        composeRule.waitUntilExactlyOneExists(FREE_STORY_TITLE)

        composeRule.onNodeWithText(FREE_STORY_TITLE).performClick()
        composeRule.waitUntilExactlyOneExists(FREE_STORY_AUTHOR)
        Espresso.pressBack()

        composeRule.waitUntilExactlyOneExists("Lighthouse")
        composeRule.onNodeWithText("Lighthouse").assertIsDisplayed()
    }

    @Test
    fun aPremiumStoryShowsATasteAndTheUnlockSheet() {
        openStory(PREMIUM_STORY_TITLE)

        composeRule.waitUntilExactlyOneExists(UNLOCK_HEADING)
        composeRule.onNodeWithText(PREMIUM_STORY_OPENING, substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithTextCount(PREMIUM_STORY_WITHHELD, substring = true).let {
            assert(it == 0) { "The withheld part of a premium story reached the screen" }
        }
    }

    @Test
    fun theSubscribeActionSaysItIsNotAvailableYet() {
        openStory(PREMIUM_STORY_TITLE)
        composeRule.waitUntilExactlyOneExists(SUBSCRIBE_ACTION)

        composeRule.onNodeWithText(SUBSCRIBE_ACTION).performClick()

        composeRule.waitUntilExactlyOneExists(SUBSCRIPTION_UNAVAILABLE)
        composeRule.onNodeWithText(SUBSCRIPTION_UNAVAILABLE).assertIsDisplayed()
    }

    @Test
    fun aSubscriberSeesTheWholePremiumStoryWithNoSheet() {
        entitlements.setPremium(true)

        openStory(PREMIUM_STORY_TITLE)

        composeRule.waitUntilExactlyOneExists(PREMIUM_STORY_WITHHELD, substring = true)
        composeRule.onAllNodesWithTextCount(UNLOCK_HEADING).let {
            assert(it == 0) { "A subscriber was shown the unlock sheet" }
        }
    }
}
