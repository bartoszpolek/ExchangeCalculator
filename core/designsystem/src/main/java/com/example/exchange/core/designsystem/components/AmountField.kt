package com.example.exchange.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing

@Composable
fun AmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    leadingContentDescription: String,
    onLeadingClick: () -> Unit,
    modifier: Modifier = Modifier,
    amountContentDescription: String? = null,
    amountPrefix: String = "",
    enabled: Boolean = true,
    leadingEnabled: Boolean = true,
    leadingContent: @Composable RowScope.() -> Unit,
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = amount,
                selection = TextRange(amount.length),
            ),
        )
    }

    LaunchedEffect(amount) {
        if (textFieldValue.text != amount) {
            textFieldValue = TextFieldValue(
                text = amount,
                selection = TextRange(amount.length),
            )
        }
    }

    fun handleAmountValueChange(proposedValue: TextFieldValue) {
        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = textFieldValue,
        )
        textFieldValue = normalizedValue
        if (normalizedValue.text != amount) {
            onAmountChange(normalizedValue.text)
        }
    }

    fun handleFocusChanged(focusState: FocusState) {
        if (!enabled || focusState.isFocused) return

        val finalizedAmount = textFieldValue.text.finalizeAmountInput()
        if (finalizedAmount != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = finalizedAmount,
                selection = TextRange(finalizedAmount.length),
            )
        }
        if (finalizedAmount != amount) {
            onAmountChange(finalizedAmount)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Row(
                modifier = Modifier
                    .width(91.dp)
                    .leadingContainer(
                        enabled = leadingEnabled,
                        contentDescription = leadingContentDescription,
                        onClick = onLeadingClick,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                leadingContent()
            }
            BasicTextField(
                value = textFieldValue,
                onValueChange = ::handleAmountValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = AmountVisualTransformation(prefix = amountPrefix),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged(::handleFocusChanged)
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

private fun Modifier.leadingContainer(
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
): Modifier =
    if (enabled) {
        this.clickable(
            onClick = onClick,
            role = Role.Button,
        ).semantics {
            this.contentDescription = contentDescription
        }
    } else {
        this
    }

@Preview(name = "AmountField", showBackground = true)
@Composable
private fun AmountFieldPreview() {
    ExchangeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AmountField(
                amount = "100.00",
                onAmountChange = {},
                leadingContentDescription = "Selected asset, tap to change",
                amountContentDescription = "Amount you send",
                amountPrefix = "$",
                onLeadingClick = {},
                modifier = Modifier.padding(Spacing.lg).height(66.dp),
            ) {
                Text(
                    text = "USDc",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
