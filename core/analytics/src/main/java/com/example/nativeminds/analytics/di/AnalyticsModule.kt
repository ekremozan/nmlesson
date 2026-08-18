package com.example.nativeminds.analytics.di

import android.content.Context
import com.example.nativeminds.analytics.FirebaseAnalyticsReporter
import com.example.nativeminds.domain.observability.AnalyticsReporter
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    abstract fun analyticsReporter(impl: FirebaseAnalyticsReporter): AnalyticsReporter

    companion object {
        @Provides
        @Suppress("MissingPermission")
        fun firebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
            FirebaseAnalytics.getInstance(context)
    }
}
