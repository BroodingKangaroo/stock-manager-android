package com.android.stockmanager.network

import android.os.Parcelable
import com.android.stockmanager.database.DatabaseMarket
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

data class NetworkEODTickerContainer(
    @Json(name = "pagination") val pagination: Pagination,
    @Json(name = "data") val data: List<NetworkEODTickerData>
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)

@Parcelize
data class NetworkEODTickerData(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "open") val open: Double,
    @Json(name = "close") val close: Double
) : Parcelable

@Parcelize
data class NetworkCompanyNameData(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String
) : Parcelable


/**
 * Convert Network results to database objects
 */
fun NetworkEODTickerContainer.asDatabaseModel(tickersName: Map<String, String>): List<DatabaseMarket> {
    return data.map {
        DatabaseMarket(
            symbol = it.symbol,
            open = it.open,
            close = it.close,
            name = tickersName[it.symbol]!!
        )
    }
}
