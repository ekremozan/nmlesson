package com.example.nativeminds.feature.home.ui.model

/** A single category filter chip (or a search suggestion, which renders the same way). */
data class ChipUiModel(
    val label: String,
    val isSelected: Boolean = false,
)
