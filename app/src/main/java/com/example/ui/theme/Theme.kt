package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Skema warna Web3 gelap mutlak untuk getaran desentralisasi premium
private val Web3DarkColorScheme = darkColorScheme(
    primary = EthereumPurple,
    onPrimary = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    tertiary = Web3Green,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFECEFF1),
    surface = DarkSurface,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCFD8DC),
    outline = Color(0xFF455A64)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Web3DarkColorScheme,
        typography = Typography,
        content = content
    )
}
