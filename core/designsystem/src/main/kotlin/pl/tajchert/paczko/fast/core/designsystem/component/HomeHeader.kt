package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Home screen header: amber logo mark, "Paczkofast" wordmark and an
 * optional trailing circular action (search, refresh, ...).
 */
@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    title: String = "Paczkofast",
    showLogo: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showLogo) {
                LogoMark()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = PaczkofastTheme.colors.textPrimary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/**
 * The amber square logo mark with the horizontal locker-slot bar.
 */
@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PaczkofastTheme.colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(PaczkofastTheme.colors.onAccent),
        )
    }
}

/**
 * Circular icon button used at the trailing edge of [HomeHeader].
 */
@Composable
fun HeaderIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(PaczkofastTheme.colors.headerIconBackground),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = PaczkofastTheme.colors.textMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun HomeHeaderPreview() {
    PaczkofastTheme {
        HomeHeader(
            actions = {
                HeaderIconButton(
                    onClick = {},
                    icon = Icons.Default.Refresh,
                    contentDescription = "Refresh parcels",
                )
            },
        )
    }
}
