package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Login flow entry point — routes between the phone entry screen (4a)
 * and the SMS code screen (4b).
 */
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    when (uiState.step) {
        AuthStep.Phone -> PhoneLoginScreen(
            state = uiState,
            onDigit = viewModel::onPhoneDigit,
            onBackspace = viewModel::onPhoneBackspace,
            onSendCode = viewModel::onSendCode,
        )

        AuthStep.Otp -> {
            BackHandler(onBack = viewModel::onBackToPhone)
            OtpScreen(
                state = uiState,
                onDigit = viewModel::onCodeDigit,
                onBackspace = viewModel::onCodeBackspace,
                onConfirm = viewModel::onConfirmCode,
                onResend = viewModel::onResendCode,
                onBackToPhone = viewModel::onBackToPhone,
            )
        }
    }
}
