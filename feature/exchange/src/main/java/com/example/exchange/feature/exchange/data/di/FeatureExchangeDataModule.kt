package com.example.exchange.feature.exchange.data.di

import com.example.exchange.feature.exchange.data.api.CurrencyListApi
import com.example.exchange.feature.exchange.data.currency.CurrencyListRemoteDataSource
import com.example.exchange.feature.exchange.data.currency.CurrencyListRepositoryImpl
import com.example.exchange.feature.exchange.data.currency.RetrofitCurrencyListDataSource
import com.example.exchange.feature.exchange.domain.repository.CurrencyListRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureExchangeDataModule {

    @Binds
    @Singleton
    abstract fun bindCurrencyListRemoteDataSource(
        dataSource: RetrofitCurrencyListDataSource,
    ): CurrencyListRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindCurrencyListRepository(
        repository: CurrencyListRepositoryImpl,
    ): CurrencyListRepository

    companion object {
        @Provides
        @Singleton
        fun provideCurrencyListApi(retrofit: Retrofit): CurrencyListApi =
            retrofit.create(CurrencyListApi::class.java)
    }
}
