package pl.tajchert.paczko.fast.core.model.error

data class AppError(
    val code: String?,
    val description: String?,
    val httpStatus: Int?,
    val diagnosticId: String?,
) {
    val displayMessage: String
        get() = description ?: code ?: "Unknown error"
}
