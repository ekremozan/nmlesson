package com.example.nativeminds.domain.model

import com.example.nativeminds.model.Lesson
import com.example.nativeminds.model.LessonContent

/**
 * What a given reader is allowed to see of a given lesson.
 *
 * A sealed hierarchy rather than a flag beside the content, because the restricted case must not
 * be able to carry the text it is withholding. [Preview] simply has nowhere to put the rest of the
 * lesson, so no screen can render it by mistake and no accessibility service can read it aloud.
 */
sealed interface ReaderAccess {
    val lesson: Lesson

    val author: String

    val paragraphs: List<String>

    /** The whole lesson: unlocked content, or premium content with an entitlement. */
    data class Full(
        override val lesson: Lesson,
        val content: LessonContent,
    ) : ReaderAccess {
        override val author: String get() = content.author

        override val paragraphs: List<String> get() = content.paragraphs
    }

    /**
     * Premium content without an entitlement: an opening taste and nothing else.
     *
     * [freeSharePercent] is what the unlock sheet quotes back to the reader, so the number on
     * screen and the number the rule used cannot drift apart.
     */
    data class Preview(
        override val lesson: Lesson,
        override val author: String,
        override val paragraphs: List<String>,
        val freeSharePercent: Int,
    ) : ReaderAccess
}
