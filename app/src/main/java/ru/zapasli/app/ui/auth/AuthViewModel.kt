package ru.zapasli.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.zapasli.app.domain.auth.AuthFailure
import ru.zapasli.app.domain.auth.AuthRepository
import ru.zapasli.app.domain.auth.AuthResult
import ru.zapasli.app.domain.auth.RestoreSessionResult
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = mutableUiState.asStateFlow()

    init {
        restoreSession()
    }

    fun setMode(mode: AuthMode) {
        mutableUiState.update {
            it.copy(mode = mode, password = "", error = null)
        }
    }

    fun setEmail(email: String) {
        mutableUiState.update { it.copy(email = email, error = null) }
    }

    fun setPassword(password: String) {
        mutableUiState.update { it.copy(password = password, error = null) }
    }

    fun setDisplayName(displayName: String) {
        mutableUiState.update { it.copy(displayName = displayName, error = null) }
    }

    fun submit() {
        val form = mutableUiState.value
        if (form.isSubmitting) return

        val validationError = validateAuthForm(
            mode = form.mode,
            email = form.email,
            password = form.password,
            displayName = form.displayName,
        )
        if (validationError != null) {
            mutableUiState.update { it.copy(error = AuthUiError.Validation(validationError)) }
            return
        }

        mutableUiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = try {
                withContext(Dispatchers.IO) {
                    when (form.mode) {
                        AuthMode.LOGIN -> authRepository.login(form.email, form.password)
                        AuthMode.REGISTER -> authRepository.register(
                            email = form.email,
                            password = form.password,
                            displayName = form.displayName,
                            locale = appLocale(),
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                AuthResult.Failure(AuthFailure.SERVER)
            }

            mutableUiState.update { state ->
                when (result) {
                    is AuthResult.Success -> state.copy(
                        session = result.session,
                        password = "",
                        isSubmitting = false,
                        error = null,
                    )
                    is AuthResult.Failure -> state.copy(
                        password = "",
                        isSubmitting = false,
                        error = AuthUiError.Request(result.reason),
                    )
                }
            }
        }
    }

    fun logout() {
        if (mutableUiState.value.isSubmitting) return
        mutableUiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { authRepository.logout() }
            mutableUiState.value = AuthUiState(
                isRestoring = false,
                mode = AuthMode.LOGIN,
            )
        }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { authRepository.restoreSession() }
            mutableUiState.update { state ->
                when (result) {
                    RestoreSessionResult.SignedOut -> state.copy(isRestoring = false)
                    is RestoreSessionResult.SignedIn -> state.copy(
                        isRestoring = false,
                        session = result.session,
                        isOfflineSession = result.isOffline,
                    )
                }
            }
        }
    }

    private fun appLocale(): String = if (Locale.getDefault().language == "en") "en" else "ru"
}
