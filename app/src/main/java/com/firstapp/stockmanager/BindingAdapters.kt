package com.firstapp.stockmanager

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.stockmanager.domain.TickerData
import com.firstapp.stockmanager.overview.TickerListAdapter

/**
 * Updates the data shown in the [RecyclerView].
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<TickerData>?) {
    val adapter = recyclerView.adapter as TickerListAdapter
    adapter.submitList(data)
}

