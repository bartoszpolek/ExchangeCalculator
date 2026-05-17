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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val currencyListRepository: CurrencyListRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
    private val calculateMidRate: CalculateMidRateUseCase,
    private val convertAmount: ConvertAmountUseCase,
) : ViewModel() {

    private val amountFormatter = AmountFormatter()

    private val _state = MutableStateFlow(ExchangeUiState())
    val state = _state.asStateFlow()

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
                _state.update { it.copy(isCurrencySheetVisible = true) }

            ExchangeAction.OnCurrencySheetDismiss ->
                _state.update { it.copy(isCurrencySheetVisible = false) }

            is ExchangeAction.OnCurrencySelected ->
                onCurrencySelected(action.currency)

            ExchangeAction.OnRetryClick ->
                fetchSelectedRate()

            ExchangeAction.OnUserMessageShown ->
                userMessageShown()
        }
    }

    private fun loadCurrencies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCurrencies = true) }

            val currencies = currencyListRepository.getCurrencies()
            val selectedCurrency = _state.value.selectedCurrency
                ?.takeIf(currencies::contains)
                ?: currencies.firstOrNull()

            _state.update {
                it.copy(
                    currencies = currencies,
                    selectedCurrency = selectedCurrency,
                    isLoadingCurrencies = false,
                )
            }

            selectedCurrency?.let(::fetchRate)
        }
    }

    private fun onAmountChange(
        input: ExchangeInput,
        value: String,
    ) {
        _state.update {
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
        _state.update {
            it.copy(
                topInput = when (it.topInput) {
                    ExchangeInput.USDC -> ExchangeInput.FIAT
                    ExchangeInput.FIAT -> ExchangeInput.USDC
                },
            )
        }

        recalculateConvertedAmount(_state.value.activeInput)
    }

    private fun onCurrencySelected(currency: CurrencyCode) {
        val current = _state.value.selectedCurrency
        if (current == currency) {
            _state.update { it.copy(isCurrencySheetVisible = false) }
            return
        }

        _state.update {
            it.copy(
                selectedCurrency = currency,
                isCurrencySheetVisible = false,
            )
        }
        fetchRate(currency)
    }

    private fun fetchSelectedRate() {
        _state.value.selectedCurrency?.let(::fetchRate)
    }

    private fun fetchRate(currency: CurrencyCode) {
        viewModelScope.launch {
            _state.update {
                it.withClearedConvertedAmount()
                    .copy(rateState = ExchangeRateUiState.Loading)
            }

            when (val result = exchangeRatesRepository.getRate(currency)) {
                is RateFetchResult.Available -> {
                    if (_state.value.selectedCurrency != currency) return@launch

                    val midRate = calculateMidRate(
                        ask = result.rate.ask,
                        bid = result.rate.bid,
                    )
                    _state.update {
                        it.copy(
                            rateState = ExchangeRateUiState.Available(midRate),
                            userMessage = null,
                        )
                    }
                    recalculateConvertedAmount(_state.value.activeInput)
                }

                RateFetchResult.Unavailable -> {
                    if (_state.value.selectedCurrency != currency) return@launch

                    _state.update {
                        it.withClearedConvertedAmount()
                            .copy(rateState = ExchangeRateUiState.Unavailable)
                    }
                }

                RateFetchResult.NetworkFailure -> {
                    if (_state.value.selectedCurrency != currency) return@launch

                    _state.update {
                        it.withClearedConvertedAmount()
                            .copy(
                                rateState = ExchangeRateUiState.Unavailable,
                                userMessage = ExchangeUserMessageType.RATE_NETWORK_ERROR,
                            )
                    }
                }
            }
        }
    }

    private fun recalculateConvertedAmount(input: ExchangeInput) {
        val currentState = _state.value
        val rateState = currentState.rateState as? ExchangeRateUiState.Available
        val amount = amountFormatter.parse(currentState.amountFor(input))

        if (rateState == null || amount == null) {
            _state.update { it.withClearedConvertedAmount(input) }
            return
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
        val formattedAmount = amountFormatter.format(
            amount = convertedAmount,
            scale = DISPLAY_SCALE,
        )

        _state.update {
            when (input) {
                ExchangeInput.USDC -> it.copy(fiatAmount = formattedAmount)
                ExchangeInput.FIAT -> it.copy(usdcAmount = formattedAmount)
            }
        }
    }

    private fun ExchangeUiState.amountFor(input: ExchangeInput): String =
        when (input) {
            ExchangeInput.USDC -> usdcAmount
            ExchangeInput.FIAT -> fiatAmount
        }

    private fun ExchangeUiState.withClearedConvertedAmount(
        input: ExchangeInput = activeInput,
    ): ExchangeUiState =
        when (input) {
            ExchangeInput.USDC -> copy(fiatAmount = "")
            ExchangeInput.FIAT -> copy(usdcAmount = "")
        }

    private fun userMessageShown() {
        _state.update { it.copy(userMessage = null) }
    }

    private companion object {
        const val DISPLAY_SCALE = 2
    }
}
