package pl.tajchert.paczko.fast.core.domain

import kotlinx.coroutines.flow.Flow
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import javax.inject.Inject

class RequestSmsCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: PhoneNumber) {
        authRepository.requestSmsCode(phoneNumber)
    }
}

class ConfirmSmsCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: PhoneNumber, smsCode: String): AuthSession =
        authRepository.confirmSmsCode(phoneNumber = phoneNumber, smsCode = smsCode)
}

class ObserveAuthSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthSession> = authRepository.observeAuthSession()
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
