package com.android.stockmanager.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.android.stockmanager.database.MarketDao
import com.android.stockmanager.database.asDomainModel
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.domain.TickerPopularity
import com.android.stockmanager.domain.asDatabaseModel
import com.android.stockmanager.network.StockManagerApi
import com.android.stockmanager.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MarketRepository(private val marketDao: MarketDao) {

    private val token = "a2e6587824c63503052b83601d96d30a"


    val popularTickersData: LiveData<List<TickerData>> =
        Transformations.map(marketDao.getAllMarketDataByPopularity()) {
            it.asDomainModel()
        }

    val favoriteTickersData: LiveData<List<TickerData>> =
        Transformations.map(marketDao.getFavoriteTickers()) {
            it.asDomainModel()
        }

    suspend fun refreshTickers(symbols: String, isFavorite: Boolean = false) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh tickers is called")
            val market = StockManagerApi.retrofitService.getTickers(token, symbols)
            marketDao.insertAll(market.asDatabaseModel(isFavorite))
        }
    }

    suspend fun updateTicker(ticker: TickerData, isFavorite: Boolean = false) {
        withContext(Dispatchers.IO) {
            Timber.d("update ticker is called")
            marketDao.updateTicker(ticker.asDatabaseModel(isFavorite))
        }
    }

    suspend fun clearDatabase() {
        withContext(Dispatchers.IO) {
            marketDao.removeAllMarketData()
        }
    }

    suspend fun clearFavorites() {
        withContext(Dispatchers.IO) {
            marketDao.clearFavorites()
        }
    }

    suspend fun updatePopularTickers(tickers: List<TickerPopularity>) {
        withContext(Dispatchers.IO) {
            val symbols = tickers.joinToString(",") { it.symbol }
            val market = StockManagerApi.retrofitService.getTickers(token, symbols)
            marketDao.insertAll(market.asDatabaseModel())
            marketDao.insertPopularity(tickers.map { it.asDatabaseModel() })
        }
    }
}