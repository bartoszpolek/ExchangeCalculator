package com.example.exchange.feature.exchange.ui

import androidx.annotation.DrawableRes
import com.example.exchange.core.designsystem.R as DesignSystemR
import com.example.exchange.feature.exchange.domain.model.CurrencyCode

object CurrencyFlag {

    @DrawableRes
    fun resourceFor(code: CurrencyCode): Int =
        when (code.code) {
            "MXN" -> DesignSystemR.drawable.ic_flag_mxn
            "ARS" -> DesignSystemR.drawable.ic_flag_ars
            "BRL" -> DesignSystemR.drawable.ic_flag_brl
            "COP" -> DesignSystemR.drawable.ic_flag_cop
            "USDC" -> DesignSystemR.drawable.ic_flag_usdc
            else -> DesignSystemR.drawable.ic_flag_unknown
        }
}
