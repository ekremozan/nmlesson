package com.example.nativeminds.feature.home.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeReducerTest {
    @Test
    fun `refresh requested only increments the sync token`() {
        val state = HomeUiState(query = "hücre", selectedSubject = "Kimya", syncToken = 2)

        val reduced = state.reduce(HomeIntent.RefreshRequested)

        assertEquals(3, reduced.syncToken)
        assertEquals(state.query, reduced.query)
        assertEquals(state.selectedSubject, reduced.selectedSubject)
        assertEquals(state.subjects, reduced.subjects)
    }
}
