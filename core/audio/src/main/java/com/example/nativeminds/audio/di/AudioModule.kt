package com.example.nativeminds.audio.di

import com.example.nativeminds.audio.TextToSpeechNarrator
import com.example.nativeminds.domain.narration.StoryNarrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Where the [StoryNarrator] domain contract meets its on-device implementation — the same shape
 * as `:core:data`'s `DataModule` for repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    @Binds
    @Singleton
    abstract fun storyNarrator(impl: TextToSpeechNarrator): StoryNarrator
}
