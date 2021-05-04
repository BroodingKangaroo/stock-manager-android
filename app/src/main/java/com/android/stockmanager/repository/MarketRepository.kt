package com.android.stockmanager.repository

import androidx.lifecycle.LiveData
import com.android.stockmanager.database.DatabaseTickerFavorite
import com.android.stockmanager.database.MarketDao
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.domain.TickerPopularity
import com.android.stockmanager.domain.asDatabaseModel
import com.android.stockmanager.network.StockManagerApi
import com.android.stockmanager.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MarketRepository(private val marketDao: MarketDao) {

    private val token = "50c77d0b7b7f002ee2359a2323b1fcce"

    val popularTickersData: LiveData<List<TickerData>> = marketDao.getAllMarketDataByPopularity()

    val favoriteTickersData: LiveData<List<TickerData>> = marketDao.getFavoriteTickers()

    suspend fun refreshTickersFromAPI(symbols: List<String>) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh tickers is called")
            val market = StockManagerApi.retrofitService.getTickers(token, symbols.joinToString(","))
            marketDao.insertMarketData(market.asDatabaseModel())
        }
    }

    suspend fun updateTicker(ticker: TickerData, isFavorite: Boolean = false) {
        Timber.d("update ticker is called")
        withContext(Dispatchers.IO) {
            marketDao.updateTicker(ticker.asDatabaseModel())
            marketDao.insertFavorite(
                listOf(
                    DatabaseTickerFavorite(
                        symbol = ticker.symbol,
                        favorite = isFavorite
                    )
                )
            )
        }
    }

    suspend fun updatePopularTickers(tickers: List<TickerPopularity>) {
        withContext(Dispatchers.IO) {
            val symbols = tickers.joinToString(",") { it.symbol }
            val market = StockManagerApi.retrofitService.getTickers(token, symbols)
            marketDao.insertMarketData(market.asDatabaseModel())
            marketDao.insertPopularity(tickers.map { it.asDatabaseModel() })
        }
    }

    suspend fun insertFavoriteTickers(symbols: List<String>) {
        withContext(Dispatchers.IO) {
            val databaseTickers = symbols.map { symbol ->
                DatabaseTickerFavorite(
                    symbol = symbol,
                    favorite = true
                )
            }
            marketDao.insertFavorite(databaseTickers)
        }
    }

    suspend fun clearFavorites() {
        withContext(Dispatchers.IO) {
            marketDao.clearFavorites()
        }
    }
}