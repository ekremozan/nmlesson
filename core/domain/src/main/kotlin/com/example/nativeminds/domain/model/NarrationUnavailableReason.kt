package com.example.nativeminds.domain.model

/** Why on-device narration could not start — each maps to its own user-facing message. */
enum class NarrationUnavailableReason {
    ENGINE_MISSING,
    LANGUAGE_UNSUPPORTED,
}
