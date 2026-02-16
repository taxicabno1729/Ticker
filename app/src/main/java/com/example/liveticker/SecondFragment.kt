package com.example.liveticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.CryptoGreeks
import com.example.liveticker.data.PortfolioToken
import com.example.liveticker.data.Resource
import com.example.liveticker.data.WalletRepository
import com.example.liveticker.databinding.FragmentSecondBinding
import com.example.liveticker.ui.PortfolioAdapter
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

        portfolioAdapter = PortfolioAdapter()
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

                        // Find ETH balance
                        val ethEntry = tokens.find { it.symbol == "ETH" && it.chainName == "Ethereum" }
                            ?: tokens.find { it.symbol == "ETH" }
                        val ethBalanceDisplay = ethEntry?.balance ?: 0.0
                        binding.portfolioEthBalance.text = String.format("%.4f ETH", ethBalanceDisplay)

                        if (tokens.isEmpty()) {
                            binding.portfolioErrorContainer.visibility = View.VISIBLE
                            binding.portfolioEmpty.text = getString(R.string.no_tokens_found)
                        } else {
                            portfolioAdapter.submitList(tokens)
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
