package com.example.nativeminds

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumented tests must not run against [NativeMindsApplication] — Hilt needs its own
 * [HiltTestApplication] so that `@TestInstallIn` module replacements take effect. Registered as
 * `testInstrumentationRunner` in this module's build file.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
