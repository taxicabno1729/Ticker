package com.example.liveticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liveticker.data.AuthRepository
import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.databinding.FragmentMarketBrowserBinding
import com.example.liveticker.ui.MarketBrowserAdapter
import com.example.liveticker.ui.MarketBrowserViewModel
import com.example.liveticker.ui.MarketBrowserViewModelFactory
import com.example.liveticker.ui.MarketListItem
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MarketBrowserFragment : Fragment() {

    private var _binding: FragmentMarketBrowserBinding? = null
    private val binding get() = _binding!!

    private lateinit var marketAdapter: MarketBrowserAdapter
    private lateinit var viewModel: MarketBrowserViewModel
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authRepository = AuthRepository(requireContext())
        val repository = PredictionMarketRepository()
        val factory = MarketBrowserViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MarketBrowserViewModel::class.java]

        setupRecyclerView()
        setupTabLayout()
        setupAuthButton()
        
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshMarkets()
        }

        binding.retryButton.setOnClickListener {
            binding.errorContainer.visibility = View.GONE
            viewModel.refreshMarkets()
        }

        observeViewModel()
        viewModel.loadMarkets()
    }

    private fun setupAuthButton() {
        updateAuthButton()
        
        binding.loginButton?.setOnClickListener {
            if (authRepository.isKalshiLoggedIn()) {
                // Show logout confirmation
                authRepository.clearKalshiAuth()
                Toast.makeText(context, "Logged out from Kalshi", Toast.LENGTH_SHORT).show()
                updateAuthButton()
            } else {
                // Navigate to login
                findNavController().navigate(R.id.action_MarketBrowserFragment_to_KalshiLoginFragment)
            }
        }
    }

    private fun updateAuthButton() {
        binding.loginButton?.text = if (authRepository.isKalshiLoggedIn()) {
            "Logout from Kalshi"
        } else {
            "Login to Kalshi"
        }
    }

    private fun setupRecyclerView() {
        marketAdapter = MarketBrowserAdapter { marketItem ->
            onMarketClicked(marketItem)
        }
        binding.marketsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = marketAdapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Polymarket"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Kalshi"))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectTab(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedTab.collectLatest { tab ->
                updateMarketList(tab)
            }
        }

        lifecycleScope.launch {
            viewModel.polymarketMarkets.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (viewModel.selectedTab.value == 0) {
                            binding.loading.visibility = View.VISIBLE
                            binding.errorContainer.visibility = View.GONE
                        }
                    }
                    is Resource.Success -> {
                        binding.loading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        if (viewModel.selectedTab.value == 0) {
                            val items = resource.data?.map { 
                                MarketListItem.PolymarketItem(it) 
                            } ?: emptyList()
                            marketAdapter.submitList(items)
                        }
                    }
                    is Resource.Error -> {
                        binding.loading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        if (viewModel.selectedTab.value == 0) {
                            showError(resource.message)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.kalshiMarkets.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (viewModel.selectedTab.value == 1) {
                            binding.loading.visibility = View.VISIBLE
                            binding.errorContainer.visibility = View.GONE
                        }
                    }
                    is Resource.Success -> {
                        binding.loading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        if (viewModel.selectedTab.value == 1) {
                            val items = resource.data?.map { 
                                MarketListItem.KalshiItem(it) 
                            } ?: emptyList()
                            marketAdapter.submitList(items)
                        }
                    }
                    is Resource.Error -> {
                        binding.loading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        if (viewModel.selectedTab.value == 1) {
                            showError(resource.message)
                        }
                    }
                }
            }
        }
    }

    private fun updateMarketList(tab: Int) {
        when (tab) {
            0 -> {
                val items = viewModel.polymarketMarkets.value.data?.map { 
                    MarketListItem.PolymarketItem(it) 
                } ?: emptyList()
                marketAdapter.submitList(items)
            }
            1 -> {
                val items = viewModel.kalshiMarkets.value.data?.map { 
                    MarketListItem.KalshiItem(it) 
                } ?: emptyList()
                marketAdapter.submitList(items)
            }
        }
    }

    private fun onMarketClicked(marketItem: MarketListItem) {
        when (marketItem) {
            is MarketListItem.PolymarketItem -> {
                val market = marketItem.market
                Toast.makeText(
                    context,
                    "${market.question}\nProbability: ${String.format("%.1f%%", market.probability * 100)}",
                    Toast.LENGTH_LONG
                ).show()
            }
            is MarketListItem.KalshiItem -> {
                val market = marketItem.market
                Toast.makeText(
                    context,
                    "${market.title}\nProbability: ${String.format("%.1f%%", market.probability * 100)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showError(message: String?) {
        binding.errorContainer.visibility = View.VISIBLE
        binding.errorText.text = message ?: "Error loading markets"
        binding.retryButton.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        updateAuthButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
