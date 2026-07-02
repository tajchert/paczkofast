package pl.tajchert.paczko.fast.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.tajchert.paczko.fast.core.datastore.di.AuthTokensDataStore
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import javax.inject.Inject

interface AuthTokensDataSource {
    val authSession: Flow<AuthSession>

    /** The phone number the user last logged in with, or null if unknown. */
    val phoneNumber: Flow<String?>

    suspend fun saveTokens(authToken: String, refreshToken: String)

    /** Persists the logged-in phone number for display (e.g. Settings). */
    suspend fun savePhoneNumber(phoneNumber: String)

    suspend fun clearTokens()
}

class DataStoreAuthTokensDataSource @Inject constructor(
    @AuthTokensDataStore
    private val dataStore: DataStore<Preferences>,
) : AuthTokensDataSource {
    override val authSession: Flow<AuthSession> = dataStore.data.map { preferences ->
        AuthSession(
            authToken = preferences[AUTH_TOKEN].orEmpty(),
            refreshToken = preferences[REFRESH_TOKEN].orEmpty(),
        )
    }

    override val phoneNumber: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PHONE_NUMBER]?.takeIf { it.isNotBlank() }
    }

    override suspend fun saveTokens(authToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = authToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun savePhoneNumber(phoneNumber: String) {
        dataStore.edit { preferences ->
            preferences[PHONE_NUMBER] = phoneNumber
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(PHONE_NUMBER)
        }
    }

    private companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
    }
}
