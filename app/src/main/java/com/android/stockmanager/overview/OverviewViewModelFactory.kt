package com.android.stockmanager.overview

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.stockmanager.repository.MarketRepository

/**
 * Simple ViewModel factory that provides context to the ViewModel.
 */
class OverviewViewModelFactory(
    private val repository: MarketRepository,
    private val favoriteFragmentModel: Boolean = false
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            return OverviewViewModel(repository, favoriteFragmentModel) as T
        }
        throw IllegalArgumentException("Unable to construct ViewModel")
    }
}
