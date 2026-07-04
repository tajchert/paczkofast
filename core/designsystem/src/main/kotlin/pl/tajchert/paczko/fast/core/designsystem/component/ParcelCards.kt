package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Prominent card for a parcel that is ready for pickup — expanded variant
 * with deadline countdown, optional QR panel and primary action.
 *
 * @param title Sender / shop name, e.g. "Zalando".
 * @param subtitle Locker + address line.
 * @param deadlineText "Pick up by Fri 3 Jul, 12:56" (hidden when null).
 * @param timeLeftText "46 h left" (hidden when null).
 * @param progress Fraction of pickup time remaining, 0f..1f.
 * @param urgent Highlights the countdown in the urgent color.
 * @param sizeLabel Parcel size letter for the corner badge (hidden when null).
 * @param qrContent Optional slot rendering the QR panel inside the card.
 * @param actionText Label of the primary action button (hidden when null).
 */
@Composable
fun ReadyParcelCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    deadlineText: String? = null,
    timeLeftText: String? = null,
    progress: Float? = null,
    urgent: Boolean = false,
    sizeLabel: String? = null,
    qrContent: (@Composable () -> Unit)? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
    actionInProgress: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PaczkofastTheme.colors.cardSurface)
            .border(1.dp, PaczkofastTheme.colors.cardBorder, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
                    color = PaczkofastTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            sizeLabel?.let { SizeBadge(size = it) }
        }

        if (deadlineText != null || timeLeftText != null || progress != null) {
            DeadlineRow(
                deadlineText = deadlineText,
                timeLeftText = timeLeftText,
                progress = progress,
                urgent = urgent,
            )
        }

        qrContent?.invoke()

        actionText?.let {
            PrimaryActionButton(
                text = it,
                onClick = onActionClick,
                isLoading = actionInProgress,
            )
        }
    }
}

/**
 * Compact card for a ready parcel that is not the focused one: title,
 * locker line and a small countdown at the trailing edge.
 */
@Composable
fun CollapsedReadyParcelCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    timeLeftText: String? = null,
    progress: Float? = null,
    urgent: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PaczkofastTheme.colors.cardSurface)
            .border(1.dp, PaczkofastTheme.colors.cardBorder, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.5.sp),
                color = PaczkofastTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PaczkofastTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (timeLeftText != null || progress != null) {
            val countdownColor = if (urgent) {
                PaczkofastTheme.colors.urgent
            } else {
                PaczkofastTheme.colors.accentText
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                timeLeftText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = countdownColor,
                    )
                }
                progress?.let {
                    DeadlineProgressBar(
                        progress = it,
                        urgent = urgent,
                        modifier = Modifier.width(56.dp),
                    )
                }
            }
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = PaczkofastTheme.colors.textFaint,
        )
    }
}

/**
 * Subtle card for a parcel still on its way: sender, status line and a
 * segmented delivery progress bar.
 */
@Composable
fun TransitParcelCard(
    title: String,
    statusText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeLabel: String? = null,
    completedSegments: Int = 0,
    totalSegments: Int = 4,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PaczkofastTheme.colors.cardSurfaceSubtle)
            .border(1.dp, PaczkofastTheme.colors.cardBorderSubtle, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    color = PaczkofastTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            sizeLabel?.let { SizeBadge(size = it) }
        }
        SegmentedProgressBar(
            completedSegments = completedSegments,
            totalSegments = totalSegments,
        )
    }
}

/**
 * Deadline line + countdown + progress bar shared by ready cards and the
 * detail screen's deadline card.
 */
@Composable
internal fun DeadlineRow(
    deadlineText: String?,
    timeLeftText: String?,
    progress: Float?,
    urgent: Boolean,
    modifier: Modifier = Modifier,
) {
    val countdownColor = if (urgent) {
        PaczkofastTheme.colors.urgent
    } else {
        PaczkofastTheme.colors.accentText
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = deadlineText.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = PaczkofastTheme.colors.textPrimary,
            )
            timeLeftText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = countdownColor,
                )
            }
        }
        progress?.let {
            DeadlineProgressBar(
                progress = it,
                urgent = urgent,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun ParcelCardsPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ReadyParcelCard(
                title = "Zalando",
                subtitle = "Locker WAW04B · Górczewska 12 · 350 m",
                deadlineText = "Pick up by Fri 3 Jul, 12:56",
                timeLeftText = "46 h left",
                progress = 0.64f,
                sizeLabel = "M",
                actionText = "Open box remotely",
                onClick = {},
            )
            CollapsedReadyParcelCard(
                title = "Marta K. · Vinted",
                subtitle = "Locker WAW04B · Górczewska 12",
                timeLeftText = "9 h left",
                progress = 0.18f,
                urgent = true,
                onClick = {},
            )
            TransitParcelCard(
                title = "MediaExpert",
                statusText = "In transit · arriving tomorrow",
                sizeLabel = "L",
                completedSegments = 2,
                onClick = {},
            )
        }
    }
}
