package com.example.nativeminds.data

import android.content.Context
import com.example.nativeminds.domain.repository.EntitlementRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

private const val PREFS_NAME = "entitlement_prefs"
private const val KEY_IS_PREMIUM = "is_premium"

/**
 * Premium status while there is no billing integration.
 *
 * Cut corner: the mock purchase and cancel flows call [setPremium] directly instead of a real
 * store, so there is no receipt, no billing-provider round trip, and no way to revoke access from
 * outside this device. What matters now is that every gating decision already reads from one
 * place, and the value survives process death via [android.content.SharedPreferences]; replacing
 * this with a billing-backed implementation is a single `@Binds` change and touches no call site.
 */
@Singleton
class SharedPreferencesEntitlementRepository @Inject constructor(
    @ApplicationContext context: Context,
) : EntitlementRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val premium = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM, false))

    override fun isPremium(): Flow<Boolean> = premium.asStateFlow()

    override fun setPremium(value: Boolean) {
        prefs.edit { putBoolean(KEY_IS_PREMIUM, value) }
        premium.value = value
    }
}
