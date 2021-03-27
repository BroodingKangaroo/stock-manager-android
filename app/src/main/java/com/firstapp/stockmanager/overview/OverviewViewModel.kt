package com.firstapp.stockmanager.overview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firstapp.stockmanager.network.StockManagerApi
import com.firstapp.stockmanager.network.TickerData
import kotlinx.coroutines.launch

class OverviewViewModel : ViewModel() {
    private val token = "de3f54e3342a0a46d718347fbdf90b9f"

    private val _listValues = MutableLiveData<List<TickerData>>()

    val listValues: LiveData<List<TickerData>> = _listValues


//    init {
//        getListValues("AAPL,MSFT")
//    }

    fun getListValues(symbols: String) {
        viewModelScope.launch {
            try {
                _listValues.value =  StockManagerApi.retrofitService.getTickers(token, symbols).data
            } catch (e: Exception) {
                _listValues.value = listOf()
                Log.d("qwe", e.stackTraceToString())
            }
        }
    }


}