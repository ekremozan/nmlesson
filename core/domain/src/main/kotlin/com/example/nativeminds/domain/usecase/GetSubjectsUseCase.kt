package com.example.nativeminds.domain.usecase

import com.example.nativeminds.domain.repository.LessonRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * The subjects available to browse, most-populated first.
 *
 * Returns the full ordered list; how many of them a given screen shows (the empty state only has
 * room for a few) is a presentation decision and stays out of here.
 */
class GetSubjectsUseCase @Inject constructor(
    private val repository: LessonRepository,
) {
    operator fun invoke(): Flow<List<String>> = repository.subjects()
}
