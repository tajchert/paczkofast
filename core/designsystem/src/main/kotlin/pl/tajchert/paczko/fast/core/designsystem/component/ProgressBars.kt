package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val BarShape = RoundedCornerShape(7.dp)
private val BarHeight = 12.dp
private val BarBorderWidth = 2.dp

/**
 * Neo-brutalist countdown bar showing how much pickup time remains: a
 * thick bordered track with a solid fill, plus an optional mono caption
 * row above it ("TIME LEFT" / "1D 14H").
 *
 * @param progress Fraction of time remaining, 0f..1f.
 * @param urgent Switches the fill and caption value to the alert palette
 *   when time is running out (e.g. "9H — HURRY!").
 * @param label Left caption text, e.g. "TIME LEFT" or "PICK UP BY …".
 *   Hidden when null.
 * @param value Right caption text, e.g. "1D 14H". Hidden when null.
 */
@Composable
fun DeadlineProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    urgent: Boolean = false,
    label: String? = null,
    value: String? = null,
) {
    val fillColor = if (urgent) PaczkofastTheme.colors.alertFill else PaczkofastTheme.colors.accent
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null || value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                label?.let {
                    Text(
                        text = it,
                        style = MonoLabel,
                        color = PaczkofastTheme.colors.monoLabel,
                    )
                }
                value?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (urgent) PaczkofastTheme.colors.alertText else PaczkofastTheme.colors.textPrimary,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .clip(BarShape)
                .background(PaczkofastTheme.colors.trackBackground)
                .border(BarBorderWidth, PaczkofastTheme.colors.borderStrong, BarShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(BarShape)
                    .background(fillColor),
            )
        }
    }
}

/**
 * Segmented delivery progress used on in-transit cards: a row of equal
 * bordered segments, filled yellow up to [completedSegments], plus an
 * optional mono caption row below ("OUT FOR DELIVERY · 3/4" … "TODAY").
 *
 * @param completedSegments Number of segments (0..[totalSegments]) to
 *   render filled.
 * @param totalSegments Total number of segments in the row.
 * @param statusLabel Left caption text, e.g. "OUT FOR DELIVERY · 3/4".
 *   Hidden when null.
 * @param etaLabel Right caption text, e.g. "TODAY". Hidden when null.
 */
@Composable
fun SegmentedProgressBar(
    completedSegments: Int,
    modifier: Modifier = Modifier,
    totalSegments: Int = 4,
    statusLabel: String? = null,
    etaLabel: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(totalSegments) { index ->
                val filled = index < completedSegments
                val color = if (filled) {
                    PaczkofastTheme.colors.accent
                } else {
                    PaczkofastTheme.colors.trackBackground
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(BarHeight)
                        .clip(BarShape)
                        .background(color)
                        .border(BarBorderWidth, PaczkofastTheme.colors.borderStrong, BarShape),
                )
            }
        }
        if (statusLabel != null || etaLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                statusLabel?.let {
                    Text(text = it, style = MonoLabel, color = PaczkofastTheme.colors.monoLabel)
                }
                etaLabel?.let {
                    Text(text = it, style = MonoLabel, color = PaczkofastTheme.colors.monoLabel)
                }
            }
        }
    }
}

@PaczkofastPreviews
@Composable
private fun DeadlineProgressBarPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DeadlineProgressBar(
                progress = 0.96f,
                label = "PICK UP BY · FRI 12:56",
                value = "46H",
            )
            DeadlineProgressBar(
                progress = 0.06f,
                urgent = true,
                label = "TIME LEFT",
                value = "9H — HURRY!",
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun SegmentedProgressBarPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SegmentedProgressBar(
                completedSegments = 1,
                statusLabel = "PICKED UP · 1/4",
                etaLabel = "TODAY",
            )
            SegmentedProgressBar(
                completedSegments = 2,
                statusLabel = "IN TRANSIT · 2/4",
                etaLabel = "TODAY",
            )
            SegmentedProgressBar(
                completedSegments = 3,
                statusLabel = "OUT FOR DELIVERY · 3/4",
                etaLabel = "TODAY",
            )
        }
    }
}
