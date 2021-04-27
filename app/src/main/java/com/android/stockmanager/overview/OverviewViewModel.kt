package com.android.stockmanager.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.domain.TickerPopularity
import com.android.stockmanager.firebase.*
import com.android.stockmanager.repository.MarketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class OverviewViewModel(
    private val marketRepository: MarketRepository,
    favoriteFragmentModel: Boolean = false
) :
    ViewModel() {

    private val _navigateToSelectedTicker = MutableLiveData<TickerData?>()
    val navigateToSelectedTicker: LiveData<TickerData?>
        get() = _navigateToSelectedTicker

    val listValues = if (favoriteFragmentModel)
        marketRepository.favoriteTickersData
    else
        marketRepository.popularTickersData

    private var _eventNetworkError = MutableLiveData<Boolean>(false)
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun refreshDataFromRepository(symbols: String, isFavorite: Boolean = false) {
        viewModelScope.launch {
            try {
                marketRepository.refreshTickers(symbols, isFavorite)
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
            } catch (networkError: IOException) {
                if (listValues.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }

    private suspend fun updateDataFromRepository(
        tickerData: TickerData,
        isFavorite: Boolean = false
    ) {
        try {
            marketRepository.updateTicker(tickerData, isFavorite)
            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false
        } catch (networkError: IOException) {
            if (listValues.value.isNullOrEmpty())
                _eventNetworkError.value = true
        }
    }

    init {

        UserData.init("", mutableListOf())

        viewModelScope.launch {
            fetchPopularTickers()
        }
    }

    /**
     * When the property is clicked, set the [_navigateToSelectedTicker] [MutableLiveData]
     * @param tickerData The [TickerData] that was clicked on.
     */
    fun displayTickerDetails(tickerData: TickerData) {
        _navigateToSelectedTicker.value = tickerData
    }

    fun displayTickerDetailsComplete() {
        _navigateToSelectedTicker.value = null
    }

    fun setFirebaseUser() {
        UserData.init(
            userAuthStateLiveData.getUserId(),
            mutableListOf()
        )
        viewModelScope.launch {
            marketRepository.clearFavorites()
            async { UserData.fetchUser() }.await()
            val userFavoriteTickers: String = UserData.tickersToString()
            if (userFavoriteTickers.isNotEmpty()) {
                refreshDataFromRepository(userFavoriteTickers, isFavorite = true)
            }
        }
    }

    fun addTickerToFavorites(tickerData: TickerData) {
        viewModelScope.launch {
            async { UserData.addTicker(tickerData.symbol) }.await()
            updateDataFromRepository(tickerData, isFavorite = true)
        }
    }

    fun removeTickerFromFavorites(ticker: TickerData) {
        viewModelScope.launch {
            async { UserData.removeTicker(ticker.symbol) }.await()
            updateDataFromRepository(ticker, isFavorite = false)
        }
    }

    fun updatePopularTickers(tickers: List<TickerPopularity>) {
        viewModelScope.launch {
            marketRepository.updatePopularTickers(tickers)
        }
    }

    fun increasePopularity(symbols: String) {
        val tickersToUpdate = mutableListOf<TickerPopularity>()
        for (symbol in symbols.split(",")) {
            val symbolUpperCase = symbol.toUpperCase(Locale.ROOT)
            if (tickersPopularity.value?.find { it.symbol == symbolUpperCase } != null) {
                tickersPopularity.value!!.find { it.symbol == symbolUpperCase }!!.no_usages += 1
                tickersToUpdate.add(tickersPopularity.value!!.find { it.symbol == symbolUpperCase }!!)
            } else {
                tickersPopularity.value!!.add(TickerPopularity(symbolUpperCase,1))
                tickersToUpdate.add(TickerPopularity(symbolUpperCase,1))
            }
        }
        updatePopularTickers(tickersToUpdate)
        viewModelScope.launch {
            for (ticker in tickersToUpdate) {
                updatePopularity(ticker)
            }
        }
    }

}