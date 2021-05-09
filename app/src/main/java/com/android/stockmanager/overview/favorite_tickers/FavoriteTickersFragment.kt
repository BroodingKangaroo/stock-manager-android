package com.android.stockmanager.overview.favorite_tickers

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
import com.android.stockmanager.R
import com.android.stockmanager.StockManagerApplication
import com.android.stockmanager.databinding.FragmentFavoriteTickersBinding
import com.android.stockmanager.firebase.AuthenticationState
import com.android.stockmanager.firebase.UserData
import com.android.stockmanager.overview.*
import timber.log.Timber


class FavoriteTickersFragment : Fragment() {

    private val viewModel: OverviewViewModel by activityViewModels {
        OverviewViewModelFactory(
            (requireNotNull(this.activity).application as StockManagerApplication).repository
        )
    }

    private lateinit var binding: FragmentFavoriteTickersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentFavoriteTickersBinding.inflate(inflater)

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
        super.onViewCreated(view, savedInstanceState)

        addObservers()
    }

    override fun onResume() {
        val navController = findNavController()
        // Navigate unauthenticated users to login fragment even after pressing back button on login fragment
        // and returning back to the favorite fragment
        when (viewModel.authenticationState.value) {
            AuthenticationState.UNAUTHENTICATED -> navController.navigate(R.id.loginFragment)
            AuthenticationState.INVALID_AUTHENTICATION -> navController.navigate(R.id.loginFragment)
            else -> {
            }
        }
        if (binding.tickersList != UserData.favoriteTickers.value) {
            binding.tickersList.adapter!!.notifyDataSetChanged()
        }
        super.onResume()
    }

    private fun addObservers() {

        viewModel.navigateToSelectedTicker.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController()
                    .navigate(OverviewFragmentDirections.actionOverviewFragmentToDetailFragment(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayTickerDetailsComplete()
            }
        })

        viewModel.eventNetworkError.observe(viewLifecycleOwner, Observer<Boolean> { networkError ->
            if (networkError) onNetworkError(viewModel, activity)
        })

        val navController = findNavController()

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> {
                    Timber.i("Authenticated")
                }
                // If the user is not logged in, they should not be able to set any favorite tickers,
                // so navigate them to the login fragment
                AuthenticationState.UNAUTHENTICATED -> navController.navigate(
                    R.id.loginFragment
                )
                else -> Timber.e(
                    "New $authenticationState state that doesn't require any UI change"
                )
            }
        })
    }

    fun onNetworkError(viewModel: OverviewViewModel, activity: FragmentActivity?) {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }
}