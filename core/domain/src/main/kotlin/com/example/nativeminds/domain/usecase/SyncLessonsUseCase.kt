package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.repository.LessonRepository
import javax.inject.Inject

/** Startup sync: seed the local database on first run, then refresh from remote if online. */
class SyncLessonsUseCase @Inject constructor(
    private val repository: LessonRepository,
) {
    suspend operator fun invoke() = repository.syncIfNeeded()
}
