package com.android.stockmanager.domain

import android.os.Parcelable
import com.android.stockmanager.database.DatabaseMarket
import com.android.stockmanager.database.DatabaseTickerPopularity
import kotlinx.parcelize.Parcelize

@Parcelize
data class TickerData(
    val symbol: String,
    val open: Double,
    val close: Double,
    val name: String,
    var favorite: Boolean = false,
    // control expandability of the RecyclerView items
    var expandedFavorite: Boolean = false,
    var expandedPopular: Boolean = false,
) : Parcelable

@Parcelize
data class TickerPopularity(
    val symbol: String,
    var no_usages: Long
) : Parcelable


@JvmName("domainToDatabaseMarket")
fun TickerData.asDatabaseModel(): DatabaseMarket {
    return DatabaseMarket(
        symbol = this.symbol,
        open = this.open,
        close = this.close,
        name = this.name
    )
}

@JvmName("domainToDatabasePopularity")
fun TickerPopularity.asDatabaseModel(): DatabaseTickerPopularity {
    return DatabaseTickerPopularity(
        symbol = this.symbol,
        no_usages = this.no_usages
    )
}