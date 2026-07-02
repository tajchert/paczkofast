package pl.tajchert.paczko.fast.feature.settings.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import pl.tajchert.paczko.fast.core.designsystem.component.PaczkofastPreviews
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.core.model.ThemeMode

@Composable
fun SettingsScreen(
    appVersion: String,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        themeMode = uiState.themeMode,
        appVersion = appVersion,
        onThemeSelected = viewModel::setThemeMode,
        onLogout = { viewModel.logout(onLoggedOut) },
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    appVersion: String,
    onThemeSelected: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = PaczkofastTheme.colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader(text = "APPEARANCE")
            ThemeMode.entries.forEach { mode ->
                ThemeOptionRow(
                    label = themeModeLabel(mode),
                    selected = mode == themeMode,
                    onClick = { onThemeSelected(mode) },
                )
            }

            SectionHeader(text = "ACCOUNT")
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(text = "Log out")
            }

            SectionHeader(text = "ABOUT")
            Text(
                text = "Paczkofast $appVersion",
                style = MaterialTheme.typography.bodyMedium,
                color = PaczkofastTheme.colors.textPrimary,
            )
            Text(
                text = "Unofficial InPost companion. Not affiliated with InPost.",
                style = MaterialTheme.typography.bodySmall,
                color = PaczkofastTheme.colors.textMuted,
            )
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

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = PaczkofastTheme.colors.textMuted,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = PaczkofastTheme.colors.textPrimary,
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@PaczkofastPreviews
@Composable
private fun SettingsContentPreview() {
    PaczkofastTheme {
        SettingsContent(
            themeMode = ThemeMode.SYSTEM,
            appVersion = "1.0.0 (1)",
            onThemeSelected = {},
            onLogout = {},
            onBack = {},
        )
    }
}
