package pl.tajchert.paczko.fast.feature.settings.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.BottomNavDestination
import pl.tajchert.paczko.fast.core.designsystem.component.HomeHeader
import pl.tajchert.paczko.fast.core.designsystem.component.LogoMark
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastBottomBar
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.SegmentedControl
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.ThemeMode

@Composable
fun SettingsScreen(
    appVersion: String,
    onOpenParcels: () -> Unit,
    onOpenHistory: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        themeMode = uiState.themeMode,
        phoneNumber = uiState.phoneNumber,
        appVersion = appVersion,
        onThemeSelected = viewModel::setThemeMode,
        onLogout = { viewModel.logout(onLoggedOut) },
        onOpenParcels = onOpenParcels,
        onOpenHistory = onOpenHistory,
    )
}

@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    phoneNumber: String?,
    appVersion: String,
    onThemeSelected: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onOpenParcels: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = { HomeHeader(title = "Settings", showLogo = false) },
        bottomBar = {
            PaczkofastBottomBar(
                selected = BottomNavDestination.Settings,
                onSelect = { destination ->
                    when (destination) {
                        BottomNavDestination.Parcels -> onOpenParcels()
                        BottomNavDestination.History -> onOpenHistory()
                        BottomNavDestination.Settings -> Unit
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionLabel(text = "Appearance")
            SurfaceCard {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleSmall,
                    color = PaczkofastTheme.colors.textPrimary,
                )
                SegmentedControl(
                    options = ThemeMode.entries.map { it to themeModeLabel(it) },
                    selected = themeMode,
                    onSelect = onThemeSelected,
                )
                Text(
                    text = themeModeCaption(themeMode),
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textFaint,
                )
            }

            SectionLabel(text = "Account")
            SurfaceCard(spacing = 0.dp) {
                Column(
                    modifier = Modifier.padding(bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Logged in",
                        style = MaterialTheme.typography.titleSmall,
                        color = PaczkofastTheme.colors.textPrimary,
                    )
                    if (phoneNumber != null) {
                        Text(
                            text = phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = PaczkofastTheme.colors.textMuted,
                        )
                    }
                }
                HorizontalDivider(color = PaczkofastTheme.colors.cardBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.titleSmall,
                        color = PaczkofastTheme.colors.urgent,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = PaczkofastTheme.colors.textFaint,
                    )
                }
            }

            SectionLabel(text = "About")
            SurfaceCard(muted = true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LogoMark()
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Paczkofast",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.5.sp),
                            color = PaczkofastTheme.colors.textPrimary,
                        )
                        Text(
                            text = "Version $appVersion",
                            style = MaterialTheme.typography.bodySmall,
                            color = PaczkofastTheme.colors.textMuted,
                        )
                    }
                }
                Text(
                    text = "Unofficial companion app. Not affiliated with or endorsed by the locker operator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textFaint,
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Log out?") },
            text = { Text(text = "You'll need to sign in with SMS again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                ) {
                    Text(text = "Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

/**
 * The rounded Black-Amber surface used for each settings group.
 *
 * @param muted Uses the subtle surface (About card).
 * @param spacing Vertical gap between children; 0 when the card lays out its
 *   own dividers.
 */
@Composable
private fun SurfaceCard(
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    spacing: androidx.compose.ui.unit.Dp = 12.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val surface = if (muted) {
        PaczkofastTheme.colors.cardSurfaceSubtle
    } else {
        PaczkofastTheme.colors.cardSurface
    }
    val borderColor = if (muted) {
        PaczkofastTheme.colors.cardBorderSubtle
    } else {
        PaczkofastTheme.colors.cardBorder
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(surface)
            .border(1.dp, borderColor, MaterialTheme.shapes.extraLarge)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = PaczkofastTheme.colors.textMuted,
        modifier = Modifier.padding(start = 4.dp, top = 10.dp),
    )
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

private fun themeModeCaption(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "Follows your phone's dark mode schedule"
    ThemeMode.LIGHT -> "Always light, regardless of the system setting"
    ThemeMode.DARK -> "Always dark, regardless of the system setting"
}

@PaczkofastPreviews
@Composable
private fun SettingsContentPreview() {
    PaczkofastTheme {
        SettingsContent(
            themeMode = ThemeMode.SYSTEM,
            phoneNumber = "+48 601 480 312",
            appVersion = "0.4.0 (213)",
            onThemeSelected = {},
            onLogout = {},
            onOpenParcels = {},
            onOpenHistory = {},
        )
    }
}
