package pl.tajchert.paczko.fast.feature.settings.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.tajchert.paczko.fast.core.designsystem.component.BottomNavDestination
import pl.tajchert.paczko.fast.core.designsystem.component.HomeHeader
import pl.tajchert.paczko.fast.core.designsystem.component.LogoMark
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastBottomBar
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastCard
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.component.SegmentedControl
import pl.tajchert.paczko.fast.core.designsystem.theme.MonoLabel
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
            PaczkofastCard {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    color = PaczkofastTheme.colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                SegmentedControl(
                    options = ThemeMode.entries.map { it to themeModeLabel(it) },
                    selected = themeMode,
                    onSelect = onThemeSelected,
                )
            }

            SectionLabel(text = "Account")
            PaczkofastCard {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Logged in",
                        style = MaterialTheme.typography.titleMedium,
                        color = PaczkofastTheme.colors.textPrimary,
                    )
                    if (phoneNumber != null) {
                        Text(
                            text = phoneNumber,
                            style = MonoLabel,
                            color = PaczkofastTheme.colors.monoLabel,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                        .height(2.5.dp)
                        .background(PaczkofastTheme.colors.borderStrong),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.titleSmall,
                        color = PaczkofastTheme.colors.alertText,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = PaczkofastTheme.colors.alertText,
                    )
                }
            }

            SectionLabel(text = "About")
            PaczkofastCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LogoMark()
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Paczkofast",
                            style = MaterialTheme.typography.titleMedium,
                            color = PaczkofastTheme.colors.textPrimary,
                        )
                        Text(
                            text = "VERSION $appVersion".uppercase(),
                            style = MonoLabel,
                            color = PaczkofastTheme.colors.monoLabel,
                        )
                    }
                }
                Text(
                    text = "Unofficial companion app. Not affiliated with or endorsed by the locker operator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PaczkofastTheme.colors.textFaint,
                    modifier = Modifier.padding(top = 10.dp),
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
 * Uppercase mono section caption ("APPEARANCE", "ACCOUNT", "ABOUT") above
 * each settings card group.
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MonoLabel,
        color = PaczkofastTheme.colors.monoLabel,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp),
    )
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@PaczkofastPreviews
@Composable
private fun SettingsContentPreview() {
    PaczkofastTheme {
        SettingsContent(
            themeMode = ThemeMode.LIGHT,
            phoneNumber = "+48 500 100 200",
            appVersion = "1.0.0 (1)",
            onThemeSelected = {},
            onLogout = {},
            onOpenParcels = {},
            onOpenHistory = {},
        )
    }
}
