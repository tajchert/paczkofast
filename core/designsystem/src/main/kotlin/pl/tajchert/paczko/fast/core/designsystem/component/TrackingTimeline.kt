package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * A single event in the parcel tracking timeline.
 *
 * @param time Timestamp line under the label; null when unknown (e.g.
 *   upcoming stages).
 * @param isCurrent The most recent event — rendered with a larger amber
 *   dot and emphasized label.
 * @param isUpcoming A future stage that hasn't happened yet — rendered
 *   with a hollow dot and faint label.
 */
@Immutable
data class TimelineEvent(
    val label: String,
    val time: String? = null,
    val isCurrent: Boolean = false,
    val isUpcoming: Boolean = false,
)

/**
 * Vertical tracking timeline: dots connected by a rail, newest event first.
 */
@Composable
fun TrackingTimeline(
    events: List<TimelineEvent>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        events.forEachIndexed { index, event ->
            TimelineRow(event = event, isLast = index == events.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(
    event: TimelineEvent,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val dotSize = if (event.isCurrent) 14.dp else 8.dp
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(dotSize)
                    .clip(RoundedCornerShape(5.dp))
                    .let { dot ->
                        when {
                            // Current stage and every already-passed stage are
                            // filled amber; only not-yet-reached stages stay hollow.
                            event.isUpcoming -> dot.border(
                                width = 1.5.dp,
                                color = PaczkofastTheme.colors.timelineRail,
                                shape = RoundedCornerShape(5.dp),
                            )
                            else -> dot.background(PaczkofastTheme.colors.accent)
                        }
                    },
            )
            if (!isLast) {
                // The rail below a reached stage represents a passed transition,
                // so it is amber; the rail hanging off an upcoming stage (down to
                // the current one) is a transition not yet made, so it stays muted.
                val railColor = if (event.isUpcoming) {
                    PaczkofastTheme.colors.timelineRail
                } else {
                    PaczkofastTheme.colors.accent
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .background(railColor),
                )
            }
        }
        Column(
            modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = event.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (event.isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                ),
                color = when {
                    event.isCurrent -> PaczkofastTheme.colors.textPrimary
                    event.isUpcoming -> PaczkofastTheme.colors.textFaint
                    else -> PaczkofastTheme.colors.textSecondary
                },
            )
            if (event.time != null) {
                Text(
                    text = event.time,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = PaczkofastTheme.colors.textMuted,
                )
            }
        }
    }
}

@PaczkofastPreviews
@Composable
private fun TrackingTimelinePreview() {
    PaczkofastTheme {
        TrackingTimeline(
            events = listOf(
                TimelineEvent("Picked up", isUpcoming = true),
                TimelineEvent("Ready for pickup", "01.07.26 · 12:56", isCurrent = true),
                TimelineEvent("Out for delivery", "01.07.26 · 8:14"),
                TimelineEvent("Sorting centre · Warsaw", "01.07.26 · 0:56"),
                TimelineEvent("In transit", "30.06.26 · 18:21"),
                TimelineEvent("Shipment created", "30.06.26 · 14:27"),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
