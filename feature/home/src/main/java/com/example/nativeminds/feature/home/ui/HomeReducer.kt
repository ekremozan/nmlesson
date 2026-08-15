package com.example.nativeminds.feature.home.ui

internal fun HomeUiState.reduce(intent: HomeIntent): HomeUiState = when (intent) {
    is HomeIntent.QueryChanged -> copy(query = intent.query)
    HomeIntent.QueryCleared -> copy(query = "", selectedSubject = null)
    is HomeIntent.SubjectSelected -> copy(selectedSubject = intent.subject)
    is HomeIntent.SuggestionSelected -> copy(query = "", selectedSubject = intent.subject)
    is HomeIntent.SubjectsLoaded -> copy(subjects = intent.subjects)
    HomeIntent.RefreshRequested -> copy(syncToken = syncToken + 1)
}
