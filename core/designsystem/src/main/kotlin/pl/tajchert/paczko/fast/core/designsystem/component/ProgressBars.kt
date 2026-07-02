package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Thin rounded bar showing how much pickup time remains.
 *
 * @param progress Fraction of time remaining, 0f..1f.
 * @param color Fill color; defaults to the amber accent. Pass
 *   [PaczkofastColors.urgent][pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastColors.urgent]
 *   when time is running out.
 */
@Composable
fun DeadlineProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = PaczkofastTheme.colors.trackActive,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(PaczkofastTheme.colors.trackBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
    }
}

/**
 * Segmented delivery progress used on in-transit cards: completed stages
 * are amber, the current stage is faint, remaining stages are empty tracks.
 */
@Composable
fun SegmentedProgressBar(
    completedSegments: Int,
    totalSegments: Int,
    modifier: Modifier = Modifier,
    hasCurrentSegment: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalSegments) { index ->
            val color = when {
                index < completedSegments -> PaczkofastTheme.colors.trackActive
                hasCurrentSegment && index == completedSegments -> PaczkofastTheme.colors.trackDone
                else -> PaczkofastTheme.colors.trackBackground
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun ProgressBarsPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DeadlineProgressBar(progress = 0.64f)
            DeadlineProgressBar(progress = 0.18f, color = PaczkofastTheme.colors.urgent)
            SegmentedProgressBar(completedSegments = 2, totalSegments = 4)
            SegmentedProgressBar(completedSegments = 1, totalSegments = 4)
        }
    }
}
