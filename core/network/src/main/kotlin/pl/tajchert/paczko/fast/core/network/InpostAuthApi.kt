package pl.tajchert.paczko.fast.core.network

import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsRequestDto
import pl.tajchert.paczko.fast.core.network.dto.ConfirmSmsResponseDto
import pl.tajchert.paczko.fast.core.network.dto.RefreshTokenRequestDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeRequestDto
import pl.tajchert.paczko.fast.core.network.dto.SendSmsCodeResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface InpostAuthApi {
    @POST("/v1/account")
    suspend fun requestSmsCode(@Body body: SendSmsCodeRequestDto): SendSmsCodeResponseDto

    @POST("/v1/account/verification")
    suspend fun confirmSmsCode(@Body body: ConfirmSmsRequestDto): ConfirmSmsResponseDto

    @POST("/v1/authenticate")
    suspend fun refreshToken(@Body body: RefreshTokenRequestDto): ConfirmSmsResponseDto
}
