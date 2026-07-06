package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Home screen header: yellow logo mark, "Paczkofast" wordmark and an
 * optional trailing action (search, refresh, ...). When [showLogo] is
 * false, [title] is rendered with the same styling used by detail-screen
 * titles (e.g. "History").
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
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showLogo) {
                LogoMark()
            }
            Text(
                text = title,
                style = if (showLogo) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                color = PaczkofastTheme.colors.textPrimary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

/**
 * The yellow, ink-bordered logo mark with a rotated ink diamond, used as
 * the "Paczkofast" wordmark's leading tile and in the settings about row.
 */
@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    NeoSurface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        fill = PaczkofastTheme.colors.accent,
        // Yellow tile keeps an ink border + ink glyph in both themes, so it never
        // grows a light outline on dark (see PaczkofastColors.accentBorder).
        borderColor = PaczkofastTheme.colors.accentBorder,
        shadow = false,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(15.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(4.dp))
                .background(PaczkofastTheme.colors.onAccent),
        )
    }
}

/**
 * Icon button used at the trailing edge of [HomeHeader]: a neo-brutalist
 * circular chip with a hard ink border and offset shadow.
 */
@Composable
fun HeaderIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    NeoSurface(
        modifier = modifier
            .size(38.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            )
            .semantics { role = Role.Button },
        shape = CircleShape,
        fill = PaczkofastTheme.colors.headerIconBackground,
        borderColor = PaczkofastTheme.colors.borderStrong,
        borderWidth = 2.dp,
        shadowOffset = 2.dp,
        pressed = pressed && enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) PaczkofastTheme.colors.textPrimary else PaczkofastTheme.colors.textFaint,
            modifier = Modifier
                .align(Alignment.Center)
                .size(18.dp),
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

@PaczkofastPreviews
@Composable
private fun HomeHeaderHistoryPreview() {
    PaczkofastTheme {
        HomeHeader(
            title = "History",
            showLogo = false,
        )
    }
}
