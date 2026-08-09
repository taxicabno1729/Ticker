package com.example.liveticker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay
import com.example.liveticker.data.PredictionMarketRepository
import com.example.liveticker.data.Resource
import com.example.liveticker.databinding.FragmentMarketDetailBinding
import com.example.liveticker.ui.MarketDetailViewModel
import com.example.liveticker.ui.MarketDetailViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class MarketDetailFragment : Fragment() {

    private var _binding: FragmentMarketDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MarketDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val polymarket = BundleCompat.getParcelable(args, "polymarket_market", PolymarketMarketDisplay::class.java)
        val kalshi = BundleCompat.getParcelable(args, "kalshi_market", KalshiMarketDisplay::class.java)
        check(polymarket != null || kalshi != null) { "MarketDetailFragment needs a market argument" }

        val factory = MarketDetailViewModelFactory(PredictionMarketRepository())
        viewModel = ViewModelProvider(this, factory)[MarketDetailViewModel::class.java]

        bindMarket(polymarket, kalshi)
        observeHistory()
        viewModel.loadHistory(polymarket, kalshi)
    }

    private fun bindMarket(polymarket: PolymarketMarketDisplay?, kalshi: KalshiMarketDisplay?) {
        val question: String
        val probability: Double
        val volume: Double
        val liquidity: Double
        val resolves: String
        val category: String
        val sourceName: String
        val webUrl: String

        if (polymarket != null) {
            question = polymarket.question
            probability = polymarket.probability
            volume = polymarket.volume24h
            liquidity = polymarket.liquidity
            resolves = polymarket.resolutionDate
            category = polymarket.category
            sourceName = "Polymarket"
            webUrl = polymarket.webUrl
        } else {
            val market = kalshi!!
            question = market.title
            probability = market.probability
            volume = market.volume24h
            liquidity = market.liquidity
            resolves = market.closeTime
            category = market.category
            sourceName = "Kalshi"
            webUrl = market.webUrl
        }

        binding.detailSourceBadge.text = sourceName
        binding.detailCategory.text = category
        binding.detailQuestion.text = question
        binding.detailProbability.text = String.format(Locale.US, "%.1f%%", probability * 100)
        binding.detailYesPrice.text = String.format(Locale.US, "%.0f¢", probability * 100)
        binding.detailNoPrice.text = String.format(Locale.US, "%.0f¢", (1 - probability) * 100)
        binding.detailVolume.text = formatCurrency(volume)
        binding.detailLiquidity.text = formatCurrency(liquidity)
        binding.detailResolution.text = resolves
        binding.detailOpenBrowser.text = "Open on $sourceName"
        binding.detailOpenBrowser.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No browser available to open market", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            viewModel.history.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> binding.detailChartLoading.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.detailChartLoading.visibility = View.GONE
                        binding.detailChart.setPoints(resource.data.orEmpty())
                    }
                    is Resource.Error -> binding.detailChartLoading.visibility = View.GONE
                }
            }
        }
    }

    private fun formatCurrency(value: Double): String = when {
        value >= 1_000_000 -> String.format(Locale.US, "$%.1fM", value / 1_000_000)
        value >= 1_000 -> String.format(Locale.US, "$%.1fK", value / 1_000)
        else -> String.format(Locale.US, "$%.0f", value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
