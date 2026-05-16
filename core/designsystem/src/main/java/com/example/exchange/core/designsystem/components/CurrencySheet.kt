package com.example.exchange.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.exchange.core.designsystem.R
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySheet(
    sheetState: SheetState,
    title: String,
    closeContentDescription: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = MaterialTheme.colorScheme.scrim,
    ) {
        CurrencySheetHeader(
            title = title,
            closeContentDescription = closeContentDescription,
            onClose = onDismissRequest,
        )
        CurrencySheetListCard(content = content)
    }
}

@Composable
private fun CurrencySheetHeader(
    title: String,
    closeContentDescription: String,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onClose) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = closeContentDescription,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun CurrencySheetListCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(content = content)
    }
}

@Preview(name = "CurrencySheet · expanded", showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun CurrencySheetPreview() {
    val shape = MaterialTheme.shapes.extraLarge.let { s ->
        (s as? CornerBasedShape)?.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize,
        ) ?: s
    }
    ExchangeTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = shape,
        ) {
            Column {
                CurrencySheetHeader(
                    title = "Choose currency",
                    closeContentDescription = "Close",
                    onClose = {},
                )
                CurrencySheetListCard {
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
}
