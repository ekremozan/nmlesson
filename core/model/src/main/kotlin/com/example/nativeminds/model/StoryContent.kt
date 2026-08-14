package com.example.nativeminds.model

/**
 * The readable payload of a [Story] — what the reader screen renders and the story list never
 * needs.
 *
 * Paragraphs arrive already split and trimmed, so nothing above this layer parses text: the
 * mapper in `:core:data` owns the storage format, and everything else sees a list.
 */
data class StoryContent(
    val storyId: Long,
    val author: String,
    val paragraphs: List<String>,
)
