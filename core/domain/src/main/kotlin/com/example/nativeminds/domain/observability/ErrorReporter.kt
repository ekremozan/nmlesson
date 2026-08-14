package com.example.nativeminds.domain.observability

/**
 * Where failures go so that they are never only shown to the user and then forgotten.
 *
 * Reporting is in addition to surfacing, never instead of it: a caught exception that reaches the
 * screen still reaches this, and one that reaches this still reaches the screen.
 *
 * Implementations must never throw.
 */
interface ErrorReporter {
    fun report(throwable: Throwable, context: String)
}
