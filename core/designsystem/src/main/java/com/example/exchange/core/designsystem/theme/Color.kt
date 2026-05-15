package com.example.exchange.core.designsystem.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val Background = Color(0xFFF8F8F8)
internal val Surface = Color(0xFFFFFFFF)
internal val SurfaceVariant = Color(0xFFF4F4F4)
internal val TextPrimary = Color(0xFF2C2C2E)
internal val TextSecondary = Color(0xFF949494)
internal val AccentBlue = Color(0xFF2E7DF6)
internal val AccentGreen = Color(0xFF22D081)
internal val OutlineSubtle = Color(0x1A000000)
internal val Scrim = Color(0x80000000)
internal val ErrorRed = Color(0xFFB3261E)

val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = AccentGreen,
    onSecondary = Color.White,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle,
    outlineVariant = OutlineSubtle,
    scrim = Scrim,
    error = ErrorRed,
    onError = Color.White,
)
