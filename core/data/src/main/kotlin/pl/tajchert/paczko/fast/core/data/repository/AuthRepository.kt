package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber

interface AuthRepository {
    fun observeAuthSession(): Flow<AuthSession>

    /**
     * The phone number the user is logged in with, formatted for display
     * (e.g. "+48 601 480 312"), or null when unknown.
     */
    fun observePhoneNumber(): Flow<String?>

    suspend fun requestSmsCode(phoneNumber: PhoneNumber)

    suspend fun confirmSmsCode(phoneNumber: PhoneNumber, smsCode: String): AuthSession

    suspend fun refreshToken(): AuthSession

    suspend fun logout()
}
