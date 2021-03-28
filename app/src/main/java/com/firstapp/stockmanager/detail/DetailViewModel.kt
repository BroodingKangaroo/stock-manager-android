package com.firstapp.stockmanager.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firstapp.stockmanager.network.TickerData

class DetailViewModel(
    tickerData: TickerData,
    app: Application
) : AndroidViewModel(app) {

    private val _selectedTicker = MutableLiveData<TickerData>()
    val selectedTicker: LiveData<TickerData>
        get() = _selectedTicker

    init {
        _selectedTicker.value = tickerData
    }

}
