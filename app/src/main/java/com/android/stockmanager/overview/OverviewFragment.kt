package com.android.stockmanager.overview

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.stockmanager.R
import com.android.stockmanager.databinding.FragmentOverviewBinding
import com.android.stockmanager.overview.popular_tickers.PopularTickersFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OverviewFragment : Fragment() {

    private val viewModel: OverviewViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(this, OverviewViewModelFactory(activity.application))
            .get(OverviewViewModel::class.java)
    }

    private lateinit var binding: FragmentOverviewBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }


    class ViewPagerAdapter(
        fm: FragmentManager,
        lifecycle: Lifecycle
    ) : FragmentStateAdapter(fm, lifecycle) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return PopularTickersFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentOverviewBinding.inflate(inflater)

        binding.lifecycleOwner = this

        viewModel.eventNetworkError.observe(
            viewLifecycleOwner,
            Observer<Boolean> { isNetworkError ->
                if (isNetworkError) onNetworkError(viewModel, activity)
            })

//        binding.authButton.setOnClickListener { launchSignInFlow() }

        val viewPagerAdapter = ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle)
        val viewPager = binding.overviewViewPager
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

//    private fun launchSignInFlow() {
//        // Give users the option to sign in / register with their email or Google account. If users
//        // choose to register with their email, they will need to create a password as well.
//        val providers = arrayListOf(
//            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
//        )
//
//        // Create and launch sign-in intent. We listen to the response of this activity with the
//        // SIGN_IN_RESULT_CODE code.
//        startActivityForResult(
//            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
//                providers
//            ).build(), SIGN_IN_RESULT_CODE
//        )
//    }

}