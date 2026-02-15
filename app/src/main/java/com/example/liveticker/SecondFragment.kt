package com.example.liveticker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.data.WalletRepository
import com.example.liveticker.databinding.FragmentSecondBinding
import com.example.liveticker.ui.PortfolioAdapter
import com.example.liveticker.ui.PortfolioViewModel
import com.example.liveticker.ui.PortfolioViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    private lateinit var portfolioAdapter: PortfolioAdapter
    private lateinit var viewModel: PortfolioViewModel
    private var walletAddress: String = ""

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
                        val ethEntry = tokens.find { it.symbol == "ETH" && it.chainName == "Ethereum" } // Simplified check, rely on data
                            ?: tokens.find { it.symbol == "ETH" }
                        val ethBalanceDisplay = ethEntry?.balance ?: 0.0
                        binding.portfolioEthBalance.text = String.format("%.4f ETH", ethBalanceDisplay)

                        if (tokens.isEmpty()) {
                            binding.portfolioErrorContainer.visibility = View.VISIBLE
                            binding.portfolioEmpty.text = getString(R.string.no_tokens_found)
                        } else {
                            portfolioAdapter.submitList(tokens)
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
    }

    private fun loadPortfolio() {
        if (walletAddress.isNotEmpty()) {
            viewModel.loadPortfolio(walletAddress)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
