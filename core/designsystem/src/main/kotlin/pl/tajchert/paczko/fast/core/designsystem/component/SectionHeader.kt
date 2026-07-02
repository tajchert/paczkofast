package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Uppercase section label with a count badge, e.g. "READY FOR PICKUP (2)".
 *
 * @param highlighted When true the badge uses the amber accent
 *   (ready-for-pickup section); otherwise a neutral badge (on-the-way section).
 */
@Composable
fun SectionHeader(
    label: String,
    count: Int,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = PaczkofastTheme.colors.textMuted,
        )
        CountBadge(count = count, highlighted = highlighted)
    }
}

/**
 * Small pill badge showing a count.
 */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    val background = if (highlighted) {
        PaczkofastTheme.colors.accent
    } else {
        PaczkofastTheme.colors.badgeBackground
    }
    val contentColor = if (highlighted) {
        PaczkofastTheme.colors.onAccent
    } else {
        PaczkofastTheme.colors.badgeContent
    }
    Box(
        modifier = modifier
            .widthIn(min = 20.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun SectionHeaderPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(label = "Ready for pickup", count = 2, highlighted = true)
            SectionHeader(label = "On the way", count = 2)
        }
    }
}
