package pl.tajchert.paczko.fast.core.model.auth

data class AuthSession(
    val authToken: String,
    val refreshToken: String,
) {
    val isAuthenticated: Boolean = authToken.isNotBlank() && refreshToken.isNotBlank()
}
