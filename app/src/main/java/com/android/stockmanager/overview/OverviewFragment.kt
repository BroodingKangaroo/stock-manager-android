package com.android.stockmanager.overview

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.firebase.ui.auth.AuthUI
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
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
                when (tab.position) {
                    0 -> {
                        binding.fab.setImageResource(R.drawable.ic_baseline_add_24)
                        binding.fab.setOnClickListener {
                            showAddTickerDialog(requireContext())
                        }
                    }
                    1 -> {
                        binding.fab.setImageResource(R.drawable.ic_logout_black_24dp)
                        binding.fab.setOnClickListener {
                            AuthUI.getInstance().signOut(requireContext())
                            viewModel.userAuthStateLiveData.firebaseAuth.signOut()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
        val snackbar = createConnectionLostSnackbar()
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

        menuItem.setOnCloseListener {
            viewModel.setSearchQuery("")
            true
        }

        menuItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                this@OverviewFragment.view?.hideKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showAddTickerDialog(context: Context) {
        val editText = EditText(context).apply { setHint(R.string.search_hint) }
        AlertDialog
            .Builder(context)
            .setTitle(getString(R.string.add_new_ticker_dialog_title))
            .setMessage(getString(R.string.add_new_ticker_dialog_message))
            .setView(editText)
            .setPositiveButton(getString(R.string.add_new_ticker_positive_button)) { _, _ ->
                val inputText = editText.text
                val snackbar =
                    if (Regex("[a-zA-Z,]+", RegexOption.IGNORE_CASE).matches(inputText)) {
                        viewModel.increasePopularity(inputText.split(","))
                        viewModel.refreshTickersFromAPI(inputText.split(","))
                        createInfoSnackbar(getString(R.string.add_new_ticker_snackbar_message))
                    } else {
                        createInfoSnackbar(getString(R.string.add_new_ticker_wrong_pattern))
                    }
                snackbar.show()
            }
            .setNegativeButton(getString(R.string.add_new_ticker_negative_button)) { dialog, _ -> dialog.cancel() }
            .show()
        editText.showKeyboard()
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun View.showKeyboard() {
        if (requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            // imm.showSoftInputMethod doesn't work well
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun createInfoSnackbar(message: String): Snackbar {
        val snackbar = Snackbar.make(
            requireView(),
            message,
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.anchorView = binding.fab
        snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
            4
        snackbar.setAction(getString(R.string.snackbar_dismiss_button)) { snackbar.dismiss() }
        return snackbar
    }

    private fun createConnectionLostSnackbar(): Snackbar {
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