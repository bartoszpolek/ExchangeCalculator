package com.example.exchange.feature.exchange.ui

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate
import com.example.exchange.feature.exchange.domain.model.RateFetchResult
import com.example.exchange.feature.exchange.domain.repository.CurrencyListRepository
import com.example.exchange.feature.exchange.domain.repository.ExchangeRatesRepository
import com.example.exchange.feature.exchange.domain.usecase.CalculateMidRateUseCase
import com.example.exchange.feature.exchange.domain.usecase.ConvertAmountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var previousLocale: Locale

    @Before fun setUp() {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        Dispatchers.setMain(testDispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(previousLocale)
    }

    @Test fun `loads currencies in init and fetches first currency rate`() = runTest {
        val currencyRepository = FakeCurrencyListRepository(
            currencies = listOf(MXN, ARS),
        )
        val ratesRepository = FakeExchangeRatesRepository(
            rateResults = mutableMapOf(
                MXN to RateFetchResult.Available(exchangeRate(MXN)),
            ),
        )
        val viewModel = createViewModel(
            currencyRepository = currencyRepository,
            ratesRepository = ratesRepository,
        )

        viewModel.state.test {
            val state = awaitItem()

            assertThat(state.isLoadingCurrencies).isFalse()
            assertThat(state.currencies).containsExactly(MXN, ARS)
            assertThat(state.selectedCurrency).isEqualTo(MXN)
            assertThat(state.rateState.availableMidRate()).isEqualByComparingTo("18.4087350000")

            cancelAndIgnoreRemainingEvents()
        }
        assertThat(ratesRepository.requestedCurrencies).containsExactly(MXN)
    }

    @Test fun `typing USDC amount converts fiat amount`() = runTest {
        val viewModel = createReadyViewModel()

        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "12.34",
            ),
        )

        assertThat(viewModel.state.value.activeInput).isEqualTo(ExchangeInput.USDC)
        assertThat(viewModel.state.value.usdcAmount).isEqualTo("12.34")
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("24.68")
    }

    @Test fun `typing fiat amount converts USDC amount`() = runTest {
        val viewModel = createReadyViewModel()

        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.FIAT,
                value = "12.34",
            ),
        )

        assertThat(viewModel.state.value.activeInput).isEqualTo(ExchangeInput.FIAT)
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("12.34")
        assertThat(viewModel.state.value.usdcAmount).isEqualTo("6.17")
    }

    @Test fun `empty active amount clears converted amount`() = runTest {
        val viewModel = createReadyViewModel()
        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "10",
            ),
        )

        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "",
            ),
        )

        assertThat(viewModel.state.value.usdcAmount).isEqualTo("")
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("")
    }

    @Test fun `swap toggles input positions without changing selected currency`() = runTest {
        val viewModel = createReadyViewModel()
        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "10",
            ),
        )

        viewModel.onAction(ExchangeAction.OnSwapClick)

        assertThat(viewModel.state.value.topInput).isEqualTo(ExchangeInput.FIAT)
        assertThat(viewModel.state.value.activeInput).isEqualTo(ExchangeInput.USDC)
        assertThat(viewModel.state.value.selectedCurrency).isEqualTo(MXN)
        assertThat(viewModel.state.value.usdcAmount).isEqualTo("10")
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("20.00")
    }

    @Test fun `selecting currency fetches new rate and recalculates current amount`() = runTest {
        val ratesRepository = FakeExchangeRatesRepository(
            rateResults = mutableMapOf(
                MXN to RateFetchResult.Available(exchangeRate(MXN, ask = "2", bid = "2")),
                ARS to RateFetchResult.Available(exchangeRate(ARS, ask = "3", bid = "3")),
            ),
        )
        val viewModel = createViewModel(ratesRepository = ratesRepository)
        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "10",
            ),
        )

        viewModel.onAction(ExchangeAction.OnCurrencySelected(ARS))

        assertThat(ratesRepository.requestedCurrencies).containsExactly(MXN, ARS)
        assertThat(viewModel.state.value.selectedCurrency).isEqualTo(ARS)
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("30.00")
    }

    @Test fun `unavailable selected rate clears converted amount`() = runTest {
        val ratesRepository = FakeExchangeRatesRepository(
            rateResults = mutableMapOf(
                MXN to RateFetchResult.Available(exchangeRate(MXN, ask = "2", bid = "2")),
                ARS to RateFetchResult.Unavailable,
            ),
        )
        val viewModel = createViewModel(ratesRepository = ratesRepository)
        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "10",
            ),
        )

        viewModel.onAction(ExchangeAction.OnCurrencySelected(ARS))

        assertThat(viewModel.state.value.rateState).isEqualTo(ExchangeRateUiState.Unavailable)
        assertThat(viewModel.state.value.usdcAmount).isEqualTo("10")
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("")
    }

    @Test fun `network failure stores user message and retry fetches selected currency again`() = runTest {
        val ratesRepository = FakeExchangeRatesRepository(
            rateResults = mutableMapOf(
                MXN to RateFetchResult.Available(exchangeRate(MXN, ask = "2", bid = "2")),
                ARS to RateFetchResult.NetworkFailure,
            ),
        )
        val viewModel = createViewModel(ratesRepository = ratesRepository)
        viewModel.onAction(
            ExchangeAction.OnAmountChange(
                input = ExchangeInput.USDC,
                value = "10",
            ),
        )

        viewModel.onAction(ExchangeAction.OnCurrencySelected(ARS))

        assertThat(viewModel.state.value.rateState).isEqualTo(ExchangeRateUiState.Unavailable)
        assertThat(viewModel.state.value.userMessage).isEqualTo(ExchangeUserMessageType.RATE_NETWORK_ERROR)

        viewModel.onAction(ExchangeAction.OnUserMessageShown)
        assertThat(viewModel.state.value.userMessage).isEqualTo(null)

        viewModel.onAction(ExchangeAction.OnRetryClick)

        assertThat(ratesRepository.requestedCurrencies).containsExactly(MXN, ARS, ARS)
        assertThat(viewModel.state.value.userMessage).isEqualTo(
            ExchangeUserMessageType.RATE_NETWORK_ERROR,
        )

        ratesRepository.rateResults[ARS] = RateFetchResult.Available(
            exchangeRate(ARS, ask = "4", bid = "4"),
        )
        viewModel.onAction(ExchangeAction.OnRetryClick)

        assertThat(ratesRepository.requestedCurrencies).containsExactly(MXN, ARS, ARS, ARS)
        assertThat(viewModel.state.value.rateState.availableMidRate()).isEqualByComparingTo("4")
        assertThat(viewModel.state.value.fiatAmount).isEqualTo("40.00")
        assertThat(viewModel.state.value.userMessage).isEqualTo(null)
    }

    private fun createReadyViewModel(): ExchangeViewModel =
        createViewModel(
            ratesRepository = FakeExchangeRatesRepository(
                rateResults = mutableMapOf(
                    MXN to RateFetchResult.Available(
                        exchangeRate(MXN, ask = "2", bid = "2"),
                    ),
                ),
            ),
        )

    private fun createViewModel(
        currencyRepository: FakeCurrencyListRepository = FakeCurrencyListRepository(
            currencies = listOf(MXN, ARS),
        ),
        ratesRepository: FakeExchangeRatesRepository = FakeExchangeRatesRepository(
            rateResults = mutableMapOf(
                MXN to RateFetchResult.Available(exchangeRate(MXN)),
            ),
        ),
    ): ExchangeViewModel =
        ExchangeViewModel(
            currencyListRepository = currencyRepository,
            exchangeRatesRepository = ratesRepository,
            calculateMidRate = CalculateMidRateUseCase(),
            convertAmount = ConvertAmountUseCase(),
        )

    private fun ExchangeRateUiState.availableMidRate(): BigDecimal =
        (this as ExchangeRateUiState.Available).midRate

    private companion object {
        val MXN = CurrencyCode("MXN")
        val ARS = CurrencyCode("ARS")

        fun exchangeRate(
            currencyCode: CurrencyCode,
            ask: String = "18.4105000000",
            bid: String = "18.4069700000",
        ): ExchangeRate =
            ExchangeRate(
                currencyCode = currencyCode,
                ask = BigDecimal(ask),
                bid = BigDecimal(bid),
            )
    }
}

private class FakeCurrencyListRepository(
    private val currencies: List<CurrencyCode>,
) : CurrencyListRepository {

    override suspend fun getCurrencies(): List<CurrencyCode> = currencies
}

private class FakeExchangeRatesRepository(
    val rateResults: MutableMap<CurrencyCode, RateFetchResult>,
) : ExchangeRatesRepository {

    val requestedCurrencies = mutableListOf<CurrencyCode>()

    override suspend fun getRate(currency: CurrencyCode): RateFetchResult {
        requestedCurrencies += currency
        return rateResults[currency] ?: RateFetchResult.Unavailable
    }
}
