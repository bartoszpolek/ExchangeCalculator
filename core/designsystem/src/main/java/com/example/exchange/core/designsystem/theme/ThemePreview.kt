package com.example.exchange.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.tokens.Spacing

@Preview(name = "Theme", showBackground = true)
@Composable
private fun ThemePreview() {
    ExchangeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Text("Exchange calculator", style = MaterialTheme.typography.headlineLarge)
                Text("Choose currency", style = MaterialTheme.typography.headlineSmall)
                Text("1 234,56", style = MaterialTheme.typography.titleLarge)
                Text("USDC", style = MaterialTheme.typography.bodyLarge)
                Swatches()
            }
        }
    }
}

@Composable
private fun Swatches() {
    val scheme = MaterialTheme.colorScheme
    val items = listOf(
        "primary" to scheme.primary,
        "secondary" to scheme.secondary,
        "surface" to scheme.surface,
        "surfaceVariant" to scheme.surfaceVariant,
        "outline" to scheme.outline,
        "error" to scheme.error,
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items.forEach { (label, color) ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color, MaterialTheme.shapes.small),
                )
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
