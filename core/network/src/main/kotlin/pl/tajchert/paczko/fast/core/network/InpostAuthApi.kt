package pl.tajchert.paczko.fast.core.network

import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsRequestDto
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsResponseDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenResponseDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Experimental integration for personal research/learning purposes only.
 * Paczkofast is unofficial and is not affiliated with, endorsed by, or
 * supported by the locker operator.
 */
interface InpostAuthApi {
    @POST("/v1/account")
    suspend fun requestSmsCode(@Body body: SendSmsCodeRequestDto)

    @POST("/v1/account/verification")
    suspend fun confirmSmsCode(@Body body: ConfirmSmsRequestDto): ConfirmSmsResponseDto

    @POST("/v1/authenticate")
    suspend fun refreshToken(@Body body: RefreshTokenRequestDto): RefreshTokenResponseDto
}
