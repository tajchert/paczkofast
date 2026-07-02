package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Locker information card on the parcel detail screen: locker id label,
 * address, opening/distance note and a "Navigate" action.
 */
@Composable
fun LockerCard(
    lockerName: String,
    address: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    navigateText: String = "Navigate",
    onNavigate: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PaczkofastTheme.colors.cardSurface)
            .border(1.dp, PaczkofastTheme.colors.cardBorder, MaterialTheme.shapes.large)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = lockerName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = PaczkofastTheme.colors.textMuted,
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = PaczkofastTheme.colors.textPrimary,
            )
            note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textMuted,
                )
            }
        }
        onNavigate?.let {
            OutlinedActionButton(text = navigateText, onClick = it)
        }
    }
}

@PaczkofastPreviews
@Composable
private fun LockerCardPreview() {
    PaczkofastTheme {
        LockerCard(
            lockerName = "Locker WAW04B",
            address = "Górczewska 12, 01-138 Warszawa",
            note = "Open 24/7 · 350 m away",
            onNavigate = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
