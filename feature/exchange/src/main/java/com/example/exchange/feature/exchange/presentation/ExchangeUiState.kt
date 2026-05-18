package com.example.exchange.feature.exchange.presentation

import com.example.exchange.core.common.format.ZERO_AMOUNT
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import java.math.BigDecimal

sealed interface ExchangeUiState {
    data object Loading : ExchangeUiState

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

internal fun ExchangeUiState.Ready.withRateFetchStarted(
    isRefresh: Boolean,
    convertedAmountReplacement: String,
): ExchangeUiState.Ready =
    if (isRefresh) {
        copy(isRefreshing = true)
    } else {
        withResetConvertedAmountForInput(replacement = convertedAmountReplacement)
            .copy(
                isRefreshing = false,
                isNetworkBannerVisible = false,
                rateState = ExchangeRateUiState.Loading,
            )
    }

internal fun ExchangeUiState.Ready.withRateFetchResult(
    nextRateState: ExchangeRateUiState,
    previousRateState: ExchangeRateUiState,
    isRefresh: Boolean,
    convertedAmountReplacement: String,
): ExchangeUiState.Ready {
    val resultState = when (nextRateState) {
        is ExchangeRateUiState.Available -> copy(
            isNetworkBannerVisible = false,
            rateState = nextRateState,
        )

        ExchangeRateUiState.Unavailable -> withResetConvertedAmountForInput(
            replacement = convertedAmountReplacement,
        ).copy(
            isNetworkBannerVisible = false,
            rateState = ExchangeRateUiState.Unavailable,
        )

        ExchangeRateUiState.NetworkFailure ->
            if (isRefresh && previousRateState is ExchangeRateUiState.Available) {
                copy(
                    isNetworkBannerVisible = true,
                    rateState = previousRateState,
                )
            } else {
                withResetConvertedAmountForInput(
                    replacement = convertedAmountReplacement,
                ).copy(
                    isNetworkBannerVisible = true,
                    rateState = ExchangeRateUiState.NetworkFailure,
                )
            }

        ExchangeRateUiState.Loading -> copy(rateState = ExchangeRateUiState.Loading)
    }
    return resultState.copy(isRefreshing = false)
}

internal fun ExchangeUiState.Ready.withResetConvertedAmountForInput(
    replacement: String,
    input: ExchangeInput = activeInput,
): ExchangeUiState.Ready =
    when (input) {
        ExchangeInput.USDC -> copy(fiatAmount = replacement)
        ExchangeInput.FIAT -> copy(usdcAmount = replacement)
    }

sealed interface ExchangeRateUiState {
    data object Loading : ExchangeRateUiState

    data class Available(
        val midRate: BigDecimal,
        val formattedRate: String,
    ) : ExchangeRateUiState

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
