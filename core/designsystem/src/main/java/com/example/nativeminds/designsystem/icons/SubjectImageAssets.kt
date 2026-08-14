package com.example.nativeminds.designsystem.icons

import androidx.annotation.DrawableRes
import com.example.nativeminds.designsystem.R

/**
 * Resolves a lesson's `image` key (a plain string the domain model carries, derived from its
 * subject — e.g. `"subject_biology"`) to one of the four bundled subject illustrations.
 *
 * The domain layer only ever sees the key — this table is the one place that knows it is backed by
 * an Android drawable resource, keeping `:core:model` free of an Android dependency.
 */
object SubjectImageAssets {
    private val subjects = mapOf(
        "subject_biology" to R.drawable.subject_biology,
        "subject_history" to R.drawable.subject_history,
        "subject_geography" to R.drawable.subject_geography,
        "subject_chemistry" to R.drawable.subject_chemistry,
    )

    @DrawableRes
    fun resolve(image: String): Int = subjects[image] ?: R.drawable.subject_biology
}
