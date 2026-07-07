# AGENTS.md

This file is the onboarding and operating manual for senior engineers working on
Paczkofast. Treat it as the first document to read before changing code.

Paczkofast is a native Android app written in Kotlin and Jetpack Compose. It is
an unofficial, experimental companion app for checking parcel state and opening a
parcel locker compartment remotely. The app is not affiliated with, endorsed by,
or supported by any locker operator.

The repo is public. Be strict about privacy: never commit real parcel numbers,
pickup codes, QR payloads, phone numbers, names, sender names, addresses, locker
IDs from live data, screenshots containing live parcel/account data, tokens,
keystores, local configuration, or generated metadata.

## Quick Start

Recommended environment:

- Android Studio current stable/canary that supports AGP 9.x.
- JDK 17.
- Android SDK with compile SDK 37 and min SDK 30 support.
- Gradle wrapper from this repo. Do not use a system Gradle installation.

Common commands:

```bash
# Build/debug compile (prod is the real app; demo is the offline showcase)
./gradlew :app:compileProdDebugKotlin
./gradlew :app:assembleProdDebug
./gradlew :app:assembleDemoDebug   # offline demo build

# Unit tests
./gradlew test
./gradlew :feature:parcels:impl:testDebugUnitTest
./gradlew :core:domain:test
./gradlew :core:data:testDebugUnitTest
./gradlew :core:network:testDebugUnitTest

# Android lint. There is no custom ktlint/detekt config at the time of writing.
./gradlew lint
./gradlew :app:lintProdDebug

# Database/android tests, requires emulator/device
./gradlew :core:database:connectedDebugAndroidTest

# Clean generated outputs
./gradlew clean
```

Demo mode: install the demoDebug variant (fully offline, mock parcels + locker
scenarios) to showcase flows or generate screenshots without a real account.

Before pushing a meaningful change, run at least:

```bash
./gradlew :app:compileProdDebugKotlin test
```

For collect/open-locker changes, also run:

```bash
./gradlew :core:domain:test :core:data:testDebugUnitTest :feature:parcels:impl:testDebugUnitTest
```

## Release Process

Release APKs are published from GitHub Releases in the public repo:

- Production app: `paczkofast-<version>.apk`.
- Offline showcase app: `paczkofast-demo-<version>.apk`.
- Each APK has a matching `.sha256` checksum file.

The production and demo APKs are built from the same commit and signed with the
same release certificate. They use different app ids, so they can be installed
side by side.

### Signing Setup

Release signing is wired in `app/build.gradle.kts`.

Local builds read the gitignored root file `keystore.properties`:

```properties
storeFile=/Users/<user>/keystores/paczkofast-release.jks
storePassword=<password>
keyAlias=paczkofast
keyPassword=<password>
```

CI reads the same values from GitHub Actions secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

`keystore.properties`, `*.jks`, and `*.keystore` must stay uncommitted. If no
release keystore is configured, Gradle intentionally falls back to the debug key
so contributor release builds and baseline-profile derived variants remain
installable. A public release must always be verified as release-signed before
uploading.

Expected release certificate:

```text
CN=Paczkofast Release, O=Tajchert, C=PL
SHA-256: 7773ed98f254490978d25cb9f8820b711b7d1f8f8f2446f24f735bcfea1ba209
```

### Versioning

Use tags named `vX.Y.Z`, matching `versionName` in `app/build.gradle.kts`.

For every public release:

- Increment `versionCode` monotonically. Android refuses in-place updates when
  the installed app has an equal or higher version code.
- Set `versionName` to the public version without the `v` prefix.
- Keep `versionNameSuffix = "-demo"` for the demo flavor.

The first public release was `v0.1.0` with `versionCode = 1`.

### Local Release Checklist

1. Start from clean `master`, up to date with `origin/master`.

   ```bash
   git checkout master
   git pull --ff-only
   git status --short
   ```

2. Update `versionCode` and `versionName` in `app/build.gradle.kts`.

3. Run tests and build both signed release APKs:

   ```bash
   ./gradlew test
   ./gradlew :app:assembleProdRelease :app:assembleDemoRelease
   ```

4. Verify both APKs are signed with the Paczkofast release certificate:

   ```bash
   APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner -type f | sort -V | tail -n1)
   "$APKSIGNER" verify --print-certs app/build/outputs/apk/prod/release/app-prod-release.apk
   "$APKSIGNER" verify --print-certs app/build/outputs/apk/demo/release/app-demo-release.apk
   ```

   The certificate DN must be:

   ```text
   CN=Paczkofast Release, O=Tajchert, C=PL
   ```

   If you see `CN=Android Debug`, do not publish. Fix local
   `keystore.properties` or CI secrets first.

5. Smoke-test the minified APKs before publishing:

   - Production APK: install, complete onboarding, open auth screen, verify the
     app does not crash before or after login.
   - Demo APK: install and click through parcel list, parcel detail, multi-box,
     settings, and collect/open-box demo flows.

   Useful install commands:

   ```bash
   adb install -r app/build/outputs/apk/prod/release/app-prod-release.apk
   adb install -r app/build/outputs/apk/demo/release/app-demo-release.apk
   ```

6. Commit the version bump:

   ```bash
   git add app/build.gradle.kts
   git commit -m "chore(release): prepare X.Y.Z"
   ```

7. Create and push the annotated tag:

   ```bash
   git tag -a vX.Y.Z -m "Paczkofast X.Y.Z"
   git push origin master vX.Y.Z
   ```

The pushed tag triggers `.github/workflows/release.yml`. The workflow runs unit
tests, builds both release APKs, verifies the signing certificate, creates
checksums, and publishes or updates the matching GitHub Release.

### Manual Release Fallback

Normally, do not create releases manually; push the version tag and let GitHub
Actions publish the assets. If GitHub Actions is unavailable, build locally and
stage artifacts in `/tmp`:

```bash
VERSION=X.Y.Z
DIST=/tmp/paczkofast-release-$VERSION
mkdir -p "$DIST"
cp app/build/outputs/apk/prod/release/app-prod-release.apk "$DIST/paczkofast-$VERSION.apk"
cp app/build/outputs/apk/demo/release/app-demo-release.apk "$DIST/paczkofast-demo-$VERSION.apk"
(cd "$DIST" && shasum -a 256 "paczkofast-$VERSION.apk" > "paczkofast-$VERSION.apk.sha256")
(cd "$DIST" && shasum -a 256 "paczkofast-demo-$VERSION.apk" > "paczkofast-demo-$VERSION.apk.sha256")
```

Then create the release with the GitHub CLI:

```bash
gh release create "v$VERSION" \
  --repo tajchert/paczkofast \
  --verify-tag \
  --title "Paczkofast $VERSION" \
  --notes-file /tmp/paczkofast-release-notes.md \
  "$DIST/paczkofast-$VERSION.apk" \
  "$DIST/paczkofast-$VERSION.apk.sha256" \
  "$DIST/paczkofast-demo-$VERSION.apk" \
  "$DIST/paczkofast-demo-$VERSION.apk.sha256"
```

Every release note must keep the unofficial-app disclaimer near the top:

```markdown
> **Unofficial app.** Paczkofast is an experimental, unofficial companion app.
> It is not affiliated with, endorsed by, or supported by InPost or any locker operator.
> The API integration is unofficial and may break at any time. Use at your own risk.
```

### Reruns And Existing Releases

The release workflow is idempotent for existing tag releases:

- If the release does not exist, it creates it.
- If the release already exists, it replaces APK/checksum assets with
  `gh release upload --clobber`.

This matters when a tag-triggered workflow is rerun after a manual release was
created. The first public release, `v0.1.0`, was created manually before the
workflow was made idempotent.

## Repository State And Public-Safety Rules

This repo has already had history rewritten to remove private artifacts. Do not
reintroduce them.

Safe public design folder:

- `claude-design-export/`
- It currently contains only cleaned HTML/JS/readme files.
- `claude-design-export/project/uploads/` was removed because it contained
  screenshots. Do not restore it unless every file is reviewed and sanitized.

Ignored private/local design folders:

- `paczkofast-app-design/`
- `package-tracking-app-design/`

Never commit:

- `local.properties`
- `.idea/`
- `.gradle/`
- `build/`
- `.DS_Store`
- APK/AAB files
- keystores
- real screenshots
- real QR codes or open codes
- real phone numbers
- real locker addresses
- real sender/receiver names

Before pushing suspicious content, scan using a local denylist that is not
committed. Put exact private values only in `/tmp/paczkofast-private-denylist`
or another local scratch file:

```bash
rg -n -i -f /tmp/paczkofast-private-denylist .
```

For full history checks, use:

```bash
git grep -n -I -i -f /tmp/paczkofast-private-denylist $(git rev-list --all) -- .
```

Suggested denylist categories:

- real private email addresses
- real phone numbers
- real shipment or package identifiers
- real pickup/open codes
- real locker IDs
- real street addresses
- real sender or receiver names
- screenshot filenames from live app sessions
- local design folder names that previously held private data

## README Graphics

The banner and showcase images at the top and inside `README.md` are generated
from the HTML/CSS design in
`claude-design-export/project/README Graphics.dc.html` (page "README Graphics",
frames `1a` hero banner, `1b` feature strip, `1c` pickup flow). The rendered
PNGs live in `docs/readme/`:

- `docs/readme/hero-banner.png` — frame `1a`, before the title in `README.md`.
- `docs/readme/feature-strip.png` — frame `1b`, in the `## Features` section.
- `docs/readme/pickup-flow.png` — frame `1c`, in the `## Why It Is Fast` section.

To create or update them:

1. Open `claude-design-export/project/README Graphics.dc.html` and copy the
   markup of the frame you need (the `div` with the matching `data-screen-label`)
   into a standalone HTML file. Include the Google Fonts link for
   `Space Grotesk` + `Space Mono`, set `html,body{margin:0;background:transparent}`,
   and drop the design-tool-only tags (`<sc-if>` wrappers, keeping their inner
   `<span>`). Use the frame's exact pixel `width`/`height`.
2. Render to a 2x, transparent-corner PNG with headless Chrome:

   ```bash
   CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
   "$CHROME" --headless=new --disable-gpu --hide-scrollbars \
     --force-device-scale-factor=2 --default-background-color=00000000 \
     --window-size=<W>,<H> --screenshot=docs/readme/<name>.png \
     "file://$PWD/<frame>.html"
   ```

   `<W>,<H>` are the frame's CSS pixels (e.g. `1280,640` for `1a`); the output
   is 2x that size for crisp retina rendering.
3. Reference each image in `README.md` with
   `<p align="center"><img src="docs/readme/<name>.png" width="100%" alt="..."></p>`.
   `width="100%"` is the only sizing attribute GitHub honors, so it scales down
   cleanly on mobile from the high-res source. Always include descriptive alt
   text.

To change **only the logo lockup** in the hero banner (frame `1a`), do not
re-render the whole frame: headless Chrome renders `Space Grotesk` with
different metrics than the committed PNG (serif fallback and a different line
wrap). Instead composite the new glyph onto the existing
`docs/readme/hero-banner.png` — clear the cream logo tile with a *rounded* rect
(a flat rect breaks its corners), then paste the glyph centred. If you must
re-render a full frame, the frame div relies on the global
`font-family:'Space Grotesk'` set in the file's `<helmet>` — copy it into your
standalone wrapper or text falls back to serif.

Keep these images free of live data — the design frames use fake demo values
(`TechNova Store`, `WAW-04N`, `DEMO STREET 12`), consistent with the privacy
rules below. Do not screenshot the real app for the README.

## App Icon

The launcher icon is an **adaptive vector**, and should stay vector-only:
`mipmap-anydpi-v26/ic_launcher.xml` points `<foreground>`/`<monochrome>` at
`@drawable/ic_launcher_foreground` over `@color/ic_launcher_background`
(`#FFD400`). minSdk is 30, so the legacy density `mipmap-*/*.webp` are never used
at runtime. Android Studio's *Image Asset* generator silently repoints those XMLs
to `@mipmap` raster and re-adds the webps — **revert that** and keep `@drawable`.
`ic_launcher-playstore.png` (512²) is the only raster that matters (Play Store
listing); regenerate it when the glyph changes.

The glyph (ink `#161511` diamond + three staggered "wind lines") is defined by
`claude-design-export/project/App Icon.dc.html`. Do **not** hand-derive the dp
coordinates from its CSS — the rotated rounded-square vertex math is error-prone.
Render the design element with headless Chrome and pixel-measure the gaps, then
match those numbers in the vector. `docs/icon/` holds the transparent foreground
export (`ic_launcher_foreground_1024.png`) and an assembled preview
(`ic_launcher_1024.png`).

## Tech Stack

Core stack:

- Kotlin 2.4.0
- Android Gradle Plugin 9.2.1
- Java/JVM target 17
- Jetpack Compose with Compose BOM 2026.05.01
- Material 3
- AndroidX Navigation 3
- Hilt 2.59.2
- KSP
- Retrofit 3
- OkHttp 5
- kotlinx.serialization JSON
- Room 2.8.4
- Preferences DataStore
- Kotlin coroutines and Flow
- ZXing for QR bitmap generation
- JUnit 4, kotlinx-coroutines-test, Turbine, Room testing

Version catalog:

- `gradle/libs.versions.toml`

Important notes:

- Android modules intentionally do not apply `org.jetbrains.kotlin.android`
  directly. AGP 9 provides built-in Kotlin support.
- The repo has no active `build-logic` include.
- Dependencies are declared directly in each module build file.

## Project Structure

Top-level modules:

- `:app`
- `:core:model`
- `:core:common`
- `:core:designsystem`
- `:core:ui`
- `:core:database`
- `:core:datastore`
- `:core:network`
- `:core:data`
- `:core:domain`
- `:core:testing`
- `:feature:auth:api`
- `:feature:auth:impl`
- `:feature:parcels:api`
- `:feature:parcels:impl`
- `:feature:settings:api`
- `:feature:settings:impl`

Module dependency intent:

```text
app
  -> feature:*:api
  -> feature:*:impl
  -> core:*

feature:*:impl
  -> feature:*:api
  -> core:domain
  -> core:data only when repository-level access is currently needed
  -> core:model
  -> core:designsystem
  -> core:ui

core:domain
  -> core:data repository interfaces
  -> core:model
  -> core:common when needed

core:data
  -> core:network
  -> core:database
  -> core:datastore
  -> core:model

core:network
  -> core:model only when required for shared types
  -> core:common

core:database
  -> core:model

core:model
  -> pure Kotlin only
```

The codebase follows a modularized "Now in Android" style:

- `api` feature modules expose navigation route keys only.
- `impl` feature modules contain screens, ViewModels, and feature-specific UI.
- Core modules are layered by responsibility.
- App composes feature entry providers into one Navigation 3 display.

## App Entry And Navigation

Main files:

- `app/src/main/java/pl/tajchert/paczko/fast/MainActivity.kt`
- `app/src/main/java/pl/tajchert/paczko/fast/MainActivityViewModel.kt`
- `app/src/main/java/pl/tajchert/paczko/fast/navigation/PaczkofastNavHost.kt`
- `app/src/main/java/pl/tajchert/paczko/fast/navigation/NavBackStackExt.kt`

Startup:

- `MainActivity` installs the splash screen.
- `MainActivityViewModel` combines user preferences and auth session.
- Splash remains while `MainActivityUiState.Loading`.
- Once state is loaded, `MainActivityUiState.Success` selects the initial route.

Initial route logic:

```kotlin
initialRoute = when {
    !preferences.hasSeenOnboarding -> OnboardingRoute
    authSession.isAuthenticated -> ParcelListRoute
    else -> AuthRoute
}
```

Navigation:

- Uses AndroidX Navigation 3.
- Routes are `@Serializable` classes/objects implementing `NavKey`.
- The root back stack is owned by `PaczkofastNavHost`.
- `NavDisplay` renders the current top entry.
- ViewModels are scoped to back stack entries through
  `rememberViewModelStoreNavEntryDecorator()`.

Root entry composition:

- `authEntries(...)`
- `parcelEntries(...)`
- `settingsEntries(...)`

When adding a feature:

1. Create `feature:newfeature:api`.
2. Define route keys there.
3. Create `feature:newfeature:impl`.
4. Add entry extension function.
5. Register it in `PaczkofastNavHost`.

## Core Architecture

### `core:model`

Pure Kotlin domain models. No Android dependencies.

Key files:

- `core/model/src/main/kotlin/.../parcel/Parcel.kt`
- `core/model/src/main/kotlin/.../parcel/ParcelDetails.kt`
- `core/model/src/main/kotlin/.../parcel/PickupPoint.kt`
- `core/model/src/main/kotlin/.../parcel/TrackingEvent.kt`
- `core/model/src/main/kotlin/.../collect/CollectModels.kt`
- `core/model/src/main/kotlin/.../auth/AuthSession.kt`
- `core/model/src/main/kotlin/.../auth/PhoneNumber.kt`
- `core/model/src/main/kotlin/.../ThemeMode.kt`
- `core/model/src/main/kotlin/.../UserPreferences.kt`

Important `Parcel` computed properties:

```kotlin
val canCollectRemotely: Boolean
    get() = operations.collect && openCode.isNullOrBlank().not()

val isMultiPackage: Boolean
    get() = multiCompartmentUuid.isNullOrBlank().not() ||
        multiPackageShipmentNumbers.isNotEmpty()
```

`Parcel.canCollectRemotely` controls whether the UI shows remote-open actions.
Do not duplicate that condition in UI code unless there is a deliberate reason.

### `core:common`

Shared result, dispatchers, and location utilities.

Key files:

- `core/common/src/main/kotlin/.../result/Result.kt`
- `core/common/src/main/kotlin/.../location/LocationProvider.kt`
- `core/common/src/main/kotlin/.../location/AndroidLocationProvider.kt`
- `core/common/src/main/kotlin/.../location/LockerDistance.kt`
- `core/common/src/main/kotlin/.../di/DispatchersModule.kt`

Result pattern:

```kotlin
sealed interface Result<out T> {
    data object Loading : Result<Nothing>
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
}

fun <T> Flow<T>.asResult(): Flow<Result<T>>
```

Use this for Flow-backed screen state that needs loading and error handling.

Location:

- `AndroidLocationProvider.currentLocation()` requires fine or coarse location.
- Fine location uses `LocationManager.GPS_PROVIDER`.
- Coarse location uses `LocationManager.NETWORK_PROVIDER`.
- Errors are plain `IllegalStateException` messages:
  - `"Location permission is required"`
  - `"Location service is unavailable"`
  - `"Current location unavailable"`

### `core:network`

Retrofit API definitions, DTOs, network auth, and network DI.

Key files:

- `core/network/src/main/kotlin/.../di/NetworkModule.kt`
- `core/network/src/main/kotlin/.../InpostAuthApi.kt`
- `core/network/src/main/kotlin/.../InpostParcelApi.kt`
- `core/network/src/main/kotlin/.../InpostCollectApi.kt`
- `core/network/src/main/kotlin/.../dto/AuthDtos.kt`
- `core/network/src/main/kotlin/.../dto/ParcelDtos.kt`
- `core/network/src/main/kotlin/.../dto/CollectDtos.kt`
- `core/network/src/main/kotlin/.../dto/ErrorDtos.kt`
- `core/network/src/main/kotlin/.../auth/AuthHeaderInterceptor.kt`
- `core/network/src/main/kotlin/.../auth/RefreshingAuthenticator.kt`
- `core/network/src/main/kotlin/.../auth/TokenProvider.kt`

Base URL:

```kotlin
private const val INPOST_BASE_URL =
    "https://api-inmobile-pl.easypack24.net/global/"
```

The API integration is experimental and unofficial. Keep disclaimers near API
interfaces and base URL.

Network clients:

- `@UnauthenticatedNetwork` client for `InpostAuthApi`.
- `@AuthenticatedNetwork` client for parcel and collect APIs.
- Authenticated client adds `AuthHeaderInterceptor`.
- Authenticated client uses `RefreshingAuthenticator` for one refresh attempt
  after a 401.

JSON:

```kotlin
Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}
```

Timeouts:

- Connect/read/write timeout: 5 seconds.

Logging:

- OkHttp `HttpLoggingInterceptor.Level.BASIC`.
- Authorization header is redacted.
- Do not switch to BODY logging in committed code. API bodies can contain
  parcel IDs, open codes, QR payloads, tokens, phone numbers, and addresses.
- There is no app-wide Timber or structured logging framework.

Auth refresh:

- `AuthHeaderInterceptor` attaches `Authorization: Bearer <token>`.
- `RefreshingAuthenticator` refreshes using `POST /v1/authenticate`.
- It stops after 2 auth attempts.
- On refresh failure it clears tokens and returns null.

### `core:database`

Room database, DAOs, entities, schema migrations.

Key files:

- `core/database/src/main/kotlin/.../PaczkofastDatabase.kt`
- `core/database/src/main/kotlin/.../migration/Migrations.kt`
- `core/database/src/main/kotlin/.../dao/ParcelDao.kt`
- `core/database/src/main/kotlin/.../dao/ParcelDetailsDao.kt`
- `core/database/src/main/kotlin/.../entity/ParcelEntity.kt`
- `core/database/src/main/kotlin/.../entity/ParcelDetailsEntity.kt`
- `core/database/src/main/kotlin/.../entity/TrackingEventEntity.kt`
- `core/database/schemas/...`

Current database version:

```kotlin
@Database(..., version = 7, exportSchema = true)
```

Migration rules:

- Never use destructive migration for normal schema changes.
- Add explicit migrations in `Migrations.kt`.
- Keep exported schemas updated.
- Add/adjust DAO tests for data preservation when schema changes matter.

Tables:

- `parcels`
- `parcel_details`
- `tracking_events`

### `core:datastore`

Preferences and auth token persistence.

Key files:

- `core/datastore/src/main/kotlin/.../UserPreferencesDataSource.kt`
- `core/datastore/src/main/kotlin/.../AuthTokensDataSource.kt`
- `core/datastore/src/main/kotlin/.../di/DataStoreModule.kt`

Preferences:

- `theme_mode`
- `has_seen_onboarding`

Auth:

- `auth_token`
- `refresh_token`
- `phone_number`

Important:

- Auth tokens are currently stored in Preferences DataStore, not encrypted
  storage. Treat this as a product/security tradeoff to revisit before a wider
  release.
- `clearTokens()` also clears stored phone number.

### `core:data`

Repository implementations and mappers. This is the main coordination layer.

Key files:

- `core/data/src/main/kotlin/.../repository/AuthRepository.kt`
- `core/data/src/main/kotlin/.../repository/DefaultAuthRepository.kt`
- `core/data/src/main/kotlin/.../repository/ParcelRepository.kt`
- `core/data/src/main/kotlin/.../repository/DefaultParcelRepository.kt`
- `core/data/src/main/kotlin/.../repository/CollectRepository.kt`
- `core/data/src/main/kotlin/.../repository/DefaultCollectRepository.kt`
- `core/data/src/main/kotlin/.../repository/CollectApiException.kt`
- `core/data/src/main/kotlin/.../repository/UserPreferencesRepository.kt`
- `core/data/src/main/kotlin/.../mapper/ParcelMappers.kt`
- `core/data/src/main/kotlin/.../mapper/ParcelDetailsMappers.kt`
- `core/data/src/main/kotlin/.../mapper/TrackingEventMappers.kt`
- `core/data/src/main/kotlin/.../di/DataModule.kt`

Pattern:

- Flow reads come from local database/DataStore.
- Refresh writes update local storage.
- UI observes local state instead of directly observing network calls.
- One-shot operations are suspend functions.

Parcel refresh:

```kotlin
override suspend fun refreshTrackedParcels() {
    do {
        val response = api.getTrackedParcels()
        parcelDao.applyTrackedParcelPage(
            parcels = response.parcels.map { it.toEntity() },
            removedShipmentNumbers = response.removedParcelList,
        )
    } while (response.more)
}
```

Parcel detail refresh:

```kotlin
val details = api.getTrackedParcel(shipmentNumber).toParcelDetails()
parcelDetailsDao.replaceParcelDetails(...)
```

Collect error mapping:

- `DefaultCollectRepository` catches Retrofit `HttpException`.
- It reads the error body.
- It decodes `ErrorResponseDto.error`.
- It throws `CollectApiException(apiValue)`.
- The domain layer maps that API value to `CollectErrorCode`.

### `core:domain`

Use cases. Keep business flow here when it combines repositories or models.

Key files:

- `core/domain/src/main/kotlin/.../LoginUseCases.kt`
- `core/domain/src/main/kotlin/.../ObserveParcelsUseCase.kt`
- `core/domain/src/main/kotlin/.../RefreshParcelsUseCase.kt`
- `core/domain/src/main/kotlin/.../ObserveParcelUseCase.kt`
- `core/domain/src/main/kotlin/.../ObserveParcelDetailsUseCase.kt`
- `core/domain/src/main/kotlin/.../RefreshParcelDetailsUseCase.kt`
- `core/domain/src/main/kotlin/.../CollectParcelUseCase.kt`

Guideline:

- ViewModels should orchestrate UI interactions.
- Repositories should abstract data sources.
- Use cases should own multi-step business flows and reusable rules.

### `core:designsystem`

Theme and reusable Compose components.

Key files:

- `core/designsystem/src/main/kotlin/.../theme/Theme.kt`
- `core/designsystem/src/main/kotlin/.../theme/Color.kt`
- `core/designsystem/src/main/kotlin/.../theme/Type.kt`
- `core/designsystem/src/main/kotlin/.../component/Button.kt`
- `core/designsystem/src/main/kotlin/.../component/Card.kt`
- `core/designsystem/src/main/kotlin/.../component/BottomNavBar.kt`
- `core/designsystem/src/main/kotlin/.../component/EmptyState.kt`
- `core/designsystem/src/main/kotlin/.../component/HoldToOpenPanel.kt`
- `core/designsystem/src/main/kotlin/.../component/HoldProgress.kt`
- `core/designsystem/src/main/kotlin/.../component/ParcelCards.kt`
- `core/designsystem/src/main/kotlin/.../component/MultiPackageCard.kt`
- `core/designsystem/src/main/kotlin/.../component/TrackingTimeline.kt`
- `core/designsystem/src/main/kotlin/.../component/PullRefreshIndicator.kt`

Design language:

- Neo-brutalist.
- Strong borders.
- Hard shadows.
- High contrast surfaces.
- Compose-only native Android UI.

Important:

- Do not create ad hoc one-off colors and typography in features unless the
  design system does not cover the case.
- Prefer extending design system components over duplicating UI primitives.
- Accessibility semantics have been added in multiple components. Preserve and
  improve semantics when editing UI.

### `core:ui`

Shared non-design-system UI. Currently important for QR rendering.

Key files:

- `core/ui/src/main/kotlin/.../QrPanel.kt`
- `core/ui/src/main/kotlin/.../QrCodeImage.kt`
- `core/ui/src/test/kotlin/.../QrCodeBitmapFactoryTest.kt`

## Screens And States

### Startup/Splash

Files:

- `MainActivity.kt`
- `MainActivityViewModel.kt`

States:

- `MainActivityUiState.Loading`
- `MainActivityUiState.Success`

The splash screen remains while loading. Success selects initial route:

- onboarding if `hasSeenOnboarding == false`
- parcels if authenticated
- auth otherwise

### Onboarding

Files:

- `feature/auth/impl/.../OnboardingScreen.kt`
- `feature/auth/impl/.../OnboardingViewModel.kt`

Purpose:

- First-run welcome.
- Unofficial/experimental disclaimer.

States:

- Page 0: welcome.
- Page 1: disclaimer.
- No explicit loading/error UI.

Completion:

- Calls `UserPreferencesRepository.setHasSeenOnboarding(true)`.
- Navigates to auth.

### Auth: Phone Entry

Files:

- `feature/auth/impl/.../AuthScreen.kt`
- `feature/auth/impl/.../AuthViewModel.kt`
- `feature/auth/impl/.../AuthUiState.kt`
- `feature/auth/impl/.../PhoneLoginScreen.kt`

State model:

```kotlin
data class AuthUiState(
    val step: AuthStep = AuthStep.Phone,
    val phoneDigits: String = "",
    val codeDigits: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val resendSecondsLeft: Int = 0,
    val isAuthenticated: Boolean = false,
)
```

Phone rules:

- Polish prefix is fixed to `+48`.
- National number length is 9 digits.
- Send button enabled only when not loading and 9 digits are entered.
- Input strips non-digits and truncates to 9.

API:

- `POST /v1/account`
- `DefaultAuthRepository.requestSmsCode(...)`

Error handling:

- Exceptions are caught in ViewModel.
- `error.message ?: "Unable to request SMS code"` shown inline.

### Auth: OTP Entry

Files:

- `OtpScreen.kt`
- `AuthViewModel.kt`

Rules:

- Code length is 6 digits.
- Verify button enabled only when not loading and 6 digits are entered.
- Resend is blocked for 30 seconds after SMS request.
- Back handler returns to phone entry and clears requested phone/code state.

API:

- `POST /v1/account/verification`

On success:

- Auth token and refresh token are saved.
- Phone number is saved for Settings display.
- `isAuthenticated = true`.
- Navigation stack is cleared and `ParcelListRoute` is pushed.

Error handling:

- Exceptions are caught in ViewModel.
- `error.message ?: "Unable to confirm SMS code"` shown inline.

### Parcel List

Files:

- `feature/parcels/impl/.../list/ParcelListScreen.kt`
- `feature/parcels/impl/.../list/ParcelListViewModel.kt`
- `feature/parcels/impl/.../list/ParcelListUiState.kt`

State model:

```kotlin
data class ParcelListUiState(
    val parcels: List<Parcel> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)
```

States:

- First load with no cache: full-screen loading indicator.
- Cached or loaded data: list content.
- Pull refresh: pull-to-refresh indicator, except first load.
- Empty active parcels: "Nothing incoming".
- Empty history: "No history yet".
- Error with empty cache: full-screen error with retry.
- Error with cached data: snackbar, cached data remains visible.

Tabs:

- Parcels.
- History.
- Settings opens separate `SettingsRoute`.

Parcels tab:

- Active parcels are `!parcel.isFinished`.
- Active parcels are partitioned into ready-for-pickup and on-the-way.
- Ready items are grouped by compartment.
- First standalone ready parcel is expanded.
- Other standalone ready parcels are collapsed.
- Multi-package groups use `MultiPackageCard`.

History tab:

- Finished parcels are grouped by month.
- Multi-package siblings collapse into one history row.
- Row opens parcel detail or multi-package box detail.

Refresh behavior:

- ViewModel refreshes once in `init`.
- Returning from detail does not auto-refresh because ViewModel survives.
- Explicit pull-to-refresh calls `refresh()`.

API:

- `GET /v4/parcels/tracked`
- Repeats while response `more == true`.

### Parcel Detail

Files:

- `feature/parcels/impl/.../detail/ParcelDetailScreen.kt`
- `feature/parcels/impl/.../detail/ParcelDetailViewModel.kt`
- `feature/parcels/impl/.../detail/ParcelDetailUiState.kt`

State model:

```kotlin
data class ParcelDetailUiState(
    val parcel: Parcel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val events: List<TrackingEvent> = emptyList(),
    val sizeCode: String? = null,
    val senderName: String? = null,
    val shipmentType: String? = null,
)
```

States:

- Loading.
- Error.
- Parcel not found.
- Ready/in-transit/delivered content.

Data behavior:

- Observes parcel from Room.
- Observes details/timeline cache from Room.
- Refreshes detail in background in `init`.
- If detail refresh fails, cached detail/list data remains visible because the
  refresh failure is intentionally swallowed.

Ready parcel content:

- status chip
- size chip
- sender/title
- shipment number
- deadline card
- QR panel if `qrCode` exists
- pickup/open code if `openCode` exists
- remote-open button if `parcel.canCollectRemotely`
- locker card with navigate action
- tracking timeline

Delivered content:

- picked-up summary
- collapsed pickup code row
- locker card
- timeline

API:

- `GET /v4/parcels/tracked/{shipmentNumber}`

### Multi-Package Box Detail

Files:

- `feature/parcels/impl/.../detail/MultiPackageDetailScreen.kt`
- `feature/parcels/impl/.../detail/MultiPackageDetailViewModel.kt`
- `feature/parcels/api/.../ParcelsNavigation.kt`

Purpose:

- Shows a shared compartment/box for parcels with the same
  `multiCompartmentUuid`.
- Provides one shared QR/code/open action.
- Lets user open member parcel details.

States:

- Loading.
- Content.
- If target parcel is missing, current code returns non-loading empty content.
  There is no explicit "not found" error UI here.

Data:

- Observes all parcels.
- Finds target shipment.
- Groups siblings with same `multiCompartmentUuid`.
- Representative is the parcel with `multiPackageShipmentNumbers` if present,
  otherwise target.

Ready content:

- status chip
- "One box xN" chip
- members list
- deadline
- QR panel
- "One code opens the box" action if collectable

Delivered content:

- picked-up summary
- member list
- collapsed QR/code row

### Collect/Open Box

Files:

- `feature/parcels/impl/.../collect/CollectScreen.kt`
- `feature/parcels/impl/.../collect/CollectViewModel.kt`
- `core/domain/.../CollectParcelUseCase.kt`
- `core/model/.../collect/CollectModels.kt`
- `core/data/.../DefaultCollectRepository.kt`
- `core/network/.../InpostCollectApi.kt`
- `core/network/.../dto/CollectDtos.kt`

State model:

```kotlin
sealed interface CollectState {
    data object Idle : CollectState
    data object Validating : CollectState
    data class Opening(val sessionUuid: String) : CollectState
    data class WaitingForOpened(val sessionUuid: String) : CollectState
    data class Opened(val sessionUuid: String) : CollectState
    data class WaitingForClosed(val sessionUuid: String) : CollectState
    data class ConfirmingClosed(val sessionUuid: String) : CollectState
    data class Claiming(val sessionUuid: String) : CollectState
    data object Completed : CollectState
    data class Failed(val message: String, val canRetryFromValidation: Boolean) : CollectState
    data object Canceled : CollectState
}
```

Screen states:

- Permission request.
- Idle hold-to-open panel.
- Transitional text states:
  - validating
  - opening
  - waiting for opened
  - canceled
- Box open screen:
  - single parcel card
  - multi-parcel checklist
- Finishing state while confirming/claiming.
- Success screen.
- Error screen.

Permission behavior:

- `CollectScreen` requests fine and coarse location.
- If granted, it calls `viewModel.arm(shipmentNumber)`.
- If denied, it calls `onLocationPermissionDenied(...)` and shows a failure.

Arm behavior:

- Loads compartment members from local parcels.
- Finds pickup point.
- Computes approximate distance with `metersToLocker`.
- Populates locker name, distance, and member labels while state is idle.

Start behavior:

- Ignores duplicate starts for the same shipment.
- Loads all compartment members.
- Uses the tapped parcel's `openCode`.
- If open code missing/blank, emits non-retryable failure:
  `"Parcel cannot be opened remotely"`.
- Claims all member shipment numbers for a shared compartment.

Collect API sequence:

1. Get current location.
2. `POST /v2/collect/validate`
   - request: `parcel.shipmentNumber`, `parcel.openCode`, `geoPoint`
   - response: `sessionUuid`
3. `POST /v1/collect/compartment/open`
   - request: `sessionUuid`
4. `POST /v1/collect/compartment/status`
   - request: `sessionUuid`, `expectedStatus = "OPENED"`
5. UI emits `Opened`.
6. `POST /v1/collect/compartment/status`
   - request: `sessionUuid`, `expectedStatus = "CLOSED"`
7. `POST /v1/collect/compartment/closed`
   - request: `sessionUuid`
8. `POST /v1/collect/compartment/claim`
   - request: `sessionUuid`, `shipmentNumbers`
9. UI emits `Completed`.

Expected API error values:

- `invalidSession`
- `sessionExpired`
- `invalidSessionState`
- `invalidCompartmentState`
- `invalidParcelState`
- `cannotFindCompartment`
- `boxMachineNotFound`
- `unknown`

Retry flags:

- `sessionExpired`: retryable from validation.
- `invalidSessionState`: retryable from validation.
- Everything else: non-retryable.

Current UI gap:

- `CollectState.Failed.canRetryFromValidation` exists but is not used by
  `ErrorScreen`.
- Both "Try again" and "Contact support" currently call `onBack`.
- If you improve collect errors, wire retry behavior deliberately and test it.

### Settings

Files:

- `feature/settings/impl/.../SettingsScreen.kt`
- `feature/settings/impl/.../SettingsViewModel.kt`
- `feature/settings/impl/.../navigation/SettingsEntries.kt`

States:

- Normal content.
- Logout confirmation dialog.
- No explicit loading/error state.

Settings:

- Theme mode: System, Light, Dark.
- Account phone number display.
- Logout.
- About/version.
- Unofficial app disclaimer.

Logout:

- Calls `AuthRepository.logout()`.
- Clears tokens and stored phone number.
- Clears navigation stack and goes to `AuthRoute`.

## API Summary

Auth:

```text
POST /v1/account
POST /v1/account/verification
POST /v1/authenticate
```

Parcels:

```text
GET /v4/parcels/tracked
GET /v4/parcels/tracked/{shipmentNumber}
```

Collect:

```text
POST /v2/collect/validate
POST /v1/collect/compartment/open
POST /v1/collect/compartment/status
POST /v1/collect/compartment/closed
POST /v1/collect/compartment/claim
```

All parcel and collect APIs use the authenticated Retrofit client.
Auth request/verification/refresh use the unauthenticated client.

## Error Handling Patterns

General patterns:

- Flow reads use `asResult()` to expose Loading/Success/Error.
- Refresh failures often surface as snackbars while cached data remains visible.
- Empty-cache errors use full-screen error components.
- Auth errors are displayed inline.
- Collect errors become `CollectState.Failed`.

Important files:

- `core/common/.../result/Result.kt`
- `core/designsystem/.../component/EmptyState.kt`
- `feature/parcels/impl/.../list/ParcelListViewModel.kt`
- `feature/parcels/impl/.../detail/ParcelDetailViewModel.kt`
- `feature/parcels/impl/.../collect/CollectViewModel.kt`
- `core/domain/.../CollectParcelUseCase.kt`
- `core/data/.../DefaultCollectRepository.kt`

Current limitations:

- Some user-facing errors directly use exception messages.
- There is no central error taxonomy beyond collect-specific errors.
- There is no retry/backoff helper.
- There is no centralized logging or crash-reporting layer.

When adding new errors:

- Prefer domain-specific error types when behavior depends on the error.
- Avoid showing raw technical exception messages to users unless they are
  intentionally human-readable.
- Preserve cached data where possible.
- Add tests for mapping and UI state behavior.

## Testing Patterns

Test modules and examples:

- `app/src/test/.../MainActivityViewModelTest.kt`
- `core/domain/src/test/.../CollectParcelUseCaseTest.kt`
- `core/data/src/test/.../DefaultCollectRepositoryTest.kt`
- `core/data/src/test/.../DefaultParcelRepositoryTest.kt`
- `core/network/src/test/.../InpostDtoSerializationTest.kt`
- `core/network/src/test/.../RefreshingAuthenticatorTest.kt`
- `core/database/src/androidTest/.../ParcelDaoTest.kt`
- `feature/auth/impl/src/test/.../AuthViewModelTest.kt`
- `feature/parcels/impl/src/test/.../ParcelListViewModelTest.kt`
- `feature/parcels/impl/src/test/.../CollectViewModelTest.kt`
- `feature/parcels/impl/src/test/.../ParcelDetailViewModelTest.kt`
- `feature/settings/impl/src/test/.../SettingsViewModelTest.kt`
- `core/designsystem/src/test/.../HoldProgressTest.kt`

Use:

- `runTest` for coroutine tests.
- `MainDispatcherRule` from `core:testing` for ViewModel tests.
- Small fake repositories over mocks where practical.
- DTO serialization tests for API contract changes.
- Mapper tests for network/database/domain transformations.
- Focused tests for any screen state branching.

Collect flow has important coverage:

- Correct API call/state order.
- Session-expired mapping.
- Location failure.
- Missing open code.
- Multi-compartment claim behavior.
- Duplicate start prevention.

When changing collect, update both:

- `core/domain/src/test/.../CollectParcelUseCaseTest.kt`
- `feature/parcels/impl/src/test/.../collect/CollectViewModelTest.kt`

## Screenshots

Two kinds, both committed and **fake-data-only** (public repo).

**Golden screenshot tests** — Compose Preview Screenshot Testing (plugin
`com.android.compose.screenshot`). `@PreviewTest` previews in a module's
`src/screenshotTest/` render its stateless content composables; reference PNGs
live in `<module>/src/screenshotTestDebug/reference/`. Wired per UI module
(`feature:parcels:impl`, `feature:auth:impl`) via `screenshotTestImplementation`
deps + `android.experimental.enableScreenshotTest=true` in `gradle.properties`.

```bash
./gradlew :feature:parcels:impl:updateDebugScreenshotTest    # record/update refs
./gradlew :feature:parcels:impl:validateDebugScreenshotTest  # verify (CI gate)
```

- **Add/update when**: a new screen or UI state, or a visual change to a covered
  composable. Add one `@PreviewTest` per state, re-record, commit the new PNG.
- Make the rendered content composable `internal` (not `private`) so the
  same-module `screenshotTest` source set can call it.
- **Determinism (critical)**: previews must not render `now()`-relative text
  (pickup countdown, "X ago") — it drifts and fails `validate` later. Use `null`
  or fixed absolute dates in fixtures.
- Generated diff/actual images and the HTML report go to `build/` (gitignored);
  only the reference PNGs are committed.

**README app screens** (`docs/readme/screens/*.png`) are captured from the
`demoDebug` build on an emulator (`adb exec-out screencap -p`, resized ~640px
wide), mock data only. Regenerate when the UI changes materially. These differ
from the `docs/readme/` banners, which are rendered from the design HTML (see
[README Graphics](#readme-graphics)).

## Performance

Performance methodology and a dated, append-only results log live in
`docs/PERFORMANCE.md`. It covers cold-startup Macrobenchmark numbers, the
baseline profile, Compose recomposition/stability checks, and QR generation.

When a change could affect startup, a hot screen, list rendering, image/QR work,
or bumps a dependency or the Compose compiler: re-run the relevant measurement
from `docs/PERFORMANCE.md` and **append a row** (never edit past rows). Baseline
profile lives in `:app` + the `:baselineprofile` generator module; regenerate it
after meaningful startup-path changes with `./gradlew :app:generateBaselineProfile`.

## Compose And UI Patterns

General rules:

- UI state lives in ViewModels as `StateFlow`.
- Composables collect with `collectAsStateWithLifecycle()`.
- Screen entry functions are small and route state/actions into content
  composables.
- Content composables are mostly private and previewable.
- Use design system components before creating new UI primitives.
- Keep business logic out of Composables.
- Prefer derived helper functions in `feature/parcels/impl` for formatting.

Common structure:

```kotlin
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeatureContent(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
private fun FeatureContent(uiState: FeatureUiState, onAction: () -> Unit) {
    ...
}
```

Accessibility:

- Preserve existing semantics on buttons, navigation, fields, QR, and icons.
- Use `contentDescription` only when the icon conveys information or is an
  action. Decorative icons should use `contentDescription = null`.
- Use `Role.Button` on custom clickable rows.
- Use `stateDescription` for fields/progress where TalkBack needs context.
- Add `onClickLabel` for ambiguous custom click targets.
- Do not remove BasicTextField semantics in auth fields.

Navigation:

- Do not pass whole domain objects as navigation arguments.
- Pass stable identifiers such as shipment number.
- Load data from repositories/ViewModels by ID.

Previews:

- Preview data must be obviously fake.
- Prefer all-zero shipment numbers.
- Never use real names, addresses, locker IDs, phone numbers, pickup codes, QR
  payloads, or screenshots in previews.

## Data Formatting Helpers

Feature-specific formatting lives mostly in:

- `feature/parcels/impl/src/main/kotlin/.../ParcelUiFormatters.kt`
- `feature/parcels/impl/src/main/kotlin/.../ParcelDisplayMetadata.kt`
- `feature/parcels/impl/src/main/kotlin/.../MultiPackageGrouping.kt`
- `feature/parcels/impl/src/main/kotlin/.../PickupWaitLabel.kt`
- `feature/parcels/impl/src/main/kotlin/.../TrackingEventLabel.kt`

Use these helpers rather than duplicating display logic in screens.

Common concepts:

- `isReadyForPickup`
- `isFinished`
- `isPickedUp`
- `parcelTitle`
- `lockerLine`
- `pickupCountdown`
- `historyOutcomeLine`
- `trackingTimelineEvents`
- `parcelSizeLabel`

## Data Privacy And Public Repo Hygiene

This app handles sensitive data:

- auth tokens
- phone number
- parcel shipment numbers
- pickup/open codes
- QR payloads
- locker IDs
- locker addresses
- sender/receiver names
- tracking timeline data

Rules:

- Never log tokens, phone numbers, parcel IDs, pickup codes, QR strings, or
  addresses.
- Never include live API response bodies in commits or issues.
- Never add screenshots from a real account.
- Never add real sample data to tests, previews, or design docs.
- Use obviously fake data:
  - `000000000000000000000001`
  - `000000`
  - `Example Sender sp. z o.o.`
  - `Example street 12, 00-000 Example City`
  - `WAW01A` only as a generic mock if not sourced from live data.
- Treat any realistic parcel code or QR payload as unsafe by default.

Network logging:

- Current logging is BASIC and redacts Authorization.
- Do not enable BODY logging in committed code.

DataStore:

- Auth tokens are persisted locally.
- Do not add debug UI or logs that reveal them.

Git:

- If sensitive data enters history, removing it from the current tree is not
  enough. Use `git filter-repo` to rewrite history before pushing.

## Adding A New Screen

Recommended steps:

1. Add route to `feature:<name>:api`.
2. Add `entry<Route>` in the feature impl navigation extension.
3. Add screen ViewModel with a single public `StateFlow`.
4. Keep UI state in a data class/sealed type.
5. Expose user actions as methods.
6. Keep Composables stateless except local ephemeral UI state.
7. Add focused ViewModel tests.
8. Add previews with fake data.
9. Verify accessibility semantics.
10. Register feature entries in `PaczkofastNavHost` if it is a new feature.

## Adding A New API Endpoint

Recommended steps:

1. Add DTOs in `core:network/dto`.
2. Add Retrofit method to the relevant API interface.
3. Add repository method or extend existing repository.
4. Add mapper from DTO to domain/database entity.
5. Add use case if orchestration or business rules are non-trivial.
6. Add ViewModel state/action.
7. Add tests:
   - DTO serialization.
   - repository behavior/error mapping.
   - use case flow if applicable.
   - ViewModel state.
8. Keep the unofficial/experimental disclaimer near API integration points.

Do not expose DTOs to feature UI.

## Adding Or Changing Database Schema

Steps:

1. Modify entity.
2. Bump `PaczkofastDatabase.version`.
3. Add migration in `Migrations.kt`.
4. Export schema.
5. Add/adjust DAO or migration tests.
6. Ensure repositories still map old/new fields safely.

Avoid:

- destructive migrations
- nullable/non-nullable changes without migration consideration
- changing primary keys casually

## Dependency Injection

Hilt modules:

- `core:data/.../di/DataModule.kt`
- `core:database/.../di/DatabaseModule.kt`
- `core:database/.../di/DaosModule.kt`
- `core:datastore/.../di/DataStoreModule.kt`
- `core:network/.../di/NetworkModule.kt`
- `core:common/.../location/LocationModule.kt`
- `core:common/.../di/DispatchersModule.kt`

Patterns:

- Use `@Binds` for interface-to-implementation bindings.
- Use `@Provides` for constructed instances such as Retrofit, OkHttp, Room,
  DataStore, and Json.
- Repositories are singletons.
- API interfaces are singletons.
- ViewModels use `@HiltViewModel`.
- Assisted injection is used for route parameters like `shipmentNumber`.

## Code Style

Current style:

- Kotlin.
- Explicit module boundaries.
- Extension mappers.
- Flow-based observation.
- Suspend functions for writes/network actions.
- Small UI state data classes.
- Preview data near screens.
- KDoc comments where architecture or behavior is not obvious.

No custom formatter/linter is configured beyond standard Gradle/Android tooling.
Keep formatting consistent with adjacent files.

Prefer:

- `val` over `var`.
- `runCatching` only when failure handling is simple and local.
- typed domain errors when behavior depends on the error.
- immutable UI state.
- small helper functions for screen formatting.
- explicit names over clever abstractions.

Avoid:

- direct network calls from ViewModels.
- exposing DTOs to UI.
- storing Compose state in repositories.
- writing UI formatting into core models.
- duplicating route constants as strings.
- adding global mutable singletons.
- logging sensitive data.

## Important Watchouts

Remote collect is high-risk:

- It opens physical locker compartments.
- Location permission is part of validation.
- The API is unofficial and can change.
- Do not guess at status semantics.
- Do not reorder collect API calls without tests.
- Do not skip `closed` or `claim` calls unless product behavior is explicitly
  redesigned.

Multi-package behavior:

- The app groups parcels by `multiCompartmentUuid`.
- Opening one shared compartment should claim all members in that compartment.
- The tapped/representative parcel's `openCode` is used for validation.
- Be careful not to claim unrelated parcels.

Auth:

- Token refresh happens in OkHttp authenticator.
- Refresh failure clears tokens.
- UI routing depends on stored auth session.

Room cache:

- Parcel list is cache-first.
- Detail refresh is background and failures are swallowed.
- Do not accidentally make detail screens blank offline.

Public repo:

- Always check design/docs/test data for private data.
- Do not commit screenshots from real app sessions.

## Key Files By Task

Startup/navigation:

- `app/src/main/java/pl/tajchert/paczko/fast/MainActivity.kt`
- `app/src/main/java/pl/tajchert/paczko/fast/MainActivityViewModel.kt`
- `app/src/main/java/pl/tajchert/paczko/fast/navigation/PaczkofastNavHost.kt`

Auth:

- `feature/auth/impl/src/main/kotlin/.../AuthViewModel.kt`
- `feature/auth/impl/src/main/kotlin/.../PhoneLoginScreen.kt`
- `feature/auth/impl/src/main/kotlin/.../OtpScreen.kt`
- `core/data/src/main/kotlin/.../DefaultAuthRepository.kt`
- `core/network/src/main/kotlin/.../InpostAuthApi.kt`

Parcels:

- `feature/parcels/impl/src/main/kotlin/.../list/ParcelListViewModel.kt`
- `feature/parcels/impl/src/main/kotlin/.../list/ParcelListScreen.kt`
- `feature/parcels/impl/src/main/kotlin/.../detail/ParcelDetailViewModel.kt`
- `feature/parcels/impl/src/main/kotlin/.../detail/ParcelDetailScreen.kt`
- `core/data/src/main/kotlin/.../DefaultParcelRepository.kt`
- `core/network/src/main/kotlin/.../InpostParcelApi.kt`

Collect/open locker:

- `feature/parcels/impl/src/main/kotlin/.../collect/CollectViewModel.kt`
- `feature/parcels/impl/src/main/kotlin/.../collect/CollectScreen.kt`
- `core/domain/src/main/kotlin/.../CollectParcelUseCase.kt`
- `core/data/src/main/kotlin/.../DefaultCollectRepository.kt`
- `core/network/src/main/kotlin/.../InpostCollectApi.kt`
- `core/model/src/main/kotlin/.../collect/CollectModels.kt`

Settings:

- `feature/settings/impl/src/main/kotlin/.../SettingsViewModel.kt`
- `feature/settings/impl/src/main/kotlin/.../SettingsScreen.kt`

Design system:

- `core/designsystem/src/main/kotlin/.../theme/Theme.kt`
- `core/designsystem/src/main/kotlin/.../component/`

Persistence:

- `core/database/src/main/kotlin/.../PaczkofastDatabase.kt`
- `core/database/src/main/kotlin/.../migration/Migrations.kt`
- `core/datastore/src/main/kotlin/.../AuthTokensDataSource.kt`
- `core/datastore/src/main/kotlin/.../UserPreferencesDataSource.kt`

Testing:

- `core/testing/src/main/kotlin/.../MainDispatcherRule.kt`
- `core/domain/src/test/.../CollectParcelUseCaseTest.kt`
- `feature/parcels/impl/src/test/.../CollectViewModelTest.kt`

## Pull Request Checklist

Before opening or merging a PR:

- Build compiles.
- Relevant unit tests pass.
- UI state branches are tested for non-trivial changes.
- No real private data in code, tests, previews, docs, assets, or screenshots.
- No BODY network logging.
- No new direct dependencies that violate module boundaries.
- No destructive database migration.
- Compose previews use fake data.
- Accessibility semantics are preserved or improved.
- Error states have a deliberate user-facing behavior.
- Public disclaimers remain near unofficial API integration points.

## Known Gaps And Future Improvements

These are not necessarily bugs, but they are important context:

- Collect error retry flag exists but UI does not use it.
- Settings has no loading/error state.
- Multi-package detail has no explicit missing-target error screen.
- Auth and several repository errors expose raw exception messages.
- Auth tokens are stored in Preferences DataStore, not encrypted storage.
- No custom lint/format/static-analysis tool is configured.
- No centralized logging/crash-reporting abstraction exists.
- Network logging level is BASIC for all builds; consider BuildConfig-gated
  logging for release hardening.
- API integration is unofficial and may break.
