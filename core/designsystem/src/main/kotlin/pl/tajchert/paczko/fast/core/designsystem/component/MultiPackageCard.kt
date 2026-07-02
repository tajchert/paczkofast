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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/** One parcel inside a [MultiPackageCard] / multi-package pickup. */
@Immutable
data class MultiPackageMember(
    val title: String,
    val sizeLabel: String? = null,
)

/**
 * Blue-accented card for 2+ parcels sharing one locker compartment (design 3a):
 * a "N parcels · one box" header, the member list, a deadline countdown and a
 * single "Open box · N parcels" action.
 */
@Composable
fun MultiPackageCard(
    title: String,
    subtitle: String,
    members: List<MultiPackageMember>,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    deadlineText: String? = null,
    timeLeftText: String? = null,
    progress: Float? = null,
    urgent: Boolean = false,
    actionInProgress: Boolean = false,
) {
    val info = PaczkofastTheme.colors.infoAccent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PaczkofastTheme.colors.infoSurface)
            .border(1.dp, PaczkofastTheme.colors.infoBorder, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = info,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "${members.size} parcels · one box".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = info,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(info.copy(alpha = 0.16f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "×${members.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = info,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(PaczkofastTheme.colors.background)
                .border(1.dp, info.copy(alpha = 0.14f), RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            members.forEach { member ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = info,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = member.title,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.5.sp),
                        color = PaczkofastTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    member.sizeLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PaczkofastTheme.colors.textMuted,
                        )
                    }
                }
            }
        }

        if (deadlineText != null || timeLeftText != null || progress != null) {
            DeadlineRow(
                deadlineText = deadlineText,
                timeLeftText = timeLeftText,
                progress = progress,
                urgent = urgent,
            )
        }

        PrimaryActionButton(
            text = "Open box · ${members.size} parcels",
            onClick = onActionClick,
            isLoading = actionInProgress,
        )
    }
}

@PaczkofastPreviews
@Composable
private fun MultiPackageCardPreview() {
    PaczkofastTheme {
        MultiPackageCard(
            title = "Zalando + MediaExpert",
            subtitle = "Locker WAW04B · Górczewska 12",
            members = listOf(
                MultiPackageMember("Zalando", "M"),
                MultiPackageMember("MediaExpert", "S"),
            ),
            deadlineText = "Pick up by Fri 3 Jul, 12:56",
            timeLeftText = "46 h left",
            progress = 0.64f,
            onClick = {},
            onActionClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
