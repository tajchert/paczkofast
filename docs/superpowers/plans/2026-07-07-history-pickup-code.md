# History Pickup Code + QR Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users reveal the stored pickup code + QR for a picked-up parcel as a read-only reference, via an expandable row on both detail screens.

**Architecture:** UI-only change in `feature:parcels:impl`. A new shared `ExpandablePickupCodeRow` composable replaces the two duplicated static "no longer needed" rows (`CollapsedPickupCodeRow` in `ParcelDetailScreen.kt`, `CollapsedBoxPickupCodeRow` in `MultiPackageDetailScreen.kt`). Expansion is local `rememberSaveable` state; the revealed content reuses the existing `QrPanel`. No model/data/ViewModel changes — the delivered `parcel` already carries `qrCode`/`openCode` from Room.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, `com.android.compose.screenshot` (Compose Preview Screenshot Testing).

## Global Constraints

- Public repo. All previews/fixtures use obviously-fake data only: `openCode = "000000"`, `qrCode = "P|000000|000000000000000000000000"` (or `DEMO|...`), all-zero shipment numbers, `WAW01A`, "Example ..." placeholders. Never real codes/QR/PII.
- No `now()`-relative text in screenshot fixtures (drifts between record/validate). Use `null` or fixed absolute ISO instants.
- UI copy is Polish (single `res/values/strings.xml`, no other locale files).
- No collect/open action in this flow — the revealed code is display-only.
- Preserve accessibility semantics; the chevron icon stays decorative (`contentDescription = null`).
- Follow existing neo-brutalist component patterns; reuse `PaczkofastCard` / `QrPanel` rather than new primitives.

---

### Task 1: Strings + `ExpandablePickupCodeRow` shared component

**Files:**
- Modify: `feature/parcels/impl/src/main/res/values/strings.xml`
- Create: `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/ExpandablePickupCodeRow.kt`

**Interfaces:**
- Produces: `internal fun ExpandablePickupCodeRow(qrCode: String?, openCode: String?, modifier: Modifier = Modifier, initiallyExpanded: Boolean = false)` — a `PaczkofastCard` row; collapsed by default, expands to show `QrPanel(payload = qrCode, code = openCode)`. `initiallyExpanded` exists so screenshot previews can render the expanded state (screenshot tests can't tap).
- Consumes: existing `PaczkofastCard`, `QrPanel`, `PaczkofastTheme`, `MonoLabelLarge`, and `R.string.pickup_code_qr`.

- [ ] **Step 1: Add the two a11y strings**

In `feature/parcels/impl/src/main/res/values/strings.xml`, add (near `pickup_code_qr`):

```xml
<string name="show_pickup_code">Pokaż kod odbioru</string>
<string name="hide_pickup_code">Ukryj kod odbioru</string>
```

Leave `<string name="pickup_code_qr">Kod odbioru i QR</string>` unchanged. Do NOT remove `no_longer_needed` yet — the old rows deleted in Tasks 2–3 still reference it; it is removed in Task 3.

- [ ] **Step 2: Create the component**

Create `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/ExpandablePickupCodeRow.kt`:

```kotlin
package pl.tajchert.paczko.fast.feature.parcels.impl.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabelLarge
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.ui.QrPanel
import pl.tajchert.paczko.fast.feature.parcels.impl.R

/**
 * Expandable row shown once a parcel/box has been picked up. Collapsed by
 * default; tapping reveals the stored pickup [qrCode] + [openCode] as a
 * read-only reference. After pickup the compartment is already emptied, so this
 * is a receipt — there is no collect/open action here.
 *
 * Callers gate rendering on code presence: only show this row when [qrCode] or
 * [openCode] is non-blank.
 *
 * [initiallyExpanded] exists only so screenshot previews can capture the
 * expanded state (screenshot tests cannot tap); real callers omit it.
 */
@Composable
internal fun ExpandablePickupCodeRow(
    qrCode: String?,
    openCode: String?,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    val colors = PaczkofastTheme.colors
    var expanded by rememberSaveable(initiallyExpanded) { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        label = "pickupCodeChevron",
    )
    val stateLabel = stringResource(
        if (expanded) R.string.hide_pickup_code else R.string.show_pickup_code,
    )

    PaczkofastCard(
        modifier = modifier.semantics { stateDescription = stateLabel },
        onClick = { expanded = !expanded },
        onClickLabel = stateLabel,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.background, RoundedCornerShape(7.dp))
                    .border(2.5.dp, colors.borderStrong, RoundedCornerShape(7.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(15.dp),
                )
            }
            Text(
                text = stringResource(R.string.pickup_code_qr),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.rotate(chevronRotation),
            )
        }

        if (expanded) {
            when {
                !qrCode.isNullOrBlank() -> QrPanel(
                    payload = qrCode,
                    modifier = Modifier.padding(top = 12.dp),
                    code = openCode,
                    qrSize = 150,
                )
                !openCode.isNullOrBlank() -> Text(
                    text = openCode,
                    style = MonoLabelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}
```

- [ ] **Step 3: Compile the module**

Run: `./gradlew :feature:parcels:impl:compileProdDebugKotlin`
Expected: `BUILD SUCCESSFUL`. The new component is unused so far, and the old rows/strings are untouched, so the module still compiles cleanly.

- [ ] **Step 4: Commit**

```bash
git add feature/parcels/impl/src/main/res/values/strings.xml \
        feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/ExpandablePickupCodeRow.kt
git commit -m "feat(parcels): add ExpandablePickupCodeRow for history pickup code"
```

---

### Task 2: Wire into `ParcelDetailScreen` and remove old row

**Files:**
- Modify: `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/ParcelDetailScreen.kt` (delivered branch ~line 236; `CollapsedPickupCodeRow` definition ~lines 344-393)

**Interfaces:**
- Consumes: `ExpandablePickupCodeRow(qrCode, openCode, ...)` from Task 1.

- [ ] **Step 1: Replace the call site**

In `ParcelDetailScreen.kt`, in the `if (delivered) { ... }` block, replace:

```kotlin
            PickedUpSummaryCard(
                waitLabel = parcel.pickupWaitLabel(),
                timestamp = formatTimelineTime(parcel.pickUpDate),
            )
            CollapsedPickupCodeRow()
```

with:

```kotlin
            PickedUpSummaryCard(
                waitLabel = parcel.pickupWaitLabel(),
                timestamp = formatTimelineTime(parcel.pickUpDate),
            )
            if (!parcel.qrCode.isNullOrBlank() || !parcel.openCode.isNullOrBlank()) {
                ExpandablePickupCodeRow(
                    qrCode = parcel.qrCode,
                    openCode = parcel.openCode,
                )
            }
```

- [ ] **Step 2: Delete the obsolete `CollapsedPickupCodeRow`**

Remove the entire `private fun CollapsedPickupCodeRow(modifier: Modifier = Modifier) { ... }` composable (including its KDoc block starting "Collapsed row replacing the QR panel once a parcel has been delivered").

- [ ] **Step 3: Remove now-unused imports**

After deletion, `ParcelDetailScreen.kt` may no longer use `MonoLabel` (used only in the deleted row's subtitle) or the `QrCode2`/`ChevronRight` icons. Check and remove any import that is now unused:

Run: `rg -n "MonoLabel\b|QrCode2|ChevronRight" feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/ParcelDetailScreen.kt`
For each symbol with no remaining usage in the file body, delete its `import` line. (Leave imports that still have usages.)

- [ ] **Step 4: Commit**

```bash
git add feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/ParcelDetailScreen.kt
git commit -m "feat(parcels): reveal pickup code+QR on delivered parcel detail"
```

---

### Task 3: Wire into `MultiPackageDetailScreen`, remove old row, compile module

**Files:**
- Modify: `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/MultiPackageDetailScreen.kt` (delivered branch ~line 173; `CollapsedBoxPickupCodeRow` definition ~lines 312-361)

**Interfaces:**
- Consumes: `ExpandablePickupCodeRow(qrCode, openCode, ...)` from Task 1. Here the code comes from `uiState.qrCode` / `uiState.openCode` (not a `Parcel`).

- [ ] **Step 1: Replace the call site**

In `MultiPackageDetailScreen.kt`, replace:

```kotlin
            if (delivered) {
                CollapsedBoxPickupCodeRow()
            } else {
```

with:

```kotlin
            if (delivered) {
                if (!uiState.qrCode.isNullOrBlank() || !uiState.openCode.isNullOrBlank()) {
                    ExpandablePickupCodeRow(
                        qrCode = uiState.qrCode,
                        openCode = uiState.openCode,
                    )
                }
            } else {
```

(Leave the `else { ... }` branch — the ready-state `QrPanel` + collect button — unchanged.)

- [ ] **Step 2: Delete the obsolete `CollapsedBoxPickupCodeRow`**

Remove the entire `private fun CollapsedBoxPickupCodeRow(modifier: Modifier = Modifier) { ... }` composable (including its KDoc "Collapsed row replacing the shared QR panel once a box has been delivered").

- [ ] **Step 3: Remove now-unused imports**

Run: `rg -n "MonoLabel\b|QrCode2|ChevronRight" feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/MultiPackageDetailScreen.kt`
Delete the `import` line for any symbol with no remaining usage in the file body.

- [ ] **Step 4: Remove the now-dead `no_longer_needed` string**

Both usages are gone after Task 2 + Step 2 above, so delete this line from `feature/parcels/impl/src/main/res/values/strings.xml`:

```xml
<string name="no_longer_needed">Już niepotrzebne</string>
```

Then confirm it is fully gone:

Run: `rg -n "no_longer_needed" feature/parcels/impl`
Expected: no matches.

- [ ] **Step 5: Compile the module**

Run: `./gradlew :feature:parcels:impl:compileProdDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/detail/MultiPackageDetailScreen.kt \
        feature/parcels/impl/src/main/res/values/strings.xml
git commit -m "feat(parcels): reveal pickup code+QR on delivered box detail"
```

---

### Task 4: Screenshot coverage + verification

**Files:**
- Create: `feature/parcels/impl/src/screenshotTest/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/screenshot/PickupCodeRowScreenshotTest.kt`
- Regenerate: reference PNGs under `feature/parcels/impl/src/screenshotTestDebug/reference/...` (existing delivered `ParcelDetailScreenshot_*_1` and `MultiPackageDetailScreenshot_*_1` change because the collapsed row lost its "Już niepotrzebne" subtitle; new PNGs for the test below)

**Interfaces:**
- Consumes: `ExpandablePickupCodeRow` (Task 1), `PaczkofastPreviews`, `PaczkofastTheme`.

- [ ] **Step 1: Add a dedicated collapsed + expanded screenshot test**

Create `PickupCodeRowScreenshotTest.kt`:

```kotlin
package pl.tajchert.paczko.fast.feature.parcels.impl.screenshot

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.feature.parcels.impl.detail.ExpandablePickupCodeRow

// Obviously-fake pickup code/QR per the repo's public-safety rules.
private const val SAMPLE_OPEN_CODE = "000000"
private const val SAMPLE_QR = "P|000000|000000000000000000000000"

@PreviewTest
@PaczkofastPreviews
@Composable
private fun PickupCodeRowCollapsed() {
    PaczkofastTheme {
        ExpandablePickupCodeRow(
            qrCode = SAMPLE_QR,
            openCode = SAMPLE_OPEN_CODE,
            modifier = Modifier.padding(16.dp),
            initiallyExpanded = false,
        )
    }
}

@PreviewTest
@PaczkofastPreviews
@Composable
private fun PickupCodeRowExpanded() {
    PaczkofastTheme {
        ExpandablePickupCodeRow(
            qrCode = SAMPLE_QR,
            openCode = SAMPLE_OPEN_CODE,
            modifier = Modifier.padding(16.dp),
            initiallyExpanded = true,
        )
    }
}
```

- [ ] **Step 2: Record all references**

Run: `./gradlew :feature:parcels:impl:updateDebugScreenshotTest`
Expected: `BUILD SUCCESSFUL`; new `PickupCodeRow*` PNGs appear and the delivered `ParcelDetailScreenshot_*_1` / `MultiPackageDetailScreenshot_*_1` PNGs are rewritten under `src/screenshotTestDebug/reference/`.

- [ ] **Step 3: Validate references are stable**

Run: `./gradlew :feature:parcels:impl:validateDebugScreenshotTest`
Expected: `BUILD SUCCESSFUL` (0 differences) — confirms fixtures are deterministic.

- [ ] **Step 4: Run module unit tests + lint**

Run: `./gradlew :feature:parcels:impl:testDebugUnitTest :feature:parcels:impl:lintProdDebug`
Expected: `BUILD SUCCESSFUL`. (No ViewModel logic changed, so no unit test is expected to fail; lint confirms no unused-resource/import regressions.)

- [ ] **Step 5: Review the recorded PNGs before committing**

Manually open the regenerated PNGs and confirm: collapsed row shows icon + "Kod odbioru i QR" + chevron and **no** "Już niepotrzebne"; expanded row shows the QR panel with `000000`. Confirm no real data leaked in.

- [ ] **Step 6: Commit**

```bash
git add feature/parcels/impl/src/screenshotTest \
        feature/parcels/impl/src/screenshotTestDebug/reference
git commit -m "test(parcels): screenshot coverage for expandable pickup code row"
```

---

## Verification (whole feature)

- [ ] Build the app: `./gradlew :app:compileProdDebugKotlin`
- [ ] Drive the change on device/emulator (see `verify` skill): open a delivered parcel in history, tap the "Kod odbioru i QR" row, confirm the QR + code appear and can be hidden again; open a delivered multi-package box and confirm the same; confirm a delivered parcel with no stored code shows no row.

---

## Self-Review

**Spec coverage:**
- Read-only reference of stored code/QR → Task 1 component + Tasks 2–3 wiring. ✓
- Shared component replacing both duplicated rows → Task 1 create, Tasks 2–3 delete originals. ✓
- Gating on code presence (hide for the 4 code-less parcels) → Task 2 Step 1 / Task 3 Step 1 `if (!isNullOrBlank ...)`. ✓
- Minimal framing, drop "Już niepotrzebne" → component has no subtitle (Task 1) + string removed (Task 3 Step 4). ✓
- Accessibility (Role.Button via `PaczkofastCard.onClick`, `onClickLabel`, `stateDescription`, decorative chevron) → Task 1 Step 2. ✓
- Strings: new a11y labels added Task 1 Step 1, dead `no_longer_needed` removed Task 3 Step 4. ✓
- Screenshot tests (collapsed + expanded) + re-record delivered refs → Task 4. ✓
- Privacy: fake fixtures, no `now()` → Task 4 uses `000000`/`P|000000|...`, no time text. ✓

**Placeholder scan:** none — all steps show concrete code/commands.

**Type consistency:** `ExpandablePickupCodeRow(qrCode: String?, openCode: String?, modifier, initiallyExpanded: Boolean)` is defined in Task 1 and called with the same names/types in Tasks 2, 3, 4. `QrPanel(payload, modifier, code, qrSize)` matches `core/ui/QrPanel.kt`. `PaczkofastCard(modifier, onClick, onClickLabel, ...)` matches `core/designsystem/component/Card.kt`.
