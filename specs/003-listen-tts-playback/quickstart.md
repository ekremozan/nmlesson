# Quickstart: Verify Listen To Story

## Prerequisites

- Debug build installed on a device/emulator with a TTS voice installed for the story's language.
- At least one unlocked story with body content synced locally.

## Scenarios

1. **Offline start** — enable airplane mode, open an unlocked story, tap "Listen" at the bottom of
   the content → narration starts within ~1s (SC-001).
2. **Pause/resume** — while playing, tap the pill → audio stops, pill shows Resume; tap again →
   narration continues from the same sentence, not the start (SC-002).
3. **Screen re-entry resets** — start narration, pause, press back to the list, reopen the same
   story → pill shows initial Listen state; tapping starts from sentence 0 (SC-003).
4. **Background continues** — start narration, press Home → audio keeps playing, a media
   notification appears with a pause action; tapping it pauses; force-stopping the app from recents
   stops audio entirely.
5. **Interruption** — start narration, trigger an incoming call (or another app's audio) → narration
   pauses; after the interruption clears, resuming from the pill continues playback.
6. **No TTS voice** — on a device/emulator with no voice installed for the story's language, tap
   Listen → a visible error message appears instead of silence, and the failure is visible in the
   configured `ErrorReporter` sink (Crashlytics/log, per current wiring) (SC-004).

See [contracts/story-narrator.md](contracts/story-narrator.md) for the interface these scenarios
exercise and [data-model.md](data-model.md) for the state shape.
