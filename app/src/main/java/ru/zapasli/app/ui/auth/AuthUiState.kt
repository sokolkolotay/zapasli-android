package ru.zapasli.app.ui.auth

import ru.zapasli.app.domain.auth.AuthFailure
import ru.zapasli.app.domain.auth.AuthSession

data class AuthUiState(
    val isRestoring: Boolean = true,
    val session: AuthSession? = null,
    val isOfflineSession: Boolean = false,
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isSubmitting: Boolean = false,
    val error: AuthUiError? = null,
)

enum class AuthMode {
    LOGIN,
    REGISTER,
}

sealed interface AuthUiError {
    data class Validation(val reason: AuthValidationError) : AuthUiError
    data class Request(val reason: AuthFailure) : AuthUiError
}

enum class AuthValidationError {
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    PASSWORD_TOO_SHORT,
    PASSWORD_TOO_LONG,
    EMPTY_DISPLAY_NAME,
    DISPLAY_NAME_TOO_LONG,
}

internal fun validateAuthForm(
    mode: AuthMode,
    email: String,
    password: String,
    displayName: String,
): AuthValidationError? = when {
    !EMAIL_PATTERN.matches(email.trim()) -> AuthValidationError.INVALID_EMAIL
    password.isEmpty() -> AuthValidationError.EMPTY_PASSWORD
    password.length > MAX_PASSWORD_LENGTH -> AuthValidationError.PASSWORD_TOO_LONG
    mode == AuthMode.REGISTER && password.length < MIN_REGISTRATION_PASSWORD_LENGTH ->
        AuthValidationError.PASSWORD_TOO_SHORT
    mode == AuthMode.REGISTER && displayName.isBlank() -> AuthValidationError.EMPTY_DISPLAY_NAME
    mode == AuthMode.REGISTER && displayName.trim().length > MAX_DISPLAY_NAME_LENGTH ->
        AuthValidationError.DISPLAY_NAME_TOO_LONG
    else -> null
}

private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private const val MIN_REGISTRATION_PASSWORD_LENGTH = 12
private const val MAX_PASSWORD_LENGTH = 128
private const val MAX_DISPLAY_NAME_LENGTH = 80
