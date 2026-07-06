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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * A single event in the parcel tracking timeline.
 *
 * @param time Timestamp line under the label; null when unknown (e.g.
 *   upcoming stages).
 * @param isCurrent The most recent event — rendered with a filled amber
 *   square node and bold label.
 * @param isUpcoming A future stage that hasn't happened yet — rendered
 *   with a hollow node and faint label.
 */
@Immutable
data class TimelineEvent(
    val label: String,
    val time: String? = null,
    val isCurrent: Boolean = false,
    val isUpcoming: Boolean = false,
)

/**
 * Vertical tracking timeline: square nodes connected by a rail, newest
 * event first.
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

private val NodeSize = 14.dp
private val NodeShape = RoundedCornerShape(3.dp)

@Composable
private fun TimelineRow(
    event: TimelineEvent,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {
                contentDescription = buildList {
                    add(event.label)
                    event.time?.let { add(it) }
                    add(
                        when {
                            event.isCurrent -> "Current status"
                            event.isUpcoming -> "Upcoming"
                            else -> "Completed"
                        },
                    )
                }.joinToString(", ")
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .width(NodeSize)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Past stages are neither current nor upcoming — mark them done
            // with an amber node and an ink check, matching the app's
            // pickup/completion motif.
            val isCompleted = !event.isCurrent && !event.isUpcoming
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(NodeSize)
                    .clip(NodeShape)
                    .background(
                        when {
                            event.isCurrent -> PaczkofastTheme.colors.accent
                            event.isUpcoming -> PaczkofastTheme.colors.timelineDotInactive
                            else -> PaczkofastTheme.colors.accent
                        },
                    )
                    .border(
                        width = 2.dp,
                        // Done/current nodes are yellow-filled → ink border in both themes;
                        // upcoming (hollow) nodes trace the neutral rail color.
                        color = if (event.isUpcoming) {
                            PaczkofastTheme.colors.timelineRail
                        } else {
                            PaczkofastTheme.colors.accentBorder
                        },
                        shape = NodeShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PaczkofastTheme.colors.onAccent,
                        modifier = Modifier.size(11.dp),
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.5.dp)
                        .weight(1f)
                        .background(PaczkofastTheme.colors.timelineRail),
                )
            }
        }
        Column(
            modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = event.label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (event.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
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
                    style = MonoLabel,
                    color = PaczkofastTheme.colors.monoLabel,
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
                TimelineEvent("Ready for pickup", "04.07.26 · 10:35", isCurrent = true),
                TimelineEvent("Out for delivery", "04.07.26 · 06:12"),
                TimelineEvent("Sorting centre · Warsaw", "03.07.26 · 22:48"),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
