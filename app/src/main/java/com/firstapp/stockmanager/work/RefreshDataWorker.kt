package com.firstapp.stockmanager.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.firstapp.stockmanager.database.getDatabase
import com.firstapp.stockmanager.repository.MarketRepository
import retrofit2.HttpException
import timber.log.Timber

class RefreshDataWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "com.firstapp.stockmanager.work.RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = MarketRepository(database)

        try {
            val symbols = database.marketDao.getAllMarketData().value?.joinToString(",")!!
            repository.refreshTickers(symbols)
            Timber.d("WorkManager: Work request for sync is run")
        } catch (e: HttpException) {
            return Result.retry()
        }

        return Result.success()
    }
}