package com.example.nativeminds.feature.home.ui

internal fun HomeUiState.reduce(intent: HomeIntent): HomeUiState = when (intent) {
    is HomeIntent.QueryChanged -> copy(query = intent.query)
    HomeIntent.QueryCleared -> copy(query = "", selectedCategory = null)
    is HomeIntent.CategorySelected -> copy(selectedCategory = intent.category)
    is HomeIntent.SuggestionSelected -> copy(query = "", selectedCategory = intent.category)
    is HomeIntent.CategoriesLoaded -> copy(categories = intent.categories)
}
