package com.example.nativeminds.domain.narration

private val SENTENCE_BOUNDARY = Regex("(?<=[.!?])\\s+")

/**
 * One sentence, located in the paragraph it came from.
 *
 * [start] and [end] (exclusive) index the *original* paragraph string, so the same segmentation
 * that decides what a [StoryNarrator] speaks also tells the reader which characters to draw the
 * narration highlight over. A flat list of sentence strings could do the first job but not the
 * second — trimming and flattening throw away the only anchor the highlight has.
 */
data class StorySentence(
    val paragraphIndex: Int,
    val start: Int,
    val end: Int,
)

/**
 * Splits story paragraphs into sentences, keeping each one's position in its paragraph.
 *
 * A paragraph break is always a sentence break too, even without terminal punctuation, so a short
 * paragraph (a title, a one-line aside) is never silently merged into the next one's utterance.
 */
fun List<String>.toStorySentences(): List<StorySentence> =
    flatMapIndexed { paragraphIndex, paragraph -> paragraph.sentencesAt(paragraphIndex) }

/** The spoken form of [toStorySentences] — the text a narrator hands to a speech engine. */
fun List<String>.sentenceTexts(): List<String> =
    toStorySentences().map { this[it.paragraphIndex].substring(it.start, it.end) }

/**
 * Running word totals across [toStorySentences], one entry per sentence boundary.
 *
 * `this[i]` is how many words come before sentence `i` and the last entry is the story's own word
 * count, which is what turns "speaking word 250 of sentence 12" into a fraction of the whole story
 * without counting anything twice.
 */
fun List<String>.sentenceWordTotals(): List<Int> {
    val totals = ArrayList<Int>(size + 1).apply { add(0) }
    sentenceTexts().forEach { sentence -> totals.add(totals.last() + sentence.wordCount()) }
    return totals
}

/** Words in a stretch of story text — whitespace-separated runs, the unit narration is paced in. */
fun String.wordCount(): Int = WORD.findAll(this).count()

private val WORD = Regex("\\S+")

private fun String.sentencesAt(paragraphIndex: Int): List<StorySentence> {
    val sentences = mutableListOf<StorySentence>()
    var from = 0
    SENTENCE_BOUNDARY.findAll(this).forEach { boundary ->
        sentences.addTrimmed(this, paragraphIndex, from, boundary.range.first)
        from = boundary.range.last + 1
    }
    sentences.addTrimmed(this, paragraphIndex, from, length)
    return sentences
}

private fun MutableList<StorySentence>.addTrimmed(
    paragraph: String,
    paragraphIndex: Int,
    from: Int,
    to: Int,
) {
    var start = from
    var end = to
    while (start < end && paragraph[start].isWhitespace()) start++
    while (end > start && paragraph[end - 1].isWhitespace()) end--
    if (start < end) add(StorySentence(paragraphIndex, start, end))
}
