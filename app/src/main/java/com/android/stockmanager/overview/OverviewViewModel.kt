package com.android.stockmanager.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.stockmanager.database.getDatabase
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.firebase.UserData
import com.android.stockmanager.firebase.userAuthStateLiveData
import com.android.stockmanager.repository.MarketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException

class OverviewViewModel(application: Application, favoriteFragmentModel: Boolean = false) :
    AndroidViewModel(application) {

    private val _navigateToSelectedTicker = MutableLiveData<TickerData?>()
    val navigateToSelectedTicker: LiveData<TickerData?>
        get() = _navigateToSelectedTicker

    private val marketRepository = MarketRepository(getDatabase(application))
    val listValues = if (favoriteFragmentModel)
        marketRepository.favoriteTickersData
    else
        marketRepository.tickersData

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

    private suspend fun refreshDataFromRepositoryWithoutScope(symbols: String, isFavorite: Boolean = false) {
        try {
            marketRepository.refreshTickers(symbols, isFavorite)
            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false
        } catch (networkError: IOException) {
            if (listValues.value.isNullOrEmpty())
                _eventNetworkError.value = true
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

        if (!favoriteFragmentModel) {
            val popularTickers: String =
                if (marketRepository.tickersData.value != null) {
                    marketRepository.tickersData.value!!.joinToString(",")
                } else {
                    "AAPL,MSFT" // TODO("update list of default symbols from database of popular tickers")
                }
            refreshDataFromRepository(popularTickers)
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
            marketRepository.clearDatabase()
            async {UserData.setUser()}.await()
            val userFavoriteTickers: String = UserData.tickersToString()
            if (userFavoriteTickers.isNotEmpty())
                refreshDataFromRepositoryWithoutScope(userFavoriteTickers, isFavorite = true)
        }
    }

    fun addTickerToFavorites(tickerData: TickerData) {
        viewModelScope.launch {
            UserData.addTicker(tickerData.symbol)
            updateDataFromRepository(tickerData, isFavorite = true)
        }
    }

    fun removeTickerFromFavorites(ticker: TickerData) {
        viewModelScope.launch {
            UserData.removeTicker(ticker.symbol)
            updateDataFromRepository(ticker, isFavorite = false)
        }
    }

}