package pl.tajchert.paczko.fast.feature.auth.impl

/** Which of the two login screens is showing. */
enum class AuthStep {
    Phone,
    Otp,
}

data class AuthUiState(
    val step: AuthStep = AuthStep.Phone,
    /** National phone number digits, no prefix, max [AuthViewModel.PHONE_LENGTH]. */
    val phoneDigits: String = "",
    /** SMS code digits, max [AuthViewModel.CODE_LENGTH]. */
    val codeDigits: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Seconds until the SMS code can be re-sent; 0 = resend available. */
    val resendSecondsLeft: Int = 0,
    val isAuthenticated: Boolean = false,
) {
    val canSendCode: Boolean
        get() = !isLoading && phoneDigits.length == AuthViewModel.PHONE_LENGTH

    val canConfirmCode: Boolean
        get() = !isLoading && codeDigits.length == AuthViewModel.CODE_LENGTH
}
