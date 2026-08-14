# Feature Specification: Story Detail (Reader) With Premium Unlock Sheet

**Feature Branch**: `001-story-detail-reader`

**Created**: 2026-08-13

**Status**: Draft

**Input**: User description: "claude design tasarımındaki 2. sayfayı yapmanı istiyorum. home sayfasından bir item'a tıkladığımızda detaya gidecek. bu detay sayfası olacak. eğer içerik premium ise premium bottom sheet ile gelecek tasarımdaki gibi değilse normal gözükecek. tasarımını yap ve diğer sayfadan buraya gelinebilir olsun ve içerik açılınca dolsun"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Open a free story and read it (Priority: P1)

A reader browsing the home list taps a story card. The app opens a dedicated reading screen showing
that story's category, reading length, title, author, cover, and its full text in the reading type
voice. The reader can scroll to the end and return to the list exactly where they left off.

**Why this priority**: This is the core of the feature — without it there is no detail screen at
all. It also delivers the case study's "stories can be read" criterion on its own.

**Independent Test**: Tap any unlocked story on home, confirm the reader opens with that story's
real content (not a placeholder), scroll to the end, press back and confirm the home list is
restored with its previous search/category filter and scroll position.

**Acceptance Scenarios**:

1. **Given** the home list is showing stories, **When** the reader taps an unlocked story card,
   **Then** the reading screen opens showing that story's category, minutes, title, author, cover
   placeholder, and full body text.
2. **Given** the reading screen is open for an unlocked story, **When** the reader scrolls to the
   bottom, **Then** the entire body is readable to the last paragraph and a "next in category"
   pointer is shown.
3. **Given** the reading screen is open, **When** the reader presses back (gesture or the header
   back control), **Then** the home list reappears with its previous query, selected category, and
   scroll position intact.
4. **Given** the reading screen is opening, **When** the story content has not been resolved yet,
   **Then** a loading indication is shown in place of the body rather than an empty screen.

---

### User Story 2 - Hit the premium wall on a locked story (Priority: P2)

A non-subscriber taps a story marked premium. The reader opens and shows the story's header and the
opening portion of the text, which fades out; a premium unlock bottom sheet rises over it explaining
what a subscription includes and offering to start a trial. The reader can dismiss the sheet, see
the faded preview, and go back to the list.

**Why this priority**: This is the gating half of the request and the case study's "non-subscribers
see a taste" criterion. It depends on Story 1 existing, but is separately demonstrable.

**Independent Test**: Tap a locked story on home as a free user, confirm the preview portion is
readable, the rest is faded/unreachable, and the unlock sheet appears with its benefit list and
call to action.

**Acceptance Scenarios**:

1. **Given** the reader is not a subscriber, **When** they open a story marked premium, **Then** the
   header shows a PREMIUM badge, the first portion of the body is readable, the text fades into the
   background, and the unlock bottom sheet is presented.
2. **Given** the unlock sheet is presented, **When** the reader reads it, **Then** it states how
   much of the story was free, lists the subscription benefits, shows the call to action, and shows
   the price and cancellation terms.
3. **Given** the unlock sheet is presented, **When** the reader dismisses it, **Then** the sheet
   closes, the faded preview stays visible, the remaining text stays unreachable, and a persistent
   control remains on screen to bring the sheet back.
4. **Given** the reader is a subscriber, **When** they open the same premium story, **Then** the
   full body is shown with no fade and no unlock sheet.
5. **Given** the reader opens a premium story, **When** the unlock sheet is presented, **Then** a
   paywall-shown event is recorded with the story that triggered it.
6. **Given** the unlock sheet is presented, **When** the reader taps the subscribe action, **Then**
   a "not yet available" message is shown, the reader stays on the reading screen, and their
   entitlement is unchanged.

---

### User Story 3 - Read what is already downloaded, with no network (Priority: P3)

A reader in airplane mode opens a story they have already seen in the list. The reader screen fills
from the local copy, with no error and no blank body. A story whose text has never been retrieved
shows a clear "not available offline" message with a retry, rather than an empty page.

**Why this priority**: Offline reading is a constitutional requirement (Principle IV) and a case
study acceptance criterion, but the screen is demonstrable without it first.

**Independent Test**: Open a story once with network, disable the network, reopen it and confirm it
renders fully; then open a story never opened before and confirm the offline message with retry.

**Acceptance Scenarios**:

1. **Given** a story's content is stored locally, **When** the reader opens it with no network,
   **Then** the full reading screen renders from the local copy with no error surfaced.
2. **Given** a story's content is not stored locally, **When** the reader opens it with no network,
   **Then** an offline message with a retry control is shown in place of the body.
3. **Given** the offline message is shown, **When** the network returns and the reader taps retry,
   **Then** the content loads and the message is replaced by the body.

---

### Edge Cases

- Content retrieval fails for a reason other than being offline: the screen shows an error state
  with retry, and the failure is reported to crash/error reporting — never silently swallowed.
- The story id passed in no longer exists locally (deleted by a sync between list and tap): the
  screen shows a "story unavailable" state with a way back rather than an indefinite spinner.
- The reader's entitlement changes while the screen is open (subscription starts elsewhere): the
  sheet dismisses and the full body becomes readable without the reader having to reopen the story.
- A premium story has less content than the preview share allows: the preview shows what exists and
  the unlock sheet still appears.
- The reader taps the listen control: it states that playback is not yet available rather than
  appearing to start and doing nothing.
- A very long story: scrolling stays smooth and the reading position is restored after a
  configuration change (rotation, theme switch, process recreation).
- Titles or author names longer than one line: the header truncates rather than pushing content off
  screen.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Tapping a story in the home list MUST open a story detail (reading) destination for
  that specific story, and MUST NOT lose the home list's query, category filter, or scroll position
  when the reader returns.
- **FR-002**: The reading screen MUST identify the story it was opened for by a stable identifier,
  and MUST survive process recreation (returning to the same story, at the same reading position).
- **FR-003**: The reading screen MUST display, for the opened story: category, reading length in
  minutes, title, author, a cover area, and the story body.
- **FR-004**: The reading screen MUST show a distinct loading state while content is being
  resolved, a distinct error state with retry when resolution fails, and a distinct offline state
  when content is unavailable without a network.
- **FR-005**: Every content-resolution failure MUST be surfaced to the reader on screen and
  reported to error reporting; none may be silently swallowed.
- **FR-006**: For a story that is not premium, or for a reader with a valid entitlement, the screen
  MUST show the complete body with no fade, no badge-driven restriction, and no unlock sheet.
- **FR-007**: For a premium story opened by a reader without entitlement, the screen MUST show only
  a bounded opening preview of the body, fade the preview into the background, and MUST NOT make
  the remaining text reachable by scrolling, selecting, or copying.
- **FR-008**: In the restricted case the screen MUST present a premium unlock bottom sheet stating
  how much was free, listing the subscription benefits, and offering the primary subscribe action
  with price and cancellation terms.
- **FR-009**: The unlock bottom sheet MUST be dismissible, and after dismissal a persistent control
  MUST remain on the reading screen to present it again.
- **FR-010**: All premium decisions on this screen MUST be taken from the single entitlement source
  of truth, never from a per-screen flag or raw storage read.
- **FR-011**: When entitlement changes while the screen is open, the screen MUST update to the
  matching state without requiring the reader to reopen the story.
- **FR-012** *(deferred post-implementation — see README Cut Corners)*: The screen MUST record analytics for content viewed (with story identity and whether
  it was restricted) and for paywall shown.
- **FR-013**: The reading screen MUST render correctly in both light and dark themes, following the
  design source's Reader screens (2a full access, 2b restricted) and the app's token layer.
- **FR-014**: All reader and unlock-sheet text MUST come from string resources.
- **FR-015**: Story content (body text and author) MUST be available from the local store so that a
  previously retrieved story renders with no network.
- **FR-016**: The premium subscribe action MUST acknowledge the tap with a clear "not yet available"
  message and leave entitlement unchanged; granting a subscription is out of scope for this feature.
  The action MUST NOT appear broken, silently do nothing, or navigate anywhere.
- **FR-017**: The reader footer MUST show the listen affordance and progress display from the
  design, where progress reflects the reader's position in the text. Audio playback is out of scope
  for this feature: the listen control MUST communicate that playback is not yet available rather
  than appearing functional and doing nothing.

### Key Entities *(include if data involved)*

- **Story**: a short story as already known to the app — identity, category, title, teaser, reading
  minutes, audio availability, premium flag. This feature extends what a story carries to include
  the attributes the reader needs.
- **Story content**: the readable payload of a story — author and the ordered body text — held
  separately from list metadata because the list does not need it and it is much larger.
- **Entitlement**: the reader's current premium status, owned by one source of truth that every
  gating decision on this screen consults.
- **Reading position**: how far through a story the reader has progressed, used to restore the view
  and to drive the progress display.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: From tapping a story card, the reading screen appears and shows the story's title and
  header within 1 second on a mid-range device, with content filled within 2 seconds when it is
  stored locally.
- **SC-002**: 100% of stories opened from the home list show that story's own content — no
  placeholder text and no mismatch between the tapped card and the opened story.
- **SC-003**: 100% of premium stories opened by a non-subscriber show a bounded preview and the
  unlock sheet; 0% expose the remaining text through scrolling, selection, or copying.
- **SC-004**: With no network, every story previously opened renders completely; every story never
  opened shows the offline message with retry instead of a blank or broken screen.
- **SC-005**: Returning from the reading screen restores the home list's filter and scroll position
  in 100% of cases.
- **SC-006**: Scrolling a full-length story stays visually smooth (no perceptible stutter) on a
  mid-range device.
- **SC-007**: Both the full-access and restricted reading screens are verifiable in light and dark
  from previews, with no hardcoded colors, sizes, or text styles.

## Assumptions

- The design source's "Reader — premium and free" screens (2a full access, 2b restricted) define
  the visual target. The design draws the restricted state as an anchored card; per the request it
  is delivered as a bottom sheet, which the design's own follow-up note anticipates.
- The free preview share is the design's stated 30% of the body.
- Entitlement is mocked for this case study; there is no real billing integration, and the default
  state is non-subscriber.
- Story body text and author are not present in the current data model and will be added as part of
  this feature; content may be seeded locally rather than fetched from a live service.
- A navigation host does not yet exist — the app currently shows the home screen directly — so this
  feature introduces the navigation layer that carries a story identifier from list to detail.
- The dedicated full-screen paywall and the full-screen audio player are separate design screens and
  are out of scope here. Consequently, the subscribe action and the listen control are present and
  honest about being unavailable (FR-016, FR-017) rather than wired to flows this feature does not
  build; both are logged as deliberate cut corners in the README.
- Because entitlement cannot be granted in-app yet, the subscriber path (Story 2, scenario 4) is
  demonstrated by driving the entitlement source directly — that source still remains the single
  place every gating decision consults.
- The overflow control in the reader header (font size, theme controls) is out of scope for this
  feature; it may be present but inert or omitted.
- Analytics and error reporting destinations are the ones the project already standardises on; this
  feature only adds the events named in FR-012.
