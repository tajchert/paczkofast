package pl.tajchert.paczko.fast.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.tajchert.paczko.fast.core.designsystem.R
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme

/**
 * Top app bar for the Paczkofast app.
 *
 * ## Design Decision
 *
 * We use CenterAlignedTopAppBar as the default because:
 * 1. It works well for single-level navigation
 * 2. Title is always visible and centered
 * 3. Consistent look across all screens
 *
 * ## Navigation Icon
 *
 * The navigation icon (back arrow) is shown when [onNavigationClick] is provided.
 * This follows the pattern of "if you can go back, show the arrow."
 *
 * ## Usage
 *
 * ```kotlin
 * Scaffold(
 *     topBar = {
 *         PaczkofastTopAppBar(
 *             title = "Parcels",
 *             onNavigationClick = { navController.popBackStack() },
 *         )
 *     }
 * ) { ... }
 * ```
 *
 * @param title The title to display in the app bar
 * @param modifier Modifier for the app bar
 * @param onNavigationClick Called when the navigation icon is clicked. If null, no icon is shown.
 * @param navigationIcon The icon to show for navigation. Defaults to back arrow.
 * @param actions Composable slot for action buttons on the right side.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaczkofastTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconContentDescription: String? = null,
    actions: @Composable () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = PaczkofastTheme.colors.textPrimary,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onNavigationClick != null) {
                BackButtonChip(
                    onClick = onNavigationClick,
                    icon = navigationIcon,
                    contentDescription = navigationIconContentDescription
                        ?: stringResource(R.string.navigate_back),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PaczkofastTheme.colors.background,
            titleContentColor = PaczkofastTheme.colors.textPrimary,
        ),
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun PaczkofastTopAppBarPreview() {
    PaczkofastTheme {
        PaczkofastTopAppBar(
            title = "Parcels",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PaczkofastTopAppBarWithBackPreview() {
    PaczkofastTheme {
        PaczkofastTopAppBar(
            title = "Parcel Details",
            onNavigationClick = {},
        )
    }
}
