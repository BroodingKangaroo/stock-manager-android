package com.android.stockmanager.overview

import androidx.lifecycle.*
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.domain.TickerPopularity
import com.android.stockmanager.firebase.*
import com.android.stockmanager.repository.MarketRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class OverviewViewModel(
    private val marketRepository: MarketRepository
) :
    ViewModel() {

    private val _navigateToSelectedTicker = MutableLiveData<TickerData?>()
    val navigateToSelectedTicker: LiveData<TickerData?>
        get() = _navigateToSelectedTicker

    val listValuesOfFavoriteTickers = marketRepository.favoriteTickersData
    val listValuesOfPopularTickers = marketRepository.popularTickersData

    private var _eventNetworkError = MutableLiveData<Boolean>(false)
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    val userAuthStateLiveData = FirebaseUserLiveData()
    var authenticationState: LiveData<AuthenticationState> = userAuthStateLiveData.map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun refreshTickersFromAPI(
        symbols: List<String>
    ) {
        viewModelScope.launch {
            try {
                marketRepository.refreshTickersFromAPI(symbols)
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
            } catch (networkError: IOException) {
                if (listValuesOfFavoriteTickers.value.isNullOrEmpty() && listValuesOfPopularTickers.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }


    init {
        UserData.init("", mutableListOf())
    }

    fun fetchPopularTickersWrapper() {
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
            marketRepository.clearExpanded()
            async { UserData.getFavoriteTickersFromFirebase() }.await()
            if (!UserData.favoriteTickers.value.isNullOrEmpty()) {
                refreshTickersFromAPI(UserData.favoriteTickers.value!!)
                marketRepository.insertFavoriteTickers(UserData.favoriteTickers.value!!)
            }
        }
    }

    fun addTickerToFavorites(ticker: TickerData) {
        viewModelScope.launch {
            marketRepository.updateTicker(ticker, true)
            async { UserData.addTicker(ticker.symbol) }.await()
        }
    }

    fun removeTickerFromFavorites(ticker: TickerData) {
        viewModelScope.launch {
            marketRepository.updateTicker(ticker, false)
            async { UserData.removeTicker(ticker.symbol) }.await()
        }
    }

    fun updatePopularTickers(tickers: List<TickerPopularity>) {
        viewModelScope.launch {
            marketRepository.updatePopularTickers(tickers)
        }
    }

    fun insertExpanded(ticker: TickerData) {
        viewModelScope.launch {
            marketRepository.insertExpanded(ticker)
        }
    }

    fun increasePopularity(symbols: List<String>) {
        val tickersToUpdate = mutableListOf<TickerPopularity>()
        for (symbol in symbols) {
            val symbolUpperCase = symbol.toUpperCase(Locale.ROOT)
            if (tickersPopularity.value?.find { it.symbol == symbolUpperCase } != null) {
                tickersPopularity.value!!.find { it.symbol == symbolUpperCase }!!.no_usages += 1
                tickersToUpdate.add(tickersPopularity.value!!.find { it.symbol == symbolUpperCase }!!)
            } else {
                tickersPopularity.value!!.add(TickerPopularity(symbolUpperCase, 1))
                tickersToUpdate.add(TickerPopularity(symbolUpperCase, 1))
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