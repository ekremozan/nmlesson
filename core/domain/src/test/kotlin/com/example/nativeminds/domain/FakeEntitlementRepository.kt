package com.example.nativeminds.domain

import com.example.nativeminds.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeEntitlementRepository(initiallyPremium: Boolean = false) : EntitlementRepository {
    private val premium = MutableStateFlow(initiallyPremium)

    override fun isPremium(): Flow<Boolean> = premium

    fun setPremium(value: Boolean) {
        premium.value = value
    }
}
