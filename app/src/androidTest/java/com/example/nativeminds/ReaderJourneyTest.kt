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

private const val FREE_LESSON_TITLE = "Hücre Yapısı ve Organeller"
private const val FREE_LESSON_AUTHOR = "by Dr. Elif Kaya"
private const val FREE_LESSON_OPENING = "Hücre, canlıların yapısal"

private const val PREMIUM_LESSON_TITLE = "DNA ve Protein Sentezi"
private const val PREMIUM_LESSON_OPENING = "DNA, canlıların kalıtsal"
private const val PREMIUM_LESSON_WITHHELD = "Protein sentezi, DNA'daki"

private const val UNLOCK_CTA = "Unlock the full lesson"
private const val PAYWALL_HEADLINE = "Unlock every lesson"
private const val PAYWALL_PURCHASE_ACTION = "Subscribe now"
private const val SUCCESS_HEADLINE = "You're premium now"
private const val SUCCESS_CONTINUE_READING = "Continue reading"

/**
 * The journeys that only exist once the graph, the database and the screens are wired together:
 * that a tap on a card lands on the right lesson, that back leaves home as it was, and that a
 * premium lesson shows a taste rather than the whole thing.
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

    private fun openLesson(title: String) {
        composeRule.waitUntilExactlyOneExists(title)
        composeRule.onNodeWithText(title).performClick()
    }

    @Test
    fun tappingALessonOpensItAndFillsItWithItsOwnContent() {
        openLesson(FREE_LESSON_TITLE)

        composeRule.waitUntilExactlyOneExists(FREE_LESSON_AUTHOR)
        composeRule.onNodeWithText(FREE_LESSON_AUTHOR).assertIsDisplayed()
        composeRule.onNodeWithText(FREE_LESSON_OPENING, substring = true).assertIsDisplayed()
    }

    @Test
    fun goingBackLeavesHomeExactlyAsItWas() {
        composeRule.waitUntilExactlyOneExists(FREE_LESSON_TITLE)
        composeRule.onNode(hasSetTextAction()).performTextInput("Hücre")
        composeRule.waitUntilExactlyOneExists(FREE_LESSON_TITLE)

        composeRule.onNodeWithText(FREE_LESSON_TITLE).performClick()
        composeRule.waitUntilExactlyOneExists(FREE_LESSON_AUTHOR)
        Espresso.pressBack()

        composeRule.waitUntilExactlyOneExists("Hücre")
        composeRule.onNodeWithText("Hücre").assertIsDisplayed()
    }

    @Test
    fun aPremiumLessonShowsATasteAndAnUnlockAction() {
        openLesson(PREMIUM_LESSON_TITLE)

        composeRule.waitUntilExactlyOneExists(UNLOCK_CTA)
        composeRule.onNodeWithText(PREMIUM_LESSON_OPENING, substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithTextCount(PREMIUM_LESSON_WITHHELD, substring = true).let {
            assert(it == 0) { "The withheld part of a premium lesson reached the screen" }
        }
    }

    @Test
    fun tappingUnlockNavigatesToThePaywall() {
        openLesson(PREMIUM_LESSON_TITLE)
        composeRule.waitUntilExactlyOneExists(UNLOCK_CTA)

        composeRule.onNodeWithText(UNLOCK_CTA).performClick()

        composeRule.waitUntilExactlyOneExists(PAYWALL_HEADLINE)
        composeRule.onNodeWithText(PAYWALL_HEADLINE).assertIsDisplayed()
    }

    @Test
    fun purchasingOnThePaywallUnlocksTheLessonAndShowsSuccess() {
        openLesson(PREMIUM_LESSON_TITLE)
        composeRule.waitUntilExactlyOneExists(UNLOCK_CTA)
        composeRule.onNodeWithText(UNLOCK_CTA).performClick()
        composeRule.waitUntilExactlyOneExists(PAYWALL_HEADLINE)

        composeRule.onNodeWithText(PAYWALL_PURCHASE_ACTION).performClick()

        composeRule.waitUntilExactlyOneExists(SUCCESS_HEADLINE)
        composeRule.onNodeWithText(SUCCESS_CONTINUE_READING).performClick()

        composeRule.waitUntilExactlyOneExists(PREMIUM_LESSON_WITHHELD, substring = true)
    }

    @Test
    fun aSubscriberSeesTheWholePremiumLessonWithNoUnlockAction() {
        entitlements.setPremium(true)

        openLesson(PREMIUM_LESSON_TITLE)

        composeRule.waitUntilExactlyOneExists(PREMIUM_LESSON_WITHHELD, substring = true)
        composeRule.onAllNodesWithTextCount(UNLOCK_CTA).let {
            assert(it == 0) { "A subscriber was shown the unlock action" }
        }
    }
}
