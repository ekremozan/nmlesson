package com.example.nativeminds

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Composition root. `@HiltAndroidApp` generates the application-level Dagger component that every
 * `@AndroidEntryPoint` and `@HiltViewModel` in the project resolves against — no manual wiring.
 */
@HiltAndroidApp
class NativeMindsApplication : Application()
