package com.android.stockmanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.stockmanager.domain.TickerData

@Entity
data class DatabaseMarket(
    @PrimaryKey
    val symbol: String,

    val open: Double,
    val close: Double,
    val favorite: Boolean = false,
    val expanded: Boolean = false
)

fun List<DatabaseMarket>.asDomainModel(): List<TickerData> {
    return map {
        TickerData(
            symbol = it.symbol,
            open = it.open,
            close = it.close,
            favorite = it.favorite,
            expanded = it.expanded
        )
    }
}