package com.example.exchange.feature.exchange.ui

import androidx.compose.runtime.Stable
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import java.math.BigDecimal

@Stable
sealed interface ExchangeUiState {
    data object Loading : ExchangeUiState

    @Stable
    data class Ready(
        val currencies: List<CurrencyCode>,
        val selectedCurrency: CurrencyCode,
        val isRefreshing: Boolean = false,
        // Refresh can fail while the previous available rate remains visible.
        val isNetworkBannerVisible: Boolean = false,
        val rateState: ExchangeRateUiState = ExchangeRateUiState.Loading,
        // topInput controls visual order after swap; activeInput tracks the edited side.
        val topInput: ExchangeInput = ExchangeInput.USDC,
        val activeInput: ExchangeInput = topInput,
        val usdcAmount: String = ZERO_AMOUNT,
        val fiatAmount: String = ZERO_AMOUNT,
        val isCurrencySheetVisible: Boolean = false,
    ) : ExchangeUiState
}

enum class ExchangeInput {
    USDC,
    FIAT,
}

internal fun ExchangeUiState.Ready.amountForInput(input: ExchangeInput): String =
    when (input) {
        ExchangeInput.USDC -> usdcAmount
        ExchangeInput.FIAT -> fiatAmount
    }

internal fun ExchangeInput.opposite(): ExchangeInput =
    when (this) {
        ExchangeInput.USDC -> ExchangeInput.FIAT
        ExchangeInput.FIAT -> ExchangeInput.USDC
    }

@Stable
sealed interface ExchangeRateUiState {
    data object Loading : ExchangeRateUiState
    @Stable
    data class Available(val midRate: BigDecimal) : ExchangeRateUiState
    data object Unavailable : ExchangeRateUiState
    data object NetworkFailure : ExchangeRateUiState
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
    data object OnRefresh : ExchangeAction
}

internal const val ZERO_AMOUNT = "0"
