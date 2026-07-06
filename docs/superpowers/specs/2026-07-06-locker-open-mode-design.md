# Locker Open Mode — Hold vs Nearby

Date: 2026-07-06
Branch: `worktree-locker-open-mode`

## Goal

Let the user choose, in Settings, **how they confirm opening a locker compartment**:

- **Hold to open** (default, today's behavior): press-and-hold a bar; the compartment
  opens only after a completed hold.
- **Near locker**: a single tap on "Open locker" opens the compartment, but that button
  is only enabled when the phone is genuinely at the locker (distance < 50 m **and** a
  trustworthy GPS fix). A secondary **"Override with hold"** control is always available
  as an escape hatch when the fix is weak or the user is farther away.

Two cross-cutting requirements:

1. **Live distance** — distance to the locker must appear fast and refine as the GPS
   fix improves, in both modes.
2. **No layout shift** — key information (locker header, the central hero, the primary
   action) stays anchored across every state: idle → holding → opening → box open →
   success/error. Nothing jumps.

## Non-goals

- No change to the collect API sequence (validate → open → status → closed → claim).
- No change to which parcels get claimed, or to multi-compartment grouping.
- No new Settings loading/error state (Settings has none today; out of scope).
- Dark mode, onboarding, and unrelated screens are untouched.

## Decisions (confirmed with user)

| Question | Decision |
|---|---|
| Layout stability scope | **Full shared scaffold** across all collect states |
| Nearby gate | `distance < 50 m` **AND** `accuracy <= 30 m` |
| Location tech | **Add FusedLocationProvider** (`play-services-location`) |
| Nearby confirm | **Single tap opens** (proximity is the confirmation) |
| Settings copy | Section **UNLOCK METHOD**; options **Hold to open** / **Near locker** |
| Nearby disabled guidance | Show reason ("Move closer — 62 m away" / "Waiting for a precise GPS fix…") + always-available "Override with hold" |

Thresholds are fixed constants: `NEARBY_DISTANCE_METERS = 50`, `NEARBY_ACCURACY_METERS = 30`.

---

## 1. New preference: `LockerOpenMode`

A new enum, persisted like `ThemeMode`.

- **`core:model`**
  - New `LockerOpenMode.kt`:
    ```kotlin
    enum class LockerOpenMode { HOLD, NEARBY }
    ```
  - `UserPreferences` gains `val lockerOpenMode: LockerOpenMode = LockerOpenMode.HOLD`.
- **`core:datastore`** — `UserPreferencesDataSource`
  - New key `stringPreferencesKey("locker_open_mode")`.
  - Parse in the `userPreferences` map (unknown/absent → `HOLD`, same `runCatching` pattern
    used for theme).
  - New `suspend fun setLockerOpenMode(mode: LockerOpenMode)`.
- **`core:data`** — `UserPreferencesRepository` interface + `DefaultUserPreferencesRepository`
  - Add `suspend fun setLockerOpenMode(mode: LockerOpenMode)` delegating to the data source.
- **`core:testing`** — `FakeUserPreferencesRepository`
  - Implement `setLockerOpenMode` (emit `current.copy(lockerOpenMode = mode)`).

## 2. Settings UI

- **`SettingsViewModel`**
  - `SettingsUiState` gains `lockerOpenMode: LockerOpenMode = LockerOpenMode.HOLD`.
  - Map it in the existing `combine`.
  - New `fun setLockerOpenMode(mode: LockerOpenMode)`.
- **`SettingsScreen`**
  - New section after "Appearance": `SectionLabel("Unlock method")` + a `PaczkofastCard`
    with a title, a one-line helper ("How you confirm opening a locker."), and the existing
    generic `SegmentedControl<LockerOpenMode>` with options
    `HOLD -> "Hold to open"`, `NEARBY -> "Near locker"`.
  - `SettingsContent` takes `lockerOpenMode` + `onOpenModeSelected`; preview updated.

## 3. Live location (FusedLocationProvider)

- **Dependency**: add `play-services-location` to `gradle/libs.versions.toml` and to
  `core/common/build.gradle.kts`.
- **`LocationProvider`** (interface) gains:
  ```kotlin
  fun locationUpdates(): Flow<GeoPoint>
  ```
  `currentLocation()` stays (the collect use case still needs one authoritative fix at
  validation time — see `CollectParcelUseCase:29`).
- **`AndroidLocationProvider`**
  - Reimplement on `FusedLocationProviderClient`:
    - `locationUpdates()` = `callbackFlow`: emit `lastLocation` immediately if present
      (instant seed), then `requestLocationUpdates` with a high-accuracy request
      (~1 s interval); `awaitClose { removeLocationUpdates }`. Permission-guarded; if no
      permission, the flow completes without emitting.
    - `currentLocation()` = fused `getCurrentLocation(HIGH_ACCURACY)` (keeps the existing
      error messages: "Location permission is required", "Current location unavailable").
  - `GeoPoint.accuracy` is populated from `Location.accuracy`.
- **Test fakes**: the private `FakeLocationProvider` in `CollectViewModelTest` and
  `CollectParcelUseCaseTest` implement `locationUpdates()` (a `flowOf(location)` /
  configurable emission list, `emptyFlow()` for the permission-failure fake).

## 4. Nearby threshold helper (`core:common`)

Add to `LockerDistance.kt` (pure, unit-testable):

```kotlin
const val NEARBY_DISTANCE_METERS = 50
const val NEARBY_ACCURACY_METERS = 30

/** True when the user is close enough AND the fix is trustworthy enough to one-tap open. */
fun isWithinNearbyThreshold(distanceMeters: Int?, accuracyMeters: Int?): Boolean =
    distanceMeters != null && distanceMeters < NEARBY_DISTANCE_METERS &&
        accuracyMeters != null && accuracyMeters <= NEARBY_ACCURACY_METERS
```

## 5. CollectViewModel — live distance + mode

- Inject `UserPreferencesRepository`.
- `CollectUiState` gains:
  - `openMode: LockerOpenMode = LockerOpenMode.HOLD`
  - `accuracyMeters: Int?` (alongside existing `distanceMeters`)
  - derived `val nearbyReady get() = isWithinNearbyThreshold(distanceMeters, accuracyMeters)`
- `arm(shipmentNumber)`:
  - Read `lockerOpenMode` from prefs (`userPreferences.first()`), set on state.
  - Resolve compartment members + pickup point (as today).
  - **Start a location-updates job** (`locationJob`): collect `locationProvider.locationUpdates()`,
    and for each fix recompute `distanceMeters = metersToLocker(...)` and
    `accuracyMeters = fix.accuracy.roundToInt()`, updating state **only while `state is Idle`**.
  - The one-shot `currentLocation()` call in `arm` is replaced by this stream (first emission
    is the instant seed).
- `start(shipmentNumber)`:
  - Cancel `locationJob` (stop draining GPS once we begin opening).
  - Everything else unchanged (validate uses `currentLocation()` inside the use case).
- `onCleared()`: cancel `locationJob`.

Both Nearby "Open locker" tap and Nearby "Override with hold" completion call the same
`start(...)`. Hold mode's completed hold also calls `start(...)`. Button *enablement* for
Nearby is a pure function of `uiState.nearbyReady` (UI-level); the VM does not itself block
`start`.

## 6. Shared collect scaffold (no layout shift)

Restructure `CollectScreen.kt` so **every** state renders through one skeleton with three
fixed zones, instead of today's separate full-screen composables.

```
┌───────────────────────────────┐
│  HEADER  "LOCKER WAW01A"       │  fixed slot, present in every state
├───────────────────────────────┤
│                                │
│         HERO (centered)        │  one Box, fixed footprint (216.dp tall)
│   ring / open-box / check / !  │  content cross-fades in place
│                                │
│   HEADLINE + SUBLINE (slots)   │  single-line title + secondary line
│                                │
│   DETAIL (members / summary)   │  variable content, consistent slot
├───────────────────────────────┤
│  ACTION ZONE (bottom)          │  fixed position
│  primary + optional secondary  │
└───────────────────────────────┘
```

**Derivation (pure, testable).** A function
`collectScreenModel(state: CollectState, uiState: CollectUiState): CollectScreenModel`
maps the current state+mode to:

- `header`: locker line ("LOCKER WAW01A", or "ERROR · LOCKER …" on failure).
- `hero`: `Distance | OpenBox | Check | Error` (which glyph/ring to show), plus ring
  `progress` when a hold is active.
- `headline` / `subline` text.
- `detail`: none / single parcel card / multi checklist / collected summary.
- `actions`: which buttons (Hold bar / Open+Override / Back / Retry+Support) and their
  enabled state.

`CollectScaffold` renders the three zones from that model. Because the header, hero
footprint, and action zone are always laid out (even when a zone's content is a spacer),
switching states changes *content*, never *position*. The hero uses `Crossfade`/
`AnimatedContent` sized to a fixed 216.dp box so the ring (216) and the 150.dp blobs share
the same center.

**Hold decomposition (designsystem).** Today `HoldToOpenPanel` bundles the ring + hold
bar + gesture state. Split it so the shared scaffold can place the ring in the hero zone and
the bar in the action zone while a single hold-progress value drives both:

- `DistanceRing(progress, distanceText, caption)` — promoted to a public component.
- `rememberHoldToOpenState(holdDurationMillis, enabled, onConfirmed): HoldToOpenState`
  exposing `progress: Float`, `isHolding: Boolean`, and a press `Modifier` (wrapping today's
  `HoldProgress` controller + `Animatable` logic verbatim — the fire-once-on-complete,
  animate-back-on-release, haptic behavior is preserved).
- `HoldBar(state, label, ...)` — the pressable amber bar bound to a `HoldToOpenState`.

`HoldToOpenPanel` is removed; `HoldProgress` and `HoldProgressTest` stay unchanged.

**Per-mode idle action zone:**

- **Hold mode**: `HoldBar` (primary). Holding fills the hero ring; completion → `start()`.
- **Nearby mode**:
  - Primary `PrimaryActionButton("Open locker", enabled = uiState.nearbyReady)` → `start()`.
  - Disabled subline: `"Move closer — {d} m away"` when a fix exists but too far/coarse,
    `"Waiting for a precise GPS fix…"` when distance is still unknown.
  - Secondary: an "Override with hold" `HoldBar` (always enabled), completion → `start()`.

**Transitional / open / finishing / success / error** all reuse the same scaffold: only the
hero glyph, headline/subline, detail card, and action buttons change. The box-open checklist
and success summary render in the DETAIL slot. Existing copy and the multi-package checklist
behavior are preserved.

Accessibility (existing semantics on the ring, hold bar, checklist rows, buttons) is carried
over; the new "Open locker" button and mode segmented control get proper roles/labels.

---

## File-by-file impact

| Module | File | Change |
|---|---|---|
| core:model | `LockerOpenMode.kt` | **new** enum |
| core:model | `UserPreferences.kt` | add `lockerOpenMode` |
| core:datastore | `UserPreferencesDataSource.kt` | key + parse + setter |
| core:data | `UserPreferencesRepository.kt` | interface + default setter |
| core:testing | `FakeUserPreferencesRepository.kt` | implement setter |
| core:common | `LocationProvider.kt` | add `locationUpdates()` |
| core:common | `AndroidLocationProvider.kt` | fused impl of both methods |
| core:common | `LockerDistance.kt` | thresholds + `isWithinNearbyThreshold` |
| core:common | `build.gradle.kts` | add dependency |
| — | `gradle/libs.versions.toml` | add `play-services-location` |
| designsystem | `HoldToOpenPanel.kt` → hold primitives | split into `DistanceRing` + `rememberHoldToOpenState` + `HoldBar` |
| settings:impl | `SettingsViewModel.kt` / `SettingsScreen.kt` | mode state + section |
| parcels:impl | `CollectViewModel.kt` | mode + live distance/accuracy |
| parcels:impl | `CollectScreen.kt` (+ new `CollectScreenModel.kt`, `CollectScaffold.kt`) | shared scaffold |

## Testing

- **`LockerDistanceTest`** (core:common): `isWithinNearbyThreshold` boundaries — 49/50/51 m,
  accuracy 30/31 m, null distance, null accuracy.
- **Preferences** (core:datastore / core:data): `lockerOpenMode` round-trips; default is `HOLD`;
  unknown stored value → `HOLD`.
- **`SettingsViewModelTest`**: `setLockerOpenMode` updates state; initial state reflects prefs.
- **`CollectViewModelTest`**:
  - `arm` populates `openMode` from prefs.
  - Live location: a fake emitting a sequence of fixes updates `distanceMeters`/`accuracyMeters`
    and flips `nearbyReady`; updates stop once `state` leaves `Idle`.
  - Existing start/multi-compartment/missing-open-code/permission-denied tests still pass after
    the `locationUpdates()` fake addition.
- **`collectScreenModel`** (pure): each `(state, mode)` maps to the expected header/hero/headline/
  actions — including Nearby-far (disabled + reason) vs Nearby-ready (enabled).
- **Manual / visual**: verify no layout shift across idle → hold → opening → open → success/error
  in both modes (previews for the scaffold states; run app if practical).

## Risks / watchouts

- **`play-services-location`** is a new Google Play Services dependency — first one for
  location; ensure it doesn't pull surprising transitive versions and that the app still
  builds/min-SDK-34 is satisfied.
- **Battery**: location updates run only while the collect screen is armed/idle and are
  cancelled the moment opening starts and in `onCleared()`.
- **Accuracy semantics**: `Location.accuracy` is 68% horizontal radius in metres; `<= 30`
  is a deliberate, conservative gate. Documented as a constant so it's easy to tune.
- **No-shift is a structural guarantee**, not just styling — it must come from the shared
  scaffold always laying out all three zones, not from per-screen tweaks.
- Privacy: never log coordinates, distance, accuracy, open codes (existing rule).
