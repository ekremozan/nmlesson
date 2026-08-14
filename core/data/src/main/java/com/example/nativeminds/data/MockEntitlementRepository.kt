package com.example.nativeminds.data

import com.example.nativeminds.domain.repository.EntitlementRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Premium status while there is no billing integration.
 *
 * Cut corner: the value lives in memory and resets with the process — the paywall's purchase
 * button calls [setPremium] directly instead of a real store. What matters now is that every
 * gating decision already reads from one place; replacing this with a billing-backed
 * implementation is a single `@Binds` change and touches no call site.
 */
@Singleton
class MockEntitlementRepository @Inject constructor() : EntitlementRepository {
    private val premium = MutableStateFlow(false)

    override fun isPremium(): Flow<Boolean> = premium.asStateFlow()

    override fun setPremium(value: Boolean) {
        premium.value = value
    }
}
