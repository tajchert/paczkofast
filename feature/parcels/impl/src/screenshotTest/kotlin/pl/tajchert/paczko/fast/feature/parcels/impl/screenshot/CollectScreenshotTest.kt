package pl.tajchert.paczko.fast.feature.parcels.impl.screenshot

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import kotlinx.collections.immutable.persistentListOf
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.collect.CollectState
import pl.tajchert.paczko.fast.feature.parcels.impl.collect.CollectContent
import pl.tajchert.paczko.fast.feature.parcels.impl.collect.CollectMember
import pl.tajchert.paczko.fast.feature.parcels.impl.collect.CollectUiState

/**
 * One screenshot per [CollectState] value. All fixture data below — locker
 * name, a fixed distance `Int`, member names/sizes — is static (no wall-clock
 * dependency), so these renders are time-independent by construction; no
 * determinism workaround is needed here (contrast with the detail/multi-box
 * screenshots in this package, which do carry a `now()`-relative countdown
 * risk that had to be neutralized).
 *
 * All values are obviously fake per the repo's public-safety rules: an
 * all-zero shipment number, `WAW01A`, "Example ..." sender placeholders.
 */
private val demoMembers = persistentListOf(
    CollectMember(shipmentNumber = "000000000000000000000001", title = "Example Sender sp. z o.o.", sizeLabel = "A"),
)

@Composable
private fun collectPreview(state: CollectState) {
    EnglishScreenshotContent {
        PaczkofastTheme {
            CollectContent(
                uiState = CollectUiState(
                    state = state,
                    lockerName = "WAW01A",
                    distanceMeters = 12,
                    members = demoMembers,
                ),
                onConfirmed = {},
                onBack = {},
            )
        }
    }
}

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectIdle() = collectPreview(CollectState.Idle)

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectValidating() = collectPreview(CollectState.Validating)

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectOpening() = collectPreview(CollectState.Opening("demo"))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectWaitingOpened() = collectPreview(CollectState.WaitingForOpened("demo"))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectOpened() = collectPreview(CollectState.Opened("demo"))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectWaitingClosed() = collectPreview(CollectState.WaitingForClosed("demo"))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectConfirmingClosed() = collectPreview(CollectState.ConfirmingClosed("demo"))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectClaiming() = collectPreview(CollectState.Claiming("demo"))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectCompleted() = collectPreview(CollectState.Completed)

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectCanceled() = collectPreview(CollectState.Canceled)

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectFailedHard() =
    collectPreview(CollectState.Failed(message = "boxMachineNotFound", canRetryFromValidation = false))

@PreviewTest
@EnglishPaczkofastPreviews
@Composable
private fun CollectFailedSoft() =
    collectPreview(
        CollectState.Failed(message = "unknown", canRetryFromValidation = false, boxAlreadyOpen = true),
    )
