package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
    // Packages icon unfolds from a closed cube (0) to an open box (1) as the tab
    // gains selection, and folds back when it loses it. Reversible spring for a
    // snappy, slightly playful neo-brutalist motion.
    val packagesOpenFraction by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "packagesOpenFraction",
    )

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
                BottomNavDestination.Parcels -> PackagesIcon(
                    openFraction = packagesOpenFraction,
                    tint = iconTint,
                )
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

/**
 * Isometric parcel box that unfolds from a closed cube ([openFraction] = 0) to
 * an open box with splayed lid flaps ([openFraction] = 1). Every vertex is
 * interpolated between its closed and open position, so intermediate frames read
 * as the box physically opening/closing. At the endpoints the strokes reproduce
 * the two design frames exactly (see [packagesIconGeometry]).
 */
@Composable
internal fun PackagesIcon(
    openFraction: Float,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val geometry = packagesIconGeometry(openFraction)
        // Geometry is authored in a 32x32 space; scale it onto the canvas.
        val scale = size.minDimension / PACKAGES_ICON_VIEWPORT
        val stroke = 2.2.dp.toPx()
        val strokeStyle = Stroke(
            width = stroke,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        fun Offset.scaled(): Offset = Offset(x * scale, y * scale)
        fun polyline(points: List<Offset>, close: Boolean): Path = Path().apply {
            points.forEachIndexed { index, point ->
                val p = point.scaled()
                if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
            if (close) close()
        }

        drawPath(polyline(geometry.body, close = true), color = tint, style = strokeStyle)
        drawPath(polyline(geometry.openingV, close = false), color = tint, style = strokeStyle)
        drawPath(polyline(geometry.leftFlap, close = false), color = tint, style = strokeStyle)
        drawPath(polyline(geometry.rightFlap, close = false), color = tint, style = strokeStyle)
        val edgeStart = geometry.centerEdge[0].scaled()
        val edgeEnd = geometry.centerEdge[1].scaled()
        drawLine(tint, edgeStart, edgeEnd, strokeWidth = stroke, cap = StrokeCap.Round)
        if (geometry.strapAlpha > 0.01f) {
            drawLine(
                color = tint.copy(alpha = tint.alpha * geometry.strapAlpha),
                start = geometry.strap[0].scaled(),
                end = geometry.strap[1].scaled(),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

private const val PACKAGES_ICON_VIEWPORT = 32f

/** Interpolated stroke geometry for [PackagesIcon] in a 32x32 space. */
internal data class PackagesIconGeometry(
    val body: List<Offset>,
    val centerEdge: List<Offset>,
    val openingV: List<Offset>,
    val leftFlap: List<Offset>,
    val rightFlap: List<Offset>,
    val strap: List<Offset>,
    val strapAlpha: Float,
)

/**
 * Vertices of the Packages icon for a given [openFraction] (0 = closed cube,
 * 1 = open box), expressed in the 32x32 design space. Each point lerps between
 * its closed and open position; at 0 the union of strokes is the exact closed
 * design frame and at 1 the exact open frame. Pure and deterministic so it can
 * be unit-tested without rendering.
 */
internal fun packagesIconGeometry(openFraction: Float): PackagesIconGeometry {
    val t = openFraction.coerceIn(0f, 1f)
    fun morph(closed: Offset, open: Offset): Offset = lerp(closed, open, t)

    // Box body: closed = lower cube hexagon + top-face V; open = open box body.
    val bTL = morph(Offset(3.5f, 9f), Offset(3.5f, 13.5f))
    val bBL = morph(Offset(3.5f, 21f), Offset(3.5f, 23f))
    val bBot = morph(Offset(16f, 27.5f), Offset(16f, 29.5f))
    val bBR = morph(Offset(28.5f, 21f), Offset(28.5f, 23f))
    val bTR = morph(Offset(28.5f, 9f), Offset(28.5f, 13.5f))
    val bTC = morph(Offset(16f, 15.5f), Offset(16f, 20f))

    // Top opening V: collapses onto the closed top-face V, rises when open.
    val o0 = morph(Offset(3.5f, 9f), Offset(3.5f, 13.5f))
    val o1 = morph(Offset(16f, 15.5f), Offset(16f, 7f))
    val o2 = morph(Offset(28.5f, 9f), Offset(28.5f, 13.5f))

    // Lid flaps: collapse onto the closed silhouette apex edges, unfold when open.
    val leftFlap = listOf(
        morph(Offset(3.5f, 9f), Offset(3.5f, 13.5f)),
        morph(Offset(3.5f, 9f), Offset(2f, 9f)),
        morph(Offset(16f, 2.5f), Offset(14.5f, 15.5f)),
        morph(Offset(16f, 2.5f), Offset(16f, 20f)),
    )
    val rightFlap = listOf(
        morph(Offset(28.5f, 9f), Offset(28.5f, 13.5f)),
        morph(Offset(28.5f, 9f), Offset(30f, 9f)),
        morph(Offset(16f, 2.5f), Offset(17.5f, 2.5f)),
        morph(Offset(16f, 2.5f), Offset(16f, 7f)),
    )

    return PackagesIconGeometry(
        body = listOf(bTL, bBL, bBot, bBR, bTR, bTC),
        centerEdge = listOf(bTC, bBot),
        openingV = listOf(o0, o1, o2),
        leftFlap = leftFlap,
        rightFlap = rightFlap,
        strap = listOf(Offset(9.5f, 6f), Offset(22f, 12.5f)),
        strapAlpha = 1f - t,
    )
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

/**
 * Line-art pair of sliders, each with a round knob offset along its track.
 * Proportions follow the design: a 7-unit knob on a 16-unit track, knobs offset
 * to opposite ends. [Stroke] is centered on the path, so the ring is drawn at
 * `outer - stroke/2` to keep the visible outer diameter at the design ratio
 * instead of bulging outward.
 */
@Composable
private fun SettingsIcon(tint: Color, knobFill: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.2.dp.toPx()
        val trackWidth = size.width * 0.72f
        val leftX = (size.width - trackWidth) / 2f
        val rightX = leftX + trackWidth
        val centerY = size.height / 2f
        val trackGapHalf = trackWidth * (3f / 16f)
        val topY = centerY - trackGapHalf
        val bottomY = centerY + trackGapHalf
        // Design knob is 7 units wide on a 16-unit track.
        val knobOuterRadius = trackWidth * (3.5f / 16f)
        val knobRingRadius = knobOuterRadius - stroke / 2f

        drawLine(tint, Offset(leftX, topY), Offset(rightX, topY), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(leftX, bottomY), Offset(rightX, bottomY), strokeWidth = stroke, cap = StrokeCap.Round)

        val topKnobCenter = Offset(leftX + trackWidth * 0.344f, topY)
        drawCircle(color = knobFill, radius = knobRingRadius, center = topKnobCenter)
        drawCircle(color = tint, radius = knobRingRadius, center = topKnobCenter, style = Stroke(width = stroke))

        val bottomKnobCenter = Offset(leftX + trackWidth * 0.656f, bottomY)
        drawCircle(color = knobFill, radius = knobRingRadius, center = bottomKnobCenter)
        drawCircle(color = tint, radius = knobRingRadius, center = bottomKnobCenter, style = Stroke(width = stroke))
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

@PaczkofastPreviews
@Composable
private fun PackagesIconUnfoldPreview() {
    PaczkofastTheme {
        Row(
            modifier = Modifier
                .background(PaczkofastTheme.colors.navBackground)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            listOf(0f, 0.5f, 1f).forEach { fraction ->
                PackagesIcon(
                    openFraction = fraction,
                    tint = PaczkofastTheme.colors.textPrimary,
                )
            }
        }
    }
}
