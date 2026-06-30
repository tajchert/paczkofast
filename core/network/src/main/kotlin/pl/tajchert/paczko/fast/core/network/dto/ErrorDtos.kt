package pl.tajchert.paczko.fast.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    val error: String? = null,
    val description: String? = null,
    val status: Int? = null,
    val diagnosticId: String? = null,
)
