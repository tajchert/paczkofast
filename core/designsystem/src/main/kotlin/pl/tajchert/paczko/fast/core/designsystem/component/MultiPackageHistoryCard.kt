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
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabelLarge
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val HistoryTileShape = RoundedCornerShape(8.dp)

/**
 * History-tab row for a collected multi-package box: a yellow box-icon
 * tile, a "N parcels · one box" header with a "×N" mono badge, the
 * outcome/locker line and date, plus a member sub-panel. Tapping opens the
 * box detail screen.
 */
@Composable
fun MultiPackageHistoryCard(
    count: Int,
    outcomeLine: String,
    dateText: String,
    members: List<MultiPackageMember>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    PaczkofastCard(
        modifier = modifier,
        onClick = onClick,
        onClickLabel = "Open shared box details",
        accessibilityLabel = buildList {
            add("$count parcels in one box")
            add(outcomeLine)
            add(dateText)
            val memberSummary = members.joinToString { member ->
                listOfNotNull(member.title, member.sizeLabel?.let { "size $it" }).joinToString(", ")
            }
            if (memberSummary.isNotBlank()) add(memberSummary)
        }.joinToString(", "),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(HistoryTileShape)
                        .background(colors.accent)
                        .border(2.dp, colors.accentBorder, HistoryTileShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "$count parcels · one box",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        CountBadge(count = count)
                    }
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
            MemberSubPanel(members = members)
        }
    }
}

@PaczkofastPreviews
@Composable
private fun MultiPackageHistoryCardPreview() {
    PaczkofastTheme {
        MultiPackageHistoryCard(
            count = 2,
            outcomeLine = "Picked up · WAW01A",
            dateText = "3 Jul",
            members = listOf(
                MultiPackageMember("Example Shop", "M"),
                MultiPackageMember("Example Sender sp. z o.o.", "S"),
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
