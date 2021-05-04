package com.android.stockmanager.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.stockmanager.domain.TickerData

@Dao
interface MarketDao {
    @Query(
        """
        SELECT m.*, f.favorite
        FROM databasetickerpopularity p 
        INNER JOIN databasemarket m ON p.symbol = m.symbol
        LEFT JOIN databasetickerfavorite f ON m.symbol = f.symbol
        ORDER BY no_usages DESC
        """
    )
    fun getAllMarketDataByPopularity(): LiveData<List<TickerData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarketData(tickers: List<DatabaseMarket>)

    @Query(
        """
        SELECT m.*, f.favorite
        FROM databasemarket m 
        INNER JOIN databasetickerfavorite f ON m.symbol = f.symbol
        WHERE f.favorite = 1
        """
    )
    fun getFavoriteTickers(): LiveData<List<TickerData>>

    @Update
    fun updateTicker(ticker: DatabaseMarket)

    @Query("DELETE FROM databasemarket")
    fun removeAllMarketData()

    @Query("UPDATE databasetickerfavorite SET favorite = 0")
    fun clearFavorites()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPopularity(tickers: List<DatabaseTickerPopularity>)

    @Query("SELECT * FROM databasetickerpopularity")
    fun getPopularity(): LiveData<DatabaseTickerPopularity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(symbols: List<DatabaseTickerFavorite>)
}


@Database(
    entities = [DatabaseMarket::class, DatabaseTickerPopularity::class, DatabaseTickerFavorite::class],
    version = 1,
    exportSchema = true
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
