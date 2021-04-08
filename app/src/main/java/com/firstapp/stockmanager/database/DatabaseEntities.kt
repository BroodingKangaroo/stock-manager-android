package com.firstapp.stockmanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.firstapp.stockmanager.domain.TickerData

@Entity
data class DatabaseMarket(
    @PrimaryKey
    val symbol: String,

    val open: Double,
    val close: Double
)

fun List<DatabaseMarket>.asDomainModel(): List<TickerData> {
    return map {
        TickerData(
            symbol = it.symbol,
            open = it.open,
            close = it.close
        )
    }
}
