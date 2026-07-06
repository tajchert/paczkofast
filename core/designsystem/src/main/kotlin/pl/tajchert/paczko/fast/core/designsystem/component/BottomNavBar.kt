package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.annotation.StringRes
import pl.tajchert.paczko.fast.core.designsystem.R
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Destinations of the app's bottom navigation.
 */
enum class BottomNavDestination(
    @StringRes val labelRes: Int,
) {
    Parcels(R.string.bottom_nav_parcels),
    History(R.string.bottom_nav_history),
    Settings(R.string.bottom_nav_settings),
}

private val NavPillShape = RoundedCornerShape(10.dp)
private val NavPillSize = androidx.compose.ui.unit.DpSize(58.dp, 30.dp)

/**
 * Bottom navigation bar with the three app destinations. The selected item's
 * pill is filled with the accent color and outlined with a strong border;
 * the icons stay ink-colored line art in every state.
 */
@Composable
fun PaczkofastBottomBar(
    selected: BottomNavDestination,
    onSelect: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = PaczkofastTheme.colors.navBorder
    val topBorderWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { 2.5.dp.toPx() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup()
            .background(PaczkofastTheme.colors.navBackground)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = topBorderWidthPx,
                )
            }
            .navigationBarsPadding()
            .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 6.dp),
    ) {
        BottomNavDestination.entries.forEach { destination ->
            BottomNavItem(
                destination = destination,
                isSelected = destination == selected,
                onClick = { onSelect(destination) },
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    destination: BottomNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val label = stringResource(destination.labelRes)
    val selectedDescription = stringResource(R.string.selected)
    val notSelectedDescription = stringResource(R.string.not_selected)
    // Selected tab sits on a yellow pill → ink icon; unselected icons are line-art
    // in the primary (light-on-dark) color.
    val iconTint = if (isSelected) PaczkofastTheme.colors.onAccent else PaczkofastTheme.colors.textPrimary
    val labelColor = if (isSelected) {
        PaczkofastTheme.colors.textPrimary
    } else {
        PaczkofastTheme.colors.textMuted
    }
    val knobFill = if (isSelected) PaczkofastTheme.colors.accent else PaczkofastTheme.colors.navBackground

    Column(
        modifier = Modifier
            .weight(1f)
            .selectable(
                selected = isSelected,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics {
                contentDescription = label
                stateDescription = if (isSelected) selectedDescription else notSelectedDescription
            }
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(NavPillSize)
                .then(
                    if (isSelected) {
                        // Yellow selected pill: ink border + inset fill (no light rim on dark).
                        Modifier.neoBorderedFill(
                            shape = NavPillShape,
                            fill = PaczkofastTheme.colors.accent,
                            borderColor = PaczkofastTheme.colors.accentBorder,
                            borderWidth = 2.dp,
                        )
                    } else {
                        Modifier.clip(NavPillShape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (destination) {
                BottomNavDestination.Parcels -> PackagesIcon(tint = iconTint)
                BottomNavDestination.History -> HistoryIcon(tint = iconTint)
                BottomNavDestination.Settings -> SettingsIcon(tint = iconTint, knobFill = knobFill)
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                letterSpacing = 0.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = labelColor,
        )
    }
}

/** Line-art box/parcel outline with a small tab notch on top. */
@Composable
private fun PackagesIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.2.dp.toPx()
        val boxWidth = size.width * 0.62f
        val boxHeight = size.height * 0.55f
        val left = (size.width - boxWidth) / 2f
        val top = (size.height - boxHeight) / 2f + size.height * 0.1f

        drawRoundRect(
            color = tint,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(stroke),
            style = Stroke(width = stroke),
        )
        val tabX = left + boxWidth * 0.3f
        drawLine(
            color = tint,
            start = Offset(tabX, top - size.height * 0.14f),
            end = Offset(tabX, top),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/** Line-art circle with clock-style hands, echoing a magnifier/clock. */
@Composable
private fun HistoryIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.2.dp.toPx()
        val radius = size.minDimension * 0.32f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = tint, radius = radius, center = center, style = Stroke(width = stroke))
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.6f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.6f, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/** Line-art pair of sliders, each with a round knob offset along its track. */
@Composable
private fun SettingsIcon(tint: Color, knobFill: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.2.dp.toPx()
        val lineWidth = size.width * 0.72f
        val leftX = (size.width - lineWidth) / 2f
        val rightX = leftX + lineWidth
        val topY = size.height * 0.36f
        val bottomY = size.height * 0.64f
        val knobRadius = size.width * 0.16f

        drawLine(tint, Offset(leftX, topY), Offset(rightX, topY), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(leftX, bottomY), Offset(rightX, bottomY), strokeWidth = stroke, cap = StrokeCap.Round)

        val topKnobCenter = Offset(leftX + knobRadius * 1.4f, topY)
        drawCircle(color = knobFill, radius = knobRadius, center = topKnobCenter)
        drawCircle(color = tint, radius = knobRadius, center = topKnobCenter, style = Stroke(width = stroke))

        val bottomKnobCenter = Offset(rightX - knobRadius * 1.4f, bottomY)
        drawCircle(color = knobFill, radius = knobRadius, center = bottomKnobCenter)
        drawCircle(color = tint, radius = knobRadius, center = bottomKnobCenter, style = Stroke(width = stroke))
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastBottomBarPackagesSelectedPreview() {
    PaczkofastTheme {
        PaczkofastBottomBar(
            selected = BottomNavDestination.Parcels,
            onSelect = {},
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastBottomBarHistorySelectedPreview() {
    PaczkofastTheme {
        PaczkofastBottomBar(
            selected = BottomNavDestination.History,
            onSelect = {},
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastBottomBarSettingsSelectedPreview() {
    PaczkofastTheme {
        PaczkofastBottomBar(
            selected = BottomNavDestination.Settings,
            onSelect = {},
        )
    }
}
