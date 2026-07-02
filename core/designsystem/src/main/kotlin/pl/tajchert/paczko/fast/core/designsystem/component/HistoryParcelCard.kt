package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Outcome of a finished parcel — drives the glyph and tint on a history row.
 */
enum class HistoryOutcome {
    /** Collected / delivered successfully. */
    PickedUp,

    /** Pickup window elapsed. */
    Expired,

    /** Sent back to the sender or otherwise not delivered. */
    Returned,
}

/**
 * A single row in the History tab: an outcome glyph, the parcel title and an
 * outcome/locker line, with the completion date and a chevron trailing.
 *
 * @param muted Renders the row on the subtle surface so older months read as
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
    val tint = when (outcome) {
        HistoryOutcome.PickedUp -> PaczkofastTheme.colors.accent
        HistoryOutcome.Expired -> PaczkofastTheme.colors.urgent
        HistoryOutcome.Returned -> PaczkofastTheme.colors.textMuted
    }
    val icon: ImageVector = when (outcome) {
        HistoryOutcome.PickedUp -> Icons.Outlined.CheckCircle
        HistoryOutcome.Expired -> Icons.Outlined.Schedule
        HistoryOutcome.Returned -> Icons.AutoMirrored.Outlined.AssignmentReturn
    }
    val surface = if (muted) {
        PaczkofastTheme.colors.cardSurfaceSubtle
    } else {
        PaczkofastTheme.colors.cardSurface
    }
    val borderColor = if (muted) {
        PaczkofastTheme.colors.cardBorderSubtle
    } else {
        PaczkofastTheme.colors.cardBorder
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(surface)
            .border(1.dp, borderColor, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                color = PaczkofastTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = outcomeLine,
                style = MaterialTheme.typography.bodySmall,
                color = PaczkofastTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall,
            color = PaczkofastTheme.colors.textMuted,
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = PaczkofastTheme.colors.textFaint,
        )
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
                title = "Zalando",
                outcomeLine = "Picked up · Locker WAW04B",
                dateText = "2 Jul, 14:32",
                outcome = HistoryOutcome.PickedUp,
                onClick = {},
            )
            HistoryParcelCard(
                title = "Marta K. · Vinted",
                outcomeLine = "Expired · returned to sender",
                dateText = "28 Jun",
                outcome = HistoryOutcome.Expired,
                onClick = {},
                muted = true,
            )
            HistoryParcelCard(
                title = "Allegro · dom-tech",
                outcomeLine = "Returned to sender",
                dateText = "24 Jun",
                outcome = HistoryOutcome.Returned,
                onClick = {},
                muted = true,
            )
        }
    }
}
