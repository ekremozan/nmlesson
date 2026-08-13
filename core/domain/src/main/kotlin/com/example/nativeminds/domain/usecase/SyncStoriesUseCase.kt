package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.repository.StoryRepository
import javax.inject.Inject

/** Startup sync: seed the local database on first run, then refresh from remote if online. */
class SyncStoriesUseCase @Inject constructor(
    private val repository: StoryRepository,
) {
    suspend operator fun invoke() = repository.syncIfNeeded()
}
