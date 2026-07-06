# Signed Release APK on GitHub Releases — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship release-signed `prodRelease` APKs as GitHub Release artifacts — first cut locally (Stage 1), then automated by a tag-triggered GitHub Actions workflow (Stage 2) — while the keystore and all passwords stay strictly private.

**Architecture:** A private keystore lives outside the repo; Gradle reads signing values from a gitignored `keystore.properties` locally or `PACZKOFAST_*` environment variables in CI, and falls back to the debug key when neither exists (so contributor builds and the baseline-profile plugin's derived variants keep working untouched). Stage 1 is a documented manual procedure (build → verify → tag → `gh release create`). Stage 2 is one workflow file that decodes the keystore from a repo secret, builds, verifies the signature, and attaches the APK + SHA-256 to the release for the pushed tag.

**Tech Stack:** AGP 9.2.1 signing configs (Kotlin DSL), JDK 17, `keytool`, `apksigner`, `gh` CLI (already authenticated as `tajchert`), GitHub Actions (`actions/checkout`, `actions/setup-java`, `gradle/actions/setup-gradle`, `actions/upload-artifact`).

## Global Constraints

- Public repo: never commit the keystore, passwords, `keystore.properties`, or any base64 of them. `.gitignore` already covers `*.jks` / `*.keystore`; Task 1 adds `keystore.properties`.
- The real app is the `prod` flavor → release artifact is `:app:assembleProdRelease` output (`app/build/outputs/apk/prod/release/app-prod-release.apk`).
- `app/build.gradle.kts:61` currently debug-signs `release` **on purpose** so the baseline-profile plugin's derived variants (`benchmarkRelease`, `nonMinifiedRelease`) stay installable. The new signing config must preserve that behavior when no keystore is configured.
- JDK 17, compile SDK 37, min SDK 34. Use the repo's Gradle wrapper only.
- Release names/tags: `vX.Y.Z` git tags matching `versionName` in `app/build.gradle.kts` (currently `1.0.0`, `versionCode 1` — correct for the first release, no bump needed).
- Env var names (CI + local shell): `PACZKOFAST_KEYSTORE_PATH`, `PACZKOFAST_KEYSTORE_PASSWORD`, `PACZKOFAST_KEY_ALIAS`, `PACZKOFAST_KEY_PASSWORD`.
- GitHub secret names: `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`.
- Every GitHub Release description must carry the unofficial-app disclaimer (repo rule: disclaimers near anything user-facing built on the unofficial API).
- This repo has no committed release APK history yet — R8/minified builds have never been exercised end-to-end, so Task 3 includes a mandatory on-device smoke test before anything is published.

---

## Stage 1 — Local build, sign, publish

### Task 1: Generate the release keystore and local signing properties

Nothing produced in this task is committed except one `.gitignore` line.

**Files:**
- Create (outside repo, never committed): `~/keystores/paczkofast-release.jks`
- Create (repo root, gitignored): `keystore.properties`
- Modify: `.gitignore` (keystore section, after the `*.keystore` line)

**Interfaces:**
- Produces: `keystore.properties` with keys `storeFile`, `storePassword`, `keyAlias`, `keyPassword` — exactly the keys Task 2's Gradle code reads. Key alias is `paczkofast`.

- [ ] **Step 1: Generate the keystore (interactive — run in your own terminal, not via an agent, so passwords never enter any transcript)**

```bash
mkdir -p ~/keystores
keytool -genkeypair -v \
  -keystore ~/keystores/paczkofast-release.jks \
  -alias paczkofast \
  -keyalg RSA -keysize 4096 \
  -validity 10000 \
  -dname "CN=Paczkofast Release, O=Tajchert, C=PL"
```

Expected: prompts for a store password (pick a strong generated one; when asked for a key password, press Enter to reuse the store password), then prints `[Storing /Users/mtajchert/keystores/paczkofast-release.jks]`.

The `-dname` is embedded in every published APK and visible to anyone — it must contain no private data beyond your public author identity. `-validity 10000` (~27 years) satisfies Play's future requirement that certs be valid past 2033.

- [ ] **Step 2: Verify the keystore**

```bash
keytool -list -v -keystore ~/keystores/paczkofast-release.jks -alias paczkofast | head -20
```

Expected: `Alias name: paczkofast`, `Signature algorithm name: SHA384withRSA` (or similar), validity dates ~27 years apart.

- [ ] **Step 3: Back up the keystore before doing anything else**

Store BOTH of these in a password manager (e.g. 1Password, as a document + password entries), and ideally a second offline copy (encrypted USB / private cloud vault):

1. The `paczkofast-release.jks` file itself.
2. The store password, key alias (`paczkofast`), and key password.

Losing this keystore means existing sideload users can never update in place — they would have to uninstall and lose local app data. There is no recovery path for a self-managed signing key.

- [ ] **Step 4: Create `keystore.properties` at the repo root (again, in your own terminal)**

```properties
storeFile=/Users/mtajchert/keystores/paczkofast-release.jks
storePassword=<store password>
keyAlias=paczkofast
keyPassword=<key password>
```

Use an absolute `storeFile` path since the file lives outside the repo.

- [ ] **Step 5: Gitignore it, verify, commit**

Add one line to `.gitignore` directly under the existing `*.keystore` line:

```gitignore
# Keystore files
*.jks
*.keystore
keystore.properties
```

Verify both secrets are ignored (both commands must print the path, i.e. exit 0):

```bash
git check-ignore keystore.properties && echo IGNORED
git status --porcelain | grep -i keystore ; echo "exit=$? (want 1 = no match)"
```

Expected: `IGNORED`, and the grep finds nothing.

```bash
git add .gitignore
git commit -m "chore(release): gitignore keystore.properties for local release signing"
```

### Task 2: Wire release signing into `:app` with a safe debug fallback

**Files:**
- Modify: `app/build.gradle.kts` (imports at top; new signing block before `android {}`; `signingConfigs` inside `android {}`; `release` build type at lines 49–67)

**Interfaces:**
- Consumes: `keystore.properties` keys from Task 1 (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`).
- Produces: release builds signed with the `release` config whenever `keystore.properties` exists **or** `PACZKOFAST_KEYSTORE_PATH` is set in the environment; debug-signed otherwise. Task 5's workflow relies on exactly the four `PACZKOFAST_*` env var names.

- [ ] **Step 1: Establish the "before" behavior (this is the failing-state check for config work)**

Temporarily rename `keystore.properties` away so you can observe the fallback later, then confirm current release builds are debug-signed:

```bash
mv keystore.properties /tmp/keystore.properties.bak
./gradlew :app:assembleProdRelease
APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner -type f | sort -V | tail -n1)
"$APKSIGNER" verify --print-certs app/build/outputs/apk/prod/release/app-prod-release.apk | head -3
```

Expected: `Signer #1 certificate DN: C=US, O=Android, CN=Android Debug`.

- [ ] **Step 2: Add the signing wiring to `app/build.gradle.kts`**

Add the import immediately above the `plugins {}` block (Kotlin scripts require imports before any statement; the header comment block above it is fine):

```kotlin
import java.util.Properties
```

Add directly below the `plugins {}` block:

```kotlin
// Release signing. Values come from keystore.properties (local, gitignored) or
// PACZKOFAST_* environment variables (CI). The keystore itself is private and
// never committed; see AGENTS.md public-safety rules.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun releaseSigningValue(property: String, envVar: String): String? =
    keystoreProperties.getProperty(property) ?: System.getenv(envVar)

val releaseStorePath = releaseSigningValue("storeFile", "PACZKOFAST_KEYSTORE_PATH")
```

Add a `signingConfigs` block inside `android {}`, above `buildTypes` (order matters — `getByName` below resolves it):

```kotlin
    signingConfigs {
        if (releaseStorePath != null) {
            create("release") {
                storeFile = file(releaseStorePath)
                storePassword = releaseSigningValue("storePassword", "PACZKOFAST_KEYSTORE_PASSWORD")
                keyAlias = releaseSigningValue("keyAlias", "PACZKOFAST_KEY_ALIAS")
                keyPassword = releaseSigningValue("keyPassword", "PACZKOFAST_KEY_PASSWORD")
            }
        }
    }
```

Replace the existing comment + `signingConfig` assignment in the `release` build type (`app/build.gradle.kts:57-61`) with:

```kotlin
            // Falls back to the debug key when no release keystore is configured,
            // so release (and the baseline-profile plugin's derived
            // benchmarkRelease/nonMinifiedRelease variants) stays installable on
            // a device for profile recording and contributor builds.
            signingConfig = if (releaseStorePath != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
```

- [ ] **Step 3: Verify the fallback path still debug-signs (no keystore present)**

```bash
./gradlew :app:assembleProdRelease
APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner -type f | sort -V | tail -n1)
"$APKSIGNER" verify --print-certs app/build/outputs/apk/prod/release/app-prod-release.apk | head -3
```

Expected: still `CN=Android Debug` — proves contributors and baseline-profile generation are unaffected.

- [ ] **Step 4: Verify the release path signs with the real key**

```bash
mv /tmp/keystore.properties.bak keystore.properties
./gradlew :app:assembleProdRelease
APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner -type f | sort -V | tail -n1)
"$APKSIGNER" verify --print-certs app/build/outputs/apk/prod/release/app-prod-release.apk | head -3
```

Expected: `Signer #1 certificate DN: CN=Paczkofast Release, O=Tajchert, C=PL`, no `ERROR` lines.

- [ ] **Step 5: Run the standard pre-push gate and commit**

```bash
./gradlew :app:compileProdDebugKotlin test
git add app/build.gradle.kts
git commit -m "feat(release): sign release builds from private keystore with debug fallback"
```

Expected: BUILD SUCCESSFUL, all tests pass. Confirm `git status --porcelain` shows no `keystore.properties`.

### Task 3: Cut v1.0.0 locally and publish it as a GitHub Release

**Files:**
- No repo file changes (first release keeps `versionCode 1` / `versionName "1.0.0"` as already set in `app/build.gradle.kts:40-41`). Build outputs and checksums are staged in `/tmp`, never inside the repo (`.gitignore` excludes `*.apk` anyway — keep it that way).

**Interfaces:**
- Consumes: signed `prodRelease` from Task 2.
- Produces: git tag `v1.0.0` and a GitHub Release with assets `paczkofast-1.0.0.apk` + `paczkofast-1.0.0.apk.sha256`. Task 5 reuses this exact naming and notes format.

- [ ] **Step 1: Smoke-test the minified release build on a device (mandatory)**

This is the first R8-minified build anyone runs. kotlinx.serialization (Navigation 3 route keys, API DTOs), Retrofit, Room, and Hilt all interact with shrinking; library consumer rules should cover them, but verify by walking the app:

```bash
adb uninstall pl.tajchert.paczko.fast 2>/dev/null  # signature changed from any prior debug-signed install
adb install app/build/outputs/apk/prod/release/app-prod-release.apk
```

Walk through: onboarding → phone entry (enter 9 digits, request SMS — a real error response from the API is fine, a crash is not) → log in → parcel list renders → open a parcel detail → open Settings → toggle theme. Watch logcat for crashes:

```bash
adb logcat --pids=$(adb shell pidof pl.tajchert.paczko.fast) '*:E'
```

Also build and click through the offline demo release (exercises collect flow UI without touching a real locker):

```bash
./gradlew :app:assembleDemoRelease
adb install app/build/outputs/apk/demo/release/app-demo-release.apk
```

Expected: no crashes, no missing screens. If R8 breaks something, fix `app/proguard-rules.pro` with a targeted `-keep` rule (plus a comment explaining which library needs it), rebuild, re-test, and commit that fix before proceeding.

- [ ] **Step 2: Rebuild clean, verify signature, stage artifacts**

```bash
./gradlew clean :app:assembleProdRelease
APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner -type f | sort -V | tail -n1)
"$APKSIGNER" verify --print-certs app/build/outputs/apk/prod/release/app-prod-release.apk
mkdir -p /tmp/paczkofast-release
cp app/build/outputs/apk/prod/release/app-prod-release.apk /tmp/paczkofast-release/paczkofast-1.0.0.apk
cd /tmp/paczkofast-release && shasum -a 256 paczkofast-1.0.0.apk > paczkofast-1.0.0.apk.sha256 && cd -
```

Expected: apksigner prints the Paczkofast Release DN with no errors.

- [ ] **Step 3: Tag and push (branch must already be merged to master)**

```bash
git tag -a v1.0.0 -m "Paczkofast 1.0.0 — first signed release"
git push origin master v1.0.0
```

- [ ] **Step 4: Create the GitHub Release with disclaimer + checksums**

```bash
gh release create v1.0.0 \
  --verify-tag \
  --title "Paczkofast 1.0.0" \
  --generate-notes \
  --notes "$(cat <<'EOF'
> **Unofficial app.** Paczkofast is an experimental, unofficial companion app.
> It is not affiliated with, endorsed by, or supported by any locker operator.
> The API integration is unofficial and may break at any time. Use at your own risk.

**Install:** download `paczkofast-1.0.0.apk` and sideload it (enable "install unknown apps"). Verify the download with the `.sha256` file: `shasum -a 256 -c paczkofast-1.0.0.apk.sha256`.
EOF
)" \
  /tmp/paczkofast-release/paczkofast-1.0.0.apk \
  /tmp/paczkofast-release/paczkofast-1.0.0.apk.sha256
```

Expected: prints the release URL. Open it and confirm both assets are attached and the disclaimer renders at the top (generated notes follow below it).

- [ ] **Step 5: Post-release check**

Download the APK back from the release page and re-run `apksigner verify --print-certs` on it — confirms GitHub served the exact signed bytes. Optionally verify it installs via [Obtainium](https://github.com/ImranR98/Obtainium), which auto-updates sideloaded apps straight from GitHub Releases.

**Releasing future versions (procedure, not a task):** bump `versionCode` (+1 every release, monotonically — Android refuses in-place updates otherwise) and `versionName` in `app/build.gradle.kts:40-41`, commit, then — once Stage 2 is live — just push the `vX.Y.Z` tag and the workflow does the rest.

---

## Stage 2 — CI/CD with GitHub Actions

### Task 4: Store signing secrets in the GitHub repo

**Files:** none (repo settings only).

**Interfaces:**
- Produces: repo secrets `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` — the exact names Task 5's workflow reads.

- [ ] **Step 1: Upload the four secrets (run in your own terminal — password values must not enter an agent transcript)**

```bash
base64 -i ~/keystores/paczkofast-release.jks | gh secret set RELEASE_KEYSTORE_BASE64
gh secret set RELEASE_KEYSTORE_PASSWORD   # paste value at the hidden prompt
gh secret set RELEASE_KEY_ALIAS           # paste: paczkofast
gh secret set RELEASE_KEY_PASSWORD        # paste value at the hidden prompt
```

- [ ] **Step 2: Verify**

```bash
gh secret list
```

Expected: all four names listed with update timestamps. Values are write-only from here on — GitHub never displays them again, and the Actions log masker redacts them if they ever would be printed.

### Task 5: Add the tag-triggered release workflow

**Files:**
- Create: `.github/workflows/release.yml`

**Interfaces:**
- Consumes: Task 4's four secret names; Task 2's `PACZKOFAST_*` env var contract; Task 3's artifact naming (`paczkofast-<version>.apk` + `.sha256`) and disclaimer text.
- Produces: on `v*` tag push — a GitHub Release with signed APK + checksum. On manual `workflow_dispatch` — a dry run that builds, signs, and verifies but only uploads a workflow artifact (no release), for testing the pipeline safely.

- [ ] **Step 1: Write `.github/workflows/release.yml`**

```yaml
# Builds, signs, and publishes the prodRelease APK to a GitHub Release.
#
# Triggers:
#   - push of a v* tag: full release (build -> verify -> attach to release)
#   - workflow_dispatch: dry run — same build + signature verification, but the
#     APK is only uploaded as a workflow artifact, no release is created.
#
# Signing: the private keystore is decoded from the RELEASE_KEYSTORE_BASE64
# repo secret into the runner's temp dir and passed to Gradle via PACZKOFAST_*
# env vars (see app/build.gradle.kts). Secrets are never available to
# fork-triggered workflows; only pushes by repo writers reach this job.
name: Release

on:
  push:
    tags:
      - "v*"
  workflow_dispatch:

permissions:
  contents: write # create the release + upload assets

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - uses: gradle/actions/setup-gradle@v4
        with:
          validate-wrappers: true

      - name: Run unit tests
        run: ./gradlew test

      - name: Check tag matches versionName
        if: github.ref_type == 'tag'
        run: |
          TAG_VERSION="${GITHUB_REF_NAME#v}"
          GRADLE_VERSION=$(grep -m1 'versionName = ' app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
          if [ "$TAG_VERSION" != "$GRADLE_VERSION" ]; then
            echo "Tag $GITHUB_REF_NAME does not match versionName $GRADLE_VERSION in app/build.gradle.kts" >&2
            exit 1
          fi

      - name: Decode release keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.RELEASE_KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 -d > "$RUNNER_TEMP/release.jks"

      - name: Build signed prodRelease APK
        env:
          PACZKOFAST_KEYSTORE_PATH: ${{ runner.temp }}/release.jks
          PACZKOFAST_KEYSTORE_PASSWORD: ${{ secrets.RELEASE_KEYSTORE_PASSWORD }}
          PACZKOFAST_KEY_ALIAS: ${{ secrets.RELEASE_KEY_ALIAS }}
          PACZKOFAST_KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
        run: ./gradlew :app:assembleProdRelease

      - name: Verify APK signature
        run: |
          APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner -type f | sort -V | tail -n1)
          "$APKSIGNER" verify --print-certs \
            app/build/outputs/apk/prod/release/app-prod-release.apk
          "$APKSIGNER" verify --print-certs \
            app/build/outputs/apk/prod/release/app-prod-release.apk \
            | grep -q "CN=Paczkofast Release" || { echo "APK is not release-signed" >&2; exit 1; }

      - name: Stage artifacts
        run: |
          if [ "${{ github.ref_type }}" = "tag" ]; then
            VERSION="${GITHUB_REF_NAME#v}"
          else
            VERSION="ci-${GITHUB_SHA::7}"
          fi
          mkdir -p dist
          cp app/build/outputs/apk/prod/release/app-prod-release.apk "dist/paczkofast-$VERSION.apk"
          cd dist && shasum -a 256 "paczkofast-$VERSION.apk" > "paczkofast-$VERSION.apk.sha256"

      - name: Create GitHub Release
        if: github.ref_type == 'tag'
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          gh release create "$GITHUB_REF_NAME" \
            --verify-tag \
            --title "Paczkofast ${GITHUB_REF_NAME#v}" \
            --generate-notes \
            --notes "$(cat <<'EOF'
          > **Unofficial app.** Paczkofast is an experimental, unofficial companion app.
          > It is not affiliated with, endorsed by, or supported by any locker operator.
          > The API integration is unofficial and may break at any time. Use at your own risk.

          **Install:** download the APK below and sideload it (enable "install unknown apps"). Verify the download against the `.sha256` checksum file.
          EOF
          )" \
            dist/*.apk dist/*.apk.sha256

      - name: Upload dry-run artifact
        if: github.ref_type != 'tag'
        uses: actions/upload-artifact@v4
        with:
          name: paczkofast-prod-release-apk
          path: dist/*
```

- [ ] **Step 2: Commit and merge**

```bash
git add .github/workflows/release.yml
git commit -m "ci: build, sign, and publish release APK on version tags"
```

Merge the branch to `master` (the workflow must exist on the default branch before `gh workflow run` can dispatch it).

- [ ] **Step 3: Dry-run the pipeline without releasing anything**

```bash
gh workflow run release.yml
gh run watch --exit-status "$(gh run list --workflow=release.yml --limit 1 --json databaseId --jq '.[0].databaseId')"
```

Expected: run succeeds; the "Verify APK signature" step logs `CN=Paczkofast Release`; a `paczkofast-prod-release-apk` artifact (containing `paczkofast-ci-<sha>.apk` + `.sha256`) is attached to the run; **no** release is created. If the run fails at decode/signing, re-check the secret values from Task 4.

- [ ] **Step 4: First automated release**

Next version (after bumping `versionCode`/`versionName` and merging):

```bash
git tag -a v1.0.1 -m "Paczkofast 1.0.1"
git push origin v1.0.1
gh run watch --exit-status "$(gh run list --workflow=release.yml --limit 1 --json databaseId --jq '.[0].databaseId')"
```

Expected: the workflow creates the `v1.0.1` release with both assets and the disclaimer. Download the APK from the release page and spot-check with `apksigner verify --print-certs`.

---

## Stage 2 requirements and cost (public repo)

What is needed — all of it free:

| Need | What/where | Cost |
|---|---|---|
| CI compute | GitHub-hosted `ubuntu-latest` standard runner (4-vCPU/16 GB for public repos) | **$0 — unlimited Actions minutes on standard runners for public repos** |
| Secret storage | 4 repo Actions secrets (Settings → Secrets → Actions) | $0 |
| Release asset hosting | GitHub Releases (2 GiB per-file limit; this APK is ~10–20 MB) | $0, doesn't count toward any storage quota |
| Dry-run artifact storage | Actions artifacts, public repo | $0 (90-day default retention) |
| Workflow file | `.github/workflows/release.yml` | $0 |

Expected usage: one release run ≈ 10–20 min of Linux runner time (unit tests + R8-minified assemble dominate). Even releasing weekly plus a per-push test workflow later, a public repo pays nothing.

For contrast, if the repo ever went **private**: Free plan includes 2,000 Actions minutes/month and 500 MB artifact storage; beyond that, Linux standard runners bill at ~$0.008/min — a 20-minute release ≈ $0.16, so realistically still under a few dollars per month. macOS runners are ~10× the Linux rate but are not needed for Android builds. (Rates as of mid-2026 — check https://docs.github.com/en/billing/concepts/product-billing/github-actions before relying on them.)

Security posture for a public repo (why this design is safe):

- **Fork PRs cannot reach the secrets.** GitHub withholds secrets from workflows triggered by `pull_request` from forks, and this workflow doesn't run on PRs at all — only on tag pushes (writer-only) and manual dispatch (writer-only).
- **Never add `pull_request_target` + PR-code checkout** to this workflow; that combination is the classic secret-exfiltration foot-gun.
- The keystore is decoded into `$RUNNER_TEMP`, which is destroyed with the ephemeral runner VM after the job.
- The workflow file itself is public but contains no secret material; log masking redacts secret values as defense-in-depth.
- `permissions:` is pinned to the minimum (`contents: write`) rather than the default token grab-bag.
- Optional hardening later: pin actions to commit SHAs instead of major-version tags, add a GitHub Environment with required reviewers on the release job, and publish [artifact attestations](https://docs.github.com/en/actions/security-for-github-actions/using-artifact-attestations) (free for public repos) for provenance.

## Keystore lifecycle and the Play Store future

- **This keystore is a forever secret.** Whether or not Play publishing ever happens, it must never appear in the repo, an issue, a CI log, or an agent transcript. Backups per Task 1 Step 3.
- **Play Store later:** enroll in **Play App Signing** at first upload. Google then generates and holds the *app signing key*, and this local keystore becomes the *upload key* — which Google can reset if it's ever lost or leaked. That makes today's keystore fully compatible with a future Play release; no need to regenerate anything.
- **One caveat once on Play:** Play re-signs the app with its own signing key, so Play installs and GitHub-sideloaded installs will have *different* signatures — users can't cross-update between the two sources without uninstalling. If Play ever becomes the primary channel, decide then whether GitHub Releases keep shipping (as a separate lineage) or just link to Play.
- **If the keystore is ever compromised:** there is no revocation for sideload signing. Rotate by generating a new keystore, bump `versionName` major, and tell users to uninstall/reinstall (release notes + README).
