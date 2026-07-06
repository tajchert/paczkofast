package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * When pressed, the content slides toward the shadow (which always stays fully drawn at
 * [offset]), leaving a 1dp visible sliver. Idle content stays at the origin (no translation).
 */
internal fun pressTranslation(pressed: Boolean, offset: Dp): Dp =
    if (pressed) (offset - 1.dp).coerceAtLeast(0.dp) else 0.dp

/** Fills [outline] with [color] in the current draw scope. */
private fun DrawScope.fillOutline(outline: Outline, color: Color) {
    when (outline) {
        is Outline.Rounded -> drawPath(Path().apply { addRoundRect(outline.roundRect) }, color)
        is Outline.Rectangle -> drawRect(color, topLeft = outline.rect.topLeft, size = outline.rect.size)
        is Outline.Generic -> drawPath(outline.path, color)
    }
}

/** Draws a solid (un-blurred) copy of [shape] offset behind the content. */
fun Modifier.hardShadow(
    offsetX: Dp = 3.dp,
    offsetY: Dp = 3.dp,
    color: Color,
    shape: Shape,
): Modifier = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(offsetX.toPx(), offsetY.toPx()) { fillOutline(outline, color) }
}

/**
 * Clips to [shape], fills it **inset by [borderWidth]**, then strokes the border.
 *
 * Insetting the fill keeps its antialiased edge from bleeding past the border's own
 * (transparent) outer AA edge — which otherwise leaves a faint 1px fill-colored rim on
 * high-contrast surfaces (e.g. a yellow tile on the dark theme). The inset also shrinks the
 * corner radii by [borderWidth] so the fill meets the border's inner edge exactly (no
 * corner gap). The visible fill is unchanged. A `Color.Transparent` [fill] draws only the
 * border.
 */
fun Modifier.neoBorderedFill(
    shape: Shape,
    fill: Color,
    borderColor: Color,
    borderWidth: Dp = 2.dp,
): Modifier = this
    .clip(shape)
    .drawBehind { drawInsetFill(shape, fill, borderWidth.toPx()) }
    .border(borderWidth, borderColor, shape)

/**
 * Fills [shape] inset by [bw] px on every side, shrinking corner radii by [bw] too so the
 * fill lands flush against a same-width border's inner edge — matching curvature, no gap.
 */
private fun DrawScope.drawInsetFill(shape: Shape, fill: Color, bw: Float) {
    if (fill == Color.Transparent) return
    if (bw <= 0f || size.minDimension <= 2f * bw) {
        fillOutline(shape.createOutline(size, layoutDirection, this), fill)
        return
    }
    when (val outline = shape.createOutline(size, layoutDirection, this)) {
        is Outline.Rounded -> {
            val rr = outline.roundRect
            fun shrink(c: CornerRadius) =
                CornerRadius((c.x - bw).coerceAtLeast(0f), (c.y - bw).coerceAtLeast(0f))
            val inner = RoundRect(
                left = rr.left + bw,
                top = rr.top + bw,
                right = rr.right - bw,
                bottom = rr.bottom - bw,
                topLeftCornerRadius = shrink(rr.topLeftCornerRadius),
                topRightCornerRadius = shrink(rr.topRightCornerRadius),
                bottomRightCornerRadius = shrink(rr.bottomRightCornerRadius),
                bottomLeftCornerRadius = shrink(rr.bottomLeftCornerRadius),
            )
            drawPath(Path().apply { addRoundRect(inner) }, fill)
        }
        is Outline.Rectangle ->
            drawRect(fill, topLeft = Offset(bw, bw), size = Size(size.width - 2f * bw, size.height - 2f * bw))
        is Outline.Generic -> {
            val innerOutline = shape.createOutline(Size(size.width - 2f * bw, size.height - 2f * bw), layoutDirection, this)
            translate(bw, bw) { fillOutline(innerOutline, fill) }
        }
    }
}

/** Base neo-brutalist surface: fill + ink border + hard offset shadow. */
@Composable
fun NeoSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    fill: Color = PaczkofastTheme.colors.cardSurface,
    borderWidth: Dp = 2.5.dp,
    borderColor: Color = PaczkofastTheme.colors.borderStrong,
    shadow: Boolean = true,
    shadowOffset: Dp = 3.dp,
    pressed: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val shift = pressTranslation(pressed, shadowOffset)
    Box(
        modifier = modifier
            .then(if (shadow) Modifier.hardShadow(shadowOffset, shadowOffset, PaczkofastTheme.colors.hardShadow, shape) else Modifier)
            .offset(x = shift, y = shift)
            .neoBorderedFill(shape, fill, borderColor, borderWidth),
        content = content,
    )
}

@PaczkofastPreviews
@Composable
private fun NeoSurfaceIdlePreview() {
    PaczkofastTheme {
        NeoSurface(modifier = Modifier.size(160.dp, 80.dp)) {
            Text(
                text = "Idle",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@PaczkofastPreviews
@Composable
private fun NeoSurfacePressedPreview() {
    PaczkofastTheme {
        NeoSurface(modifier = Modifier.size(160.dp, 80.dp), pressed = true) {
            Text(
                text = "Pressed",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}
