package com.nextersolutions.soulmetric.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Purple700,
    onPrimary = Color.White,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple800,
    secondary = BlueViolet,
    onSecondary = Color.White,
    background = Background,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Purple50,
    onSurfaceVariant = OnSurface60,
    outline = Divider,
    error = Error,
    onError = Color.White
)

@Composable
fun SoulMetricTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Using light theme only for v1; extend for dark theme support
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
