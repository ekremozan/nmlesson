package com.example.nativeminds.domain

import com.example.nativeminds.domain.observability.ErrorReporter

class RecordingErrorReporter : ErrorReporter {
    val reported = mutableListOf<Pair<Throwable, String>>()

    override fun report(throwable: Throwable, context: String) {
        reported += throwable to context
    }
}
