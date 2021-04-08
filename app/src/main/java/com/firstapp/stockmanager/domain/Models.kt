package com.firstapp.stockmanager.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TickerData(
    val symbol: String,
    val open: Double,
    val close: Double,

    //TODO("move to another class")
    var expanded: Boolean = false // control expandability of the RecyclerView items
) : Parcelable