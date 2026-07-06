# Reveal pickup code + QR for picked-up parcels (history)

## Problem

Once a parcel is picked up, the detail screens replace the QR panel with a
static `CollapsedPickupCodeRow` that shows "Kod odbioru i QR / Już niepotrzebne"
and a chevron that looks tappable but does nothing — the actual code/QR is never
shown. Real device data confirms the values are still there: of 18 delivered
parcels on the test phone, **14 retain both a 6-digit `openCode` and a 21-char
`qrCode`** in Room. Users can't look up the pickup code they used as a record.

## Goal

Let users reveal the stored pickup code + QR for a picked-up parcel as a
**read-only reference / receipt**. It is not an action: after pickup the
compartment was already emptied, so the code can no longer open anything. There
is no re-open / collect affordance in this flow.

## Scope

**UI only, `feature:parcels:impl`.** No changes to `core:model`, `core:data`,
`core:network`, `core:database`, or any ViewModel. The delivered `parcel` object
already carries `qrCode` + `openCode` (they survive pickup in Room), so this is
purely a rendering change on the two detail screens.

## Design

### Shared component: `ExpandablePickupCodeRow`

Extract a single composable into a new file in the `detail` package
(`feature/parcels/impl/.../detail/`), replacing the duplicated
`CollapsedPickupCodeRow` currently defined in **both** `ParcelDetailScreen.kt`
and `MultiPackageDetailScreen.kt`.

Behavior:

- **Collapsed (default):** QR icon + "Kod odbioru i QR" title + trailing
  chevron. The old "Już niepotrzebne" subtitle is **removed** (minimal framing —
  no archival caption).
- **Expanded:** reveal `QrPanel(payload = qrCode, code = openCode)` inline,
  display-only. No collect/open button. Chevron rotates to indicate state.
- **Code-only fallback:** if `openCode` is present but `qrCode` is blank, show
  the code text without a QR (all 14 real delivered-with-code parcels have both;
  this is defensive only).
- Expanded/collapsed is **local ephemeral UI state** via `rememberSaveable`. No
  ViewModel involvement.

### Gating

Render the row only when the parcel/representative has a non-blank `qrCode`
**or** `openCode`. For the 4 delivered parcels with no code, the row renders
nothing — this also fixes today's bug where the misleading "Kod odbioru i QR"
row shows even when there is no code.

### Call sites

- `ParcelDetailScreen.kt` delivered branch (currently `CollapsedPickupCodeRow()`
  at ~line 236): use `ExpandablePickupCodeRow(qrCode = parcel.qrCode, openCode =
  parcel.openCode)` gated on code presence.
- `MultiPackageDetailScreen.kt` delivered branch (~line 349): same, using the
  representative parcel's `qrCode` / `openCode`.

### Accessibility

- Row is a clickable with `Role.Button`, an `onClickLabel` ("Pokaż kod odbioru"
  / "Ukryj kod odbioru" depending on state), and a `stateDescription`
  (expanded / collapsed).
- The chevron icon is decorative: `contentDescription = null` (state is conveyed
  on the row).
- Preserve `QrPanel`'s existing QR/code semantics when expanded.

### Strings (`res/values/strings.xml`)

- Remove `no_longer_needed` (no longer used).
- Keep `pickup_code_qr` ("Kod odbioru i QR").
- Add `show_pickup_code` / `hide_pickup_code` for the a11y `onClickLabel` /
  `stateDescription`.

## Testing

- **No ViewModel logic changes** → no new/changed ViewModel tests.
- **Golden screenshot tests** (`@PreviewTest`, `src/screenshotTest/`): add
  previews for the row's **collapsed** and **expanded** states, and re-record any
  existing delivered-detail preview that shifts. Confirm during implementation
  whether the delivered detail state is already covered; add coverage if not.
  - Fixtures must be obviously fake and non-`now()`-relative:
    `openCode = "000000"`, `qrCode = "P|000000|000000000000000000000000"`.
  - Make the rendered content composable `internal` if the screenshot source set
    needs to call it.
- Run `./gradlew :feature:parcels:impl:compileProdDebugKotlin
  :feature:parcels:impl:testDebugUnitTest` and
  `:feature:parcels:impl:updateDebugScreenshotTest` /
  `validateDebugScreenshotTest`.

## Files touched

- `feature/parcels/impl/.../detail/ExpandablePickupCodeRow.kt` (new)
- `feature/parcels/impl/.../detail/ParcelDetailScreen.kt`
- `feature/parcels/impl/.../detail/MultiPackageDetailScreen.kt`
- `feature/parcels/impl/src/main/res/values/strings.xml`
- Screenshot reference PNGs under
  `feature/parcels/impl/src/screenshotTestDebug/reference/` (new/updated)

## Privacy

Public repo. All fixtures/previews use fake data (`000000`,
`P|000000|000000000000000000000000`). No real codes, QR payloads, shipment
numbers, or screenshots. The revealed code/QR is rendered from local Room data
only; nothing is logged.

## Out of scope

- No re-open / remote-collect action for picked-up parcels.
- No changes to active-parcel rendering.
- No data model, retention, or API changes.
