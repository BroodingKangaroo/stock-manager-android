package com.android.stockmanager.domain

import android.os.Parcelable
import com.android.stockmanager.database.DatabaseMarket
import kotlinx.parcelize.Parcelize

@Parcelize
data class TickerData(
    val symbol: String,
    val open: Double,
    val close: Double,
    var favorite: Boolean = false,
    var expanded: Boolean = false // control expandability of the RecyclerView items
) : Parcelable


fun TickerData.asDatabaseModel(isFavorite: Boolean = false): DatabaseMarket {
    return DatabaseMarket(
        symbol = this.symbol,
        open = this.open,
        close = this.close,
        favorite = isFavorite
    )
}