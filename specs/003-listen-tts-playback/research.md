# Research: Listen To Story

Only decisions that needed resolving; no unresolved unknowns remain.

## Sentence-level resume granularity

- **Decision**: Split story body text into sentences before narration; feed `TextToSpeech.speak()`
  one sentence per utterance (unique `utteranceId` = sentence index) instead of the whole body in
  one call.
- **Rationale**: `TextToSpeech` has no native "pause mid-utterance and resume from that exact word"
  API — `stop()` discards the rest of the current utterance. Chunking by sentence gives a real,
  cheap resume point (resume = re-speak from the interrupted sentence index) and matches SC-002
  ("within one sentence of the paused point").
- **Alternatives considered**: Word-level chunking (too many `speak()` calls, audible micro-gaps
  between words); whole-body single utterance (cannot resume, only restart — fails FR-005 outright).

## Background playback + notification

- **Decision**: `androidx.media3:media3-session`, using `SimpleBasePlayer` (wraps the sentence-queue
  TTS engine as a minimal `Player`) hosted inside a `MediaSessionService`. The service becomes a
  foreground service the moment playback starts, giving the system-generated media notification and
  its play/pause action for free.
- **Rationale**: This is the maintained, modern replacement for hand-rolling
  `MediaSessionCompat` + `NotificationCompat` + a manual foreground `Service`; `SimpleBasePlayer`
  exists specifically for non-`ExoPlayer` playback sources (like a TTS-driven queue) that still want
  session/notification integration. Matches CLAUDE.md's "Audio: TTS or pre-generated audio via
  Media3" pending decision.
- **Alternatives considered**: Legacy `MediaSessionCompat` (deprecated surface, more boilerplate for
  the same outcome); plain `Service` with a hand-built `NotificationCompat` and no session (would
  need custom notification-action handling and loses Android Auto/Bluetooth/media-key integration
  media3-session gives for free).

## Audio focus / interruption handling (FR-012)

- **Decision**: `media3-session`'s default audio focus handling on the underlying `Player` — pause
  on transient loss (phone call), stay paused on permanent loss, matching the pill's existing
  Paused visual state.
- **Rationale**: Built into the session/player integration; no custom `AudioManager` listener needed.
- **Alternatives considered**: Manual `AudioFocusRequest` listener — redundant given media3 already
  does this correctly for a registered `Player`.

## TTS engine/voice unavailability (FR-011)

- **Decision**: Check `TextToSpeech.OnInitListener` result and `isLanguageAvailable()` for the
  story's language at narrator-start time; surface a domain-level `NarrationState.Unavailable`
  which the reducer turns into `ReaderEffect.ShowAudioUnavailable`, and report through the existing
  `ErrorReporter` interface.
- **Rationale**: Reuses the app's one existing observability seam instead of inventing a second one;
  `ShowAudioUnavailable` already exists in `ReaderContract.kt` as a placeholder effect.
- **Alternatives considered**: None — this is a direct application of an existing pattern.
