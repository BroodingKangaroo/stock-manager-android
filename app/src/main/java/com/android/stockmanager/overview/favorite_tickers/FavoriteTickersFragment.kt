package com.android.stockmanager.overview.favorite_tickers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.android.stockmanager.R
import com.android.stockmanager.databinding.FragmentFavoriteTickersBinding
import com.android.stockmanager.overview.*
import com.firebase.ui.auth.AuthUI
import timber.log.Timber


class FavoriteTickersFragment : Fragment() {

    companion object {
        const val TAG = "FavoriteTickersFragment"
    }

    private val viewModel: OverviewViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, OverviewViewModelFactory(activity.application))
            .get(OverviewViewModel::class.java)
    }

    private lateinit var binding: FragmentFavoriteTickersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteTickersBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        binding.tickersList.adapter = TickerListAdapter(TickerListListener { tickerData ->
            viewModel.displayTickerDetails(tickerData)
        })

        viewModel.navigateToSelectedTicker.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController()
                    .navigate(OverviewFragmentDirections.actionOverviewFragmentToDetailFragment(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayTickerDetailsComplete()
            }
        })

        viewModel.eventNetworkError.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isNetworkError ->
                if (isNetworkError) onNetworkError(viewModel, activity)
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                OverviewViewModel.AuthenticationState.AUTHENTICATED -> {
                    Timber.i("$TAG Authenticated")
                    binding.logoutButton.setOnClickListener {
                        AuthUI.getInstance().signOut(requireContext())
                    }
                }
                // If the user is not logged in, they should not be able to set any preferences,
                // so navigate them to the login fragment
                OverviewViewModel.AuthenticationState.UNAUTHENTICATED -> navController.navigate(
                    R.id.loginFragment
                )
                else -> Timber.e(
                    "$TAG New $authenticationState state that doesn't require any UI change"
                )
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }

    fun onNetworkError(viewModel: OverviewViewModel, activity: FragmentActivity?) {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }
}