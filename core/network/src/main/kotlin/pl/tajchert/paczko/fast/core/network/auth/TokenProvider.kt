package pl.tajchert.paczko.fast.core.network.auth

interface TokenProvider {
    fun authToken(): String?

    fun refreshToken(): String?

    fun saveTokens(authToken: String, refreshToken: String)

    fun clearTokens()
}
