package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Square outlined badge showing the parcel size letter (S / M / L)
 * in the top-right corner of parcel cards.
 */
@Composable
fun SizeBadge(
    size: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .border(
                width = 1.dp,
                color = PaczkofastTheme.colors.sizeBadgeBorder,
                shape = MaterialTheme.shapes.small,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = size,
            style = MaterialTheme.typography.labelMedium,
            color = PaczkofastTheme.colors.sizeBadgeContent,
        )
    }
}

@PaczkofastPreviews
@Composable
private fun SizeBadgePreview() {
    PaczkofastTheme {
        SizeBadge(size = "M", modifier = Modifier.padding(16.dp))
    }
}
