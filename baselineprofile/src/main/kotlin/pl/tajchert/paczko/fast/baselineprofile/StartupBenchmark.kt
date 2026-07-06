package pl.tajchert.paczko.fast.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Measures cold-start time so the baseline profile's effect is quantifiable.
 *
 * Runs the same cold startup under two compilation modes:
 *  - [CompilationMode.None] — no AOT, JIT only (the "before" case).
 *  - [CompilationMode.Partial] with [BaselineProfileMode.Require] — the bundled
 *    baseline profile is compiled ahead of time (the "after" case).
 *
 * Run against the profileable, non-debuggable benchmarkRelease variant:
 *   ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest \
 *     -Pandroid.testInstrumentationRunnerArguments.class=\
 *       pl.tajchert.paczko.fast.baselineprofile.StartupBenchmark
 */
@LargeTest
@RunWith(Parameterized::class)
class StartupBenchmark(private val compilationMode: CompilationMode) {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "pl.tajchert.paczko.fast",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
    }

    companion object {
        @Parameterized.Parameters(name = "compilation={0}")
        @JvmStatic
        fun parameters() = listOf(
            CompilationMode.None(),
            CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require),
        )
    }
}
