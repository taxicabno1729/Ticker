package com.example.liveticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.liveticker.data.PredictionMarketMetrics
import com.example.liveticker.data.PredictionMarketPosition
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.databinding.FragmentPredictionMarketBinding
import com.example.liveticker.ui.PositionMetricsAdapter
import com.example.liveticker.ui.PredictionMarketAdapter
import com.example.liveticker.ui.PredictionMarketViewModel
import com.example.liveticker.ui.PredictionMarketViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class PredictionMarketFragment : Fragment() {

    private var _binding: FragmentPredictionMarketBinding? = null
    private val binding get() = _binding!!

    private lateinit var positionsAdapter: PredictionMarketAdapter
    private lateinit var metricsAdapter: PositionMetricsAdapter
    private lateinit var viewModel: PredictionMarketViewModel
    private var walletAddress: String = ""
    private var metricsExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPredictionMarketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        walletAddress = arguments?.getString("wallet_address") ?: ""

        val repository = PredictionMarketRepository()
        val factory = PredictionMarketViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PredictionMarketViewModel::class.java]

        positionsAdapter = PredictionMarketAdapter { position ->
            navigateToMarketDetail(position)
        }
        binding.pmRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = positionsAdapter
        }

        metricsAdapter = PositionMetricsAdapter()
        binding.positionMetricsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = metricsAdapter
        }

        binding.pmMetricsToggle.setOnClickListener {
            metricsExpanded = !metricsExpanded
            binding.positionMetricsRecycler.visibility = if (metricsExpanded) View.VISIBLE else View.GONE
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.pmSwipeRefresh.setOnRefreshListener {
            loadPositions()
        }

        binding.pmRetryButton.setOnClickListener {
            binding.pmErrorContainer.visibility = View.GONE
            binding.pmRetryButton.visibility = View.GONE
            loadPositions()
        }

        observeViewModel()

        if (walletAddress.isNotEmpty()) {
            binding.pmWalletAddress.text = walletAddress
            loadPositions()
        } else {
            binding.pmErrorContainer.visibility = View.VISIBLE
            binding.pmEmpty.text = getString(R.string.no_wallet_connected)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.positions.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.pmLoading.visibility = View.VISIBLE
                        binding.pmErrorContainer.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.pmLoading.visibility = View.GONE
                        binding.pmSwipeRefresh.isRefreshing = false
                        val positions = resource.data ?: emptyList()

                        val totalValue = positions.sumOf { it.currentValue }
                        val totalInvested = positions.sumOf { it.invested }
                        val totalPnl = positions.sumOf { it.pnl }
                        val totalPnlPercent = if (totalInvested > 0) (totalPnl / totalInvested) * 100 else 0.0

                        binding.pmTotalValue.text = String.format("$%,.2f", totalValue)
                        
                        val pnlColor = if (totalPnl >= 0) R.color.accent_green else R.color.accent_red
                        binding.pmTotalPnl.setTextColor(requireContext().getColor(pnlColor))
                        binding.pmTotalPnl.text = String.format(
                            Locale.US, "%s$%,.2f (%+.2f%%)",
                            if (totalPnl >= 0) "+" else "",
                            totalPnl,
                            totalPnlPercent
                        )

                        if (positions.isEmpty()) {
                            binding.pmErrorContainer.visibility = View.VISIBLE
                            binding.pmEmpty.text = getString(R.string.no_positions_found)
                        } else {
                            positionsAdapter.submitList(positions)
                            binding.pmMetricsCard.visibility = View.VISIBLE
                        }
                    }
                    is Resource.Error -> {
                        binding.pmLoading.visibility = View.GONE
                        binding.pmSwipeRefresh.isRefreshing = false
                        binding.pmErrorContainer.visibility = View.VISIBLE
                        binding.pmEmpty.text = "Error: ${resource.message}"
                        binding.pmRetryButton.visibility = View.VISIBLE
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.metrics.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.pmMetricsLoading.visibility = View.VISIBLE
                        binding.pmMetricsGrid.visibility = View.GONE
                        binding.pmMetricsError.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.pmMetricsLoading.visibility = View.GONE
                        binding.pmMetricsError.visibility = View.GONE
                        val state = resource.data ?: return@collectLatest
                        binding.pmMetricsGrid.visibility = View.VISIBLE
                        populateMetrics(state.metrics)
                        metricsAdapter.submitList(state.positionMetrics)
                    }
                    is Resource.Error -> {
                        binding.pmMetricsLoading.visibility = View.GONE
                        binding.pmMetricsGrid.visibility = View.GONE
                        binding.pmMetricsError.visibility = View.VISIBLE
                        binding.pmMetricsError.text = resource.message
                    }
                }
            }
        }
    }

    private fun loadPositions() {
        if (walletAddress.isNotEmpty()) {
            viewModel.loadPositions(walletAddress)
        }
    }

    private fun navigateToMarketDetail(position: PredictionMarketPosition) {
        // For now, show a toast. In the future, navigate to a detailed market view
        Toast.makeText(
            context,
            "${position.marketQuestion}\nCurrent: $${position.currentPrice}",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun populateMetrics(metrics: PredictionMarketMetrics) {
        binding.pmProbDelta.text = String.format(Locale.US, "%+.3f", metrics.probabilityDelta)
        setMetricColor(binding.pmProbDelta, metrics.probabilityDelta)

        binding.pmProbGamma.text = String.format(Locale.US, "%.3f", metrics.probabilityGamma)

        binding.pmTimeTheta.text = String.format(Locale.US, "%+.2f", metrics.timeTheta)
        setMetricColor(binding.pmTimeTheta, metrics.timeTheta)

        binding.pmLiquidityVega.text = String.format(Locale.US, "%.3f", metrics.liquidityVega)

        binding.pmVolumeRho.text = String.format(Locale.US, "%+.3f", metrics.volumeRho)
        setMetricColor(binding.pmVolumeRho, metrics.volumeRho)

        binding.pmImpliedProb.text = String.format(Locale.US, "%.1f%%", metrics.impliedProbability * 100)
    }

    private fun setMetricColor(view: android.widget.TextView, value: Double) {
        val color = if (value >= 0) R.color.accent_green else R.color.accent_red
        view.setTextColor(requireContext().getColor(color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
