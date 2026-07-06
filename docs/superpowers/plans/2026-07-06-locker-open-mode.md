# Locker Open Mode (Hold vs Nearby) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the user pick, in Settings, how they confirm opening a locker — "Hold to open" (default) or "Near locker" (single-tap when within 50 m with a good GPS fix, else an always-available hold override) — with a live-refining distance readout and a shared, non-shifting collect screen.

**Architecture:** A new `LockerOpenMode` preference flows through DataStore → repository → Settings and → `CollectViewModel`. `LocationProvider` gains a `locationUpdates()` Flow (FusedLocationProvider) that the ViewModel streams into `distanceMeters`/`accuracyMeters` while idle. `CollectScreen` is restructured into one shared scaffold (fixed header / center hero / fixed action zone) driven by a pure `collectScreenModel(...)` derivation, so nothing jumps between states.

**Tech Stack:** Kotlin 2.4, Jetpack Compose, Hilt, Preferences DataStore, kotlinx.coroutines/Flow, Google Play Services Location (new), JUnit4 + kotlinx-coroutines-test.

## Global Constraints

- Kotlin/JVM target 17; compileSdk 37; minSdk 34. Do not lower any floor.
- Module boundaries (from AGENTS.md): `core:model` is pure Kotlin; features never see DTOs; `core:common` holds location/distance utilities.
- Nearby thresholds are fixed constants: `NEARBY_DISTANCE_METERS = 50`, `NEARBY_ACCURACY_METERS = 30`. Gate = `distance < 50 && accuracy <= 30`.
- Default `LockerOpenMode` is `HOLD`. Unknown/absent stored value parses to `HOLD`.
- Settings copy: section **"Unlock method"**; options **"Hold to open"** (`HOLD`) / **"Near locker"** (`NEARBY`).
- Do NOT change the collect API sequence (validate → open → status → closed → claim) or which parcels are claimed.
- Privacy: never log coordinates, distance, accuracy, open codes, or QR/phone data. No BODY network logging.
- Use fake sample data only in previews/tests (e.g. `WAW01A`, `Example Sender sp. z o.o.`).
- Follow existing patterns: `@Binds`/`@Provides` DI, `StateFlow` UI state, `collectAsStateWithLifecycle`, design-system components over new primitives, `MainDispatcherRule` for ViewModel tests.

---

### Task 1: Nearby-threshold helper (`core:common`, pure)

**Files:**
- Modify: `core/common/src/main/kotlin/pl/tajchert/paczko/fast/core/common/location/LockerDistance.kt`
- Test: `core/common/src/test/kotlin/pl/tajchert/paczko/fast/core/common/location/LockerDistanceTest.kt` (create)

**Interfaces:**
- Produces:
  - `const val NEARBY_DISTANCE_METERS = 50`
  - `const val NEARBY_ACCURACY_METERS = 30`
  - `fun isWithinNearbyThreshold(distanceMeters: Int?, accuracyMeters: Int?): Boolean`

- [ ] **Step 1: Write the failing test**

Create `core/common/src/test/kotlin/pl/tajchert/paczko/fast/core/common/location/LockerDistanceTest.kt`:

```kotlin
package pl.tajchert.paczko.fast.core.common.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockerDistanceTest {

    @Test
    fun withinThresholdWhenCloseAndAccurate() {
        assertTrue(isWithinNearbyThreshold(distanceMeters = 10, accuracyMeters = 8))
        assertTrue(isWithinNearbyThreshold(distanceMeters = 49, accuracyMeters = 30))
    }

    @Test
    fun notWithinThresholdAtOrBeyondDistanceLimit() {
        assertFalse(isWithinNearbyThreshold(distanceMeters = 50, accuracyMeters = 5))
        assertFalse(isWithinNearbyThreshold(distanceMeters = 51, accuracyMeters = 5))
    }

    @Test
    fun notWithinThresholdWhenFixTooCoarse() {
        assertFalse(isWithinNearbyThreshold(distanceMeters = 10, accuracyMeters = 31))
    }

    @Test
    fun notWithinThresholdWhenDataMissing() {
        assertFalse(isWithinNearbyThreshold(distanceMeters = null, accuracyMeters = 5))
        assertFalse(isWithinNearbyThreshold(distanceMeters = 10, accuracyMeters = null))
    }

    @Test
    fun constantsMatchSpec() {
        assertEquals(50, NEARBY_DISTANCE_METERS)
        assertEquals(30, NEARBY_ACCURACY_METERS)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:common:testDebugUnitTest --tests "*LockerDistanceTest"`
Expected: FAIL — unresolved reference `isWithinNearbyThreshold` / `NEARBY_DISTANCE_METERS`.

- [ ] **Step 3: Write minimal implementation**

Append to `core/common/src/main/kotlin/pl/tajchert/paczko/fast/core/common/location/LockerDistance.kt` (below the existing `metersToLocker`):

```kotlin
/** Max distance (metres) for one-tap "Near locker" opening. */
const val NEARBY_DISTANCE_METERS = 50

/** Max horizontal accuracy radius (metres) for a fix trustworthy enough to one-tap open. */
const val NEARBY_ACCURACY_METERS = 30

/**
 * True when the user is close enough AND the GPS fix is trustworthy enough to open the
 * locker with a single tap (see [NEARBY_DISTANCE_METERS] / [NEARBY_ACCURACY_METERS]).
 */
fun isWithinNearbyThreshold(distanceMeters: Int?, accuracyMeters: Int?): Boolean =
    distanceMeters != null && distanceMeters < NEARBY_DISTANCE_METERS &&
        accuracyMeters != null && accuracyMeters <= NEARBY_ACCURACY_METERS
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core:common:testDebugUnitTest --tests "*LockerDistanceTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add core/common/src/main/kotlin/pl/tajchert/paczko/fast/core/common/location/LockerDistance.kt \
        core/common/src/test/kotlin/pl/tajchert/paczko/fast/core/common/location/LockerDistanceTest.kt
git commit -m "feat(common): add Nearby distance/accuracy threshold helper"
```

---

### Task 2: `LockerOpenMode` preference + Settings UI

**Files:**
- Create: `core/model/src/main/kotlin/pl/tajchert/paczko/fast/core/model/LockerOpenMode.kt`
- Modify: `core/model/src/main/kotlin/pl/tajchert/paczko/fast/core/model/UserPreferences.kt`
- Modify: `core/datastore/src/main/kotlin/pl/tajchert/paczko/fast/core/datastore/UserPreferencesDataSource.kt`
- Modify: `core/data/src/main/kotlin/pl/tajchert/paczko/fast/core/data/repository/UserPreferencesRepository.kt`
- Modify: `core/testing/src/main/kotlin/pl/tajchert/paczko/fast/core/testing/repository/FakeUserPreferencesRepository.kt`
- Modify: `feature/settings/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/settings/impl/SettingsViewModel.kt`
- Modify: `feature/settings/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/settings/impl/SettingsScreen.kt`
- Test: `feature/settings/impl/src/test/kotlin/pl/tajchert/paczko/fast/feature/settings/impl/SettingsViewModelTest.kt`

**Interfaces:**
- Produces:
  - `enum class LockerOpenMode { HOLD, NEARBY }`
  - `UserPreferences.lockerOpenMode: LockerOpenMode` (default `HOLD`)
  - `UserPreferencesRepository.setLockerOpenMode(mode: LockerOpenMode)` (suspend)
  - `SettingsViewModel.setLockerOpenMode(mode: LockerOpenMode)`; `SettingsUiState.lockerOpenMode`
- Consumes: existing `FakeUserPreferencesRepository`, `SegmentedControl<T>(options, selected, onSelect)`.

- [ ] **Step 1: Write the failing tests**

Add to `SettingsViewModelTest.kt` (new imports: `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`):

```kotlin
    @Test
    fun setLockerOpenModeWritesPreference() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(prefs, FakeAuthRepository())

        viewModel.setLockerOpenMode(LockerOpenMode.NEARBY)
        advanceUntilIdle()

        assertEquals(LockerOpenMode.NEARBY, prefs.currentPreferences.lockerOpenMode)
    }

    @Test
    fun uiStateReflectsStoredLockerOpenMode() = runTest {
        val prefs = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(prefs, FakeAuthRepository())
        prefs.setLockerOpenMode(LockerOpenMode.NEARBY)
        advanceUntilIdle()

        assertEquals(LockerOpenMode.NEARBY, viewModel.uiState.value.lockerOpenMode)
    }

    @Test
    fun defaultLockerOpenModeIsHold() = runTest {
        val viewModel = SettingsViewModel(FakeUserPreferencesRepository(), FakeAuthRepository())
        advanceUntilIdle()

        assertEquals(LockerOpenMode.HOLD, viewModel.uiState.value.lockerOpenMode)
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :feature:settings:impl:testDebugUnitTest --tests "*SettingsViewModelTest"`
Expected: FAIL — unresolved `LockerOpenMode`, `setLockerOpenMode`, `lockerOpenMode`.

- [ ] **Step 3: Implement the model**

Create `core/model/src/main/kotlin/pl/tajchert/paczko/fast/core/model/LockerOpenMode.kt`:

```kotlin
package pl.tajchert.paczko.fast.core.model

/**
 * How the user confirms opening a locker compartment.
 *
 * - [HOLD]   Press-and-hold a bar; opens only after a completed hold. Default.
 * - [NEARBY] Single tap opens, enabled only when the phone is at the locker
 *            (see isWithinNearbyThreshold); a hold override is always available.
 */
enum class LockerOpenMode {
    HOLD,
    NEARBY,
}
```

Modify `core/model/.../UserPreferences.kt`:

```kotlin
package pl.tajchert.paczko.fast.core.model

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hasSeenOnboarding: Boolean = false,
    val lockerOpenMode: LockerOpenMode = LockerOpenMode.HOLD,
)
```

- [ ] **Step 4: Implement persistence (datastore + repository + fake)**

In `UserPreferencesDataSource.kt`:
- Add import `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`.
- Add key in `companion object`: `private val LOCKER_OPEN_MODE = stringPreferencesKey("locker_open_mode")`.
- In the `userPreferences` map, parse and include it:

```kotlin
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val stored = preferences[THEME_MODE]
        val themeMode = stored
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
        val hasSeenOnboarding = preferences[HAS_SEEN_ONBOARDING] ?: false
        val lockerOpenMode = preferences[LOCKER_OPEN_MODE]
            ?.let { runCatching { LockerOpenMode.valueOf(it) }.getOrNull() }
            ?: LockerOpenMode.HOLD
        UserPreferences(
            themeMode = themeMode,
            hasSeenOnboarding = hasSeenOnboarding,
            lockerOpenMode = lockerOpenMode,
        )
    }
```

- Add setter:

```kotlin
    suspend fun setLockerOpenMode(mode: LockerOpenMode) {
        dataStore.edit { preferences ->
            preferences[LOCKER_OPEN_MODE] = mode.name
        }
    }
```

In `core/data/.../UserPreferencesRepository.kt`:
- Add import `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`.
- Add to the interface: `suspend fun setLockerOpenMode(mode: LockerOpenMode)`.
- Add to `DefaultUserPreferencesRepository`:

```kotlin
    override suspend fun setLockerOpenMode(mode: LockerOpenMode) {
        dataSource.setLockerOpenMode(mode)
    }
```

In `core/testing/.../FakeUserPreferencesRepository.kt`:
- Add import `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`.
- Add override:

```kotlin
    override suspend fun setLockerOpenMode(mode: LockerOpenMode) {
        val current = currentPreferences
        preferencesFlow.tryEmit(current.copy(lockerOpenMode = mode))
    }
```

- [ ] **Step 5: Implement SettingsViewModel wiring**

In `SettingsViewModel.kt`:
- Add import `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`.
- Map the new field in `combine`:

```kotlin
    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferences,
        authRepository.observePhoneNumber(),
    ) { preferences, phoneNumber ->
        SettingsUiState(
            themeMode = preferences.themeMode,
            phoneNumber = phoneNumber,
            lockerOpenMode = preferences.lockerOpenMode,
        )
    }
```

- Add setter:

```kotlin
    fun setLockerOpenMode(mode: LockerOpenMode) {
        viewModelScope.launch {
            userPreferencesRepository.setLockerOpenMode(mode)
        }
    }
```

- Extend the state class:

```kotlin
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val phoneNumber: String? = null,
    val lockerOpenMode: LockerOpenMode = LockerOpenMode.HOLD,
)
```

- [ ] **Step 6: Run the ViewModel tests to verify they pass**

Run: `./gradlew :feature:settings:impl:testDebugUnitTest --tests "*SettingsViewModelTest"`
Expected: PASS (all old + 3 new).

- [ ] **Step 7: Add the Settings UI section**

In `SettingsScreen.kt`:
- Add imports: `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`.
- Thread state/action through `SettingsScreen` → `SettingsContent`:

```kotlin
    SettingsContent(
        themeMode = uiState.themeMode,
        lockerOpenMode = uiState.lockerOpenMode,
        phoneNumber = uiState.phoneNumber,
        appVersion = appVersion,
        onThemeSelected = viewModel::setThemeMode,
        onOpenModeSelected = viewModel::setLockerOpenMode,
        onLogout = { viewModel.logout(onLoggedOut) },
        onOpenParcels = onOpenParcels,
        onOpenHistory = onOpenHistory,
    )
```

- Update `SettingsContent` signature to add `lockerOpenMode: LockerOpenMode` and `onOpenModeSelected: (LockerOpenMode) -> Unit`.
- Insert a new section directly after the Appearance card, before the Account `SectionLabel`:

```kotlin
            SectionLabel(text = "Unlock method")
            PaczkofastCard {
                Text(
                    text = "How you open lockers",
                    style = MaterialTheme.typography.titleMedium,
                    color = PaczkofastTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                Text(
                    text = "How you confirm opening a locker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textFaint,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                SegmentedControl(
                    options = LockerOpenMode.entries.map { it to lockerOpenModeLabel(it) },
                    selected = lockerOpenMode,
                    onSelect = onOpenModeSelected,
                )
            }
```

- Add helper near `themeModeLabel`:

```kotlin
private fun lockerOpenModeLabel(mode: LockerOpenMode): String = when (mode) {
    LockerOpenMode.HOLD -> "Hold to open"
    LockerOpenMode.NEARBY -> "Near locker"
}
```

- Update `SettingsContentPreview` to pass `lockerOpenMode = LockerOpenMode.HOLD` and `onOpenModeSelected = {}`.

- [ ] **Step 8: Compile the settings module**

Run: `./gradlew :feature:settings:impl:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add core/model core/datastore core/data core/testing feature/settings
git commit -m "feat(settings): add Unlock method preference (Hold to open / Near locker)"
```

---

### Task 3: Location streaming — `locationUpdates()` + FusedLocationProvider

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core/common/build.gradle.kts`
- Modify: `core/common/src/main/kotlin/pl/tajchert/paczko/fast/core/common/location/LocationProvider.kt`
- Modify: `core/common/src/main/kotlin/pl/tajchert/paczko/fast/core/common/location/AndroidLocationProvider.kt`
- Modify: `feature/parcels/impl/src/test/kotlin/.../collect/CollectViewModelTest.kt` (FakeLocationProvider)
- Modify: `core/domain/src/test/kotlin/.../CollectParcelUseCaseTest.kt` (FakeLocationProvider)

**Interfaces:**
- Produces: `LocationProvider.locationUpdates(): Flow<GeoPoint>` (existing `suspend fun currentLocation(): GeoPoint` unchanged).
- Consumes: `GeoPoint(latitude, longitude, accuracy)`.

> This task adds an abstract method to `LocationProvider`; its gate is that every implementer (real + both test fakes) compiles and existing collect suites stay green. No new standalone unit test — `AndroidLocationProvider` needs Android/Play Services and is not JVM-unit-testable, consistent with it being untested today.

- [ ] **Step 1: Add the dependency to the version catalog**

In `gradle/libs.versions.toml`, add under `[versions]` (in the AndroidX/Google area):

```toml
playServicesLocation = "21.3.0"
```

Add under `[libraries]`:

```toml
play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }
```

In `core/common/build.gradle.kts`, add to `dependencies { ... }` under `// AndroidX`:

```kotlin
    // Location (fused provider for fast, refining fixes)
    implementation(libs.play.services.location)
```

- [ ] **Step 2: Extend the interface**

Replace `LocationProvider.kt` with:

```kotlin
package pl.tajchert.paczko.fast.core.common.location

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

interface LocationProvider {
    /** One authoritative fix (used for collect validation). */
    suspend fun currentLocation(): GeoPoint

    /**
     * Stream of location fixes: emits a fast initial value (last known) when available,
     * then refines as better fixes arrive. Completes without emitting if permission is
     * missing. Callers cancel by cancelling collection.
     */
    fun locationUpdates(): Flow<GeoPoint>
}
```

- [ ] **Step 3: Implement fused provider**

Replace `AndroidLocationProvider.kt` with:

```kotlin
package pl.tajchert.paczko.fast.core.common.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.tajchert.paczko.fast.core.model.collect.GeoPoint

class AndroidLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationProvider {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    private fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint {
        if (!hasPermission()) error("Location permission is required")
        return suspendCancellableCoroutine { cont ->
            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (!cont.isActive) return@addOnSuccessListener
                    if (location == null) {
                        cont.resumeWithException(IllegalStateException("Current location unavailable"))
                    } else {
                        cont.resume(location.toGeoPoint())
                    }
                }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<GeoPoint> = callbackFlow {
        if (!hasPermission()) { close(); return@callbackFlow }

        client.lastLocation.addOnSuccessListener { last ->
            if (last != null) trySend(last.toGeoPoint())
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000L)
            .setMinUpdateIntervalMillis(500L)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toGeoPoint()) }
            }
        }
        client.requestLocationUpdates(request, callback, context.mainLooper)
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun android.location.Location.toGeoPoint() = GeoPoint(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy.toDouble(),
    )
}
```

- [ ] **Step 4: Update the test fakes so implementers compile**

In `core/domain/src/test/kotlin/.../CollectParcelUseCaseTest.kt`, the private `FakeLocationProvider` — add the import `import kotlinx.coroutines.flow.Flow`, `import kotlinx.coroutines.flow.emptyFlow`, `import kotlinx.coroutines.flow.flowOf` and the override:

```kotlin
private class FakeLocationProvider(
    private val geoPoint: GeoPoint? = null,
    private val failure: Throwable? = null,
) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint {
        failure?.let { throw it }
        return requireNotNull(geoPoint)
    }

    override fun locationUpdates(): Flow<GeoPoint> =
        geoPoint?.let { flowOf(it) } ?: emptyFlow()
}
```

In `feature/parcels/impl/src/test/kotlin/.../collect/CollectViewModelTest.kt`, extend the private `FakeLocationProvider` to also serve a stream (this is used by Task 4). Add imports `import kotlinx.coroutines.flow.Flow`, `import kotlinx.coroutines.flow.asFlow`:

```kotlin
private class FakeLocationProvider(
    private val location: GeoPoint = GeoPoint(latitude = 52.1, longitude = 21.0, accuracy = 12.0),
    private val updates: List<GeoPoint> = listOf(location),
) : LocationProvider {
    override suspend fun currentLocation(): GeoPoint = location
    override fun locationUpdates(): Flow<GeoPoint> = updates.asFlow()
}
```

- [ ] **Step 5: Verify existing collect suites compile and pass**

Run: `./gradlew :core:common:compileDebugKotlin :core:domain:test :feature:parcels:impl:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, all existing tests pass.

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml core/common core/domain/src/test feature/parcels/impl/src/test
git commit -m "feat(common): add locationUpdates() stream via FusedLocationProvider"
```

---

### Task 4: CollectViewModel — live distance + open mode

**Files:**
- Modify: `feature/parcels/impl/src/main/kotlin/.../collect/CollectViewModel.kt`
- Test: `feature/parcels/impl/src/test/kotlin/.../collect/CollectViewModelTest.kt`

**Interfaces:**
- Consumes: `UserPreferencesRepository.userPreferences`, `LocationProvider.locationUpdates()`, `isWithinNearbyThreshold(...)`, `metersToLocker(...)`, `LockerOpenMode`.
- Produces (on `CollectUiState`): `openMode: LockerOpenMode`, `accuracyMeters: Int?`, `val nearbyReady: Boolean`. New VM constructor param `userPreferencesRepository: UserPreferencesRepository` (4th arg).

- [ ] **Step 1: Write the failing tests**

Add imports to `CollectViewModelTest.kt`:
`import pl.tajchert.paczko.fast.core.model.LockerOpenMode`,
`import pl.tajchert.paczko.fast.core.model.UserPreferences`,
`import pl.tajchert.paczko.fast.core.testing.repository.FakeUserPreferencesRepository`,
`import kotlinx.coroutines.test.advanceUntilIdle`.

Add tests:

```kotlin
    @Test
    fun armLoadsOpenModeFromPreferences() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(shipmentNumber = "123", openCode = "456"),
        )
        val prefs = FakeUserPreferencesRepository()
        prefs.setLockerOpenMode(LockerOpenMode.NEARBY)
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(FakeCollectRepository(), FakeLocationProvider()),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(),
            userPreferencesRepository = prefs,
        )

        viewModel.arm("123")
        advanceUntilIdle()

        assertEquals(LockerOpenMode.NEARBY, viewModel.uiState.value.openMode)
    }

    @Test
    fun armStreamsDistanceAndAccuracyAndFlipsNearbyReady() = runTest {
        val parcelRepository = FakeParcelRepository(
            parcel = parcel(
                shipmentNumber = "123",
                openCode = "456",
                latitude = 52.100000,
                longitude = 21.000000,
            ),
        )
        // First fix is coarse and far; second is close and precise.
        val far = GeoPoint(latitude = 52.20, longitude = 21.00, accuracy = 60.0)
        val near = GeoPoint(latitude = 52.100050, longitude = 21.000000, accuracy = 8.0)
        val viewModel = CollectViewModel(
            collectParcel = CollectParcelUseCase(FakeCollectRepository(), FakeLocationProvider()),
            parcelRepository = parcelRepository,
            locationProvider = FakeLocationProvider(location = near, updates = listOf(far, near)),
            userPreferencesRepository = FakeUserPreferencesRepository(),
        )

        viewModel.arm("123")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(8, state.accuracyMeters)
        assertTrue(state.distanceMeters != null && state.distanceMeters!! < 50)
        assertTrue(state.nearbyReady)
    }
```

> Note: the existing tests construct `CollectViewModel(useCase, parcelRepository, FakeLocationProvider())` — update each of those call sites to add `, FakeUserPreferencesRepository()` as the 4th argument (constructor gains `userPreferencesRepository`). Also ensure `parcel(...)` helper accepts `latitude`/`longitude` (it already builds a `PickupPoint`; pass coordinates through — if the helper hardcodes them, add optional params defaulting to the current values).

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :feature:parcels:impl:testDebugUnitTest --tests "*CollectViewModelTest"`
Expected: FAIL — constructor arity / unresolved `openMode`, `accuracyMeters`, `nearbyReady`.

- [ ] **Step 3: Implement the ViewModel changes**

In `CollectViewModel.kt`:
- Add imports:
  `import kotlinx.coroutines.flow.first`, `import kotlin.math.roundToInt`,
  `import pl.tajchert.paczko.fast.core.common.location.isWithinNearbyThreshold`,
  `import pl.tajchert.paczko.fast.core.data.repository.UserPreferencesRepository`,
  `import pl.tajchert.paczko.fast.core.model.LockerOpenMode`.
- Add the 4th constructor param:

```kotlin
@HiltViewModel
class CollectViewModel @Inject constructor(
    private val collectParcel: CollectParcelUseCase,
    private val parcelRepository: ParcelRepository,
    private val locationProvider: LocationProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
```

- Add a location job field alongside the others:

```kotlin
    private var locationJob: Job? = null
```

- Rewrite `arm(...)` to load mode, resolve pickup, and stream location while idle:

```kotlin
    fun arm(shipmentNumber: String) {
        if (armedShipmentNumber == shipmentNumber) return
        if (startedShipmentNumber == shipmentNumber) return
        armedShipmentNumber = shipmentNumber
        viewModelScope.launch {
            val mode = userPreferencesRepository.userPreferences.first().lockerOpenMode
            val members = compartmentMembers(shipmentNumber)
            val pickup = members.firstOrNull { it.shipmentNumber == shipmentNumber }?.pickupPoint
                ?: members.firstOrNull()?.pickupPoint
            _uiState.update {
                if (it.state !is CollectState.Idle) it else it.copy(
                    openMode = mode,
                    lockerName = pickup?.name,
                    members = members.map(::toCollectMember).toImmutableList(),
                )
            }
            locationJob?.cancel()
            locationJob = launch {
                locationProvider.locationUpdates().collect { fix ->
                    val distance = metersToLocker(
                        from = fix,
                        lockerLatitude = pickup?.latitude,
                        lockerLongitude = pickup?.longitude,
                    )
                    _uiState.update {
                        if (it.state !is CollectState.Idle) it else it.copy(
                            distanceMeters = distance,
                            accuracyMeters = fix.accuracy.roundToInt(),
                        )
                    }
                }
            }
        }
    }
```

- In `start(...)`, cancel streaming at the top of the launched block (right after `startedShipmentNumber = shipmentNumber`):

```kotlin
        locationJob?.cancel()
```

- Add `onCleared`:

```kotlin
    override fun onCleared() {
        locationJob?.cancel()
    }
```

- Extend `CollectUiState`:

```kotlin
@Immutable
data class CollectUiState(
    val state: CollectState = CollectState.Idle,
    val lockerName: String? = null,
    val distanceMeters: Int? = null,
    val accuracyMeters: Int? = null,
    val openMode: LockerOpenMode = LockerOpenMode.HOLD,
    val members: ImmutableList<CollectMember> = persistentListOf(),
) {
    val nearbyReady: Boolean get() = isWithinNearbyThreshold(distanceMeters, accuracyMeters)
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :feature:parcels:impl:testDebugUnitTest --tests "*CollectViewModelTest"`
Expected: PASS (old + 2 new).

- [ ] **Step 5: Commit**

```bash
git add feature/parcels/impl/src/main feature/parcels/impl/src/test
git commit -m "feat(collect): stream live distance/accuracy and load open mode in ViewModel"
```

---

### Task 5: Hold primitives in the design system

**Files:**
- Modify: `core/designsystem/src/main/kotlin/pl/tajchert/paczko/fast/core/designsystem/component/HoldToOpenPanel.kt`
- (No test change: `HoldProgress`/`HoldProgressTest` are untouched. Gate = designsystem compiles, existing designsystem tests pass, previews render.)

**Interfaces:**
- Produces (public):
  - `@Composable fun DistanceRing(progress: Float, distanceText: String, caption: String, modifier: Modifier = Modifier)`
  - `class HoldToOpenState { val progress: Float; val isHolding: Boolean; val pressModifier: Modifier }`
  - `@Composable fun rememberHoldToOpenState(holdDurationMillis: Int = 1200, enabled: Boolean = true, onConfirmed: () -> Unit): HoldToOpenState`
  - `@Composable fun HoldBar(state: HoldToOpenState, label: String = "Hold to open", modifier: Modifier = Modifier)`
- Keep `HoldToOpenPanel` for now (reimplement it on top of the primitives so nothing else breaks); Task 7 removes it once `CollectScreen` no longer calls it.

- [ ] **Step 1: Extract `DistanceRing` as public**

Change the private `DistanceRing` in `HoldToOpenPanel.kt` to `fun DistanceRing(...)` (public), keeping its body. Add a `modifier: Modifier = Modifier` param and apply it to the outer `Box` (`Modifier.size(216.dp).then(modifier)...` — preserve the semantics block).

- [ ] **Step 2: Lift the hold state machine into `rememberHoldToOpenState` + `HoldBar`**

Add (reusing the exact `HoldProgress` controller + `Animatable` logic currently inside `HoldToOpenPanel`'s `LaunchedEffect`):

```kotlin
@Stable
class HoldToOpenState internal constructor(
    val progress: Float,
    val isHolding: Boolean,
    val pressModifier: Modifier,
)

@Composable
fun rememberHoldToOpenState(
    holdDurationMillis: Int = 1200,
    enabled: Boolean = true,
    onConfirmed: () -> Unit,
): HoldToOpenState {
    val controller = remember(holdDurationMillis) { HoldProgress(holdDurationMillis.toLong()) }
    val fill = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    val currentOnConfirmed by rememberUpdatedState(onConfirmed)
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed, enabled) {
        if (!pressed || !enabled) {
            controller.onRelease()
            if (fill.value != 0f) fill.animateTo(0f, tween(180))
            return@LaunchedEffect
        }
        var started = false
        while (pressed) {
            val frame = withFrameMillis { it }
            if (!started) { controller.onPress(frame); started = true }
            fill.snapTo(controller.progressAt(frame))
            if (controller.consumeCompletion(frame)) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                currentOnConfirmed()
            }
            if (fill.value >= 1f) break
        }
    }

    val pressModifier = Modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
        detectTapGestures(onPress = {
            pressed = true
            tryAwaitRelease()
            pressed = false
        })
    }
    return HoldToOpenState(
        progress = fill.value,
        isHolding = pressed && enabled,
        pressModifier = pressModifier,
    )
}
```

Refactor the existing `HoldBar` to accept a `HoldToOpenState` (it currently takes `progress/pressed/enabled/onPressChange`). New signature applies `state.pressModifier` to the `NeoSurface` and reads `state.progress`/`state.isHolding`; keep the `HoldProgressBar`, semantics, and the amber styling verbatim. Add a `label: String = "Hold to open"` param for the button text (Nearby override uses "Override with hold").

- [ ] **Step 3: Reimplement `HoldToOpenPanel` on the primitives**

Keep the public `HoldToOpenPanel(...)` signature. Internally: `val hold = rememberHoldToOpenState(holdDurationMillis, enabled, onConfirmed)`, render `DistanceRing(progress = hold.progress, ...)` in the center and `HoldBar(state = hold)` at the bottom, preserving the "Keep holding…"/"Hold to open" headline logic (drive it off `hold.isHolding`) and the footer caption. This removes the duplicated state machine.

- [ ] **Step 4: Compile + run design-system tests**

Run: `./gradlew :core:designsystem:compileDebugKotlin :core:designsystem:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; `HoldProgressTest` passes.

- [ ] **Step 5: Commit**

```bash
git add core/designsystem
git commit -m "refactor(designsystem): extract DistanceRing + hold-to-open state primitives"
```

---

### Task 6: `collectScreenModel` — pure state→layout derivation

**Files:**
- Create: `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/collect/CollectScreenModel.kt`
- Test: `feature/parcels/impl/src/test/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/collect/CollectScreenModelTest.kt` (create)

**Interfaces:**
- Consumes: `CollectState`, `CollectUiState`, `LockerOpenMode`.
- Produces:
  - `enum class CollectHero { Distance, OpenBox, Check, Error }`
  - `enum class CollectAction { HoldOnly, NearbyOpen, BackOnly, RetrySupport, None }`
  - `data class CollectScreenModel(header: String, hero: CollectHero, headline: String, subline: String?, action: CollectAction, openEnabled: Boolean, showOverrideHold: Boolean)`
  - `fun collectScreenModel(state: CollectState, uiState: CollectUiState): CollectScreenModel`

- [ ] **Step 1: Write the failing tests**

Create `CollectScreenModelTest.kt`:

```kotlin
package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState

class CollectScreenModelTest {

    private fun ui(
        state: CollectState = CollectState.Idle,
        mode: LockerOpenMode = LockerOpenMode.HOLD,
        distance: Int? = null,
        accuracy: Int? = null,
    ) = CollectUiState(
        state = state,
        openMode = mode,
        distanceMeters = distance,
        accuracyMeters = accuracy,
        lockerName = "WAW01A",
    )

    @Test
    fun holdIdleUsesHoldAction() {
        val m = collectScreenModel(CollectState.Idle, ui(mode = LockerOpenMode.HOLD))
        assertEquals(CollectHero.Distance, m.hero)
        assertEquals(CollectAction.HoldOnly, m.action)
        assertFalse(m.showOverrideHold)
    }

    @Test
    fun nearbyIdleReadyEnablesOpenAndShowsOverride() {
        val m = collectScreenModel(
            CollectState.Idle,
            ui(mode = LockerOpenMode.NEARBY, distance = 10, accuracy = 8),
        )
        assertEquals(CollectAction.NearbyOpen, m.action)
        assertTrue(m.openEnabled)
        assertTrue(m.showOverrideHold)
    }

    @Test
    fun nearbyIdleFarDisablesOpenWithReason() {
        val m = collectScreenModel(
            CollectState.Idle,
            ui(mode = LockerOpenMode.NEARBY, distance = 62, accuracy = 8),
        )
        assertFalse(m.openEnabled)
        assertEquals("Move closer — 62 m away", m.subline)
        assertTrue(m.showOverrideHold)
    }

    @Test
    fun nearbyIdleNoFixWaitsForGps() {
        val m = collectScreenModel(
            CollectState.Idle,
            ui(mode = LockerOpenMode.NEARBY, distance = null, accuracy = null),
        )
        assertFalse(m.openEnabled)
        assertEquals("Waiting for a precise GPS fix…", m.subline)
    }

    @Test
    fun openedStateShowsOpenBoxHero() {
        val m = collectScreenModel(CollectState.Opened("s"), ui(state = CollectState.Opened("s")))
        assertEquals(CollectHero.OpenBox, m.hero)
        assertEquals(CollectAction.None, m.action)
    }

    @Test
    fun completedShowsCheckAndBack() {
        val m = collectScreenModel(CollectState.Completed, ui(state = CollectState.Completed))
        assertEquals(CollectHero.Check, m.hero)
        assertEquals(CollectAction.BackOnly, m.action)
    }

    @Test
    fun failedBeforeOpenShowsErrorAndRetry() {
        val failed = CollectState.Failed("boxMachineNotFound", canRetryFromValidation = false)
        val m = collectScreenModel(failed, ui(state = failed))
        assertEquals(CollectHero.Error, m.hero)
        assertEquals(CollectAction.RetrySupport, m.action)
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :feature:parcels:impl:testDebugUnitTest --tests "*CollectScreenModelTest"`
Expected: FAIL — unresolved `collectScreenModel`, `CollectHero`, `CollectAction`.

- [ ] **Step 3: Implement the derivation**

Create `CollectScreenModel.kt`:

```kotlin
package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import pl.tajchert.paczko.fast.core.model.LockerOpenMode
import pl.tajchert.paczko.fast.core.model.collect.CollectState

enum class CollectHero { Distance, OpenBox, Check, Error }

enum class CollectAction { HoldOnly, NearbyOpen, BackOnly, RetrySupport, None }

/**
 * Pure mapping from collect state + ui state to the fixed-scaffold slots. Keeping this
 * side-effect-free makes the "no layout shift" contract unit-testable and keeps
 * [CollectScreen] declarative.
 */
data class CollectScreenModel(
    val header: String,
    val hero: CollectHero,
    val headline: String,
    val subline: String?,
    val action: CollectAction,
    val openEnabled: Boolean,
    val showOverrideHold: Boolean,
)

private fun lockerHeader(lockerName: String?): String =
    (lockerName?.let { "Locker $it" } ?: "Locker").uppercase()

fun collectScreenModel(state: CollectState, uiState: CollectUiState): CollectScreenModel {
    val locker = uiState.lockerName
    return when (state) {
        is CollectState.Idle -> when (uiState.openMode) {
            LockerOpenMode.HOLD -> CollectScreenModel(
                header = lockerHeader(locker),
                hero = CollectHero.Distance,
                headline = "Hold to open",
                subline = collectSubline(uiState.members.size),
                action = CollectAction.HoldOnly,
                openEnabled = false,
                showOverrideHold = false,
            )
            LockerOpenMode.NEARBY -> CollectScreenModel(
                header = lockerHeader(locker),
                hero = CollectHero.Distance,
                headline = if (uiState.nearbyReady) "Ready to open" else "Get closer",
                subline = nearbySubline(uiState),
                action = CollectAction.NearbyOpen,
                openEnabled = uiState.nearbyReady,
                showOverrideHold = true,
            )
        }

        CollectState.Validating,
        is CollectState.Opening,
        is CollectState.WaitingForOpened -> CollectScreenModel(
            header = lockerHeader(locker),
            hero = CollectHero.Distance,
            headline = transitionalHeadline(state),
            subline = null,
            action = CollectAction.None,
            openEnabled = false,
            showOverrideHold = false,
        )

        is CollectState.Opened,
        is CollectState.WaitingForClosed,
        is CollectState.ConfirmingClosed,
        is CollectState.Claiming -> CollectScreenModel(
            header = lockerHeader(locker),
            hero = CollectHero.OpenBox,
            headline = "The box is open",
            subline = null,
            action = CollectAction.None,
            openEnabled = false,
            showOverrideHold = false,
        )

        CollectState.Completed -> CollectScreenModel(
            header = "Box closed".uppercase(),
            hero = CollectHero.Check,
            headline = if (uiState.members.size > 1) "All picked up!" else "Picked up!",
            subline = null,
            action = CollectAction.BackOnly,
            openEnabled = false,
            showOverrideHold = false,
        )

        is CollectState.Failed -> CollectScreenModel(
            header = (locker?.let { "Error · Locker $it" } ?: "Error").uppercase(),
            hero = CollectHero.Error,
            headline = "The box didn't open",
            subline = state.message,
            action = CollectAction.RetrySupport,
            openEnabled = false,
            showOverrideHold = false,
        )

        CollectState.Canceled -> CollectScreenModel(
            header = lockerHeader(locker),
            hero = CollectHero.Distance,
            headline = "Collection canceled",
            subline = null,
            action = CollectAction.BackOnly,
            openEnabled = false,
            showOverrideHold = false,
        )
    }
}

private fun transitionalHeadline(state: CollectState): String = when (state) {
    CollectState.Validating -> "Checking parcel and location"
    is CollectState.Opening -> "Opening compartment"
    is CollectState.WaitingForOpened -> "Waiting for the door to open"
    else -> ""
}

private fun nearbySubline(uiState: CollectUiState): String? = when {
    uiState.nearbyReady -> "You're at the locker — tap to open"
    uiState.distanceMeters != null -> "Move closer — ${uiState.distanceMeters} m away"
    else -> "Waiting for a precise GPS fix…"
}

internal fun collectSubline(count: Int): String = when {
    count > 1 -> "$count parcels share this box — you'll take them all at once"
    else -> "Stand at the locker before you start"
}
```

> `collectSubline` currently lives as a `private fun` in `CollectScreen.kt` — move it here as `internal` (Task 7 deletes the duplicate to keep it DRY).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :feature:parcels:impl:testDebugUnitTest --tests "*CollectScreenModelTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/collect/CollectScreenModel.kt \
        feature/parcels/impl/src/test/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/collect/CollectScreenModelTest.kt
git commit -m "feat(collect): add pure collectScreenModel state→scaffold derivation"
```

---

### Task 7: Shared collect scaffold + `CollectScreen` rewrite

**Files:**
- Create: `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/collect/CollectScaffold.kt`
- Modify: `feature/parcels/impl/src/main/kotlin/pl/tajchert/paczko/fast/feature/parcels/impl/collect/CollectScreen.kt`
- (UI task: gate = compile + previews render + manual no-shift check. No new unit test — logic lives in Task 6's tested `collectScreenModel`.)

**Interfaces:**
- Consumes: `collectScreenModel(...)`, `CollectHero`, `CollectAction`, `DistanceRing`, `rememberHoldToOpenState`, `HoldBar`, existing `CollectMember`, and existing private composables in `CollectScreen.kt` (`OpenBoxBlob`, `CheckBlob`, the checklist/summary content).

- [ ] **Step 1: Build the scaffold**

Create `CollectScaffold.kt` — one skeleton with three always-laid-out zones so switching states never moves anchors:

```kotlin
package pl.tajchert.paczko.fast.feature.parcels.impl.collect

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Fixed three-zone collect layout: header (top), a 216.dp centered hero that cross-fades
 * its glyph in place, headline+subline+detail below it, and a bottom action zone. Every
 * zone is always laid out so state changes swap content without moving anchors.
 */
@Composable
fun CollectScaffold(
    header: String,
    hero: @Composable () -> Unit,
    heroKey: Any,
    headline: String,
    subline: String?,
    detail: (@Composable () -> Unit)?,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = header,
            style = MonoLabel,
            color = colors.monoLabel,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(216.dp),
                contentAlignment = Alignment.Center,
            ) {
                Crossfade(targetState = heroKey, label = "collect-hero") { _ -> hero() }
            }
            Text(
                text = headline,
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp),
            )
            Box(
                modifier = Modifier.height(24.dp).padding(top = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (subline != null) {
                    Text(
                        text = subline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (detail != null) {
                Box(modifier = Modifier.padding(top = 18.dp)) { detail() }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            action()
        }
    }
}
```

- [ ] **Step 2: Rewrite `CollectContent` to drive the scaffold**

In `CollectScreen.kt`, replace the `when`-based body of `CollectContent` so it computes `val model = collectScreenModel(state, uiState)` and renders `CollectScaffold(...)`. Map each slot:

- **hero** (switch on `model.hero`): `Distance` → `DistanceRing(progress = holdState.progress, distanceText = uiState.distanceMeters?.let { "$it m" } ?: "—", caption = uiState.lockerName?.let { "to locker $it" } ?: "to the locker")`; `OpenBox` → `OpenBoxBlob()`; `Check` → `CheckBlob(count = uiState.members.size)`; `Error` → the red "!" blob (extract today's inline error blob into a private `ErrorBlob()`). `heroKey = model.hero`.
- **detail**: box-open states → the existing checklist (multi) / single parcel card; `Completed` → the collected-summary card; otherwise `null`. Reuse the exact composables already in the file (move them out of `BoxOpenScreen`/`SuccessScreen` into small private `@Composable` helpers: `BoxOpenDetail(members)`, `CollectedSummary(members)`).
- **action** (switch on `model.action`):
  - `HoldOnly` → `HoldBar(state = holdState)` (holding drives the hero ring via shared `holdState.progress`).
  - `NearbyOpen` → a `Column` with `PrimaryActionButton("Open locker", enabled = model.openEnabled, onClick = onConfirmed)` and, when `model.showOverrideHold`, `HoldBar(state = holdState, label = "Override with hold")` below it.
  - `BackOnly` → `PrimaryActionButton("Back to my parcels", onClick = onBack)` (Completed) — for `Canceled` use `PrimaryActionButton("Close", onClick = onBack)`.
  - `RetrySupport` → `PrimaryActionButton("Try again", onClick = onBack)` + `OutlinedActionButton("Contact support", onClick = onBack)`.
  - `None` → empty `Box` (zone still reserved, no shift).
- Create the shared hold state once in `CollectContent`:
  `val holdState = rememberHoldToOpenState(enabled = state is CollectState.Idle, onConfirmed = onConfirmed)`.
  Both Hold mode and the Nearby override use the same `holdState`, so a hold fills the hero ring in both modes.
- Keep the existing `collectedButUnconfirmed` snackbar behavior and the `Scaffold`/`DetailTopBar` top bar (hide top bar on `Completed` and the collected-but-unconfirmed case, as today). Keep permission handling in `CollectScreen` unchanged.
- Delete the now-unused `HoldToOpenPanel` usage; then delete `HoldToOpenPanel` from the design system (it was reimplemented in Task 5 solely for transition safety) and remove the private `collectSubline` duplicate (now in `CollectScreenModel.kt`).

- [ ] **Step 3: Update previews**

Replace the old per-screen previews with scaffold-driven ones covering: Hold idle, Nearby idle (ready), Nearby idle (far/disabled), box open (single + multi), success (single + multi), error. Use only fake data (`WAW01A`, `Example Sender sp. z o.o.`). Each preview constructs a `CollectUiState` and calls `CollectContent(...)` (or a preview-friendly inner composable) so the fixed layout is visible.

- [ ] **Step 4: Compile the feature + full collect test suite**

Run: `./gradlew :feature:parcels:impl:compileDebugKotlin :feature:parcels:impl:testDebugUnitTest`
Expected: BUILD SUCCESSFUL; all collect tests pass.

- [ ] **Step 5: Whole-app compile**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add feature/parcels/impl core/designsystem
git commit -m "feat(collect): shared non-shifting scaffold with Hold and Nearby actions"
```

---

### Task 8: Full verification pass

**Files:** none (verification only).

- [ ] **Step 1: Build + targeted test suites**

Run:
```bash
./gradlew :app:compileDebugKotlin test \
  :core:common:testDebugUnitTest \
  :core:domain:test \
  :core:data:testDebugUnitTest \
  :feature:parcels:impl:testDebugUnitTest \
  :feature:settings:impl:testDebugUnitTest \
  :core:designsystem:testDebugUnitTest
```
Expected: BUILD SUCCESSFUL, all green.

- [ ] **Step 2: Lint**

Run: `./gradlew :app:lintDebug`
Expected: no new errors introduced by these changes.

- [ ] **Step 3: Manual no-shift + behavior check (device/emulator, if available)**

Verify, in both modes, that the locker header, hero center, and bottom action stay put across idle → holding → opening → box open → success/error (nothing jumps as distance updates or state changes). In Nearby: "Open locker" disabled with a live reason when far/coarse; "Override with hold" always works; single tap opens when within 50 m with accuracy ≤ 30. Confirm the Settings toggle persists across app restarts. Do NOT use a real account — use test/dev data only.

- [ ] **Step 4: Update PERFORMANCE.md if a hot path or dependency shifted**

Adding `play-services-location` and touching the collect screen can affect startup/screen cost. If measured, **append** a dated row to `docs/PERFORMANCE.md` per its instructions (never edit past rows). If not measured, note it as unmeasured in the PR description.

- [ ] **Step 5: Commit any docs updates**

```bash
git add docs
git commit -m "docs: note locker-open-mode verification results" || echo "nothing to commit"
```

---

## Self-Review notes

- **Spec coverage:** setting (Task 2) · live/refining distance (Tasks 3–4) · Nearby 50 m + accuracy ≤ 30 gate (Tasks 1, 4, 6) · single-tap open + override hold (Task 7) · disabled reason copy (Task 6) · FusedLocationProvider (Task 3) · full shared non-shifting scaffold (Tasks 5–7) · Settings copy "Unlock method / Hold to open / Near locker" (Task 2). All covered.
- **Order/deps:** pure helper → setting → location interface → ViewModel → primitives → screen model → scaffold → verify. Each task compiles on its own; `LocationProvider`'s new method and the fakes land together in Task 3; `HoldToOpenPanel` survives (reimplemented) until Task 7 removes it.
- **Type consistency:** `isWithinNearbyThreshold`, `CollectUiState.{openMode,accuracyMeters,nearbyReady}`, `collectScreenModel`, `CollectHero`, `CollectAction`, `rememberHoldToOpenState`/`HoldToOpenState`/`HoldBar`, `DistanceRing`, `setLockerOpenMode` are used with identical names/signatures across tasks.
