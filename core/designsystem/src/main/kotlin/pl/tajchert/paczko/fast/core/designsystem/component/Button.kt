package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Primary filled button for the Paczkofast app.
 *
 * ## Why a Custom [NeoSurface]-Based Implementation?
 *
 * The neo-brutalist design system relies on a hard offset shadow that visually
 * "collapses" when pressed (the surface slides into its own shadow). Material 3's
 * `Button` draws its own ripple/elevation and would fight with that effect, so this
 * button is built directly on [NeoSurface] with a manual `clickable` + interaction
 * source instead of wrapping `Button`.
 *
 * ## Usage
 *
 * ```kotlin
 * PaczkofastButton(
 *     onClick = { viewModel.submit() },
 *     text = "Submit",
 *     isLoading = uiState.isSubmitting,
 * )
 * ```
 *
 * @param onClick Called when the button is clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param isLoading Whether to show loading indicator
 * @param text The button text
 */
@Composable
fun PaczkofastButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String,
) {
    val colors = PaczkofastTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isEnabled = enabled && !isLoading

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(14.dp),
        fill = if (isEnabled) colors.accent else colors.accentDisabled,
        borderColor = colors.borderStrong,
        shadow = isEnabled,
        shadowOffset = 3.dp,
        pressed = pressed && isEnabled,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            PaczkofastButtonContent(
                text = text,
                isLoading = isLoading,
                contentColor = if (isEnabled) colors.onAccent else colors.onAccentDisabled,
            )
        }
    }
}

/**
 * Outlined button variant - for secondary actions.
 *
 * Use this for actions that are less prominent than the primary action, e.g.
 * "Navigate" or "Contact support".
 */
@Composable
fun PaczkofastOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    text: String,
) {
    val colors = PaczkofastTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isEnabled = enabled && !isLoading

    NeoSurface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(14.dp),
        fill = colors.cardSurface,
        borderColor = colors.outlineButtonBorder,
        shadow = true,
        shadowOffset = 2.dp,
        pressed = pressed && isEnabled,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            PaczkofastButtonContent(
                text = text,
                isLoading = isLoading,
                contentColor = colors.textPrimary,
            )
        }
    }
}

/**
 * Text button variant - for the least prominent actions.
 *
 * Use this for actions in dialogs or as tertiary actions. Just a clickable [Text],
 * no surface/border/shadow.
 */
@Composable
fun PaczkofastTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = PaczkofastTheme.colors.textPrimary.let {
                if (enabled) it else it.copy(alpha = 0.5f)
            },
        )
    }
}

/**
 * Internal button content with loading support.
 */
@Composable
private fun PaczkofastButtonContent(
    text: String,
    isLoading: Boolean,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = contentColor,
        )
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@PaczkofastPreviews
@Composable
private fun PaczkofastButtonPreview() {
    PaczkofastTheme {
        PaczkofastButton(
            onClick = {},
            text = "Primary Button",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastButtonLoadingPreview() {
    PaczkofastTheme {
        PaczkofastButton(
            onClick = {},
            text = "Loading",
            isLoading = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastButtonDisabledPreview() {
    PaczkofastTheme {
        PaczkofastButton(
            onClick = {},
            text = "Disabled",
            enabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastOutlinedButtonPreview() {
    PaczkofastTheme {
        PaczkofastOutlinedButton(
            onClick = {},
            text = "Outlined Button",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PaczkofastPreviews
@Composable
private fun PaczkofastTextButtonPreview() {
    PaczkofastTheme {
        PaczkofastTextButton(
            onClick = {},
            text = "Text Button",
            modifier = Modifier.padding(16.dp),
        )
    }
}
