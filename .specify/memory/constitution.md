<!--
Sync Impact Report
Version change: (unversioned template) → 1.0.0
Bump rationale: Initial ratification — placeholders replaced with concrete, project-specific
governance derived from CLAUDE.md and the case-study acceptance criteria.
Modified principles:
  [PRINCIPLE_1_NAME] → I. Defensible Decisions
  [PRINCIPLE_2_NAME] → II. Clean Architecture & Model Separation (NON-NEGOTIABLE)
  [PRINCIPLE_3_NAME] → III. MVI With A Single Mutation Path (NON-NEGOTIABLE)
  [PRINCIPLE_4_NAME] → IV. Offline-First With Visible Failures
  [PRINCIPLE_5_NAME] → V. Design System Is The Only Source Of Style
Added sections:
  Technology & Product Constraints (was [SECTION_2_NAME])
  Development Workflow & Quality Gates (was [SECTION_3_NAME])
Removed sections: none
Follow-up TODOs: none
-->

# NativeMinds Constitution

## Core Principles

### I. Defensible Decisions

Every architectural choice MUST be defensible in review; the case study evaluates reasoning, not
output volume. Any non-obvious choice — a library, a pattern, a data model, a gating rule — MUST be
recorded in the README "Key Decisions" section with four parts: what was chosen, why, the trade-off
accepted, and what would change at 10× scale. Any knowingly cut corner (mock billing, hardcoded
seed, skipped edge case) MUST be logged in README "Cut Corners / Assumptions" in the same change
that introduces it, never later. New dependencies MUST be declared in `gradle/libs.versions.toml`,
referenced through `libs.` aliases, and justified in the same commit; prefer fewer, well-known
libraries.

Rationale: an undocumented decision is indistinguishable from an accident, and the project is graded
on the reasoning trail.

### II. Clean Architecture & Model Separation (NON-NEGOTIABLE)

Dependency arrows MUST point inwards towards `:core:domain`. `:core:domain` stays pure Kotlin — no
Android, no Hilt, only `javax.inject` and plain `dagger`. `:feature:*` modules depend on
`:core:domain` and MUST NOT depend on `:core:data` or `:core:database`; `:app` is the only module
allowed to see concrete implementations. Data models (DTO/entity), domain models, and UI models MUST
be separate classes, converted only through extension-function mappers (`StoryDto.toDomain()`,
`Story.toUiModel()`). A DTO or Room entity reaching a composable is a defect. Every dependency MUST
be resolved by Hilt: `@Inject constructor` plus a `@Binds` (interfaces) or `@Provides` (types we do
not own) in the owning module's `di/` package. Service locators, `getInstance()` singletons, and
hand-written ViewModel factories are forbidden. `CoroutineDispatcher` and `CoroutineScope` MUST
always be injected with a qualifier (`@IoDispatcher`, `@DefaultDispatcher`, `@ApplicationScope`).

Rationale: module boundaries and model separation are what keep the layers independently testable
and stop persistence concerns from leaking into the UI.

### III. MVI With A Single Mutation Path (NON-NEGOTIABLE)

Each feature owns a `…Contract.kt` (state + intents + effects) and a `…Reducer.kt`. The reducer MUST
be a pure top-level extension function — no Android, no coroutines, no ViewModel — and MUST be the
only thing that writes state. A ViewModel that branches on an intent itself, or assigns to its state
flow outside the reducer, is a bug. Every ViewModel exposes exactly one `onIntent(…)`; sets of
`onSomethingHappened()` methods and public mutable state flows are forbidden. External data (a Room
`Flow`, a sync result) MUST fold back in as an intent, so user input and background data share one
mutation path. State holds only irreducible facts; anything derivable MUST be a computed property.
One-shot events (snackbar, navigation) MUST use an effect `Channel(BUFFERED)` + `receiveAsFlow()`
collected with `flowWithLifecycle` — never `MutableSharedFlow`, which drops events raised while the
screen is stopped. The intent boundary stops at the screen: reusable composables keep plain
callbacks, and only `…ScreenContent` translates them into intents. `PagingData` MUST stay outside
state, and any pager deriving parameters from state MUST apply `distinctUntilChanged()` to that
slice.

Rationale: one writer and one mutation path is what makes state transitions reviewable and testable
without a device.

### IV. Offline-First With Visible Failures

Room is the single source of truth; remote data syncs into it and the UI reads only from it. Cached
content MUST remain fully readable with no network, and audio MUST degrade gracefully rather than
break the screen. Exceptions MUST NOT be silently swallowed: every failure is surfaced to crash
reporting always, and to the user wherever it affects what they can do. Premium gating MUST go
through a single entitlement source of truth — scattered `isPremium` checks against raw storage are
forbidden. Key funnel events (content viewed, listen started, paywall shown, subscription started,
AI feature used) MUST be logged to analytics.

Rationale: the product is judged on behaving correctly at the edges — no network, no entitlement,
failed sync — and an invisible failure cannot be diagnosed or fixed.

### V. Design System Is The Only Source Of Style

The Claude Design project "NativeMinds home screen" is the UI source of truth and MUST be read via
`DesignSync` before implementing or changing a screen. Composables MUST NOT contain hardcoded
colors, sizes, or text styles; they use `MaterialTheme.colorScheme` for standard roles and
`NativeMindsTheme.colors/typography/spacing` for brand roles. A value the system lacks MUST be added
to `ui/theme/` rather than inlined. Light and dark are both first-class — every screen works in
both, and dynamic color stays off. Every composable MUST have a `@Preview`: `@ThemePreviews` +
`PreviewSurface { }` for components, `@ScreenThemePreviews` + `NativeMindsTheme { }` for screens,
with states supplied by a `PreviewParameterProvider` rather than duplicated `@Preview` functions.
Previews live at the bottom of the same file as the composable, and their fixtures live in the
feature's `ui/preview/` data file with no ViewModel or database dependency. Exempt: Hilt-wired
wrapper composables, the `NativeMindsTheme` wrapper, and preview scaffolding.

Rationale: a token layer only holds if nothing bypasses it, and previews are the only cheap way to
verify both themes on every state.

## Technology & Product Constraints

- Platform: Android, Kotlin + Jetpack Compose, Material 3. `compileSdk`/`targetSdk` 36, `minSdk` 24,
  Java 11. Application ID and namespace `com.example.nativeminds`.
- Versions are managed exclusively in the Gradle version catalog `gradle/libs.versions.toml`.
- The build MUST satisfy all case-study acceptance criteria, and every feature MUST trace to one:
  browsable + searchable stories that can be read and listened to; premium gating with a
  mock/sandbox subscription flow; at least one genuinely useful AI capability for premium users;
  production concerns (performance at scale, offline, analytics + crash reporting); and the
  deliverables (repo, architecture diagram, installable build, demo video, README).
- No `//` comments anywhere in the codebase — not explanations, not rationale, not section banners.
  KDoc blocks (`/** … */`) on public declarations are the only exception; anything else that needs
  explaining is renamed, extracted, or written into README "Key Decisions".
- All user-facing text MUST come from string resources, never hardcoded literals.
- Scope discipline: ship a vertical slice that works end-to-end rather than a broad set of stub
  screens.

## Development Workflow & Quality Gates

- Work proceeds in small, reviewable steps. The author MUST be able to defend every line; large
  generated dumps are rejected regardless of correctness.
- Domain and repository logic carrying business rules — gating, paging, offline fallback — MUST have
  unit tests. UI is verified by a small number of Compose tests.
- Before a change is considered done: `./gradlew test` and `./gradlew lint` pass, and the touched
  screens render in both themes via their previews.
- Instrumented tests are run by naming the modules that have `androidTest` sources
  (`./gradlew :app:connectedDebugAndroidTest :core:database:connectedDebugAndroidTest`); the
  project-wide task fails on modules without them.
- When a prompt or iteration produces a notable decision, correction, or rejected AI suggestion, it
  MUST be recorded in README "How I Worked With AI" — what was asked, what came back wrong, what was
  done instead.
- Commits, code, and identifiers are written in English; conversation with the author is in Turkish.

## Governance

This constitution supersedes all other practices. Where CLAUDE.md, a README section, or a generated
plan conflicts with it, the constitution wins and the conflicting document is corrected in the same
change.

Amendments MUST be made by editing this file through `/speckit-constitution`, with the Sync Impact
Report at the top updated in the same change. Versioning follows semantic versioning: MAJOR for a
backward-incompatible removal or redefinition of a principle or governance rule, MINOR for a new
principle or materially expanded guidance, PATCH for clarifications and wording. `Ratified` never
changes after the initial adoption; `Last Amended` is set to the date of every substantive change.

Compliance is verified at review time: every pull request MUST be checked against Principles I–V,
and any deviation MUST be either fixed or recorded as an explicit entry in README "Cut Corners /
Assumptions" with its justification. Complexity that is not justified is removed rather than
documented. CLAUDE.md remains the runtime development guidance for day-to-day work and MUST stay
consistent with this document.

**Version**: 1.0.0 | **Ratified**: 2026-08-13 | **Last Amended**: 2026-08-13
