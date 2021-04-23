package com.android.stockmanager.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MarketDao {

    @Query("select * from databasemarket")
    fun getAllMarketData(): LiveData<List<DatabaseMarket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tickers: List<DatabaseMarket>)

    @Query("select * from databasemarket where favorite = 1")
    fun getFavoriteTickers(): LiveData<List<DatabaseMarket>>

    @Update
    fun updateTicker(ticker: DatabaseMarket)

    @Query("delete from databasemarket")
    fun removeAllMarketData()
}

@Database(entities = [DatabaseMarket::class], version = 1, exportSchema = true)
abstract class MarketDatabase : RoomDatabase() {
    abstract val marketDao: MarketDao
}


private lateinit var INSTANCE: MarketDatabase

fun getDatabase(context: Context): MarketDatabase {
    synchronized(MarketDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                MarketDatabase::class.java,
                "databasemarket"
            ).build()
        }
    }
    return INSTANCE
}
