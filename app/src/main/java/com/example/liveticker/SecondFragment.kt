package com.example.liveticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liveticker.data.ChainConfigs
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.CryptoGreeks
import com.example.liveticker.data.PortfolioToken
import com.example.liveticker.data.Resource
import com.example.liveticker.data.WalletRepository
import com.example.liveticker.databinding.FragmentSecondBinding
import com.example.liveticker.ui.PortfolioAdapter
import com.example.liveticker.ui.PortfolioListItem
import com.example.liveticker.ui.PortfolioViewModel
import com.example.liveticker.ui.PortfolioViewModelFactory
import com.example.liveticker.ui.TokenGreeksAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private lateinit var portfolioAdapter: PortfolioAdapter
    private lateinit var tokenGreeksAdapter: TokenGreeksAdapter
    private lateinit var viewModel: PortfolioViewModel
    private var walletAddress: String = ""
    private var breakdownExpanded = false
    private var lastPortfolioTokens: List<PortfolioToken>? = null
    
    // Track expanded state for each chain
    private val expandedChains = mutableSetOf<String>()
    private var currentGroupedItems: List<PortfolioListItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        walletAddress = arguments?.getString("wallet_address") ?: ""

        val coinRepository = CoinRepository()
        val walletRepository = WalletRepository()
        val factory = PortfolioViewModelFactory(walletRepository, coinRepository)
        viewModel = ViewModelProvider(this, factory)[PortfolioViewModel::class.java]

        portfolioAdapter = PortfolioAdapter(
            onChainClick = { chainName ->
                toggleChainExpansion(chainName)
            },
            onTokenClick = { token ->
                navigateToTokenDetail(token)
            }
        )
        binding.portfolioRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = portfolioAdapter
        }

        tokenGreeksAdapter = TokenGreeksAdapter()
        binding.tokenGreeksRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tokenGreeksAdapter
        }

        binding.perCoinToggle.setOnClickListener {
            breakdownExpanded = !breakdownExpanded
            binding.tokenGreeksRecycler.visibility = if (breakdownExpanded) View.VISIBLE else View.GONE
        }

        binding.portfolioGreeksApplyButton.setOnClickListener { applyPortfolioGreeksDays() }
        binding.portfolioGreeksDaysInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                applyPortfolioGreeksDays()
                true
            } else false
        }

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.swipeRefresh.setOnRefreshListener {
            loadPortfolio()
        }

        binding.portfolioRetryButton.setOnClickListener {
            binding.portfolioErrorContainer.visibility = View.GONE
            binding.portfolioRetryButton.visibility = View.GONE
            loadPortfolio()
        }

        observeViewModel()

        if (walletAddress.isNotEmpty()) {
            binding.portfolioAddress.text = walletAddress
            loadPortfolio()
        } else {
            binding.portfolioErrorContainer.visibility = View.VISIBLE
            binding.portfolioEmpty.text = getString(R.string.no_wallet_connected)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.portfolio.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.portfolioLoading.visibility = View.VISIBLE
                        binding.portfolioErrorContainer.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.portfolioLoading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        val tokens = resource.data ?: emptyList()

                        val totalValue = tokens.sumOf { it.valueUsd }
                        binding.portfolioTotalValue.text = String.format("$%,.2f", totalValue)

                        // Calculate total portfolio value in ETH equivalent
                        val ethPrice = tokens.find { it.symbol == "ETH" && it.contractAddress == null }?.priceUsd ?: 0.0
                        val totalEthEquivalent = if (ethPrice > 0) totalValue / ethPrice else 0.0
                        binding.portfolioEthBalance.text = String.format("%.4f ETH", totalEthEquivalent)

                        if (tokens.isEmpty()) {
                            binding.portfolioErrorContainer.visibility = View.VISIBLE
                            binding.portfolioEmpty.text = getString(R.string.no_tokens_found)
                        } else {
                            // Initialize expanded state for new chains (all collapsed by default)
                            val chainGroups = tokens.groupBy { it.chainName }
                            chainGroups.keys.forEach { chainName ->
                                // Chains start collapsed; user must tap to expand
                                // expandedChains only contains chains that ARE expanded
                            }
                            
                            // Create grouped list and update adapter
                            currentGroupedItems = createGroupedPortfolioList(tokens)
                            updateVisibleItems()
                            
                            lastPortfolioTokens = tokens
                            binding.portfolioGreeksCard.visibility = View.VISIBLE
                            viewModel.loadPortfolioGreeks(tokens)
                        }
                    }
                    is Resource.Error -> {
                        binding.portfolioLoading.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        binding.portfolioErrorContainer.visibility = View.VISIBLE
                        binding.portfolioEmpty.text = "Error: ${resource.message}"
                        binding.portfolioRetryButton.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.portfolioGreeks.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.portfolioGreeksLoading.visibility = View.VISIBLE
                        binding.portfolioGreeksGrid.visibility = View.GONE
                        binding.portfolioGreeksError.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.portfolioGreeksLoading.visibility = View.GONE
                        binding.portfolioGreeksError.visibility = View.GONE
                        val state = resource.data ?: return@collectLatest
                        binding.portfolioGreeksGrid.visibility = View.VISIBLE
                        binding.portfolioGreeksTitle.text = getString(R.string.greek_indicators_fmt, state.days)
                        populatePortfolioGreeks(state.portfolioGreeks)
                        tokenGreeksAdapter.submitList(state.tokenGreeks)
                    }
                    is Resource.Error -> {
                        binding.portfolioGreeksLoading.visibility = View.GONE
                        binding.portfolioGreeksGrid.visibility = View.GONE
                        binding.portfolioGreeksError.visibility = View.VISIBLE
                        binding.portfolioGreeksError.text = resource.message
                    }
                }
            }
        }
    }

    /**
     * Toggle expansion state for a chain and refresh the list
     */
    private fun toggleChainExpansion(chainName: String) {
        if (expandedChains.contains(chainName)) {
            expandedChains.remove(chainName)
        } else {
            expandedChains.add(chainName)
        }
        updateVisibleItems()
    }

    /**
     * Navigate to token detail screen
     */
    private fun navigateToTokenDetail(token: PortfolioToken) {
        if (token.coingeckoId.isEmpty()) {
            Toast.makeText(context, "Token details not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        val bundle = bundleOf(
            "coin_id" to token.coingeckoId,
            "coin_name" to token.name,
            "coin_symbol" to token.symbol
        )
        findNavController().navigate(R.id.action_SecondFragment_to_CoinDetailFragment, bundle)
    }

    /**
     * Update the adapter with visible items based on expanded state
     */
    private fun updateVisibleItems() {
        val visibleItems = currentGroupedItems.filter { item ->
            when (item) {
                is PortfolioListItem.Header -> true // Always show headers
                is PortfolioListItem.Token -> {
                    // Show token only if its chain is expanded
                    expandedChains.contains(item.portfolioToken.chainName)
                }
            }
        }.map { item ->
            // Update header with current expansion state
            when (item) {
                is PortfolioListItem.Header -> item.copy(isExpanded = expandedChains.contains(item.chainName))
                else -> item
            }
        }
        portfolioAdapter.submitList(visibleItems)
    }

    /**
     * Groups tokens by chain and creates a list with headers for each chain section.
     * Chains are sorted by total value (descending), and tokens within each chain
     * are also sorted by value (descending).
     */
    private fun createGroupedPortfolioList(tokens: List<PortfolioToken>): List<PortfolioListItem> {
        // Group tokens by chain
        val grouped = tokens.groupBy { it.chainName }
        
        val listItems = mutableListOf<PortfolioListItem>()
        
        // Sort chains by their total value (descending)
        val sortedChains = grouped.keys.sortedByDescending { chainName ->
            grouped[chainName]?.sumOf { it.valueUsd } ?: 0.0
        }
        
        for (chainName in sortedChains) {
            val chainTokens = grouped[chainName] ?: continue
            
            // Calculate chain total value
            val chainTotalValue = chainTokens.sumOf { it.valueUsd }
            
            // Get chain symbol from the first token (all tokens in chain should have same native symbol)
            val chainSymbol = chainTokens.firstOrNull()?.symbol ?: ""
            
            // Add header for this chain
            listItems.add(PortfolioListItem.Header(
                chainName = chainName,
                chainSymbol = chainSymbol,
                totalValue = chainTotalValue,
                isExpanded = expandedChains.contains(chainName)
            ))
            
            // Add tokens for this chain, sorted by value (descending)
            val sortedTokens = chainTokens.sortedByDescending { it.valueUsd }
            for (token in sortedTokens) {
                listItems.add(PortfolioListItem.Token(token))
            }
        }
        
        return listItems
    }

    private fun loadPortfolio() {
        if (walletAddress.isNotEmpty()) {
            viewModel.loadPortfolio(walletAddress)
        }
    }

    private fun applyPortfolioGreeksDays() {
        val text = binding.portfolioGreeksDaysInput.text?.toString() ?: return
        val days = text.toIntOrNull() ?: return
        val tokens = lastPortfolioTokens ?: return
        if (days > 0 && tokens.isNotEmpty()) {
            viewModel.loadPortfolioGreeks(tokens, days)
        }
    }

    private fun populatePortfolioGreeks(greeks: CryptoGreeks) {
        binding.portfolioDelta.text = String.format(Locale.US, "%+.4f", greeks.delta)
        setGreekColor(binding.portfolioDelta, greeks.delta)

        binding.portfolioGamma.text = String.format(Locale.US, "%.4f", greeks.gamma)

        binding.portfolioTheta.text = String.format(Locale.US, "%+.2f%%", greeks.theta * 100)
        setGreekColor(binding.portfolioTheta, greeks.theta)

        binding.portfolioVega.text = String.format(Locale.US, "%.2f%%", greeks.vega * 100)

        binding.portfolioRho.text = String.format(Locale.US, "%+.2f", greeks.rho)
        setGreekColor(binding.portfolioRho, greeks.rho)
    }

    private fun setGreekColor(view: android.widget.TextView, value: Double) {
        val color = if (value >= 0) R.color.accent_green else R.color.accent_red
        view.setTextColor(requireContext().getColor(color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
