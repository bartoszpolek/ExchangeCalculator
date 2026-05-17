package com.example.exchange.feature.exchange.data.currency

import assertk.assertThat
import assertk.assertions.containsExactly
import com.example.exchange.core.common.result.Result
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.CurrencyListError
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CurrencyListRepositoryImplTest {

    private val remoteDataSource = FakeCurrencyListRemoteDataSource()
    private val localDataSource = LocalCurrencyListDataSource()
    private val repository = CurrencyListRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
    )

    @Test fun `uses remote codes when remote succeeds`() = runTest {
        remoteDataSource.nextResult = Result.Success(
            listOf(
                CurrencyCode("ARS"),
                CurrencyCode("MXN"),
            ),
        )

        assertThat(repository.getCurrencies()).containsExactly(
            CurrencyCode("ARS"),
            CurrencyCode("MXN"),
        )
    }

    @Test fun `uses local codes when remote returns empty list`() = runTest {
        remoteDataSource.nextResult = Result.Success(emptyList())

        assertThat(repository.getCurrencies()).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
            CurrencyCode("BRL"),
            CurrencyCode("COP"),
        )
    }

    @Test fun `passes through unknown remote codes for forward compatibility`() = runTest {
        remoteDataSource.nextResult = Result.Success(
            listOf(
                CurrencyCode("MXN"),
                CurrencyCode("NEW"),
            ),
        )

        assertThat(repository.getCurrencies()).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("NEW"),
        )
    }

    @Test fun `uses local codes when remote is not implemented`() = runTest {
        remoteDataSource.nextResult = Result.Failure(CurrencyListError.NotImplemented)

        assertThat(repository.getCurrencies()).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
            CurrencyCode("BRL"),
            CurrencyCode("COP"),
        )
    }

    @Test fun `uses local codes when remote has server error`() = runTest {
        remoteDataSource.nextResult = Result.Failure(CurrencyListError.Server(500))

        assertThat(repository.getCurrencies()).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
            CurrencyCode("BRL"),
            CurrencyCode("COP"),
        )
    }

    @Test fun `uses local codes when remote has network error`() = runTest {
        remoteDataSource.nextResult = Result.Failure(CurrencyListError.Network)

        assertThat(repository.getCurrencies()).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
            CurrencyCode("BRL"),
            CurrencyCode("COP"),
        )
    }
}
