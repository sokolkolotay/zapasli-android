package ru.zapasli.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ZapasliInk,
    onPrimary = ZapasliPaper,
    primaryContainer = ZapasliLime,
    onPrimaryContainer = ZapasliInk,
    secondary = ZapasliCoral,
    onSecondary = ZapasliInk,
    secondaryContainer = ColorTokens.lightCoralContainer,
    onSecondaryContainer = ZapasliInk,
    tertiary = ZapasliMint,
    onTertiary = ZapasliInk,
    tertiaryContainer = ColorTokens.lightMintContainer,
    onTertiaryContainer = ZapasliInk,
    background = ZapasliCream,
    onBackground = ZapasliInk,
    surface = ZapasliPaper,
    onSurface = ZapasliInk,
    surfaceVariant = Cream100,
    onSurfaceVariant = ZapasliInkSoft,
    outline = Ink300,
    outlineVariant = Ink150,
    error = Red500,
    onError = Cream0,
    errorContainer = Red50,
    onErrorContainer = Red600,
)

private val DarkColors = darkColorScheme(
    primary = ZapasliLime,
    onPrimary = ZapasliInk,
    primaryContainer = ColorTokens.darkLimeContainer,
    onPrimaryContainer = ZapasliLime,
    secondary = ZapasliCoral,
    onSecondary = ZapasliInk,
    secondaryContainer = ColorTokens.darkCoralContainer,
    onSecondaryContainer = ZapasliPaper,
    tertiary = ZapasliMint,
    onTertiary = ZapasliInk,
    tertiaryContainer = ColorTokens.darkMintContainer,
    onTertiaryContainer = ZapasliMint,
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
    val lightCoralContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD2)
    val lightMintContainer = androidx.compose.ui.graphics.Color(0xFFD3F7E9)
    val darkLimeContainer = androidx.compose.ui.graphics.Color(0xFF405628)
    val darkCoralContainer = androidx.compose.ui.graphics.Color(0xFF6B281D)
    val darkMintContainer = androidx.compose.ui.graphics.Color(0xFF174C3D)
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
