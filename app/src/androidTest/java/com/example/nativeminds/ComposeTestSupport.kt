package com.example.nativeminds

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText

private const val DEFAULT_TIMEOUT_MS = 5_000L

/**
 * `onAllNodesWithText` throws when the tree is momentarily empty rather than returning zero, which
 * makes it useless inside a `waitUntil` predicate. This is the counting version that does not.
 */
fun ComposeTestRule.onAllNodesWithTextCount(text: String, substring: Boolean = false): Int =
    runCatching {
        onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().size
    }.getOrDefault(0)

/** Waits for exactly the node a test is about to act on, instead of sleeping for a fixed time. */
fun ComposeTestRule.waitUntilExactlyOneExists(text: String, substring: Boolean = false) {
    waitUntil(timeoutMillis = DEFAULT_TIMEOUT_MS) {
        onAllNodesWithTextCount(text, substring) == 1
    }
}
