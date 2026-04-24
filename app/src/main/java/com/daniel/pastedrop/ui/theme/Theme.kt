package com.daniel.pastedrop.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF6FDFB0),
    onPrimary        = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513D),
    secondary        = Color(0xFF82CBFE),
    onSecondary      = Color(0xFF003550),
    background       = Color(0xFF0D1117),
    onBackground     = Color(0xFFE6EDF3),
    surface          = Color(0xFF161B22),
    onSurface        = Color(0xFFE6EDF3),
    surfaceVariant   = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF8B949E),
    error            = Color(0xFFFF7B72),
    outline          = Color(0xFF30363D)
)

@Composable
fun PasteDropTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
