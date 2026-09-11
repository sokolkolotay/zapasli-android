package ru.zapasli.app.domain.auth

data class AuthUser(
    val id: String,
    val displayName: String,
    val locale: String,
)

data class AuthSession(
    val user: AuthUser,
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
)

sealed interface RestoreSessionResult {
    data object SignedOut : RestoreSessionResult
    data class SignedIn(
        val session: AuthSession,
        val isOffline: Boolean,
    ) : RestoreSessionResult
}

sealed interface AuthResult {
    data class Success(val session: AuthSession) : AuthResult
    data class Failure(val reason: AuthFailure) : AuthResult
}

enum class AuthFailure {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_REGISTERED,
    RATE_LIMITED,
    NETWORK,
    SERVER,
}
