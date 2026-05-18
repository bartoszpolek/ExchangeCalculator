package com.example.exchange.feature.exchange.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.exchange.core.designsystem.R as DesignSystemR
import com.example.exchange.core.designsystem.components.AmountField
import com.example.exchange.core.designsystem.components.AppBottomSheet
import com.example.exchange.core.designsystem.components.StatusBanner
import com.example.exchange.core.designsystem.components.SwapButton
import com.example.exchange.core.designsystem.theme.ExchangeTheme
import com.example.exchange.core.designsystem.tokens.Spacing
import com.example.exchange.feature.exchange.R
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.ui.components.CurrencyRow
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExchangeRoute(
    modifier: Modifier = Modifier,
    viewModel: ExchangeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExchangeScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    state: ExchangeUiState,
    onAction: (ExchangeAction) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.lg),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        val readyState = state as? ExchangeUiState.Ready
        val isNetworkBannerVisible = readyState?.isNetworkBannerVisible == true

        PullToRefreshBox(
            isRefreshing = readyState?.isRefreshing == true,
            onRefresh = { onAction(ExchangeAction.OnRefresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .safeDrawingPadding(),
                ) {
                    if (isNetworkBannerVisible) {
                        StatusBanner(
                            text = stringResource(R.string.exchange_network_banner),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(contentPadding)
                            .padding(
                                top = if (isNetworkBannerVisible) {
                                    Spacing.xl
                                } else {
                                    HeroTopSpacing
                                },
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.exchange_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        RateStatus(
                            rateState = readyState?.rateState ?: ExchangeRateUiState.Loading,
                            selectedCurrency = readyState?.selectedCurrency,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )

                        Spacer(modifier = Modifier.height(Spacing.xl))

                        // Loading still renders disabled amount fields, so keep the field order stable.
                        val topInput = readyState?.topInput ?: ExchangeInput.USDC
                        ExchangeAmountFields(
                            topField = amountFieldUi(
                                readyState = readyState,
                                input = topInput,
                            ),
                            bottomField = amountFieldUi(
                                readyState = readyState,
                                input = topInput.opposite(),
                            ),
                            onAmountChange = { input, value ->
                                onAction(ExchangeAction.OnAmountChange(input, value))
                            },
                            onSwapClick = { onAction(ExchangeAction.OnSwapClick) },
                            onCurrencyClick = { onAction(ExchangeAction.OnCurrencyClick) },
                        )
                    }
                }

                if (readyState?.isCurrencySheetVisible == true) {
                    AppBottomSheet(
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        title = stringResource(R.string.exchange_currency_sheet_title),
                        closeContentDescription = stringResource(
                            R.string.exchange_content_description_close_currency_sheet,
                        ),
                        onDismissRequest = { onAction(ExchangeAction.OnCurrencySheetDismiss) },
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = CurrencySheetMaxHeight),
                        ) {
                            items(
                                items = readyState.currencies,
                                key = { currency -> currency.code },
                            ) { currency ->
                                CurrencyRow(
                                    currencyCode = currency.code,
                                    flagRes = CurrencyFlag.resourceFor(currency),
                                    flagContentDescription = null,
                                    selected = currency == readyState.selectedCurrency,
                                    onClick = {
                                        onAction(ExchangeAction.OnCurrencySelected(currency))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RateStatus(
    rateState: ExchangeRateUiState,
    selectedCurrency: CurrencyCode?,
    modifier: Modifier = Modifier,
) {
    val loadingContentDescription = stringResource(R.string.exchange_rate_loading)

    when (rateState) {
        ExchangeRateUiState.Loading,
        ExchangeRateUiState.NetworkFailure -> {
            RateSkeleton(
                modifier = modifier.semantics {
                    contentDescription = loadingContentDescription
                },
            )
        }

        is ExchangeRateUiState.Available -> {
            if (selectedCurrency != null) {
                RateText(
                    midRate = rateState.midRate,
                    currencyCode = selectedCurrency.code,
                    modifier = modifier,
                )
            }
        }

        ExchangeRateUiState.Unavailable -> {
            Text(
                text = stringResource(R.string.exchange_rate_unavailable),
                modifier = modifier,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun RateSkeleton(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(144.dp)
            .height(20.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(100.dp),
            ),
    )
}

@Composable
private fun RateText(
    midRate: BigDecimal,
    currencyCode: String,
    modifier: Modifier = Modifier,
) {
    val numberFormat = remember { createRateNumberFormat() }
    val formattedRate = remember(midRate, numberFormat) {
        numberFormat.format(midRate)
    }

    Text(
        text = stringResource(
            R.string.exchange_rate_format,
            formattedRate,
            currencyCode,
        ),
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun ExchangeAmountFields(
    topField: AmountFieldUi,
    bottomField: AmountFieldUi,
    onAmountChange: (ExchangeInput, String) -> Unit,
    onSwapClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            ExchangeAmountField(
                field = topField,
                onAmountChange = onAmountChange,
                onCurrencyClick = onCurrencyClick,
            )

            ExchangeAmountField(
                field = bottomField,
                onAmountChange = onAmountChange,
                onCurrencyClick = onCurrencyClick,
            )
        }

        SwapButton(
            onClick = onSwapClick,
            contentDescription = stringResource(R.string.exchange_content_description_swap),
        )
    }
}

@Composable
private fun ExchangeAmountField(
    field: AmountFieldUi,
    onAmountChange: (ExchangeInput, String) -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AmountField(
        amount = field.amount,
        onAmountChange = { onAmountChange(field.input, it) },
        leadingContentDescription = field.currencyCode,
        amountContentDescription = stringResource(field.amountContentDescriptionRes),
        amountPrefix = AMOUNT_PREFIX,
        enabled = field.isAmountEnabled,
        onLeadingClick = onCurrencyClick,
        leadingEnabled = field.isAmountEnabled && field.isCurrencySelectorEnabled,
        modifier = modifier,
    ) {
        if (field.isCurrencyLoading) {
            CurrencySelectorSkeleton()
        } else {
            CurrencySelectorContent(
                currencyCode = field.currencyCode,
                flagRes = field.flagRes,
                showDropdown = field.isCurrencySelectorEnabled,
            )
        }
    }
}

@Composable
private fun CurrencySelectorSkeleton() {
    Box(
        modifier = Modifier
            .size(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
            ),
    )
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(14.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(100.dp),
            ),
    )
}

@Composable
private fun CurrencySelectorContent(
    currencyCode: String,
    @DrawableRes flagRes: Int?,
    showDropdown: Boolean,
) {
    if (flagRes != null) {
        Image(
            painter = painterResource(flagRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape),
        )
    }
    Text(
        text = currencyCode,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    if (showDropdown) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_chevron_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp),
        )
    }
}

private fun amountFieldUi(
    readyState: ExchangeUiState.Ready?,
    input: ExchangeInput,
): AmountFieldUi =
    when (input) {
        ExchangeInput.USDC ->
            AmountFieldUi(
                input = input,
                amount = readyState?.usdcAmount ?: ZERO_AMOUNT,
                currencyCode = USDC_DISPLAY_CODE,
                flagRes = CurrencyFlag.resourceFor(USDC_CURRENCY_CODE),
                amountContentDescriptionRes = R.string.exchange_content_description_usdc_amount,
                isAmountEnabled = readyState != null,
                isCurrencySelectorEnabled = false,
                isCurrencyLoading = false,
            )

        ExchangeInput.FIAT ->
            AmountFieldUi(
                input = input,
                amount = readyState?.fiatAmount ?: ZERO_AMOUNT,
                currencyCode = readyState?.selectedCurrency?.code.orEmpty(),
                flagRes = readyState?.selectedCurrency?.let(CurrencyFlag::resourceFor),
                amountContentDescriptionRes = R.string.exchange_content_description_fiat_amount,
                isAmountEnabled = readyState != null,
                isCurrencySelectorEnabled = readyState != null,
                isCurrencyLoading = readyState == null,
            )
    }

private fun createRateNumberFormat(): NumberFormat =
    NumberFormat.getNumberInstance(Locale.US).apply {
        isGroupingUsed = true
        maximumFractionDigits = RATE_MAX_FRACTION_DIGITS
        minimumFractionDigits = 0
        roundingMode = RoundingMode.HALF_EVEN
    }

private data class AmountFieldUi(
    val input: ExchangeInput,
    val amount: String,
    val currencyCode: String,
    @param:DrawableRes val flagRes: Int?,
    @param:StringRes val amountContentDescriptionRes: Int,
    val isAmountEnabled: Boolean,
    val isCurrencySelectorEnabled: Boolean,
    val isCurrencyLoading: Boolean,
)

private val USDC_CURRENCY_CODE = CurrencyCode("USDC")
private val HeroTopSpacing = 100.dp
private val CurrencySheetMaxHeight = 420.dp
private const val USDC_DISPLAY_CODE = "USDc"
private const val AMOUNT_PREFIX = "$"
private const val RATE_MAX_FRACTION_DIGITS = 4

@Preview(name = "Exchange screen")
@Composable
private fun ExchangeScreenPreview() {
    ExchangeTheme {
        ExchangeScreen(
            state = ExchangeUiState.Ready(
                currencies = listOf(
                    CurrencyCode("ARS"),
                    CurrencyCode("COP"),
                    CurrencyCode("MXN"),
                    CurrencyCode("BRL"),
                ),
                selectedCurrency = CurrencyCode("MXN"),
                rateState = ExchangeRateUiState.Available(BigDecimal("18.4097")),
                usdcAmount = "9999",
                fiatAmount = "184065.59",
            ),
            onAction = {},
        )
    }
}
