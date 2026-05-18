package com.example.exchange.feature.exchange.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchange.core.common.format.AmountFormatter
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.RateFetchResult
import com.example.exchange.feature.exchange.domain.repository.CurrencyListRepository
import com.example.exchange.feature.exchange.domain.repository.ExchangeRatesRepository
import com.example.exchange.feature.exchange.domain.usecase.CalculateMidRateUseCase
import com.example.exchange.feature.exchange.domain.usecase.ConvertAmountUseCase
import com.example.exchange.feature.exchange.domain.usecase.ConvertDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val currencyListRepository: CurrencyListRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val calculateMidRate: CalculateMidRateUseCase,
    private val convertAmount: ConvertAmountUseCase,
    private val amountFormatter: AmountFormatter,
) : ViewModel() {

    private val _state = MutableStateFlow<ExchangeUiState>(ExchangeUiState.Loading)
    val state = _state.asStateFlow()
    private var rateFetchJob: Job? = null

    init {
        loadCurrencies()
    }

    fun onAction(action: ExchangeAction) {
        when (action) {
            is ExchangeAction.OnAmountChange ->
                onAmountChange(action.input, action.value)

            ExchangeAction.OnSwapClick ->
                onSwapClick()

            ExchangeAction.OnCurrencyClick ->
                updateReady { it.copy(isCurrencySheetVisible = true) }

            ExchangeAction.OnCurrencySheetDismiss ->
                updateReady { it.copy(isCurrencySheetVisible = false) }

            is ExchangeAction.OnCurrencySelected ->
                onCurrencySelected(action.currency)

            ExchangeAction.OnRefresh ->
                refreshSelectedRate()
        }
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            val currencies = currencyListRepository.getCurrencies()
            val selectedCurrency = currencies.firstOrNull() ?: return@launch

            _state.update {
                ExchangeUiState.Ready(
                    currencies = currencies,
                    selectedCurrency = selectedCurrency,
                )
            }
            fetchRate(selectedCurrency)
        }
    }

    private fun onAmountChange(
        input: ExchangeInput,
        value: String,
    ) {
        updateReady {
            when (input) {
                ExchangeInput.USDC ->
                    it.copy(
                        activeInput = input,
                        usdcAmount = value,
                    )

                ExchangeInput.FIAT ->
                    it.copy(
                        activeInput = input,
                        fiatAmount = value,
                    )
            }
        }

        recalculateConvertedAmount(input)
    }

    private fun onSwapClick() {
        val currentState = _state.value as? ExchangeUiState.Ready ?: return
        val newTopInput = currentState.topInput.opposite()

        updateReady {
            it.copy(
                topInput = newTopInput,
                activeInput = newTopInput,
            )
        }

        recalculateConvertedAmount(newTopInput)
    }

    private fun onCurrencySelected(currency: CurrencyCode) {
        val currentState = _state.value as? ExchangeUiState.Ready ?: return

        if (currentState.selectedCurrency == currency) {
            updateReady { it.copy(isCurrencySheetVisible = false) }
            return
        }

        updateReady {
            it.copy(
                selectedCurrency = currency,
                isCurrencySheetVisible = false,
            )
        }
        fetchRate(currency)
    }

    private fun refreshSelectedRate() {
        val currentState = _state.value as? ExchangeUiState.Ready ?: return
        if (currentState.isRefreshing) return

        fetchRate(currency = currentState.selectedCurrency, isRefresh = true)
    }

    private fun fetchRate(
        currency: CurrencyCode,
        isRefresh: Boolean = false,
    ) {
        rateFetchJob?.cancel()
        rateFetchJob = viewModelScope.launch {
            val previousRateState = (_state.value as? ExchangeUiState.Ready)?.rateState
                ?: ExchangeRateUiState.Loading
            updateReady { it.withRateFetchStarted(isRefresh) }

            val result = exchangeRatesRepository.getRate(currency)
            val midRate = (result as? RateFetchResult.Available)?.let { available ->
                calculateMidRate(
                    ask = available.rate.ask,
                    bid = available.rate.bid,
                )
            }

            updateReady { state ->
                if (state.selectedCurrency != currency) {
                    state
                } else {
                    state.withRateFetchResult(
                        result = result,
                        midRate = midRate,
                        previousRateState = previousRateState,
                        isRefresh = isRefresh,
                    )
                }
            }
            val inputToRecalculate = (_state.value as? ExchangeUiState.Ready)
                ?.takeIf { state ->
                    result is RateFetchResult.Available && state.selectedCurrency == currency
                }
                ?.activeInput
            inputToRecalculate?.let(::recalculateConvertedAmount)
        }
    }

    private fun recalculateConvertedAmount(input: ExchangeInput) {
        updateReady {
            val rateState = it.rateState as? ExchangeRateUiState.Available
            val amount = amountFormatter.parse(it.amountForInput(input))

            if (rateState == null || amount == null) {
                return@updateReady it.withResetConvertedAmountForInput(input)
            }

            val direction = when (input) {
                ExchangeInput.USDC -> ConvertDirection.USDC_TO_FIAT
                ExchangeInput.FIAT -> ConvertDirection.FIAT_TO_USDC
            }
            val convertedAmount = convertAmount(
                amount = amount,
                midRate = rateState.midRate,
                direction = direction,
            )
            val formattedAmount = convertedAmount.toDisplayAmount()

            when (input) {
                ExchangeInput.USDC -> it.copy(fiatAmount = formattedAmount)
                ExchangeInput.FIAT -> it.copy(usdcAmount = formattedAmount)
            }
        }
    }

    private fun updateReady(
        transform: (ExchangeUiState.Ready) -> ExchangeUiState.Ready,
    ) {
        _state.update { state ->
            when (state) {
                ExchangeUiState.Loading -> state
                is ExchangeUiState.Ready -> transform(state)
            }
        }
    }

    private fun ExchangeUiState.Ready.withRateFetchStarted(
        isRefresh: Boolean,
    ): ExchangeUiState.Ready =
        if (isRefresh) {
            copy(isRefreshing = true)
        } else {
            withResetConvertedAmountForInput()
                .copy(
                    isRefreshing = false,
                    isNetworkBannerVisible = false,
                    rateState = ExchangeRateUiState.Loading,
                )
        }

    private fun ExchangeUiState.Ready.withRateFetchResult(
        result: RateFetchResult,
        midRate: BigDecimal?,
        previousRateState: ExchangeRateUiState,
        isRefresh: Boolean,
    ): ExchangeUiState.Ready {
        val resultState = when (result) {
            is RateFetchResult.Available -> copy(
                isNetworkBannerVisible = false,
                rateState = ExchangeRateUiState.Available(checkNotNull(midRate)),
            )

            RateFetchResult.Unavailable -> withResetConvertedAmountForInput()
                .copy(
                    isNetworkBannerVisible = false,
                    rateState = ExchangeRateUiState.Unavailable,
                )

            RateFetchResult.NetworkFailure ->
                if (isRefresh && previousRateState is ExchangeRateUiState.Available) {
                    copy(
                        isNetworkBannerVisible = true,
                        rateState = previousRateState,
                    )
                } else {
                    withResetConvertedAmountForInput()
                        .copy(
                            isNetworkBannerVisible = true,
                            rateState = ExchangeRateUiState.NetworkFailure,
                        )
                }
        }
        return resultState.copy(isRefreshing = false)
    }

    private fun ExchangeUiState.Ready.withResetConvertedAmountForInput(
        input: ExchangeInput = activeInput,
    ): ExchangeUiState.Ready {
        val convertedAmountReplacement =
            if (amountFormatter.parse(amountForInput(input))?.compareTo(BigDecimal.ZERO) == 0) {
                ZERO_AMOUNT
            } else {
                ""
            }

        return when (input) {
            ExchangeInput.USDC -> copy(fiatAmount = convertedAmountReplacement)
            ExchangeInput.FIAT -> copy(usdcAmount = convertedAmountReplacement)
        }
    }

    private fun BigDecimal.toDisplayAmount(): String =
        if (compareTo(BigDecimal.ZERO) == 0) {
            ZERO_AMOUNT
        } else {
            setScale(DISPLAY_SCALE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
                .toPlainString()
        }

    private companion object {
        const val DISPLAY_SCALE = 2
    }
}
