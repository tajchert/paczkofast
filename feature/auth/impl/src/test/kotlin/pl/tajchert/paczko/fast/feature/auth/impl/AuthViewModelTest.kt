package pl.tajchert.paczko.fast.feature.auth.impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import pl.tajchert.paczko.fast.core.data.repository.AuthRepository
import pl.tajchert.paczko.fast.core.domain.ConfirmSmsCodeUseCase
import pl.tajchert.paczko.fast.core.domain.RequestSmsCodeUseCase
import pl.tajchert.paczko.fast.core.model.auth.AuthSession
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber
import pl.tajchert.paczko.fast.core.testing.util.MainDispatcherRule
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun confirmCodeEmitsAuthenticatedState() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = AuthViewModel(
            requestSmsCode = RequestSmsCodeUseCase(auth),
            confirmSmsCode = ConfirmSmsCodeUseCase(auth),
        )

        viewModel.onPhoneChanged("600123456")
        viewModel.onRequestCode()
        viewModel.onSmsCodeChanged("1234")
        viewModel.onConfirmCode()

        assertEquals(AuthUiState.Authenticated, viewModel.uiState.value)
        assertEquals(PhoneNumber("48", "600123456"), auth.confirmedPhone)
    }

    @Test
    fun confirmCodeBeforeRequestLeavesStateUnchanged() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = AuthViewModel(
            requestSmsCode = RequestSmsCodeUseCase(auth),
            confirmSmsCode = ConfirmSmsCodeUseCase(auth),
        )

        viewModel.onPhoneChanged("600123456")
        viewModel.onSmsCodeChanged("1234")
        viewModel.onConfirmCode()

        assertEquals(AuthUiState.EnterPhone, viewModel.uiState.value)
        assertEquals(0, auth.confirmRequests)
    }

    @Test
    fun requestCodeWithBlankPhoneLeavesStateUnchanged() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = AuthViewModel(
            requestSmsCode = RequestSmsCodeUseCase(auth),
            confirmSmsCode = ConfirmSmsCodeUseCase(auth),
        )

        viewModel.onRequestCode()

        assertEquals(AuthUiState.EnterPhone, viewModel.uiState.value)
        assertEquals(0, auth.requestRequests)
    }

    @Test
    fun confirmCodeUsesRequestedPhoneWhenPhoneChangesDuringRequest() = runTest {
        val requestCompleted = CompletableDeferred<Unit>()
        val auth = FakeAuthRepository(
            authSession = AuthSession("access", "refresh"),
            requestCompleted = requestCompleted,
        )
        val viewModel = AuthViewModel(
            requestSmsCode = RequestSmsCodeUseCase(auth),
            confirmSmsCode = ConfirmSmsCodeUseCase(auth),
        )

        viewModel.onPhoneChanged("600123456")
        viewModel.onRequestCode()
        viewModel.onPhoneChanged("700123456")
        requestCompleted.complete(Unit)
        viewModel.onSmsCodeChanged("1234")
        viewModel.onConfirmCode()

        assertEquals(AuthUiState.Authenticated, viewModel.uiState.value)
        assertEquals(PhoneNumber("48", "600123456"), auth.confirmedPhone)
    }
}

private class FakeAuthRepository(
    private val authSession: AuthSession,
    private val requestCompleted: CompletableDeferred<Unit>? = null,
) : AuthRepository {
    var confirmedPhone: PhoneNumber? = null
    var requestRequests: Int = 0
    var confirmRequests: Int = 0

    override fun observeAuthSession(): Flow<AuthSession> = flowOf(authSession)

    override suspend fun requestSmsCode(phoneNumber: PhoneNumber) {
        requestRequests += 1
        requestCompleted?.await()
    }

    override suspend fun confirmSmsCode(
        phoneNumber: PhoneNumber,
        smsCode: String,
    ): AuthSession {
        confirmRequests += 1
        confirmedPhone = phoneNumber
        return authSession
    }

    override suspend fun refreshToken(): AuthSession = authSession

    override suspend fun logout() = Unit
}
