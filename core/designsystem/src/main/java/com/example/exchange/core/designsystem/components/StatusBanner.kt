package com.example.exchange.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@Composable
fun StatusBanner(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Preview(name = "StatusBanner", showBackground = true)
@Composable
private fun StatusBannerPreview() {
    ExchangeTheme {
        StatusBanner(
            text = "No connection. Pull down to try again.",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
