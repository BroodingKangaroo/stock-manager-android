package com.android.stockmanager.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.domain.TickerPopularity

@Entity
data class DatabaseMarket(
    @PrimaryKey
    val symbol: String,

    val open: Double,
    val close: Double,
    val expanded: Boolean = false
)

@Entity
data class DatabaseTickerPopularity(
    @PrimaryKey
    val symbol: String,
    val no_usages: Long = 0
)

@Entity
data class DatabaseTickerFavorite(
    @PrimaryKey
    val symbol: String,
    val favorite: Boolean = false
)

@JvmName("marketToDomain")
fun List<DatabaseMarket>.asDomainModel(isFavorite: Boolean): List<TickerData> {
    return map {
        TickerData(
            symbol = it.symbol,
            open = it.open,
            close = it.close,
            favorite = isFavorite,
            expanded = it.expanded,
        )
    }
}

@JvmName("popularityToDomain")
fun List<DatabaseTickerPopularity>.asDomainModel() : List<TickerPopularity> {
    return map {
        TickerPopularity(
            symbol = it.symbol,
            no_usages = it.no_usages
        )
    }
}