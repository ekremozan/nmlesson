# Feature Specification: Remote Lesson Content

**Feature Branch**: `004-remote-lesson-content`

**Created**: 2026-08-15

**Status**: Draft

**Input**: User description: "içeriklerimizin hepsini dersleri konuları başlıkları vs şu an local db mizde olan her şeyi subapase entegre etmek istiyorum artık fake değil remotetan almak istiyorum."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Learner sees real, remotely-sourced lessons (Priority: P1)

A learner opens the app with an internet connection. Instead of the fixed set of placeholder
lessons that ship inside the app, the subject list, lesson titles, teasers, and full lesson content
come from a remote content source and are shown exactly as currently authored there.

**Why this priority**: This is the entire feature — until content is actually coming from a remote
source instead of the bundled dummy seed, there is nothing else to build on. It's also what turns
the app from a static demo into something whose catalog can grow without a new app release.

**Independent Test**: With the dummy/mock seed removed and the device online, launch the app fresh
and confirm every subject, lesson title, teaser, and lesson body visible in the app matches what is
currently stored in the remote content source, not the old hardcoded values.

**Acceptance Scenarios**:

1. **Given** a first-time install with network access, **When** the app launches, **Then** the
   subject and lesson list shown to the learner is fetched from the remote content source rather
   than from bundled sample data.
2. **Given** a lesson is open, **When** its full content is requested, **Then** the text shown is
   the current remote content for that lesson.
3. **Given** the remote content source has no reachable lessons for some reason, **When** the app
   launches for the first time with no local cache yet, **Then** the learner sees a clear empty/error
   state instead of silently falling back to the old placeholder content.

---

### User Story 2 - Lessons keep working fully offline after the first sync (Priority: P1)

A learner who has already opened the app once while online later opens it with no network
connection (e.g. airplane mode, subway). Every subject and lesson they could see before is still
browsable, readable, and listenable exactly as before, using the last successfully synced content.

**Why this priority**: The app's offline-first requirement does not change just because content now
comes from a remote source — losing offline reading would be a regression, not a trade-off users
accept for "real" content.

**Independent Test**: Launch the app online once to let it sync, force-quit, enable airplane mode,
relaunch, and confirm the full lesson catalog and lesson bodies already synced are still browsable,
searchable, and readable with no network calls required.

**Acceptance Scenarios**:

1. **Given** the app has synced remote content at least once, **When** the device goes offline,
   **Then** previously synced subjects, lesson lists, and lesson content remain fully readable.
2. **Given** the app is offline and a new sync cannot run, **When** the learner browses lessons,
   **Then** no error interrupts reading of already-cached content.

---

### User Story 3 - Content updates reach existing users without an app update (Priority: P2)

Whoever maintains the lesson catalog adds a new lesson, edits an existing one, or removes one in the
remote content source. Learners who already have the app installed see that change — the new
lesson appears, the edited text updates, or the removed lesson disappears — the next time the app
has a chance to sync, without installing a new app version.

**Why this priority**: This is the actual payoff of "not fake, from remote" — a catalog that can
grow and be corrected after release. It depends on User Story 1 existing first.

**Independent Test**: Add a new lesson to the remote content source, relaunch the app (or trigger a
manual refresh) on a device that already has synced content, and confirm the new lesson appears
without reinstalling the app.

**Acceptance Scenarios**:

1. **Given** a lesson's content is edited remotely, **When** the app next syncs successfully,
   **Then** the learner sees the updated content the next time they open that lesson.
2. **Given** a lesson is removed remotely, **When** the app next syncs successfully, **Then** the
   lesson is no longer shown in the catalog.
3. **Given** a sync attempt fails (e.g. network drops mid-request), **When** the learner keeps using
   the app, **Then** the previously synced content is left untouched — a failed sync never leaves the
   catalog partially updated or empty.

---

### Edge Cases

- What happens on the very first launch when the device is offline and no content has ever been
  synced? There is nothing to show — the app must present this as an explicit "no content yet,
  connect to load lessons" state, not an empty list that looks broken.
- What happens if the remote content source returns a lesson with missing or malformed fields (e.g.
  no title)? That lesson must be skipped/reported rather than shown broken or crashing the sync.
- What happens if a learner is actively reading or listening to a lesson at the moment it gets
  edited or removed remotely? The in-progress session is not interrupted; the change applies the
  next time the catalog is loaded.
- What happens to premium gating when content comes from remote instead of bundled seed? Free vs.
  premium access rules must keep working exactly as before — gating is not tied to where the content
  came from.
- What happens the first time an existing (pre-remote) install upgrades to this version? Its old
  bundled placeholder content must not remain visible alongside or instead of the real remote
  content once a sync has succeeded.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST source the full lesson catalog — subjects, lesson titles, teasers, and
  full lesson content — from a remote content source rather than from content bundled inside the
  app.
- **FR-002**: The system MUST persist the most recently synced lesson catalog locally so it remains
  fully readable (browse, search, read, listen) without a network connection.
- **FR-003**: The system MUST retrieve the current state of the remote content source and apply
  additions, edits, and removals to the local catalog on each successful sync.
- **FR-004**: The system MUST leave the existing local catalog untouched if a sync attempt fails
  partway through, so learners never see a partially-updated or emptied catalog because of a network
  problem.
- **FR-005**: The system MUST report sync failures through the app's existing error-reporting path
  without interrupting a learner's ability to read already-cached content.
- **FR-006**: The system MUST show a clear, explicit state (not a blank/broken-looking list) when no
  content has ever been successfully synced and none is cached yet.
- **FR-007**: The existing free/premium content-gating rules MUST apply identically to remotely
  sourced lessons — access rules must not depend on where a lesson's content came from.
- **FR-008**: The bundled placeholder/dummy lesson content MUST be retired as the app's content
  source once remote sync is verified working — it must not remain a fallback that could silently
  show fake content to a learner in production.
- **FR-009**: The lesson content currently hardcoded in the app MUST be loaded into the remote
  content source as its starting dataset, so the remote catalog begins equivalent to what the app
  already ships today.
- **FR-010**: Lesson cover artwork MUST continue to come from the four bundled subject illustrations
  and is out of scope for this remote migration — only lesson text content (subjects, titles,
  teasers, bodies) moves to the remote source.

### Key Entities

- **Subject**: A top-level topic grouping lessons (e.g. Biology, History, Geography, Chemistry).
  Represented remotely and mirrored locally so the catalog can be filtered by subject.
- **Lesson**: A single learning unit within a subject — has a title, a short teaser, a premium/free
  flag, and a reference to its subject and cover illustration. Lives in the remote content source and
  is cached locally after sync.
- **Lesson Content**: The full readable/listenable body of a lesson, fetched and cached separately
  from the lesson's catalog entry (mirrors the existing local split between lesson metadata and
  lesson body).
- **Sync State**: Tracks whether the local catalog reflects the latest successful fetch from the
  remote content source, so failed syncs can be told apart from up-to-date ones.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of subjects, lesson titles, teasers, and lesson bodies shown in the app trace back
  to the remote content source — none remain hardcoded in the shipped app.
- **SC-002**: A learner who has synced once can browse and read every previously-seen lesson with the
  device fully offline, with zero content gaps compared to being online.
- **SC-003**: A content change made in the remote source (add, edit, or remove a lesson) is visible
  to an existing installed app within one normal app open/refresh, with no app store update required.
- **SC-004**: A failed sync never reduces or corrupts what a learner can already see — the catalog
  after a failed sync is identical to the catalog before it was attempted.
- **SC-005**: First-time install with no network produces a clear "connect to load lessons" state in
  100% of cases, never a silently empty or broken-looking screen.

## Assumptions

- The remote content source is managed directly by the project owner (e.g. through the backend
  provider's own dashboard/table editor); no in-app or admin authoring interface is in scope for this
  feature.
- Sync happens automatically when the app has network access (matching the app's existing
  offline-first sync pattern) plus an explicit manual refresh the learner can trigger; no separate
  push-notification-driven sync is required.
- This is a pre-release case-study app with no existing installed user base to migrate — "existing
  users" in the edge cases refers to the app's own local cache/session continuity, not a live
  migration of real end users' data.
- Lesson cover images stay as the four bundled subject illustrations already in the app (per prior
  discussion) and are not part of what moves to the remote content source.
- Authentication/authorization for who can read the remote content source is out of scope — content
  is treated as public read access, consistent with the app's current no-login content model.
