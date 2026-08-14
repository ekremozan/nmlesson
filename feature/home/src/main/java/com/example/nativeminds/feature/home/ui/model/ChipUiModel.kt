package com.example.nativeminds.feature.home.ui.model

/**
 * A single subject filter chip (or a search suggestion, which renders the same way).
 *
 * [subject] `null` is the "All" chip — no filter. It carries no label because "All" is UI text,
 * not content: the composable resolves it from a string resource, while a real subject is data
 * that comes out of the database as-is.
 */
data class ChipUiModel(
    val subject: String?,
    val isSelected: Boolean = false,
)
