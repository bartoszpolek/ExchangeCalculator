package com.example.exchange.feature.exchange.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchange.core.common.format.AmountFormatter
import com.example.exchange.core.common.format.ZERO_AMOUNT
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
        val currentState = currentReadyState() ?: return
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
        val currentState = currentReadyState() ?: return

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
        val currentState = currentReadyState() ?: return
        if (currentState.isRefreshing) return

        fetchRate(currency = currentState.selectedCurrency, isRefresh = true)
    }

    private fun fetchRate(
        currency: CurrencyCode,
        isRefresh: Boolean = false,
    ) {
        rateFetchJob?.cancel()
        rateFetchJob = viewModelScope.launch {
            val previousRateState = currentReadyState()?.rateState ?: ExchangeRateUiState.Loading
            updateReady {
                it.withRateFetchStarted(
                    isRefresh = isRefresh,
                    convertedAmountReplacement = it.convertedAmountReplacement(),
                )
            }

            val nextRateState = fetchRateUiState(currency)

            updateReadyForCurrency(currency) { state ->
                state.withRateFetchResult(
                    nextRateState = nextRateState,
                    previousRateState = previousRateState,
                    isRefresh = isRefresh,
                    convertedAmountReplacement = state.convertedAmountReplacement(),
                )
            }
            val inputToRecalculate = currentReadyState()
                ?.takeIf { state ->
                    nextRateState is ExchangeRateUiState.Available && state.selectedCurrency == currency
                }
                ?.activeInput
            inputToRecalculate?.let(::recalculateConvertedAmount)
        }
    }

    private suspend fun fetchRateUiState(currency: CurrencyCode): ExchangeRateUiState =
        when (val result = exchangeRatesRepository.getRate(currency)) {
            is RateFetchResult.Available -> {
                val midRate = calculateMidRate(
                    ask = result.rate.ask,
                    bid = result.rate.bid,
                )
                ExchangeRateUiState.Available(
                    midRate = midRate,
                    formattedRate = amountFormatter.formatRate(midRate),
                )
            }

            RateFetchResult.Unavailable -> ExchangeRateUiState.Unavailable
            RateFetchResult.NetworkFailure -> ExchangeRateUiState.NetworkFailure
        }

    private fun recalculateConvertedAmount(input: ExchangeInput) {
        updateReady {
            val rateState = it.rateState as? ExchangeRateUiState.Available
            val amount = amountFormatter.parse(it.amountForInput(input))

            if (rateState == null || amount == null) {
                return@updateReady it.withResetConvertedAmountForInput(
                    input = input,
                    replacement = it.convertedAmountReplacement(input),
                )
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
            val formattedAmount = amountFormatter.formatDisplayAmount(convertedAmount)

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

    private fun updateReadyForCurrency(
        currency: CurrencyCode,
        transform: (ExchangeUiState.Ready) -> ExchangeUiState.Ready,
    ) {
        updateReady { state ->
            if (state.selectedCurrency == currency) {
                transform(state)
            } else {
                state
            }
        }
    }

    private fun currentReadyState(): ExchangeUiState.Ready? =
        _state.value as? ExchangeUiState.Ready

    private fun ExchangeUiState.Ready.convertedAmountReplacement(
        input: ExchangeInput = activeInput,
    ): String =
        if (amountFormatter.parse(amountForInput(input))?.compareTo(BigDecimal.ZERO) == 0) {
            ZERO_AMOUNT
        } else {
            ""
        }
}
