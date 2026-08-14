package com.example.nativeminds.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for whether the current reader has premium access.
 *
 * Every gating decision in the app goes through this and nothing else — no screen reads a flag
 * from storage, and no ViewModel keeps its own copy. That is what makes the rule changeable in one
 * place when real billing replaces the mock behind it.
 *
 * A [Flow] rather than a suspend read, so a subscription that starts elsewhere reaches an open
 * screen without the reader having to leave and come back.
 */
interface EntitlementRepository {
    fun isPremium(): Flow<Boolean>
}
