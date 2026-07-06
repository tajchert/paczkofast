package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.R
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Full-screen centered loading indicator.
 *
 * ## Accessibility
 *
 * The loading indicator includes a content description for screen readers.
 * This announces "Loading" when the indicator is focused.
 *
 * ## Usage
 *
 * ```kotlin
 * when (uiState) {
 *     is UiState.Loading -> PaczkofastLoadingIndicator()
 *     is UiState.Success -> Content(uiState.data)
 *     is UiState.Error -> ErrorState(uiState.message)
 * }
 * ```
 *
 * @param modifier Modifier for the container Box
 */
@Composable
fun PaczkofastLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaczkofastTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        val loading = stringResource(R.string.loading)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            NeoSurface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                fill = PaczkofastTheme.colors.accent,
                borderColor = PaczkofastTheme.colors.accentBorder,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(22.dp)
                        .semantics { contentDescription = loading },
                    color = PaczkofastTheme.colors.onAccent,
                    strokeWidth = 2.5.dp,
                )
            }
            Text(
                text = loading.uppercase(),
                style = MonoLabel,
                color = PaczkofastTheme.colors.monoLabel,
            )
        }
    }
}

/**
 * Small inline loading indicator for use within other components.
 *
 * @param modifier Modifier for the indicator
 */
@Composable
fun PaczkofastSmallLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    val loading = stringResource(R.string.loading)
    CircularProgressIndicator(
        modifier = modifier
            .size(24.dp)
            .semantics { contentDescription = loading },
        color = PaczkofastTheme.colors.textPrimary,
        strokeWidth = 2.dp,
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun PaczkofastLoadingIndicatorPreview() {
    PaczkofastTheme {
        PaczkofastLoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun PaczkofastSmallLoadingIndicatorPreview() {
    PaczkofastTheme {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            PaczkofastSmallLoadingIndicator()
        }
    }
}
