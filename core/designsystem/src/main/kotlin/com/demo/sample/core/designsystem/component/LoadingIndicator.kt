package com.demo.sample.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.demo.sample.core.designsystem.theme.SampleTheme

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
 *     is UiState.Loading -> SampleLoadingIndicator()
 *     is UiState.Success -> Content(uiState.data)
 *     is UiState.Error -> ErrorState(uiState.message)
 * }
 * ```
 *
 * @param modifier Modifier for the container Box
 */
@Composable
fun SampleLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Loading" },
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
        )
    }
}

/**
 * Small inline loading indicator for use within other components.
 *
 * @param modifier Modifier for the indicator
 */
@Composable
fun SampleSmallLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(24.dp)
            .semantics { contentDescription = "Loading" },
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp,
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun SampleLoadingIndicatorPreview() {
    SampleTheme {
        SampleLoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun SampleSmallLoadingIndicatorPreview() {
    SampleTheme {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            SampleSmallLoadingIndicator()
        }
    }
}
