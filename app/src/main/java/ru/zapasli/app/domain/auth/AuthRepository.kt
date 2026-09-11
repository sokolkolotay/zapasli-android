package ru.zapasli.app.domain.auth

interface AuthRepository {
    suspend fun restoreSession(): RestoreSessionResult

    suspend fun login(email: String, password: String): AuthResult

    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        locale: String,
    ): AuthResult

    suspend fun logout()
}
