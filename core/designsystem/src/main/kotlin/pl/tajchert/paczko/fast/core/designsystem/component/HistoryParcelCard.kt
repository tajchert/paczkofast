package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AssignmentReturn
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.R
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabelLarge
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Outcome of a finished parcel — drives the glyph and tile tint on a
 * history row.
 */
enum class HistoryOutcome {
    /** Collected / delivered successfully. */
    PickedUp,

    /** Pickup window elapsed. */
    Expired,

    /** Sent back to the sender or otherwise not delivered. */
    Returned,
}

private val HistoryTileShape = RoundedCornerShape(8.dp)

/**
 * A single row in the History tab: a small yellow icon tile, the sender
 * name and a mono outcome/locker line, with the completion date and a
 * chevron trailing — all on a white [NeoSurface].
 *
 * @param muted Renders the row with a lighter fill so older entries read as
 *   archive.
 */
@Composable
fun HistoryParcelCard(
    title: String,
    outcomeLine: String,
    dateText: String,
    outcome: HistoryOutcome,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    val colors = PaczkofastTheme.colors
    val tileFill = when (outcome) {
        // Collected parcels always read as "done" — amber tile + check —
        // regardless of how old (muted) the row is.
        HistoryOutcome.PickedUp -> colors.accent
        else -> colors.background
    }
    val iconTint = when (outcome) {
        HistoryOutcome.PickedUp -> colors.textPrimary
        HistoryOutcome.Expired -> colors.alertText
        HistoryOutcome.Returned -> colors.textMuted
    }
    val icon: ImageVector = when (outcome) {
        HistoryOutcome.PickedUp -> Icons.Outlined.CheckCircle
        HistoryOutcome.Expired -> Icons.Outlined.Schedule
        HistoryOutcome.Returned -> Icons.AutoMirrored.Outlined.AssignmentReturn
    }

    PaczkofastCard(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = stringResource(R.string.open_parcel_details),
        accessibilityLabel = "$title, $outcomeLine, $dateText",
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(HistoryTileShape)
                    .background(tileFill)
                    .border(2.dp, colors.borderStrong, HistoryTileShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = outcomeLine.uppercase(),
                    style = MonoLabelLarge,
                    color = colors.monoLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = dateText.uppercase(),
                style = MonoLabelLarge,
                color = colors.monoLabel,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun HistoryParcelCardPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HistoryParcelCard(
                title = "Example Shop",
                outcomeLine = "Picked up · WAW01A",
                dateText = "4 Jul",
                outcome = HistoryOutcome.PickedUp,
                onClick = {},
            )
            HistoryParcelCard(
                title = "Example Sender sp. z o.o.",
                outcomeLine = "Expired · returned to sender",
                dateText = "28 Jun",
                outcome = HistoryOutcome.Expired,
                onClick = {},
                muted = true,
            )
            HistoryParcelCard(
                title = "Example Shop 2",
                outcomeLine = "Returned to sender",
                dateText = "24 Jun",
                outcome = HistoryOutcome.Returned,
                onClick = {},
                muted = true,
            )
        }
    }
}
