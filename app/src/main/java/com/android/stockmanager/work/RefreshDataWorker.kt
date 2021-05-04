package com.android.stockmanager.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.stockmanager.database.MarketRoomDatabase.Companion.getDatabase
import com.android.stockmanager.repository.MarketRepository
import retrofit2.HttpException
import timber.log.Timber

class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "com.android.stockmanager.work.RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = MarketRepository(database.marketDao())

        try {
            val tickers = database.marketDao().getAllMarketDataByPopularity().value!!
            repository.refreshTickersFromAPI(tickers.map { ticker-> ticker.symbol })
            Timber.d("WorkManager: Work request for sync is run")
        } catch (e: HttpException) {
            return Result.retry()
        }

        return Result.success()
    }
}