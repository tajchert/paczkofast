package pl.tajchert.paczko.fast.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records a Baseline Profile of app startup. The recorded hot paths are compiled
 * ahead-of-time at install, cutting cold-start jank on the first launches before
 * the JIT warms up.
 *
 * Run: `./gradlew :app:generateBaselineProfile` (needs a connected device/emulator).
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        // Release applicationId (the profile is generated on the non-minified
        // release variant, which has no `.debug` suffix).
        packageName = "pl.tajchert.paczko.fast",
        // Also emit a startup profile so the hottest startup classes are compiled first.
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
    }
}
