package com.lathe.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    background = LatheBackground,
    surface = LatheSurface,
    onSurface = LatheOnSurface,
    primary = LatheGreen,
    secondary = LatheRed,
)

private val LightColors = lightColorScheme(
    primary = LatheGreen,
    secondary = LatheRed,
)

@Composable
fun LatheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = scheme, content = content)
}
