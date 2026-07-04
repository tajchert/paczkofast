package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/** One parcel inside a [MultiPackageCard] / multi-package pickup. */
@Immutable
data class MultiPackageMember(
    val title: String,
    val sizeLabel: String? = null,
)

/**
 * Neo-brutalist card for 2+ parcels sharing one locker compartment: a
 * "N parcels · one box" header with a yellow "×N" mono badge, the member
 * list nested in a cream sub-panel, a deadline countdown and a single
 * "Open box · N parcels" action.
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
    val colors = PaczkofastTheme.colors
    PaczkofastCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "${members.size} parcels · one box".uppercase(),
                    style = MonoLabel,
                    color = colors.monoLabel,
                    modifier = Modifier.weight(1f),
                )
                CountBadge(count = members.size)
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle.uppercase(),
                    style = MonoLabel,
                    color = colors.monoLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            MemberSubPanel(members = members)

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
}

/** Small yellow mono "×N" pill used on multi-package cards/rows. */
@Composable
internal fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    val shape = RoundedCornerShape(6.dp)
    Text(
        text = "×$count",
        style = MonoLabel,
        color = colors.onAccent,
        modifier = modifier
            .clip(shape)
            .background(colors.accent)
            .border(2.dp, colors.borderStrong, shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

/** Nested cream sub-panel listing each member of a multi-package box. */
@Composable
internal fun MemberSubPanel(members: List<MultiPackageMember>, modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    val shape = RoundedCornerShape(11.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.background)
            .border(2.dp, colors.borderStrong, shape)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        members.forEach { member ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = member.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                member.sizeLabel?.let {
                    Text(
                        text = it.uppercase(),
                        style = MonoLabel,
                        color = colors.monoLabel,
                    )
                }
            }
        }
    }
}

@PaczkofastPreviews
@Composable
private fun MultiPackageCardPreview() {
    PaczkofastTheme {
        MultiPackageCard(
            title = "Example Shop + Example Sender sp. z o.o.",
            subtitle = "WAW01A · Example street 12",
            members = listOf(
                MultiPackageMember("Example Shop", "M"),
                MultiPackageMember("Example Sender sp. z o.o.", "S"),
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
