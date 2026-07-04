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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

private val SizeBadgeShape = RoundedCornerShape(7.dp)

/**
 * Small mono pill showing the parcel size letter (XS / S / M / L), e.g. on
 * parcel cards and the parcel detail screen.
 *
 * By default it renders as an outline pill (transparent/white fill). Set
 * [highlighted] to render it filled with the brand accent (yellow), as seen
 * on "ready for pickup" cards in the mocks.
 */
@Composable
fun SizeBadge(
    size: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val borderColor = PaczkofastTheme.colors.sizeBadgeBorder
    val fill = if (highlighted) PaczkofastTheme.colors.accent else Color.Transparent
    val content = if (highlighted) PaczkofastTheme.colors.onAccent else PaczkofastTheme.colors.sizeBadgeContent
    Box(
        modifier = modifier
            .clip(SizeBadgeShape)
            .background(fill)
            .border(width = 2.dp, color = borderColor, shape = SizeBadgeShape),
    ) {
        Text(
            text = size.uppercase(),
            style = MonoLabel.copy(fontSize = 13.sp, lineHeight = 16.sp),
            color = content,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun SizeBadgePreview() {
    PaczkofastTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            SizeBadge(size = "XS")
            SizeBadge(size = "S")
            SizeBadge(size = "M")
            SizeBadge(size = "L", highlighted = true)
        }
    }
}
