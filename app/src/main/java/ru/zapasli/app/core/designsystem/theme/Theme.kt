package ru.zapasli.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green700,
    onPrimary = Cream0,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Orange500,
    onSecondary = Cream0,
    secondaryContainer = Orange100,
    onSecondaryContainer = Ink900,
    tertiary = Blue500,
    onTertiary = Cream0,
    tertiaryContainer = Blue50,
    onTertiaryContainer = Ink900,
    background = Cream50,
    onBackground = Ink900,
    surface = Cream0,
    onSurface = Ink900,
    surfaceVariant = Cream100,
    onSurfaceVariant = Ink700,
    outline = Ink300,
    outlineVariant = Ink150,
    error = Red500,
    onError = Cream0,
    errorContainer = Red50,
    onErrorContainer = Red600,
)

private val DarkColors = darkColorScheme(
    primary = Green300,
    onPrimary = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Orange300,
    onSecondary = Ink900,
    secondaryContainer = ColorTokens.darkOrangeContainer,
    onSecondaryContainer = Orange100,
    tertiary = ColorTokens.darkBlue,
    onTertiary = Ink900,
    tertiaryContainer = ColorTokens.darkBlueContainer,
    onTertiaryContainer = Blue50,
    background = DarkBackground,
    onBackground = Ink100,
    surface = DarkSurface,
    onSurface = Ink100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Ink150,
    outline = Ink500,
    outlineVariant = Ink700,
    error = ColorTokens.darkRed,
    onError = Ink900,
    errorContainer = ColorTokens.darkRedContainer,
    onErrorContainer = Red50,
)

private object ColorTokens {
    val darkOrangeContainer = androidx.compose.ui.graphics.Color(0xFF5A3217)
    val darkBlue = androidx.compose.ui.graphics.Color(0xFF9DBBFF)
    val darkBlueContainer = androidx.compose.ui.graphics.Color(0xFF183663)
    val darkRed = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
    val darkRedContainer = androidx.compose.ui.graphics.Color(0xFF6E201A)
}

@Composable
fun ZapasliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ZapasliTypography,
        shapes = ZapasliShapes,
        content = content,
    )
}
