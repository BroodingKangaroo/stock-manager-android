package com.android.stockmanager.overview.popular_tickers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.stockmanager.StockManagerApplication
import com.android.stockmanager.databinding.FragmentPopularTickersBinding
import com.android.stockmanager.firebase.tickersPopularity
import com.android.stockmanager.network.NetworkConnection
import com.android.stockmanager.overview.*


class PopularTickersFragment : Fragment() {

    private val viewModel: OverviewViewModel by activityViewModels {
        OverviewViewModelFactory(
            (requireNotNull(this.activity).application as StockManagerApplication).repository
        )
    }

    private lateinit var binding: FragmentPopularTickersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPopularTickersBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        binding.tickersList.adapter = TickerListAdapter(
            TickerListListener { tickerData ->
                viewModel.displayTickerDetails(tickerData)
            },
            this,
            viewModel
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        addObservers()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        binding.tickersList.adapter!!.notifyDataSetChanged()
        super.onResume()
    }

    private fun addObservers() {

        val networkConnection = NetworkConnection(requireContext())

        networkConnection.observe(viewLifecycleOwner, Observer { isConnected ->
            if (isConnected) {
                viewModel.fetchPopularTickersWrapper()
            }
        })

        viewModel.navigateToSelectedTicker.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController()
                    .navigate(OverviewFragmentDirections.actionOverviewFragmentToDetailFragment(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayTickerDetailsComplete()
            }
        })

        tickersPopularity.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.updatePopularTickers(tickersPopularity.value!!)
            }
        })

        viewModel.eventNetworkError.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isNetworkError ->
                if (isNetworkError) onNetworkError(viewModel, activity)
            })
    }

    private fun onNetworkError(viewModel: OverviewViewModel, activity: FragmentActivity?) {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }
}