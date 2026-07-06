package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val StatusChipShape = RoundedCornerShape(7.dp)

/** Visual treatment for [StatusChip]. */
enum class StatusChipStyle {
    /** Yellow fill, ink text, e.g. "READY TO PICKUP". */
    Accent,

    /** Inverted ink fill, yellow text, e.g. "DELIVERED". */
    Ink,

    /** Neutral white/card fill, ink text, e.g. "ECONOMY", "SIZE M". */
    Neutral,
}

private data class StatusChipColors(val fill: Color, val content: Color, val border: Color)

@Composable
private fun colorsFor(style: StatusChipStyle): StatusChipColors = when (style) {
    // Yellow fill keeps an ink border in both themes (see PaczkofastColors.accentBorder).
    StatusChipStyle.Accent -> StatusChipColors(
        fill = PaczkofastTheme.colors.accent,
        content = PaczkofastTheme.colors.onAccent,
        border = PaczkofastTheme.colors.accentBorder,
    )
    StatusChipStyle.Ink -> StatusChipColors(
        fill = PaczkofastTheme.colors.borderStrong,
        content = PaczkofastTheme.colors.accent,
        border = PaczkofastTheme.colors.borderStrong,
    )
    StatusChipStyle.Neutral -> StatusChipColors(
        fill = PaczkofastTheme.colors.cardSurface,
        content = PaczkofastTheme.colors.textPrimary,
        border = PaczkofastTheme.colors.borderStrong,
    )
}

/**
 * Uppercase mono status pill, e.g. "READY TO PICKUP" / "DELIVERED" at the top
 * of the parcel detail screen. See [StatusChipStyle] for the available fills.
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    style: StatusChipStyle = StatusChipStyle.Accent,
    textStyle: TextStyle = MonoLabel,
) {
    val colors = colorsFor(style)
    Box(
        modifier = modifier
            .neoBorderedFill(StatusChipShape, colors.fill, colors.border, 2.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = textStyle,
            color = colors.content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * Neutral companion to [StatusChip] — a white/card-fill uppercase label with
 * an ink border, e.g. the "SIZE M" chip beside the status on the detail
 * screen. Equivalent to `StatusChip(text, modifier, StatusChipStyle.Neutral)`.
 */
@Composable
fun OutlinedStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MonoLabel,
) {
    StatusChip(text = text, modifier = modifier, style = StatusChipStyle.Neutral, textStyle = textStyle)
}

@PaczkofastPreviews
@Composable
private fun StatusChipPreview() {
    PaczkofastTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            StatusChip(text = "Ready to pickup", style = StatusChipStyle.Accent)
            StatusChip(text = "Delivered", style = StatusChipStyle.Ink)
            StatusChip(text = "Economy", style = StatusChipStyle.Neutral)
        }
    }
}

@PaczkofastPreviews
@Composable
private fun OutlinedStatusChipPreview() {
    PaczkofastTheme {
        OutlinedStatusChip(text = "Size M", modifier = Modifier.padding(16.dp))
    }
}
