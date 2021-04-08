package com.firstapp.stockmanager.network

import android.os.Parcelable
import com.firstapp.stockmanager.database.DatabaseMarket
import com.firstapp.stockmanager.domain.TickerData
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

data class NetworkTickerContainer(
    @Json(name = "pagination") val pagination: Pagination,
    @Json(name = "data") val data: List<NetworkTickerData>
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)

@Parcelize
data class NetworkTickerData(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "open") val open: Double,
    @Json(name = "close") val close: Double,


) : Parcelable

//TODO("Add an example of incoming json")


/**
 * Convert Network results to domain objects
 */
fun NetworkTickerContainer.asDomainModel(): List<TickerData> {
    return data.map {
        TickerData(
            symbol = it.symbol,
            open = it.open,
            close = it.close
        )
    }
}


/**
 * Convert Network results to database objects
 */
fun NetworkTickerContainer.asDatabaseModel(): List<DatabaseMarket> {
    return data.map {
        DatabaseMarket(
            symbol = it.symbol,
            open = it.open,
            close = it.close
        )
    }
}
