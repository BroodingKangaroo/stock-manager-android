package com.android.stockmanager.overview

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.android.stockmanager.R
import com.android.stockmanager.databinding.FragmentOverviewBinding
import com.android.stockmanager.firebase.AuthenticationState
import com.android.stockmanager.firebase.authenticationState
import com.android.stockmanager.overview.favorite_tickers.FavoriteTickersFragment
import com.android.stockmanager.overview.popular_tickers.PopularTickersFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, OverviewViewModelFactory(activity.application))
            .get(OverviewViewModel::class.java)
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
        authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                // If the user is not logged in, they should not be able to see any favorite tickers,
                // so navigate them to the popular tickers fragment
                AuthenticationState.UNAUTHENTICATED -> viewPager.currentItem = 0
                else -> Timber.e(
                    "New $authenticationState state that doesn't require any UI change"
                )
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)

        val menuItem = menu.findItem(R.id.search).actionView as SearchView

        menuItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.isNotEmpty()) {
                    viewModel.refreshDataFromRepository(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    fun onNetworkError(viewModel: OverviewViewModel, activity: FragmentActivity?) {
        if (!viewModel.isNetworkErrorShown.value!!) {
            Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
            viewModel.onNetworkErrorShown()
        }
    }
}