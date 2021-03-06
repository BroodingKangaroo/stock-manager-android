package com.android.stockmanager.overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.stockmanager.R
import com.android.stockmanager.databinding.RecyclerviewItemBinding
import com.android.stockmanager.domain.TickerData
import com.android.stockmanager.firebase.AuthenticationState
import com.android.stockmanager.overview.favorite_tickers.FavoriteTickersFragment
import com.android.stockmanager.overview.popular_tickers.PopularTickersFragment
import kotlin.math.absoluteValue

class TickerListAdapter(
    private val clickListener: TickerListListener,
    private val fragment: Fragment,
    private val viewModel: OverviewViewModel
) :
    ListAdapter<TickerData, TickerListAdapter.TickerListViewHolder>(DiffCallback) {

    class TickerListViewHolder(
        internal var binding: RecyclerviewItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            clickListener: TickerListListener,
            tickerData: TickerData
        ) {
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
        holder.bind(clickListener, getItem(position))

        val itemExpanded = when (fragment) {
            is PopularTickersFragment -> getItem(position).expandedPopular
            is FavoriteTickersFragment -> getItem(position).expandedFavorite
            else -> false
        }

        holder.binding.expandedItem.visibility = when (itemExpanded) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        holder.binding.groupDivider.visibility = when (itemExpanded) {
            true -> View.VISIBLE
            false -> View.GONE
        }

        holder.binding.header.setOnClickListener {
            try {
                val ticker = getItem(position)
                when (fragment) {
                    is PopularTickersFragment -> ticker.apply { expandedPopular = !expandedPopular }
                    is FavoriteTickersFragment -> ticker.apply {
                        expandedFavorite = !expandedFavorite
                    }
                }
                viewModel.insertExpanded(ticker)
                notifyItemChanged(position)
            } catch (e: IndexOutOfBoundsException) {
                notifyDataSetChanged()
            }
        }

        processDayChange(holder, position)

        manageFavoriteButton(holder, position)
    }


    private fun manageFavoriteButton(holder: TickerListViewHolder, position: Int) {
        holder.binding.addToFavoritesButton.setOnClickListener {
            val ticker: TickerData = getItem(position)
            when (ticker.favorite) {
                true -> viewModel.removeTickerFromFavorites(ticker)
                false -> viewModel.addTickerToFavorites(ticker)
            }
            ticker.favorite = !ticker.favorite
            notifyItemChanged(position)
        }

        holder.binding.addToFavoritesButton.visibility =
            when (viewModel.authenticationState.value) {
                AuthenticationState.AUTHENTICATED -> View.VISIBLE
                else -> View.GONE
            }

        viewModel.authenticationState.observe(fragment.viewLifecycleOwner, Observer {
            if (it == AuthenticationState.AUTHENTICATED) {
                holder.binding.addToFavoritesButton.visibility = View.VISIBLE
            } else {
                holder.binding.addToFavoritesButton.visibility = View.GONE
            }
        })

        holder.binding.addToFavoritesButton.text = when (getItem(position).favorite) {
            true -> holder.itemView.context.getString(R.string.remove_favorite)
            false -> holder.itemView.context.getString(R.string.add_favorite)
        }
    }

    private fun processDayChange(holder: TickerListViewHolder, position: Int) {
        val ticker = getItem(position)
        val dayChange = (ticker.open - ticker.close)
        val dayChangeAbs = dayChange.absoluteValue
        val relativeChange = (dayChange / ticker.open).absoluteValue
        val textFormat = fragment.getString(R.string.ticker_cost_text_format)
        when {
            dayChange > 0 -> {
                holder.binding.dayChange.setTextColor(fragment.resources.getColor(R.color.green))
                holder.binding.dayChange.text = textFormat.format("+", dayChangeAbs, relativeChange)
            }
            dayChange < 0 -> {
                holder.binding.dayChange.setTextColor(fragment.resources.getColor(R.color.red))
                holder.binding.dayChange.text = textFormat.format("-", dayChangeAbs, relativeChange)
            }
            dayChange == 0.0 -> {
                holder.binding.dayChange.setTextColor(fragment.resources.getColor(R.color.black))
                holder.binding.dayChange.text = textFormat.format("", dayChangeAbs, relativeChange)
            }
        }
    }
}


class TickerListListener(val clickListener: (tickerData: TickerData) -> Unit) {
    fun onClick(tickerData: TickerData) = clickListener(tickerData)
}