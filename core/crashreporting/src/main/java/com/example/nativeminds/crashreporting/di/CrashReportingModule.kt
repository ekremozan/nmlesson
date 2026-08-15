package com.example.nativeminds.crashreporting.di

import com.example.nativeminds.crashreporting.FirebaseCrashlyticsErrorReporter
import com.example.nativeminds.domain.observability.ErrorReporter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReportingModule {
    @Binds
    abstract fun errorReporter(impl: FirebaseCrashlyticsErrorReporter): ErrorReporter

    companion object {
        @Provides
        fun firebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
    }
}
