package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Primary amber action button, e.g. "Open box remotely".
 * Full width, 50dp tall, Space Grotesk label.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = PaczkofastTheme.colors.accent,
            contentColor = PaczkofastTheme.colors.onAccent,
            disabledContainerColor = PaczkofastTheme.colors.accentDisabled,
            disabledContentColor = PaczkofastTheme.colors.onAccentDisabled,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = PaczkofastTheme.colors.onAccentDisabled,
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * Secondary outlined action button, e.g. "Navigate" on the locker card.
 * Full width, 42dp tall, subtle amber-tinted border.
 */
@Composable
fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, PaczkofastTheme.colors.outlineButtonBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PaczkofastTheme.colors.textPrimary,
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@PaczkofastPreviews
@Composable
private fun ActionButtonsPreview() {
    PaczkofastTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryActionButton(text = "Open box remotely", onClick = {})
            PrimaryActionButton(text = "Open box remotely", onClick = {}, isLoading = true)
            OutlinedActionButton(text = "Navigate", onClick = {})
        }
    }
}
