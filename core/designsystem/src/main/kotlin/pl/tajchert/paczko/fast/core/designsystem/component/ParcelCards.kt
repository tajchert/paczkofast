package pl.tajchert.paczko.fast.core.designsystem.component

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabelLarge
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Prominent neo-brutalist card for a parcel that is ready for pickup —
 * sender name, mono locker line, deadline countdown, optional QR panel and
 * a primary "open" action, all on a white [NeoSurface] with an ink border
 * and hard shadow.
 *
 * @param title Sender / shop name, e.g. "Example Shop".
 * @param subtitle Locker + address line, e.g. "WAW01A · Example street 12".
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
    PaczkofastCard(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = "Open parcel details",
        accessibilityLabel = readyParcelAccessibilityLabel(
            title = title,
            subtitle = subtitle,
            deadlineText = deadlineText,
            timeLeftText = timeLeftText,
            sizeLabel = sizeLabel,
            urgent = urgent,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                    color = PaczkofastTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                sizeLabel?.let { SizeBadge(size = it, highlighted = true) }
            }
            Text(
                text = subtitle.uppercase(),
                style = MonoLabelLarge,
                color = PaczkofastTheme.colors.monoLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

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
                    accessibilityLabel = it,
                )
            }
        }
    }
}

/**
 * Compact neo-brutalist card for a ready parcel that is not the focused
 * one: title, mono locker line and a small countdown at the trailing edge.
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
    PaczkofastCard(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = "Open parcel details",
        accessibilityLabel = readyParcelAccessibilityLabel(
            title = title,
            subtitle = subtitle,
            deadlineText = null,
            timeLeftText = timeLeftText,
            sizeLabel = null,
            urgent = urgent,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = PaczkofastTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle.uppercase(),
                    style = MonoLabelLarge,
                    color = PaczkofastTheme.colors.monoLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (timeLeftText != null || progress != null) {
                val countdownColor = if (urgent) {
                    PaczkofastTheme.colors.alertText
                } else {
                    PaczkofastTheme.colors.textPrimary
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    timeLeftText?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
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
        }
    }
}

/**
 * Neo-brutalist card for a parcel still on its way: sender, size badge and
 * a segmented delivery progress bar with a mono status caption.
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
    PaczkofastCard(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = "Open parcel details",
        accessibilityLabel = transitParcelAccessibilityLabel(
            title = title,
            statusText = statusText,
            sizeLabel = sizeLabel,
            completedSegments = completedSegments,
            totalSegments = totalSegments,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = PaczkofastTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                sizeLabel?.let { SizeBadge(size = it) }
            }
            SegmentedProgressBar(
                completedSegments = completedSegments,
                totalSegments = totalSegments,
                statusLabel = "${statusText.uppercase()} · $completedSegments/$totalSegments",
            )
        }
    }
}

private fun readyParcelAccessibilityLabel(
    title: String,
    subtitle: String,
    deadlineText: String?,
    timeLeftText: String?,
    sizeLabel: String?,
    urgent: Boolean,
): String = buildList {
    add(title)
    add("Ready for pickup")
    add(subtitle)
    sizeLabel?.let { add("Size $it") }
    deadlineText?.let { add(it) }
    timeLeftText?.let { add(it) }
    if (urgent) add("Urgent")
}.joinToString(", ")

private fun transitParcelAccessibilityLabel(
    title: String,
    statusText: String,
    sizeLabel: String?,
    completedSegments: Int,
    totalSegments: Int,
): String = buildList {
    add(title)
    add(statusText)
    sizeLabel?.let { add("Size $it") }
    add("${completedSegments.coerceIn(0, totalSegments)} of $totalSegments delivery steps complete")
}.joinToString(", ")

/**
 * Deadline line + countdown + progress bar shared by ready cards and the
 * detail screen's deadline card. Thin wrapper over [DeadlineProgressBar].
 */
@Composable
internal fun DeadlineRow(
    deadlineText: String?,
    timeLeftText: String?,
    progress: Float?,
    urgent: Boolean,
    modifier: Modifier = Modifier,
) {
    DeadlineProgressBar(
        progress = progress ?: 0f,
        modifier = modifier.fillMaxWidth(),
        urgent = urgent,
        label = deadlineText,
        value = timeLeftText,
    )
}

@PaczkofastPreviews
@Composable
private fun ParcelCardsPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReadyParcelCard(
                title = "Example Shop",
                subtitle = "WAW01A · Example street 12",
                deadlineText = "Pick up by Fri 3 Jul, 12:56",
                timeLeftText = "46 h left",
                progress = 0.64f,
                sizeLabel = "M",
                actionText = "Open locker",
                onClick = {},
            )
            ReadyParcelCard(
                title = "Example Sender sp. z o.o.",
                subtitle = "WAW01A · Example street 12",
                deadlineText = "Time left",
                timeLeftText = "9 h — hurry!",
                progress = 0.06f,
                urgent = true,
                sizeLabel = "S",
                actionText = "Open locker",
                onClick = {},
            )
            CollapsedReadyParcelCard(
                title = "Example Sender sp. z o.o.",
                subtitle = "WAW01A · Example street 12",
                timeLeftText = "9 h left",
                progress = 0.18f,
                urgent = true,
                onClick = {},
            )
            TransitParcelCard(
                title = "Example Shop",
                statusText = "Out for delivery",
                sizeLabel = "L",
                completedSegments = 3,
                onClick = {},
            )
        }
    }
}
