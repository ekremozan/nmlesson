package com.example.nativeminds.crashreporting

import com.example.nativeminds.domain.observability.ErrorReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single point every handled failure in the app routes through, wired to a real backend: the
 * same call that keeps a real crash from ever going unreported also carries every non-fatal one
 * to the same Crashlytics dashboard, breadcrumbed with where it happened.
 */
@Singleton
class FirebaseCrashlyticsErrorReporter @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
) : ErrorReporter {
    override fun report(throwable: Throwable, context: String) {
        crashlytics.log(context)
        crashlytics.recordException(throwable)
    }
}
