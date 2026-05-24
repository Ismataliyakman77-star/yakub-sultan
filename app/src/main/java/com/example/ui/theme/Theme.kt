package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberBlue,
    secondary = CyberBlueDark,
    tertiary = StarkGold,
    background = DarkBackground,
    surface = TranslucentSteel,
    onPrimary = Color(0xFF010409),
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = HologramText,
    onSurface = HologramText
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    // J.A.R.V.I.S. uses a cohesive premium high-contrast holographic dark mode consistently
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
