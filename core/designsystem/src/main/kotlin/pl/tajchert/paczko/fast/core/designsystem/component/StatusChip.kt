package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Uppercase status chip on translucent amber, e.g. "READY FOR PICKUP"
 * at the top of the parcel detail screen.
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(PaczkofastTheme.colors.statusChipBackground),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = PaczkofastTheme.colors.statusChipContent,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
        )
    }
}

/**
 * Outlined companion to [StatusChip] — an uppercase label inside a hairline
 * border, e.g. the "SIZE M" chip beside the status on the detail screen.
 */
@Composable
fun OutlinedStatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .border(
                width = 1.dp,
                color = PaczkofastTheme.colors.sizeBadgeBorder,
                shape = MaterialTheme.shapes.extraSmall,
            ),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = PaczkofastTheme.colors.sizeBadgeContent,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun StatusChipPreview() {
    PaczkofastTheme {
        StatusChip(
            text = "Ready for pickup",
            modifier = Modifier.padding(16.dp),
        )
    }
}
