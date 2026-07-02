package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Card on the parcel detail screen showing the pickup deadline with a
 * large countdown ("46 h") and a remaining-time bar.
 */
@Composable
fun DeadlineCard(
    deadlineText: String,
    countdownText: String,
    progress: Float,
    modifier: Modifier = Modifier,
    urgent: Boolean = false,
) {
    val countdownColor = if (urgent) {
        PaczkofastTheme.colors.urgent
    } else {
        PaczkofastTheme.colors.accentText
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PaczkofastTheme.colors.cardSurface)
            .border(1.dp, PaczkofastTheme.colors.cardBorder, MaterialTheme.shapes.large)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = deadlineText,
                style = MaterialTheme.typography.titleSmall,
                color = PaczkofastTheme.colors.textPrimary,
            )
            Text(
                text = countdownText,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                color = countdownColor,
            )
        }
        DeadlineProgressBar(
            progress = progress,
            color = if (urgent) countdownColor else PaczkofastTheme.colors.trackActive,
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
