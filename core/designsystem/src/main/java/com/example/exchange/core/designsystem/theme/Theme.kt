package com.example.exchange.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ExchangeTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = ExchangeTypography,
        shapes = ExchangeShapes,
        content = content,
    )
}
