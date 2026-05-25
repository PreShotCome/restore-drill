package com.lethe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LetheColors = darkColorScheme(
    background = LetheMidnight,
    onBackground = LetheCream,
    surface = LetheMidnightSoft,
    onSurface = LetheCream,
    surfaceVariant = LetheMidnightDeep,
    onSurfaceVariant = LetheCreamMuted,
    primary = LetheKeep,
    onPrimary = LetheMidnight,
    secondary = LetheDiscard,
    onSecondary = LetheCream,
)

@Composable
fun LetheTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LetheColors, content = content)
}
