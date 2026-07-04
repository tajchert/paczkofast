package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Card on the parcel detail screen showing the pickup deadline with a
 * mono label, a large countdown value and a bordered progress bar — on a
 * white [NeoSurface] with an ink border and hard shadow.
 */
@Composable
fun DeadlineCard(
    deadlineText: String,
    countdownText: String,
    progress: Float,
    modifier: Modifier = Modifier,
    urgent: Boolean = false,
) {
    PaczkofastCard(modifier = modifier) {
        DeadlineProgressBar(
            progress = progress,
            urgent = urgent,
            label = deadlineText,
            value = countdownText,
        )
    }
}

@PaczkofastPreviews
@Composable
private fun DeadlineCardPreview() {
    PaczkofastTheme {
        DeadlineCard(
            deadlineText = "Pick up by Fri 3 Jul, 12:56",
            countdownText = "46 h",
            progress = 0.64f,
            modifier = Modifier.padding(16.dp),
        )
    }
}
