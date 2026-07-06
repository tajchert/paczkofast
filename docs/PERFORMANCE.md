# Performance

This file tracks Paczkofast's performance over time: how to measure it, and a
dated log of results so regressions and wins are visible across major changes.

**When you make a change that could affect performance** (startup, a hot screen,
list rendering, image/QR work, dependency or Compose-compiler bumps), re-run the
relevant measurement below and **append a row** to the matching results table.
Never edit past rows — the history is the point.

Record the device, the short commit hash, and the date with every row. Numbers
are device-specific, so only compare rows measured on the same device.

## Reference device

Unless a row says otherwise, measurements were taken on:

- **OnePlus CPH2747**, Android 16 (API 36), physical device, non-rooted.

Physical non-rooted devices print a clock-instability warning during
macrobenchmarks; results are still valid for relative before/after comparison,
but absolute numbers vary run-to-run. Prefer comparing the **median**, and watch
the **max** for the slow-cold-start tail.

---

## 1. Cold startup (Macrobenchmark)

Measures cold-start time-to-initial-display under two compilation modes:
`None` (JIT only — the "before") vs `BaselineProfile` (AOT via the bundled
profile — the "after"). Source: `baselineprofile/StartupBenchmark.kt`.

**Run:**

```bash
./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=pl.tajchert.paczko.fast.baselineprofile.StartupBenchmark
```

Results JSON (gitignored):
`baselineprofile/build/outputs/connected_android_test_additional_output/benchmarkRelease/connected/<device>/*-benchmarkData.json`

Regenerate the baseline profile itself after meaningful startup-path changes:

```bash
./gradlew :app:generateBaselineProfile
```

### Results — timeToInitialDisplay (ms), COLD, 10 iterations

| Date | Commit | Device | None median | Profile median | Δ median | None max | Profile max | Notes |
|------|--------|--------|------------:|---------------:|---------:|---------:|------------:|-------|
| 2026-07-06 | c409da5 | OnePlus CPH2747 (A16) | 225.6 | 203.0 | −22.6 (−10%) | 3044.4 | 213.7 | First baseline. Profile mainly kills the slow-cold-start tail (3.0 s → 214 ms). |

---

## 2. Compose recomposition & stability

Two complementary checks that a change didn't reintroduce recomposition churn.

### 2a. Compiler stability report (static)

Temporarily enable reports in the module under review, e.g. in
`feature/parcels/impl/build.gradle.kts`:

```kotlin
composeCompiler {
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
    reportsDestination = layout.buildDirectory.dir("compose_reports")
}
```

```bash
./gradlew :feature:parcels:impl:compileReleaseKotlin --rerun-tasks
# then inspect build/compose_reports/*-classes.txt (stable class?) and
# *-composables.txt (restartable skippable?)
```

Revert the block after checking — it is not committed.

### 2b. Runtime recomposition counts (dynamic)

Drop a temporary counter into a composable and drive the app via adb, watching
`RECOMP` logs; confirm state-consuming composables skip when their inputs are
unchanged. (See git history around commit `0878dad` for the probe used.)

### Results — Compose stability

| Date | Commit | Check | Result |
|------|--------|-------|--------|
| 2026-07-06 | 7c43c58 | Compiler report | All parcel-feature UI states `stable`; `ParcelListContent`, `ParcelSections`, `HistoryList`, `MemberListCard`, `BoxOpenScreen`, `SuccessScreen`, `MultiPackageGroupCard`, and per-item ready cards are `restartable skippable`. |
| 2026-07-06 | 7c43c58 | Runtime (launch + init refresh) | On the refresh-complete emission (`isRefreshing` flips, parcels unchanged) the container recomposed but `ParcelSections`/cards **skipped** — no redundant re-partition/grouping. |

---

## 3. QR generation (parcel detail)

The detail screen generated the pickup QR on the main thread. Measured with a
temporary timing log around `QrCodeBitmapFactory.create` while opening a
ready-parcel detail.

### Results — QR bitmap generation

| Date | Commit | Device | Time (main thread) | Notes |
|------|--------|--------|-------------------:|-------|
| 2026-07-06 | (pre-fix) | OnePlus CPH2747 (A16) | 258 ms | 768×768 bitmap via per-pixel `setPixel` loop, blocking the first frame. |
| 2026-07-06 | 178bf5c | OnePlus CPH2747 (A16) | 28 ms → 0 blocking | Rendered at native module resolution (33×33) + bulk `setPixels`, then moved off-thread via `produceState(Dispatchers.Default)`. |

---

## Major performance changes (changelog)

Newest first. Link each entry to the measurement rows above.

- **`4597291` / `c409da5` — Baseline profile + startup benchmark.** Generated
  baseline profile (16,106 rules) bundled into the release APK; startup median
  225.6 → 203.0 ms, worst-case tail 3.0 s → 214 ms. See §1.
- **`0878dad` / `32960da` / `7c43c58` — Compose stability.** Parcel UI state uses
  `ImmutableList` + `@Immutable`; a `compose_stability.conf` marks `core:model`
  domain types stable in every Compose module. State/composables now skippable.
  See §2.
- **`178bf5c` — QR rendering.** Module-resolution bitmap + off-main-thread
  generation; detail-screen open no longer blocks ~258 ms on QR. See §3.
