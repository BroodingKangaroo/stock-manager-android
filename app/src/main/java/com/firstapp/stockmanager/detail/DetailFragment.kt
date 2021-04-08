package com.firstapp.stockmanager.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firstapp.stockmanager.databinding.FragmentDetailBinding
import com.firstapp.stockmanager.domain.TickerData

class DetailFragment : Fragment() {

    private lateinit var tickerData: TickerData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val application = requireNotNull(activity).application
        val binding = FragmentDetailBinding.inflate(inflater)
        binding.lifecycleOwner = this

        tickerData = DetailFragmentArgs.fromBundle(requireArguments()).selectedTicker
        val viewModelFactory = DetailViewModelFactory(tickerData, application)
        binding.viewModel =
            ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)

        return binding.root
    }

}