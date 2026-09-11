package ru.zapasli.app.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthUiStateTest {
    @Test
    fun `login accepts an existing password of any non-zero length`() {
        assertNull(
            validateAuthForm(
                mode = AuthMode.LOGIN,
                email = "user@example.com",
                password = "old-password",
                displayName = "",
            ),
        )
    }

    @Test
    fun `registration requires a twelve character password`() {
        assertEquals(
            AuthValidationError.PASSWORD_TOO_SHORT,
            validateAuthForm(
                mode = AuthMode.REGISTER,
                email = "user@example.com",
                password = "short",
                displayName = "Alex",
            ),
        )
    }

    @Test
    fun `registration validates name and email`() {
        assertEquals(
            AuthValidationError.INVALID_EMAIL,
            validateAuthForm(
                mode = AuthMode.REGISTER,
                email = "not-an-email",
                password = "long-enough-password",
                displayName = "Alex",
            ),
        )
        assertEquals(
            AuthValidationError.EMPTY_DISPLAY_NAME,
            validateAuthForm(
                mode = AuthMode.REGISTER,
                email = "user@example.com",
                password = "long-enough-password",
                displayName = "  ",
            ),
        )
    }

    @Test
    fun `valid registration has no validation error`() {
        assertNull(
            validateAuthForm(
                mode = AuthMode.REGISTER,
                email = "user@example.com",
                password = "long-enough-password",
                displayName = "Alex",
            ),
        )
    }
}
