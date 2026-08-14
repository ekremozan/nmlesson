package com.example.nativeminds.designsystem.icons

import androidx.annotation.DrawableRes
import com.example.nativeminds.designsystem.R

/**
 * Resolves a story's `image` key (a plain string the domain model carries, e.g. `"cover_03"`) to
 * one of the ten bundled procedural cover drawables.
 *
 * The domain layer only ever sees the key — this table is the one place that knows it is backed by
 * an Android drawable resource, keeping `:core:model` free of an Android dependency.
 */
object StoryCoverAssets {
    private val covers = mapOf(
        "cover_01" to R.drawable.story_cover_01,
        "cover_02" to R.drawable.story_cover_02,
        "cover_03" to R.drawable.story_cover_03,
        "cover_04" to R.drawable.story_cover_04,
        "cover_05" to R.drawable.story_cover_05,
        "cover_06" to R.drawable.story_cover_06,
        "cover_07" to R.drawable.story_cover_07,
        "cover_08" to R.drawable.story_cover_08,
        "cover_09" to R.drawable.story_cover_09,
        "cover_10" to R.drawable.story_cover_10,
    )

    @DrawableRes
    fun resolve(image: String): Int = covers[image] ?: R.drawable.story_cover_01
}
