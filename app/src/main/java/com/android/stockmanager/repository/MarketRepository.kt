package com.android.stockmanager.repository

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.android.stockmanager.database.DatabaseTickerExpanded
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
import java.util.*

class MarketRepository(private val marketDao: MarketDao) {

    private val token = "50c77d0b7b7f002ee2359a2323b1fcce"

    private val popularTickers = marketDao.getAllMarketDataByPopularity()
    private val favoriteTickers = marketDao.getFavoriteTickers()

    val filteredFavoriteTickers = Transformations.switchMap(favoriteTickers) {
        val result = MediatorLiveData<List<TickerData>>()
        result.addSource(favoriteTickers) { tickers ->
            result.value = tickers
        }
        result.addSource(searchFieldTextLiveData) { searchString ->
            result.value = filterFavoriteTickers(searchString?.toUpperCase(Locale.ROOT) ?: "")
        }
        result
    }

    val filteredPopularTickers = Transformations.switchMap(popularTickers) {
        val result = MediatorLiveData<List<TickerData>>()
        result.addSource(popularTickers) { tickers ->
            result.value = tickers
        }
        result.addSource(searchFieldTextLiveData) { searchString ->
            result.value = filterPopularTickers(searchString?.toUpperCase(Locale.ROOT) ?: "")
        }
        result
    }

    val searchFieldTextLiveData = MutableLiveData<String>()

    private fun filterPopularTickers(searchString: String?) =
        popularTickers.value?.filter { tickerData ->
            tickerData.symbol.contains(searchString?.toUpperCase(Locale.ROOT) ?: "")
                    || tickerData.name.toUpperCase(Locale.ROOT).contains(searchString ?: "")
        }

    private fun filterFavoriteTickers(searchString: String?) =
        favoriteTickers.value?.filter { tickerData ->
            tickerData.symbol.contains(searchString ?: "")
                    || tickerData.name.toUpperCase(Locale.ROOT).contains(searchString ?: "")
        }

    suspend fun refreshTickersFromAPI(symbols: List<String>) {
        withContext(Dispatchers.IO) {
            Timber.d("refresh tickers is called")
            val market =
                StockManagerApi.retrofitService.getEODLatest(token, symbols.joinToString(","))
            val tickersName = symbols.map { symbol ->
                StockManagerApi.retrofitService.getTickerName(symbol, token)
            }.associateBy({ it.symbol }, { it.name })


            marketDao.insertMarketData(market.asDatabaseModel(tickersName))
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
            val market = StockManagerApi.retrofitService.getEODLatest(token, symbols)
            val tickersName = tickers.map { ticker ->
                StockManagerApi.retrofitService.getTickerName(ticker.symbol, token)
            }.associateBy({ it.symbol }, { it.name })
            marketDao.insertMarketData(market.asDatabaseModel(tickersName))
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

    suspend fun clearExpanded() {
        withContext(Dispatchers.IO) {
            marketDao.clearExpanded()
        }
    }

    suspend fun insertExpanded(ticker: TickerData) {
        withContext(Dispatchers.IO) {
            marketDao.insertExpanded(
                listOf(
                    DatabaseTickerExpanded(
                        ticker.symbol,
                        ticker.expandedPopular,
                        ticker.expandedFavorite
                    )
                )
            )
        }
    }
}