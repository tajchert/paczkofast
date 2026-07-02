package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * In-app numeric keypad used by the login flow, matching the design's
 * dark keypad: 3-column grid of 52dp keys with a backspace key in the
 * bottom-right corner and an empty slot bottom-left.
 */
@Composable
fun NumericKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeyGap),
    ) {
        listOf("123", "456", "789").forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KeyGap),
            ) {
                row.forEach { digit ->
                    DigitKey(digit = digit, onClick = { onDigit(digit) }, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeyGap),
        ) {
            Box(modifier = Modifier.weight(1f).height(KeyHeight))
            DigitKey(digit = '0', onClick = { onDigit('0') }, modifier = Modifier.weight(1f))
            BackspaceKey(onClick = onBackspace, modifier = Modifier.weight(1f))
        }
    }
}

private val KeyGap = 7.dp
private val KeyHeight = 52.dp
private val KeyShape = RoundedCornerShape(12.dp)

@Composable
private fun DigitKey(
    digit: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(KeyHeight)
            .clip(KeyShape)
            .background(PaczkofastTheme.colors.cardSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 21.sp, lineHeight = 26.sp),
            color = PaczkofastTheme.colors.textPrimary,
        )
    }
}

@Composable
private fun BackspaceKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(KeyHeight)
            .clip(KeyShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Backspace" },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = backspacePainter(PaczkofastTheme.colors.textMuted),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** Outlined back-delete glyph from the design (keyboard shape with an ×). */
@Composable
private fun backspacePainter(tint: Color): Painter {
    val vector = remember(tint) { backspaceVector(tint) }
    return rememberVectorPainter(vector)
}

private fun backspaceVector(tint: Color): ImageVector =
    ImageVector.Builder(
        name = "Backspace",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(tint),
            strokeLineWidth = 1.7f,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(8.5f, 5.5f)
            lineTo(3.5f, 12f)
            lineTo(8.5f, 18.5f)
            lineTo(20f, 18.5f)
            lineTo(20f, 5.5f)
            close()
        }
        path(
            stroke = SolidColor(tint),
            strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(11.5f, 9.5f)
            lineTo(16.5f, 14.5f)
            moveTo(16.5f, 9.5f)
            lineTo(11.5f, 14.5f)
        }
    }.build()

@PaczkofastPreviews
@Composable
private fun NumericKeypadPreview() {
    PaczkofastTheme {
        NumericKeypad(
            onDigit = {},
            onBackspace = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
