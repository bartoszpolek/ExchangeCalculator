package com.example.exchange.feature.exchange.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing
import com.example.exchange.feature.exchange.R

@Composable
fun FlagIcon(
    @DrawableRes flagRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    flagSize: Dp = 28.dp,
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(flagRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(flagSize)
                .clip(CircleShape),
        )
    }
}

@Preview(name = "FlagIcon", showBackground = true)
@Composable
private fun FlagIconPreview() {
    ExchangeTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.padding(Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlagIcon(
                    flagRes = R.drawable.ic_flag_mxn,
                    contentDescription = "Mexican Peso",
                )
                FlagIcon(
                    flagRes = R.drawable.ic_flag_ars,
                    contentDescription = "Argentine Peso",
                    containerSize = 32.dp,
                    flagSize = 24.dp,
                )
            }
        }
    }
}
