package com.firstapp.stockmanager.network

import com.squareup.moshi.Json

data class APIResponse(
    @Json(name = "pagination") val pagination: Pagination,
    @Json(name = "data") val data: List<TickerData>
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)

data class TickerData(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "open") val open: Double,
    @Json(name = "close") val close: Double,

    var expanded: Boolean = false // control expandability of RecyclerView items
)
