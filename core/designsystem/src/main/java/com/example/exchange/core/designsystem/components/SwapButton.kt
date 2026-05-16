package com.example.exchange.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.R
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@Composable
fun SwapButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_swap_vertical),
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Preview(name = "SwapButton", showBackground = true)
@Composable
private fun SwapButtonPreview() {
    ExchangeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SwapButton(
                onClick = {},
                contentDescription = "Swap currencies",
                modifier = Modifier.padding(Spacing.lg),
            )
        }
    }
}
