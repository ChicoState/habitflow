package com.example.habitflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = hf_mid_blue,
    onPrimary = hf_text_primary,
    secondary = hf_teal,
    onSecondary = hf_text_primary,
    background = hf_background,
    onBackground = hf_text_primary,
    surface = hf_surface,
    onSurface = hf_text_primary,
    primaryContainer = hf_primary_container_dark,
    onPrimaryContainer = hf_text_primary,
)

private val LightColorScheme = lightColorScheme(
    primary = hf_teal,
    onPrimary = hf_dark_blue,
    secondary = hf_mid_blue,
    onSecondary = hf_text_primary,
    background = Color(0xFFFFFFFF),
    onBackground = hf_dark_blue,
    surface = Color(0xFFF5F5F5),
    onSurface = hf_dark_blue,
    primaryContainer = hf_primary_container_light,
    onPrimaryContainer = hf_dark_blue,
)

@Composable
fun HabitflowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
