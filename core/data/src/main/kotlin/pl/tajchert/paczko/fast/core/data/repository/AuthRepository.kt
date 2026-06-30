package pl.tajchert.paczko.fast.core.data.repository

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber

interface AuthRepository {
    fun observeAuthSession(): Flow<AuthSession>

    suspend fun requestSmsCode(phoneNumber: PhoneNumber)

    suspend fun confirmSmsCode(phoneNumber: PhoneNumber, smsCode: String): AuthSession

    suspend fun refreshToken(): AuthSession

    suspend fun logout()
}
