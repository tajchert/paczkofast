package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    val canEnterCode = uiState == AuthUiState.CodeRequested || uiState == AuthUiState.Loading
    val canEditPhone = uiState != AuthUiState.Loading && uiState != AuthUiState.CodeRequested
    val canRequestCode = uiState != AuthUiState.Loading && phone.isNotBlank()
    val canConfirmCode = uiState == AuthUiState.CodeRequested && code.isNotBlank()
    LaunchedEffect(uiState) {
        if (uiState == AuthUiState.Authenticated) onAuthenticated()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Paczkofast", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    viewModel.onPhoneChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone number") },
                enabled = canEditPhone,
            )
            Button(
                onClick = viewModel::onRequestCode,
                enabled = canRequestCode,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Send SMS code")
            }
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    viewModel.onSmsCodeChanged(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("SMS code") },
                enabled = canEnterCode,
            )
            Button(
                onClick = viewModel::onConfirmCode,
                enabled = canConfirmCode,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log in")
            }
            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
