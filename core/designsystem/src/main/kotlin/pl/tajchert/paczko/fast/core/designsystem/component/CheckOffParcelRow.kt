package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val CheckOffParcelRowShape = RoundedCornerShape(16.dp)
private val CheckTileShape = RoundedCornerShape(9.dp)

/**
 * Multi-package check-off row shown after opening a shared box: sender name,
 * a mono status line ("TAKEN" / "STILL IN THE BOX?"), a size badge, and a
 * tappable check tile that toggles [checked] via [onToggle].
 */
@Composable
fun CheckOffParcelRow(
    sender: String,
    size: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PaczkofastTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle,
            ),
        shape = CheckOffParcelRowShape,
        fill = colors.cardSurface,
        borderColor = colors.borderStrong,
        pressed = pressed,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CheckTile(checked = checked)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sender,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.textPrimary,
                )
                Text(
                    text = if (checked) "TAKEN" else "STILL IN THE BOX?",
                    style = MonoLabel,
                    color = colors.monoLabel,
                )
            }
            SizeBadge(size = size)
        }
    }
}

/** 28dp rounded-square tile — yellow with an ink check glyph when checked, cream when not. */
@Composable
private fun CheckTile(checked: Boolean, modifier: Modifier = Modifier) {
    val colors = PaczkofastTheme.colors
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CheckTileShape)
            .background(if (checked) colors.accent else colors.background)
            .border(width = 2.5.dp, color = colors.borderStrong, shape = CheckTileShape),
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val stroke = 2.5.dp.toPx()
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawLine(
                    color = colors.borderStrong,
                    start = Offset(cx - size.width * 0.18f, cy),
                    end = Offset(cx - size.width * 0.03f, cy + size.height * 0.15f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = colors.borderStrong,
                    start = Offset(cx - size.width * 0.03f, cy + size.height * 0.15f),
                    end = Offset(cx + size.width * 0.2f, cy - size.height * 0.18f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@PaczkofastPreviews
@Composable
private fun CheckOffParcelRowCheckedPreview() {
    PaczkofastTheme {
        CheckOffParcelRow(
            sender = "Example Sender sp. z o.o.",
            size = "S",
            checked = true,
            onToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun CheckOffParcelRowUncheckedPreview() {
    PaczkofastTheme {
        CheckOffParcelRow(
            sender = "Example Sender sp. z o.o.",
            size = "M",
            checked = false,
            onToggle = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
