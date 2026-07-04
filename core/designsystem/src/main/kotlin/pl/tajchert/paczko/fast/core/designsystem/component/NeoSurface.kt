package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/** When pressed, a surface collapses toward its shadow by [offset]. */
internal fun pressTranslation(pressed: Boolean, offset: Dp): Dp =
    if (pressed) offset else 0.dp

/** Draws a solid (un-blurred) copy of [shape] offset behind the content. */
fun Modifier.hardShadow(
    offsetX: Dp = 3.dp,
    offsetY: Dp = 3.dp,
    color: Color,
    shape: Shape,
): Modifier = this.drawBehind {
    val ox = offsetX.toPx()
    val oy = offsetY.toPx()
    val outline = shape.createOutline(size, layoutDirection, this)
    translate(ox, oy) {
        when (outline) {
            is Outline.Rounded -> drawPath(Path().apply { addRoundRect(outline.roundRect) }, color)
            is Outline.Rectangle -> drawRect(color, topLeft = outline.rect.topLeft, size = outline.rect.size)
            is Outline.Generic -> drawPath(outline.path, color)
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
    val effectiveOffset = if (pressed) 1.dp else shadowOffset
    val shift = pressTranslation(pressed, shadowOffset)
    Box(
        modifier = modifier
            .then(if (shadow) Modifier.hardShadow(effectiveOffset, effectiveOffset, PaczkofastTheme.colors.hardShadow, shape) else Modifier)
            .offset(x = shift, y = shift)
            .clip(shape)
            .background(fill)
            .border(borderWidth, borderColor, shape),
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
