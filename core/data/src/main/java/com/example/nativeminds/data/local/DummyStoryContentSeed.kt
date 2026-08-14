package com.example.nativeminds.data.local

import com.example.nativeminds.model.StoryContent

/**
 * The text behind each story in [DummyStorySeed], keyed by the same flat-index-derived ids — see
 * [StorySeedBases] for where the paragraphs actually live.
 *
 * Kept in its own file rather than inlined into the story list: the metadata list is scanned by
 * anyone changing the catalog, and burying full story text inside it would make that unreadable.
 *
 * Cut corner: hand-written content standing in for a real catalog until a backend exists.
 */
object DummyStoryContentSeed {
    val content: List<StoryContent> = StorySeedBases.all.flatMapIndexed { baseIndex, base ->
        base.titles.indices.map { variantIndex ->
            val flatIndex = baseIndex * StorySeedBases.VARIANTS_PER_BASE + variantIndex
            StoryContent(
                storyId = (flatIndex + 1).toLong(),
                author = base.author,
                paragraphs = base.paragraphs,
            )
        }
    }
}
