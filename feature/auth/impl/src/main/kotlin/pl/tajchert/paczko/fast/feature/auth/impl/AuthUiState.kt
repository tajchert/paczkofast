package pl.tajchert.paczko.fast.feature.auth.impl

sealed interface AuthUiState {
    data object EnterPhone : AuthUiState
    data object CodeRequested : AuthUiState
    data object Loading : AuthUiState
    data object Authenticated : AuthUiState
    data class Error(val message: String) : AuthUiState
}
