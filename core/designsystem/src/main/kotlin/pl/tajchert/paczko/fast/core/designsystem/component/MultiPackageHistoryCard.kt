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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * History-tab row for a collected multi-package box (design 6a): a blue box-icon
 * header ("N parcels · one box" + ×N), the outcome/locker line and date, plus a
 * member sublist. Tapping opens the box detail (7a).
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
    val info = PaczkofastTheme.colors.infoAccent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PaczkofastTheme.colors.cardSurface)
            .border(1.dp, PaczkofastTheme.colors.infoBorder, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(info.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = info,
                    modifier = Modifier.size(16.dp),
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
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                        color = PaczkofastTheme.colors.textPrimary,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(info.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "×$count",
                            style = MaterialTheme.typography.labelMedium,
                            color = info,
                        )
                    }
                }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(PaczkofastTheme.colors.cardSurfaceSubtle)
                .border(1.dp, info.copy(alpha = 0.12f), RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp),
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
    }
}

@PaczkofastPreviews
@Composable
private fun MultiPackageHistoryCardPreview() {
    PaczkofastTheme {
        MultiPackageHistoryCard(
            count = 2,
            outcomeLine = "Picked up · Locker WAW04B",
            dateText = "3 Jul, 09:14",
            members = listOf(
                MultiPackageMember("Zalando", "M"),
                MultiPackageMember("MediaExpert", "S"),
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
