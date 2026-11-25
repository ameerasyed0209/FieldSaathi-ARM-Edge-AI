package com.baasc.krishisaathi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ButtonGreen,
    onPrimary = TextLight,
    primaryContainer = InputBackground,
    onPrimaryContainer = TextPrimary,
    secondary = ForestGreen,
    onSecondary = TextLight,
    background = CardBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    error = ErrorColor
)

private val DarkColorScheme = darkColorScheme(
    primary = LightLeafGreen,
    onPrimary = DeepGreen,
    secondary = ForestGreen,
    background = DeepGreen,
    surface = DeepGreen,
    error = ErrorColor
)

@Composable
fun KrishiSaathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
