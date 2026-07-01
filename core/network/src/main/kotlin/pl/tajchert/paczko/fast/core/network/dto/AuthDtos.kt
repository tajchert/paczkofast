package pl.tajchert.paczko.fast.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PhoneNumberDto(
    val prefix: String,
    val value: String,
)

@Serializable
data class SendSmsCodeRequestDto(
    val phoneNumber: PhoneNumberDto,
)

@Serializable
data class ConfirmSmsRequestDto(
    val phoneNumber: PhoneNumberDto,
    val smsCode: String,
    val devicePlatform: String,
)

@Serializable
data class ConfirmSmsResponseDto(
    val authToken: String,
    val refreshToken: String,
)

@Serializable
data class RefreshTokenResponseDto(
    val authToken: String,
    val refreshToken: String? = null,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
    val phoneOS: String,
)
