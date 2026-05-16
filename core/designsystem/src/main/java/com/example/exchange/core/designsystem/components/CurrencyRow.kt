package com.example.exchange.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.R
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@Composable
fun CurrencyRow(
    currencyCode: String,
    @DrawableRes flagRes: Int,
    flagContentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        FlagIcon(
            flagRes = flagRes,
            contentDescription = flagContentDescription,
        )
        Text(
            text = currencyCode,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        SelectionIndicator(selected = selected)
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    val size = 24.dp
    if (selected) {
        Image(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            modifier = Modifier.size(size),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .border(
                    BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    CircleShape,
                ),
        )
    }
}

@Preview(name = "CurrencyRow · list", showBackground = true)
@Composable
private fun CurrencyRowPreview() {
    ExchangeTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column {
                CurrencyRow(
                    currencyCode = "ARS",
                    flagRes = R.drawable.ic_flag_ars,
                    flagContentDescription = "Argentine Peso",
                    onClick = {},
                )
                CurrencyRow(
                    currencyCode = "EURc",
                    flagRes = R.drawable.ic_flag_eurc,
                    flagContentDescription = "Euro Coin",
                    onClick = {},
                )
                CurrencyRow(
                    currencyCode = "COP",
                    flagRes = R.drawable.ic_flag_cop,
                    flagContentDescription = "Colombian Peso",
                    onClick = {},
                )
                CurrencyRow(
                    currencyCode = "MXN",
                    flagRes = R.drawable.ic_flag_mxn,
                    flagContentDescription = "Mexican Peso",
                    onClick = {},
                    selected = true,
                )
                CurrencyRow(
                    currencyCode = "BRL",
                    flagRes = R.drawable.ic_flag_brl,
                    flagContentDescription = "Brazilian Real",
                    onClick = {},
                )
            }
        }
    }
}
