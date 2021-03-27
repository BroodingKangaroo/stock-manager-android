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

    private val _listValues = MutableLiveData<List<TickerData>>()

    val listValues: LiveData<List<TickerData>> = _listValues

    private val _listItemExpanded = MutableLiveData<Boolean>()
    val listItemExpanded: LiveData<Boolean>
        get() = _listItemExpanded

    init {
        getListValues()
    }

    private fun getListValues() {
        viewModelScope.launch {
            try {
                _listValues.value =  StockManagerApi.retrofitService.getTickers().data
                Log.d("qwe", "Something went wrong")
            } catch (e: Exception) {
                _listValues.value = listOf()
                Log.d("qwe", e.stackTraceToString())
            }
        }
    }


}