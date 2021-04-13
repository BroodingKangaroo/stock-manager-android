package com.android.stockmanager.overview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.stockmanager.database.getDatabase
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.repository.MarketRepository
import kotlinx.coroutines.launch
import java.io.IOException

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val _navigateToSelectedTicker = MutableLiveData<TickerData?>()
    val navigateToSelectedTicker: LiveData<TickerData?>
        get() = _navigateToSelectedTicker

    private val videosRepository = MarketRepository(getDatabase(application))
    val listValues = videosRepository.tickersData

    private var _eventNetworkError = MutableLiveData<Boolean>(false)
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun refreshDataFromRepository(symbols: String) {
        viewModelScope.launch {
            try {
                videosRepository.refreshTickers(symbols)
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false
            } catch (networkError: IOException) {
                if (listValues.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }

    init {
        val symbols: String =
            if (videosRepository.tickersData.value != null) {
                videosRepository.tickersData.value!!.joinToString(",")
            } else {
                "AAPL,MSFT" // TODO("update list of default symbols")
            }
        refreshDataFromRepository(symbols)
    }

    /**
     * When the property is clicked, set the [_navigateToSelectedTicker] [MutableLiveData]
     * @param tickerData The [TickerData] that was clicked on.
     */
    fun displayTickerDetails(tickerData: TickerData) {
        _navigateToSelectedTicker.value = tickerData
    }

    /**
     * After the navigation has taken place, make sure _navigateToSelectedTicker is set to null
     */
    fun displayTickerDetailsComplete() {
        _navigateToSelectedTicker.value = null
    }
}