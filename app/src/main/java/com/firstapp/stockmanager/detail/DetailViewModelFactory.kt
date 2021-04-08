package com.firstapp.stockmanager.detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.firstapp.stockmanager.domain.TickerData

/**
 * Simple ViewModel factory that provides the TickerData and context to the ViewModel.
 */
class DetailViewModelFactory(
    private val tickerData: TickerData,
    private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(tickerData, application) as T
        }
        throw IllegalArgumentException("Unable to construct ViewModel")
    }
}
