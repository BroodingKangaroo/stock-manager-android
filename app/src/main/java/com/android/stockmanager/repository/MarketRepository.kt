package com.android.stockmanager.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.stockmanager.database.MarketDatabase
import com.android.stockmanager.database.asDomainModel
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.domain.asDatabaseModel
import com.android.stockmanager.network.StockManagerApi
import com.android.stockmanager.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MarketRepository(private val database: MarketDatabase) {

    private val token = "adc7abee314bf5e0464021ecc2c8e1da"


    val tickersData: LiveData<List<TickerData>> =
        Transformations.map(database.marketDao.getAllMarketData()) {
            it.asDomainModel()
        }

    val favoriteTickersData: LiveData<List<TickerData>> =
        Transformations.map(database.marketDao.getFavoriteTickers()) {
            it.asDomainModel()
        }

    suspend fun refreshTickers(symbols: String, isFavorite: Boolean = false) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh tickers is called")
            val market = StockManagerApi.retrofitService.getTickers(token, symbols)
            database.marketDao.insertAll(market.asDatabaseModel(isFavorite))
        }
    }

    suspend fun updateTicker(ticker: TickerData, isFavorite: Boolean = false) {
        withContext(Dispatchers.IO) {
            Timber.d("update ticker is called")
            database.marketDao.updateTicker(ticker.asDatabaseModel(isFavorite))
        }
    }

    suspend fun clearDatabase() {
        withContext(Dispatchers.IO) {
            database.marketDao.removeAllMarketData()
        }
    }
}