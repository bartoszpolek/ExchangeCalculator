package com.example.exchange.feature.exchange.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.feature.exchange.R
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class ExchangeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val robot by lazy { ExchangeScreenRobot(composeRule) }

    @Test
    fun amountInputEmitsAmountChangeAction() {
        robot
            .render(
                state = readyState(
                    usdcAmount = "9",
                    fiatAmount = "165.69",
                ),
            )
            .replaceUsdcAmount("12")
            .assertActions(
                ExchangeAction.OnAmountChange(ExchangeInput.USDC, "12"),
            )
    }

    @Test
    fun availableRateShowsFormattedRate() {
        robot
            .render(
                state = readyState(
                    rateState = ExchangeRateUiState.Available(
                        midRate = BigDecimal("18.4097"),
                        formattedRate = "18.4097",
                    ),
                ),
            )
            .assertText(
                R.string.exchange_rate_format,
                "18.4097",
                MXN.code,
            )
    }

    @Test
    fun swapButtonEmitsSwapAction() {
        robot
            .render(state = readyState())
            .clickSwap()
            .assertActions(ExchangeAction.OnSwapClick)
    }

    @Test
    fun loadingRateShowsLoadingIndicator() {
        robot
            .render(state = ExchangeUiState.Loading)
            .assertRateLoading()
    }

    @Test
    fun networkErrorShowsConnectionBannerAndRateSkeleton() {
        robot
            .render(
                state = readyState(
                    isNetworkBannerVisible = true,
                    rateState = ExchangeRateUiState.NetworkFailure,
                ),
            )
            .assertText(R.string.exchange_network_banner)
            .assertRateLoading()
    }

    @Test
    fun unavailableRateShowsPairErrorInRateArea() {
        robot
            .render(state = readyState(rateState = ExchangeRateUiState.Unavailable))
            .assertText(R.string.exchange_rate_unavailable)
    }

    @Test
    fun fiatSelectorEmitsCurrencyClickAction() {
        robot
            .render(state = readyState())
            .clickFiatSelector(MXN)
            .assertActions(ExchangeAction.OnCurrencyClick)
    }

    @Test
    fun currencySheetShowsCurrenciesAndEmitsSelection() {
        robot
            .render(state = readyState(isCurrencySheetVisible = true))
            .assertText(R.string.exchange_currency_sheet_title)
            .clickCurrency(ARS)
            .assertActions(ExchangeAction.OnCurrencySelected(ARS))
    }

    @Test
    fun currencySheetCloseEmitsDismissAction() {
        robot
            .render(state = readyState(isCurrencySheetVisible = true))
            .clickCloseSheet()
            .assertActions(ExchangeAction.OnCurrencySheetDismiss)
    }

    private fun readyState(
        usdcAmount: String = "9999",
        fiatAmount: String = "184065.59",
        isCurrencySheetVisible: Boolean = false,
        isNetworkBannerVisible: Boolean = false,
        rateState: ExchangeRateUiState = ExchangeRateUiState.Available(
            midRate = BigDecimal("18.4097"),
            formattedRate = "18.4097",
        ),
    ): ExchangeUiState =
        ExchangeUiState.Ready(
            currencies = listOf(MXN, ARS, BRL, COP),
            selectedCurrency = MXN,
            rateState = rateState,
            usdcAmount = usdcAmount,
            fiatAmount = fiatAmount,
            isNetworkBannerVisible = isNetworkBannerVisible,
            isCurrencySheetVisible = isCurrencySheetVisible,
        )

    private companion object {
        val MXN = CurrencyCode("MXN")
        val ARS = CurrencyCode("ARS")
        val BRL = CurrencyCode("BRL")
        val COP = CurrencyCode("COP")
    }
}

private class ExchangeScreenRobot(
    private val composeRule: ComposeContentTestRule,
) {

    private val actions = mutableListOf<ExchangeAction>()

    fun render(state: ExchangeUiState) = apply {
        actions.clear()
        composeRule.setContent {
            ExchangeTheme {
                ExchangeScreen(
                    state = state,
                    onAction = actions::add,
                )
            }
        }
    }

    fun replaceUsdcAmount(value: String) = apply {
        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_content_description_usdc_amount),
        )
            .performTextReplacement(value)
        composeRule.waitForIdle()
    }

    fun clickSwap() = apply {
        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_content_description_swap),
        ).performClick()
    }

    fun clickFiatSelector(currency: CurrencyCode) = apply {
        composeRule.onNodeWithContentDescription(currency.code)
            .performClick()
    }

    fun clickCurrency(currency: CurrencyCode) = apply {
        composeRule.onNodeWithText(currency.code)
            .performClick()
    }

    fun clickCloseSheet() = apply {
        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_content_description_close_currency_sheet),
        ).performClick()
    }

    fun assertRateLoading() = apply {
        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_rate_loading),
        ).assertIsDisplayed()
    }

    fun assertText(
        @StringRes id: Int,
        vararg formatArgs: Any,
    ) = apply {
        composeRule.onNodeWithText(stringResource(id, *formatArgs))
            .assertIsDisplayed()
    }

    fun assertActions(vararg expected: ExchangeAction) = apply {
        assertEquals(expected.toList(), actions)
    }

    private fun stringResource(
        @StringRes id: Int,
        vararg formatArgs: Any,
    ): String =
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getString(id, *formatArgs)
}
