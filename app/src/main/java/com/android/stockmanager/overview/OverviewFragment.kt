package com.android.stockmanager.overview

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.stockmanager.R
import com.android.stockmanager.StockManagerApplication
import com.android.stockmanager.databinding.FragmentOverviewBinding
import com.android.stockmanager.firebase.AuthenticationState
import com.android.stockmanager.network.NetworkConnection
import com.android.stockmanager.overview.favorite_tickers.FavoriteTickersFragment
import com.android.stockmanager.overview.popular_tickers.PopularTickersFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by activityViewModels {
        OverviewViewModelFactory(
            (requireNotNull(this.activity).application as StockManagerApplication).repository
        )
    }

    class ViewPagerAdapter(
        activity: FragmentActivity
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PopularTickersFragment()
                1 -> FavoriteTickersFragment()
                else -> PopularTickersFragment()
            }
        }
    }

    private lateinit var viewPager: ViewPager2

    private lateinit var binding: FragmentOverviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentOverviewBinding.inflate(inflater)

        binding.lifecycleOwner = this

        viewModel.eventNetworkError.observe(
            viewLifecycleOwner,
            { isNetworkError ->
                if (isNetworkError) onNetworkError(viewModel, activity)
            })


        val viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPager = binding.overviewViewPager
        viewPager.adapter = viewPagerAdapter
        val tabLayout: TabLayout = binding.overviewTabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Popular"
                1 -> tab.text = "Favorite"
            }
        }.attach()

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val networkConnection = NetworkConnection(requireContext())
        val snackbar = createSnackbar()
        networkConnection.observe(viewLifecycleOwner, Observer { isConnected ->
            if (isConnected) {
                if (snackbar.isShown) snackbar.dismiss()
            } else {
                snackbar.show()
            }
        })

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            if (authenticationState == AuthenticationState.AUTHENTICATED) {
                viewModel.setFirebaseUser()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)

        val menuItem = menu.findItem(R.id.search).actionView as SearchView

        menuItem.queryHint = getString(R.string.search_hint)

        menuItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.isNotEmpty()) {
                    viewModel.increasePopularity(query.split(","))
                    viewModel.refreshTickersFromAPI(query.split(","))
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun createSnackbar(): Snackbar {
        val snackbar = Snackbar.make(
            requireView(),
            getString(R.string.offline),
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.view.minimumHeight = 10
        snackbar.view.setBackgroundResource(R.color.red)
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.textSize = 15F
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        return snackbar
    }

    fun onNetworkError(viewModel: OverviewViewModel, activity: FragmentActivity?) {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }
}