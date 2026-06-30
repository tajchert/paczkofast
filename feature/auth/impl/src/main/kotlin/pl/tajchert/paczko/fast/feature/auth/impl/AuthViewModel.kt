package pl.tajchert.paczko.fast.feature.auth.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.tajchert.paczko.fast.core.domain.ConfirmSmsCodeUseCase
import pl.tajchert.paczko.fast.core.domain.RequestSmsCodeUseCase
import pl.tajchert.paczko.fast.core.model.auth.PhoneNumber

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val requestSmsCode: RequestSmsCodeUseCase,
    private val confirmSmsCode: ConfirmSmsCodeUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.EnterPhone)
    val uiState: StateFlow<AuthUiState> = _uiState

    private var phoneValue = ""
    private var smsCode = ""
    private var requestedPhone: PhoneNumber? = null

    fun onPhoneChanged(value: String) {
        phoneValue = value.filter(Char::isDigit)
        requestedPhone = null
        if (_uiState.value == AuthUiState.CodeRequested) {
            _uiState.value = AuthUiState.EnterPhone
        }
    }

    fun onSmsCodeChanged(value: String) {
        smsCode = value.filter(Char::isDigit)
    }

    fun onRequestCode() {
        if (phoneValue.isBlank()) return

        val phoneNumber = PhoneNumber("48", phoneValue)
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { requestSmsCode(phoneNumber) }
                .onSuccess {
                    requestedPhone = phoneNumber
                    _uiState.value = AuthUiState.CodeRequested
                }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Unable to request SMS code")
                }
        }
    }

    fun onConfirmCode() {
        val phoneNumber = requestedPhone ?: return
        if (smsCode.isBlank()) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            runCatching { confirmSmsCode(phoneNumber, smsCode) }
                .onSuccess { _uiState.value = AuthUiState.Authenticated }
                .onFailure {
                    _uiState.value = AuthUiState.Error(it.message ?: "Unable to confirm SMS code")
                }
        }
    }
}
