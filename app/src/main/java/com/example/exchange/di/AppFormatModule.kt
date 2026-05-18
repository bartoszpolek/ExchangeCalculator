package com.example.exchange.di

import com.example.exchange.core.common.format.AmountFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppFormatModule {

    @Provides
    @Singleton
    fun provideAmountFormatter(): AmountFormatter =
        AmountFormatter(Locale.US)
}
