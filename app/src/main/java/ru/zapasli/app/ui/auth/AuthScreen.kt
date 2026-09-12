package ru.zapasli.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.zapasli.app.R
import ru.zapasli.app.core.designsystem.theme.ZapasliSpacing
import ru.zapasli.app.core.designsystem.theme.ZapasliTheme
import ru.zapasli.app.core.designsystem.theme.ZapasliInk
import ru.zapasli.app.core.designsystem.theme.ZapasliLime
import ru.zapasli.app.domain.auth.AuthFailure

@Composable
fun AuthScreen(
    state: AuthUiState,
    onModeSelected: (AuthMode) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZapasliSpacing.lg, vertical = ZapasliSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = ZapasliLime,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Z",
                        color = ZapasliInk,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(ZapasliSpacing.md))
            Text(
                text = stringResource(R.string.auth_welcome_title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(R.string.auth_welcome_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(ZapasliSpacing.lg))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(ZapasliSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(ZapasliSpacing.md),
                ) {
                    AuthModeSelector(state.mode, onModeSelected)

                    if (state.mode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("display_name_input"),
                            value = state.displayName,
                            onValueChange = onDisplayNameChanged,
                            label = { Text(stringResource(R.string.display_name_label)) },
                            singleLine = true,
                            enabled = !state.isSubmitting,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )
                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        value = state.email,
                        onValueChange = onEmailChanged,
                        label = { Text(stringResource(R.string.email_label)) },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        value = state.password,
                        onValueChange = onPasswordChanged,
                        label = { Text(stringResource(R.string.password_label)) },
                        supportingText = if (state.mode == AuthMode.REGISTER) {
                            { Text(stringResource(R.string.password_hint)) }
                        } else {
                            null
                        },
                        singleLine = true,
                        enabled = !state.isSubmitting,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    )

                    state.error?.let { error ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_error"),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                modifier = Modifier.padding(ZapasliSpacing.sm),
                                text = authErrorText(error),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit"),
                        onClick = onSubmit,
                        enabled = !state.isSubmitting,
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = stringResource(
                                    if (state.mode == AuthMode.LOGIN) R.string.sign_in else R.string.create_account,
                                ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(ZapasliSpacing.md))
            Text(
                text = stringResource(R.string.auth_security_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun AuthLoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_loading"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(ZapasliSpacing.md))
            Text(
                text = stringResource(R.string.restoring_session),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AuthModeSelector(
    selectedMode: AuthMode,
    onModeSelected: (AuthMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZapasliSpacing.xs),
    ) {
        AuthMode.entries.forEach { mode ->
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(
                            if (mode == AuthMode.LOGIN) R.string.sign_in else R.string.registration,
                        ),
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}

@Composable
private fun authErrorText(error: AuthUiError): String = when (error) {
    is AuthUiError.Validation -> stringResource(
        when (error.reason) {
            AuthValidationError.INVALID_EMAIL -> R.string.error_invalid_email
            AuthValidationError.EMPTY_PASSWORD -> R.string.error_empty_password
            AuthValidationError.PASSWORD_TOO_SHORT -> R.string.error_short_password
            AuthValidationError.PASSWORD_TOO_LONG -> R.string.error_long_password
            AuthValidationError.EMPTY_DISPLAY_NAME -> R.string.error_empty_display_name
            AuthValidationError.DISPLAY_NAME_TOO_LONG -> R.string.error_long_display_name
        },
    )
    is AuthUiError.Request -> stringResource(
        when (error.reason) {
            AuthFailure.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
            AuthFailure.EMAIL_ALREADY_REGISTERED -> R.string.error_email_registered
            AuthFailure.RATE_LIMITED -> R.string.error_rate_limited
            AuthFailure.NETWORK -> R.string.error_auth_network
            AuthFailure.SERVER -> R.string.error_auth_server
        },
    )
}

@Preview(name = "Sign in", showBackground = true, locale = "ru")
@Composable
private fun AuthScreenPreview() {
    ZapasliTheme {
        AuthScreen(
            state = AuthUiState(isRestoring = false),
            onModeSelected = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onDisplayNameChanged = {},
            onSubmit = {},
        )
    }
}
