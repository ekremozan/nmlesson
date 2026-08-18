package com.example.nativeminds.data.repository

import com.example.nativeminds.data.NetworkMonitor
import com.example.nativeminds.data.remote.quiz.GeminiQuizDataSource
import com.example.nativeminds.data.remote.quiz.dto.toDomain
import com.example.nativeminds.domain.model.QuizQuestion
import com.example.nativeminds.domain.repository.OfflineException
import com.example.nativeminds.domain.repository.QuizRepository
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val dataSource: GeminiQuizDataSource,
    private val networkMonitor: NetworkMonitor,
) : QuizRepository {
    override suspend fun generateQuestion(storyTitle: String, storyBody: String): QuizQuestion {
        if (!networkMonitor.isOnline()) {
            throw OfflineException("Cannot generate a quiz question while offline")
        }
        return dataSource.generateQuestion(storyTitle, storyBody).toDomain(storyTitle)
    }
}
