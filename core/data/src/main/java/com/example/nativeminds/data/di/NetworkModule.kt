package com.example.nativeminds.data.di

import com.example.nativeminds.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * The one place the app's Supabase URL/anon key and Gemini API key are read — [BuildConfig]
 * fields sourced from the git-ignored `local.properties`, never a literal (see
 * `specs/004-remote-lesson-content/research.md` §3 and
 * `specs/007-ai-quiz-generation/research.md` §R3).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun supabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Postgrest)
    }

    /**
     * Plain Ktor client for the Gemini REST API — not the `com.google.ai.client.generativeai` SDK,
     * which is compiled against Ktor 2.x and crashes at runtime
     * (`ClassNotFoundException: io.ktor.client.plugins.HttpTimeout`) once Gradle's single-version
     * resolution force-upgrades it to this project's Ktor 3.3.1 (see
     * `specs/007-ai-quiz-generation/research.md` R1 — corrected after an on-device crash).
     */
    @Provides
    @Singleton
    fun geminiHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
