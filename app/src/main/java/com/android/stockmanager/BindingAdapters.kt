package com.android.stockmanager

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.overview.TickerListAdapter

/**
 * Updates the data shown in the [RecyclerView].
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<TickerData>?) {
    val adapter = recyclerView.adapter as TickerListAdapter
    adapter.submitList(data)
}

