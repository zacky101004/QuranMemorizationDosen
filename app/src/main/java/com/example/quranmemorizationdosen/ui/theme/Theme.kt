package com.example.quranmemorizationdosen.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen,
    onPrimary = IslamicWhite,
    background = IslamicWhite
)

@Composable
fun QuranMemorizationTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}