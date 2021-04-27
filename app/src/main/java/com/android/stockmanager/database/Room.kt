package com.android.stockmanager.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MarketDao {

    @Query(
        """
        SELECT m.* 
        FROM databasetickerpopularity p INNER JOIN databasemarket m ON p.symbol=m.symbol
        ORDER BY no_usages DESC
        """
    )
    fun getAllMarketDataByPopularity(): LiveData<List<DatabaseMarket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(tickers: List<DatabaseMarket>)

    @Query("SELECT * FROM databasemarket WHERE favorite = 1")
    fun getFavoriteTickers(): LiveData<List<DatabaseMarket>>

    @Update
    fun updateTicker(ticker: DatabaseMarket)

    @Query("DELETE FROM databasemarket")
    fun removeAllMarketData()

    @Query("UPDATE databasemarket SET favorite = 0")
    fun clearFavorites()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPopularity(tickers: List<DatabaseTickerPopularity>)

    @Query("SELECT * FROM databasetickerpopularity")
    fun getPopularity(): LiveData<DatabaseTickerPopularity>
}


@Database(
    entities = [DatabaseMarket::class, DatabaseTickerPopularity::class],
    version = 1,
    exportSchema = false
)
abstract class MarketRoomDatabase : RoomDatabase() {

    abstract fun marketDao(): MarketDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MarketRoomDatabase? = null

        fun getDatabase(context: Context): MarketRoomDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketRoomDatabase::class.java,
                    "databasemarket"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
