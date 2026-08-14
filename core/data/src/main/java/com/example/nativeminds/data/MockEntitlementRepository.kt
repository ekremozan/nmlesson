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
 * Cut corner: the value lives in memory and resets with the process, because nothing in the app
 * can legitimately grant a subscription yet — the paywall's call to action deliberately reports
 * that it is unavailable. What matters now is that every gating decision already reads from one
 * place; replacing this with a billing-backed implementation is a single `@Binds` change and
 * touches no call site.
 *
 * [setPremium] exists so a demo or an instrumented test can drive the state that the product will
 * eventually drive through purchase.
 */
@Singleton
class MockEntitlementRepository @Inject constructor() : EntitlementRepository {
    private val premium = MutableStateFlow(false)

    override fun isPremium(): Flow<Boolean> = premium.asStateFlow()

    fun setPremium(value: Boolean) {
        premium.value = value
    }
}
