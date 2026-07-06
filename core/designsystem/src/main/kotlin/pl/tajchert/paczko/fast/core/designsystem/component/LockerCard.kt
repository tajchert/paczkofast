package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.R
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Locker information card on the parcel detail screen: mono locker id
 * label, bold address, an optional note and a "Navigate" action — on a
 * white [NeoSurface] with an ink border and hard shadow.
 */
@Composable
fun LockerCard(
    lockerName: String,
    address: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    navigateText: String? = null,
    onNavigate: (() -> Unit)? = null,
) {
    val resolvedNavigateText = navigateText ?: stringResource(R.string.navigate)
    PaczkofastCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = lockerName.uppercase(),
                    style = MonoLabel,
                    color = PaczkofastTheme.colors.monoLabel,
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = PaczkofastTheme.colors.textPrimary,
                )
                note?.let {
                    Text(
                        text = it.uppercase(),
                        style = MonoLabel,
                        color = PaczkofastTheme.colors.monoLabel,
                    )
                }
            }
            onNavigate?.let {
                OutlinedActionButton(text = resolvedNavigateText, onClick = it)
            }
        }
    }
}

@PaczkofastPreviews
@Composable
private fun LockerCardPreview() {
    PaczkofastTheme {
        LockerCard(
            lockerName = "Locker WAW01A",
            address = "Example street 12, 00-000 Example City",
            note = "Open 24/7 · 350 m away",
            onNavigate = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
