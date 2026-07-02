package pl.tajchert.paczko.fast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import pl.tajchert.paczko.fast.core.designsystem.theme.PaczkofastTheme
import pl.tajchert.paczko.fast.navigation.PaczkofastNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// =============================================================================
// MAIN ACTIVITY
// =============================================================================
// Single Activity architecture - all screens are Compose destinations.
//
// ## Key Patterns Demonstrated
//
// 1. **Edge-to-Edge**: Content draws behind system bars
// 2. **Splash Screen**: Shows while preferences load
// 3. **Theme from Preferences**: Dark/light/system follows user choice
// 4. **Hilt Integration**: @AndroidEntryPoint for dependency injection
//
// ## Splash Screen Condition
//
// We keep the splash screen visible until preferences are loaded.
// This prevents a flash of wrong theme on app start.
// =============================================================================

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Track UI state for splash screen condition
        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)

        // Observe UI state
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect {}
            }
        }

        // Keep splash screen while loading preferences
        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainActivityUiState.Loading -> true
                is MainActivityUiState.Success -> false
            }
        }

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)

            // Keep system bar icon contrast in sync with the app theme,
            // which can differ from the system dark mode setting.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                )
                onDispose {}
            }

            PaczkofastTheme(darkTheme = darkTheme) {
                when (val state = uiState) {
                    MainActivityUiState.Loading -> Unit
                    is MainActivityUiState.Success -> PaczkofastNavHost(
                        startDestination = state.initialRoute,
                    )
                }
            }
        }
    }
}

/**
 * Determine if dark theme should be used.
 *
 * @param uiState The current UI state from ViewModel
 * @return true if dark theme should be applied
 */
@Composable
private fun shouldUseDarkTheme(uiState: MainActivityUiState): Boolean {
    val isSystemDark = isSystemInDarkTheme()
    return uiState.shouldUseDarkTheme(isSystemDark)
}
