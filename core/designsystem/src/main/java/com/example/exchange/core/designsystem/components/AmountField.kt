package com.example.exchange.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.R
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    currencyCode: String,
    @DrawableRes flagRes: Int,
    flagContentDescription: String?,
    currencySelectorContentDescription: String,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier,
    amountContentDescription: String? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Row(
                modifier = Modifier
                    .clickable(
                        onClick = onCurrencyClick,
                        role = Role.Button,
                    )
                    .semantics {
                        contentDescription = currencySelectorContentDescription
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Image(
                    painter = painterResource(flagRes),
                    contentDescription = flagContentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape),
                )
                Text(
                    text = currencyCode,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (amountContentDescription != null) {
                            Modifier.semantics {
                                contentDescription = amountContentDescription
                            }
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

@Preview(name = "AmountField", showBackground = true)
@Composable
private fun AmountFieldPreview() {
    ExchangeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AmountField(
                amount = "100.00",
                onAmountChange = {},
                currencyCode = "USDc",
                flagRes = R.drawable.ic_flag_usdc,
                flagContentDescription = "USD Coin",
                currencySelectorContentDescription = "Selected: USDc, tap to change",
                amountContentDescription = "Amount you send",
                onCurrencyClick = {},
                modifier = Modifier.padding(Spacing.lg).height(66.dp),
            )
        }
    }
}
