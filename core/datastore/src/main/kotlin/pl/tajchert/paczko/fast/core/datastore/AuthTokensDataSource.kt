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

    suspend fun saveTokens(authToken: String, refreshToken: String)

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

    override suspend fun saveTokens(authToken: String, refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = authToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
        }
    }

    private companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}
