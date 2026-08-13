package com.example.nativeminds.feature.home.ui.model

/**
 * A single category filter chip (or a search suggestion, which renders the same way).
 *
 * [category] `null` is the "All" chip — no filter. It carries no label because "All" is UI text,
 * not content: the composable resolves it from a string resource, while a real category is data
 * that comes out of the database as-is.
 */
data class ChipUiModel(
    val category: String?,
    val isSelected: Boolean = false,
)
