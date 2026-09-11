package ru.zapasli.app.data.auth

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

internal interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): Response<AuthResponseDto>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): Response<Unit>

    @GET("api/v1/me")
    suspend fun currentUser(@Header("Authorization") authorization: String): Response<UserDto>
}

@Serializable
internal data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
    val locale: String,
)

@Serializable
internal data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
internal data class RefreshRequestDto(val refreshToken: String)

@Serializable
internal data class LogoutRequestDto(val refreshToken: String)

@Serializable
internal data class UserDto(
    val id: String,
    val displayName: String,
    val locale: String,
)

@Serializable
internal data class AuthResponseDto(
    val user: UserDto,
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
)

@Serializable
internal data class ApiErrorResponseDto(val error: ApiErrorDto)

@Serializable
internal data class ApiErrorDto(
    val code: String,
    val message: String,
)
