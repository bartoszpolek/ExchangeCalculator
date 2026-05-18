package com.example.exchange.feature.exchange.ui

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
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
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExchangeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun amountInputEmitsAmountChangeAction() {
        val actions = render(
            state = readyState(
                usdcAmount = "9",
                fiatAmount = "165.69",
            ),
        )

        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_content_description_usdc_amount),
        )
            .performTextReplacement("12")
        composeRule.waitForIdle()

        assertEquals(
            listOf(ExchangeAction.OnAmountChange(ExchangeInput.USDC, "12")),
            actions,
        )
    }

    @Test
    fun swapButtonEmitsSwapAction() {
        val actions = render(state = readyState())

        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_content_description_swap),
        ).performClick()

        assertEquals(listOf(ExchangeAction.OnSwapClick), actions)
    }

    @Test
    fun loadingRateShowsLoadingIndicator() {
        render(
            state = ExchangeUiState.Loading,
        )

        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_rate_loading),
        ).assertIsDisplayed()
    }

    @Test
    fun networkErrorShowsConnectionBannerAndRateSkeleton() {
        render(
            state = readyState(
                isNetworkBannerVisible = true,
                rateState = ExchangeRateUiState.NetworkFailure,
            ),
        )

        composeRule.onNodeWithText(stringResource(R.string.exchange_network_banner))
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_rate_loading),
        ).assertIsDisplayed()
    }

    @Test
    fun unavailableRateShowsPairErrorInRateArea() {
        render(state = readyState(rateState = ExchangeRateUiState.Unavailable))

        composeRule.onNodeWithText(stringResource(R.string.exchange_rate_unavailable))
            .assertIsDisplayed()
    }

    @Test
    fun fiatSelectorEmitsCurrencyClickAction() {
        val actions = render(state = readyState())

        composeRule.onNodeWithContentDescription(MXN.code)
            .performClick()

        assertEquals(listOf(ExchangeAction.OnCurrencyClick), actions)
    }

    @Test
    fun currencySheetShowsCurrenciesAndEmitsSelection() {
        val actions = render(state = readyState(isCurrencySheetVisible = true))

        composeRule.onNodeWithText(stringResource(R.string.exchange_currency_sheet_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(ARS.code)
            .performClick()

        assertEquals(listOf(ExchangeAction.OnCurrencySelected(ARS)), actions)
    }

    @Test
    fun currencySheetCloseEmitsDismissAction() {
        val actions = render(state = readyState(isCurrencySheetVisible = true))

        composeRule.onNodeWithContentDescription(
            label = stringResource(R.string.exchange_content_description_close_currency_sheet),
        ).performClick()

        assertEquals(listOf(ExchangeAction.OnCurrencySheetDismiss), actions)
    }

    private fun render(state: ExchangeUiState): MutableList<ExchangeAction> {
        val actions = mutableListOf<ExchangeAction>()
        composeRule.setContent {
            ExchangeTheme {
                ExchangeScreen(
                    state = state,
                    onAction = actions::add,
                )
            }
        }
        return actions
    }

    private fun readyState(
        usdcAmount: String = "9999",
        fiatAmount: String = "184065.59",
        isCurrencySheetVisible: Boolean = false,
        isNetworkBannerVisible: Boolean = false,
        rateState: ExchangeRateUiState = ExchangeRateUiState.Available(BigDecimal("18.4097")),
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

    private fun stringResource(@StringRes id: Int): String =
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getString(id)

    private companion object {
        val MXN = CurrencyCode("MXN")
        val ARS = CurrencyCode("ARS")
        val BRL = CurrencyCode("BRL")
        val COP = CurrencyCode("COP")
    }
}
