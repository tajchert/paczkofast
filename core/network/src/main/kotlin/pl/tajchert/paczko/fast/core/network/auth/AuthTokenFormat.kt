package pl.tajchert.paczko.fast.core.network.auth

fun String.normalizedAuthToken(): String {
    val trimmed = trim()
    return if (trimmed.startsWith(BEARER_PREFIX, ignoreCase = true)) {
        trimmed.substring(BEARER_PREFIX.length).trimStart()
    } else {
        trimmed
    }
}

fun String.asBearerAuthorizationHeader(): String = "$BEARER_PREFIX${normalizedAuthToken()}"

private const val BEARER_PREFIX = "Bearer "
