package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.domain.ConfirmSmsCodeUseCase
import pl.tajchert.paczko.fast.core.domain.RequestSmsCodeUseCase
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val requestSmsCode: RequestSmsCodeUseCase,
    private val confirmSmsCode: ConfirmSmsCodeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var requestedPhone: PhoneNumber? = null
    private var resendTimerJob: Job? = null

    fun onPhoneDigit(digit: Char) {
        if (!digit.isDigit()) return
        _uiState.update { state ->
            if (state.isLoading || state.phoneDigits.length >= PHONE_LENGTH) {
                state
            } else {
                state.copy(phoneDigits = state.phoneDigits + digit, errorMessage = null)
            }
        }
    }

    fun onPhoneBackspace() {
        _uiState.update { state ->
            if (state.isLoading) {
                state
            } else {
                state.copy(phoneDigits = state.phoneDigits.dropLast(1), errorMessage = null)
            }
        }
    }

    /**
     * Replaces the phone number from a native text field: keeps digits only and
     * caps at [PHONE_LENGTH]. Ignored while a request is in flight.
     */
    fun onPhoneChange(input: String) {
        _uiState.update { state ->
            if (state.isLoading) {
                state
            } else {
                state.copy(
                    phoneDigits = input.filter(Char::isDigit).take(PHONE_LENGTH),
                    errorMessage = null,
                )
            }
        }
    }

    fun onCodeDigit(digit: Char) {
        if (!digit.isDigit()) return
        _uiState.update { state ->
            if (state.isLoading || state.codeDigits.length >= CODE_LENGTH) {
                state
            } else {
                state.copy(codeDigits = state.codeDigits + digit, errorMessage = null)
            }
        }
    }

    fun onCodeBackspace() {
        _uiState.update { state ->
            if (state.isLoading) {
                state
            } else {
                state.copy(codeDigits = state.codeDigits.dropLast(1), errorMessage = null)
            }
        }
    }

    /**
     * Replaces the SMS code from a native text field: keeps digits only and
     * caps at [CODE_LENGTH]. Ignored while a request is in flight.
     */
    fun onCodeChange(input: String) {
        _uiState.update { state ->
            if (state.isLoading) {
                state
            } else {
                state.copy(
                    codeDigits = input.filter(Char::isDigit).take(CODE_LENGTH),
                    errorMessage = null,
                )
            }
        }
    }

    fun onSendCode() {
        val state = _uiState.value
        if (!state.canSendCode) return

        val phoneNumber = PhoneNumber(PHONE_PREFIX, state.phoneDigits)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { requestSmsCode(phoneNumber) }
                .onSuccess {
                    requestedPhone = phoneNumber
                    _uiState.update {
                        it.copy(isLoading = false, step = AuthStep.Otp, codeDigits = "")
                    }
                    startResendTimer()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to request SMS code",
                        )
                    }
                }
        }
    }

    fun onResendCode() {
        val phoneNumber = requestedPhone ?: return
        val state = _uiState.value
        if (state.isLoading || state.resendSecondsLeft > 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { requestSmsCode(phoneNumber) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, codeDigits = "") }
                    startResendTimer()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to request SMS code",
                        )
                    }
                }
        }
    }

    fun onConfirmCode() {
        val phoneNumber = requestedPhone ?: return
        val state = _uiState.value
        if (!state.canConfirmCode) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { confirmSmsCode(phoneNumber, state.codeDigits) }
                .onSuccess {
                    resendTimerJob?.cancel()
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to confirm SMS code",
                        )
                    }
                }
        }
    }

    /** Back arrow / "wrong number?" on the code screen. */
    fun onBackToPhone() {
        if (_uiState.value.step != AuthStep.Otp) return
        resendTimerJob?.cancel()
        requestedPhone = null
        _uiState.update {
            it.copy(
                step = AuthStep.Phone,
                codeDigits = "",
                errorMessage = null,
                resendSecondsLeft = 0,
            )
        }
    }

    private fun startResendTimer() {
        resendTimerJob?.cancel()
        resendTimerJob = viewModelScope.launch {
            _uiState.update { it.copy(resendSecondsLeft = RESEND_COOLDOWN_SECONDS) }
            while (_uiState.value.resendSecondsLeft > 0) {
                delay(1_000)
                _uiState.update { it.copy(resendSecondsLeft = it.resendSecondsLeft - 1) }
            }
        }
    }

    companion object {
        const val PHONE_PREFIX = "48"
        const val PHONE_LENGTH = 9
        const val CODE_LENGTH = 6
        const val RESEND_COOLDOWN_SECONDS = 30
    }
}
