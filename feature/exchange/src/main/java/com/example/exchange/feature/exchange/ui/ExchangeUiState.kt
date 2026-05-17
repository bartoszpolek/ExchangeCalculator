package com.example.exchange.feature.exchange.ui

import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import java.math.BigDecimal

data class ExchangeUiState(
    val currencies: List<CurrencyCode> = emptyList(),
    val selectedCurrency: CurrencyCode? = null,
    val isLoadingCurrencies: Boolean = true,
    val rateState: ExchangeRateUiState = ExchangeRateUiState.Loading,
    val topInput: ExchangeInput = ExchangeInput.USDC,
    val activeInput: ExchangeInput = ExchangeInput.USDC,
    val usdcAmount: String = "",
    val fiatAmount: String = "",
    val isCurrencySheetVisible: Boolean = false,
    val userMessage: ExchangeUserMessageType? = null,
)

enum class ExchangeInput {
    USDC,
    FIAT,
}

sealed interface ExchangeRateUiState {
    data object Loading : ExchangeRateUiState
    data class Available(val midRate: BigDecimal) : ExchangeRateUiState
    data object Unavailable : ExchangeRateUiState
}

sealed interface ExchangeAction {
    data class OnAmountChange(
        val input: ExchangeInput,
        val value: String,
    ) : ExchangeAction

    data object OnSwapClick : ExchangeAction
    data object OnCurrencyClick : ExchangeAction
    data object OnCurrencySheetDismiss : ExchangeAction
    data class OnCurrencySelected(val currency: CurrencyCode) : ExchangeAction
    data object OnRetryClick : ExchangeAction
    data object OnUserMessageShown : ExchangeAction
}

enum class ExchangeUserMessageType {
    RATE_NETWORK_ERROR,
}
