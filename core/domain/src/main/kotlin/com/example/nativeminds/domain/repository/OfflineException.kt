package com.example.nativeminds.domain.repository

import java.io.IOException

/**
 * Thrown when something could only have been fetched over a network that is not there.
 *
 * Declared in the domain layer so callers can tell "you are offline" apart from "the fetch failed"
 * without inspecting messages or matching on data-layer exception types — the two lead to
 * different words on screen and different advice.
 */
class OfflineException(message: String) : IOException(message)
