package com.lethe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = LetheBackground,
    surface = LetheSurface,
    onSurface = LetheOnSurface,
    primary = LetheGreen,
    secondary = LetheRed,
)

private val LightColors = lightColorScheme(
    primary = LetheGreen,
    secondary = LetheRed,
)

@Composable
fun LetheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = scheme, content = content)
}
