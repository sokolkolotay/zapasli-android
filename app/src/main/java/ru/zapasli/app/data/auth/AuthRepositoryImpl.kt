package ru.zapasli.app.data.auth

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import retrofit2.Response
import ru.zapasli.app.domain.auth.AuthFailure
import ru.zapasli.app.domain.auth.AuthRepository
import ru.zapasli.app.domain.auth.AuthResult
import ru.zapasli.app.domain.auth.AuthSession
import ru.zapasli.app.domain.auth.AuthUser
import ru.zapasli.app.domain.auth.RestoreSessionResult
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val storage: SessionStorage,
    private val json: Json,
) : AuthRepository {
    override suspend fun restoreSession(): RestoreSessionResult {
        val cached = storage.read() ?: return RestoreSessionResult.SignedOut

        return try {
            val profileResponse = api.currentUser(cached.bearerToken())
            when {
                profileResponse.isSuccessful -> {
                    val profile = profileResponse.body()?.toDomain()
                        ?: return RestoreSessionResult.SignedIn(cached, isOffline = true)
                    val verified = cached.copy(user = profile)
                    storage.write(verified)
                    RestoreSessionResult.SignedIn(verified, isOffline = false)
                }
                profileResponse.code() == HTTP_UNAUTHORIZED -> refreshOrRestoreOffline(cached)
                else -> RestoreSessionResult.SignedIn(cached, isOffline = true)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            RestoreSessionResult.SignedIn(cached, isOffline = true)
        }
    }

    override suspend fun login(email: String, password: String): AuthResult = performAuth {
        api.login(LoginRequestDto(email.trim().lowercase(), password))
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
        locale: String,
    ): AuthResult = performAuth {
        api.register(
            RegisterRequestDto(
                email = email.trim().lowercase(),
                password = password,
                displayName = displayName.trim(),
                locale = locale,
            ),
        )
    }

    override suspend fun logout() {
        val current = storage.read()
        try {
            if (current != null) {
                api.logout(LogoutRequestDto(current.refreshToken))
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Local credentials are still removed when the server is temporarily unavailable.
        } finally {
            storage.clear()
        }
    }

    private suspend fun refreshOrRestoreOffline(cached: AuthSession): RestoreSessionResult {
        val response = api.refresh(RefreshRequestDto(cached.refreshToken))
        return when {
            response.isSuccessful -> {
                val refreshed = response.body()?.toDomain()
                    ?: return RestoreSessionResult.SignedIn(cached, isOffline = true)
                storage.write(refreshed)
                RestoreSessionResult.SignedIn(refreshed, isOffline = false)
            }
            response.code() == HTTP_UNAUTHORIZED -> {
                storage.clear()
                RestoreSessionResult.SignedOut
            }
            else -> RestoreSessionResult.SignedIn(cached, isOffline = true)
        }
    }

    private suspend fun performAuth(
        request: suspend () -> Response<AuthResponseDto>,
    ): AuthResult = try {
        val response = request()
        if (response.isSuccessful) {
            val session = response.body()?.toDomain()
                ?: return AuthResult.Failure(AuthFailure.SERVER)
            storage.write(session)
            AuthResult.Success(session)
        } else {
            AuthResult.Failure(response.toFailure())
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: IOException) {
        AuthResult.Failure(AuthFailure.NETWORK)
    } catch (_: Exception) {
        AuthResult.Failure(AuthFailure.SERVER)
    }

    private fun Response<*>.toFailure(): AuthFailure {
        val apiCode = runCatching {
            errorBody()?.string()?.let { json.decodeFromString<ApiErrorResponseDto>(it).error.code }
        }.getOrNull()

        return when {
            apiCode == "INVALID_CREDENTIALS" -> AuthFailure.INVALID_CREDENTIALS
            apiCode == "EMAIL_ALREADY_REGISTERED" -> AuthFailure.EMAIL_ALREADY_REGISTERED
            code() == HTTP_TOO_MANY_REQUESTS -> AuthFailure.RATE_LIMITED
            code() >= HTTP_SERVER_ERROR -> AuthFailure.SERVER
            else -> AuthFailure.SERVER
        }
    }

    private fun AuthSession.bearerToken() = "Bearer $accessToken"

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val HTTP_SERVER_ERROR = 500
    }
}

private fun UserDto.toDomain() = AuthUser(
    id = id,
    displayName = displayName,
    locale = locale,
)

private fun AuthResponseDto.toDomain() = AuthSession(
    user = user.toDomain(),
    accessToken = accessToken,
    accessTokenExpiresAt = accessTokenExpiresAt,
    refreshToken = refreshToken,
)
