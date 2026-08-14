package com.example.nativeminds.data.observability

import android.util.Log
import com.example.nativeminds.domain.observability.ErrorReporter
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "NativeMinds"

/**
 * Cut corner: failures are written to logcat rather than sent to Crashlytics, because wiring a
 * crash-reporting backend is a build-and-account concern separate from any one screen. The seam is
 * what matters now — every failure already routes through [ErrorReporter], so the real reporter is
 * a `@Binds` change and nothing above this layer moves.
 */
@Singleton
class LogcatErrorReporter @Inject constructor() : ErrorReporter {
    override fun report(throwable: Throwable, context: String) {
        Log.e(TAG, context, throwable)
    }
}
