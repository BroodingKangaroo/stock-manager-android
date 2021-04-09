package com.android.stockmanager.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.stockmanager.database.MarketDatabase
import com.android.stockmanager.database.asDomainModel
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.network.StockManagerApi
import com.android.stockmanager.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MarketRepository(private val database: MarketDatabase) {

    private val token = "de3f54e3342a0a46d718347fbdf90b9f"


    val tickersData: LiveData<List<TickerData>> =
        Transformations.map(database.marketDao.getAllMarketData()) {
            it.asDomainModel()
        }

    suspend fun refreshTickers(symbols: String) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh tickers is called")
            val market = StockManagerApi.retrofitService.getTickers(token, symbols)
            database.marketDao.insertAll(market.asDatabaseModel())
        }
    }
}