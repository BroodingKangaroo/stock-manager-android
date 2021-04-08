package com.firstapp.stockmanager.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.stockmanager.databinding.RecyclerviewItemBinding
import com.firstapp.stockmanager.domain.TickerData

class TickerListAdapter(private val clickListener: TickerListListener) :
    ListAdapter<TickerData, TickerListAdapter.TickerListViewHolder>(DiffCallback) {


    class TickerListViewHolder(
        internal var binding: RecyclerviewItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: TickerListListener, tickerData: TickerData) {
            binding.tickerData = tickerData
            binding.clickListener = clickListener
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TickerData>() {
        override fun areItemsTheSame(oldItem: TickerData, newItem: TickerData): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: TickerData, newItem: TickerData): Boolean {
            return oldItem.symbol == newItem.symbol
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TickerListViewHolder {
        return TickerListViewHolder(
            RecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: TickerListViewHolder, position: Int) {
        val ticker = getItem(position)
        holder.bind(clickListener, ticker)

        val isExpandedRecycleViewItem: Boolean = getItem(position).expanded
        holder.binding.expandedItem.visibility =
            if (isExpandedRecycleViewItem) View.VISIBLE else View.GONE

        holder.binding.groupDivider.visibility =
            if (isExpandedRecycleViewItem) View.VISIBLE else View.GONE

        holder.binding.header.setOnClickListener {
            ticker.expanded = !ticker.expanded
            notifyItemChanged(position)
        }
    }
}


class TickerListListener(val clickListener: (tickerData: TickerData) -> Unit) {
    fun onClick(tickerData: TickerData) = clickListener(tickerData)
}
