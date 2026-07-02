package pl.tajchert.paczko.fast.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * White panel with the pickup QR code and the numeric open code beneath it
 * ("CODE 828 316"). Stays high-contrast in both themes so locker scanners
 * can read it.
 *
 * @param payload Content encoded into the QR code.
 * @param code Human-readable open code; shown grouped in threes when it's
 *   a plain digit string.
 */
@Composable
fun QrPanel(
    payload: String,
    modifier: Modifier = Modifier,
    code: String? = null,
    qrSize: Int = 168,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(PaczkofastTheme.colors.qrPanelBackground)
            .border(1.dp, PaczkofastTheme.colors.qrPanelBorder, MaterialTheme.shapes.medium)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        QrCodeImage(
            payload = payload,
            modifier = Modifier.size(qrSize.dp),
        )
        code?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = PaczkofastTheme.colors.qrLabel,
                )
                Text(
                    text = formatOpenCode(it),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 19.sp,
                        letterSpacing = 3.sp,
                    ),
                    color = PaczkofastTheme.colors.qrInk,
                )
            }
        }
    }
}

/**
 * Groups plain digit codes in threes ("000000" → "828 316");
 * anything else is returned unchanged.
 */
fun formatOpenCode(code: String): String =
    if (code.all(Char::isDigit)) code.chunked(3).joinToString(" ") else code

@PaczkofastPreviews
@Composable
private fun QrPanelPreview() {
    PaczkofastTheme {
        QrPanel(
            payload = "P|000000|000000000000000000000000",
            code = "000000",
            modifier = Modifier.padding(16.dp),
        )
    }
}
