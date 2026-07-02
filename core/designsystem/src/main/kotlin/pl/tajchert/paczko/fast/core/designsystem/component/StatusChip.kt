package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
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
