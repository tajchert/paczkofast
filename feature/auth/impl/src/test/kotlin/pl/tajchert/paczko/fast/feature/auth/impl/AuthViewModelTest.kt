package pl.tajchert.paczko.fast.feature.auth.impl

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@kotlinx.coroutines.ExperimentalCoroutinesApi
class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(auth: AuthRepository) = AuthViewModel(
        requestSmsCode = RequestSmsCodeUseCase(auth),
        confirmSmsCode = ConfirmSmsCodeUseCase(auth),
    )

    private fun AuthViewModel.typePhone(digits: String) = digits.forEach(::onPhoneDigit)
    private fun AuthViewModel.typeCode(digits: String) = digits.forEach(::onCodeDigit)

    @Test
    fun confirmCodeEmitsAuthenticatedState() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = viewModel(auth)

        viewModel.typePhone("600123456")
        viewModel.onSendCode()
        runCurrent()
        assertEquals(AuthStep.Otp, viewModel.uiState.value.step)

        viewModel.typeCode("123456")
        viewModel.onConfirmCode()
        runCurrent()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals(PhoneNumber("48", "600123456"), auth.confirmedPhone)
    }

    @Test
    fun confirmCodeBeforeRequestDoesNothing() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = viewModel(auth)

        viewModel.typePhone("600123456")
        viewModel.typeCode("123456")
        viewModel.onConfirmCode()
        runCurrent()

        assertFalse(viewModel.uiState.value.isAuthenticated)
        assertEquals(0, auth.confirmRequests)
    }

    @Test
    fun sendCodeWithIncompletePhoneDoesNothing() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = viewModel(auth)

        viewModel.typePhone("60012345")
        viewModel.onSendCode()
        runCurrent()

        assertEquals(AuthStep.Phone, viewModel.uiState.value.step)
        assertEquals(0, auth.requestRequests)
    }

    @Test
    fun phoneInputIsCappedAtNineDigits() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = viewModel(auth)

        viewModel.typePhone("6001234567890")

        assertEquals("600123456", viewModel.uiState.value.phoneDigits)
    }

    @Test
    fun confirmCodeUsesRequestedPhoneWhenPhoneEditedAfterRequest() = runTest {
        val requestCompleted = CompletableDeferred<Unit>()
        val auth = FakeAuthRepository(
            authSession = AuthSession("access", "refresh"),
            requestCompleted = requestCompleted,
        )
        val viewModel = viewModel(auth)

        viewModel.typePhone("600123456")
        viewModel.onSendCode()
        requestCompleted.complete(Unit)
        runCurrent()
        viewModel.typeCode("123456")
        viewModel.onConfirmCode()
        runCurrent()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertEquals(PhoneNumber("48", "600123456"), auth.confirmedPhone)
    }

    @Test
    fun resendTimerCountsDownAndAllowsResend() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = viewModel(auth)

        viewModel.typePhone("600123456")
        viewModel.onSendCode()
        runCurrent()
        assertEquals(AuthViewModel.RESEND_COOLDOWN_SECONDS, viewModel.uiState.value.resendSecondsLeft)

        viewModel.onResendCode()
        runCurrent()
        assertEquals(1, auth.requestRequests)

        advanceTimeBy(AuthViewModel.RESEND_COOLDOWN_SECONDS * 1_000L + 1)
        assertEquals(0, viewModel.uiState.value.resendSecondsLeft)

        viewModel.onResendCode()
        runCurrent()
        assertEquals(2, auth.requestRequests)
    }

    @Test
    fun backToPhoneClearsCodeAndTimer() = runTest {
        val auth = FakeAuthRepository(AuthSession("access", "refresh"))
        val viewModel = viewModel(auth)

        viewModel.typePhone("600123456")
        viewModel.onSendCode()
        runCurrent()
        viewModel.typeCode("123")
        viewModel.onBackToPhone()

        val state = viewModel.uiState.value
        assertEquals(AuthStep.Phone, state.step)
        assertEquals("", state.codeDigits)
        assertEquals(0, state.resendSecondsLeft)
        assertEquals("600123456", state.phoneDigits)
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
