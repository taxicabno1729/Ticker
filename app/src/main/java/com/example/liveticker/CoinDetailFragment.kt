package com.example.liveticker

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.liveticker.data.CoinDetail
import com.example.liveticker.data.CoinDetailState
import com.example.liveticker.data.CoinRepository
import com.example.liveticker.data.CryptoGreeks
import com.example.liveticker.data.Resource
import com.example.liveticker.databinding.FragmentCoinDetailBinding
import com.example.liveticker.ui.CoinDetailViewModel
import com.example.liveticker.ui.CoinDetailViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class CoinDetailFragment : Fragment() {

    private var _binding: FragmentCoinDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CoinDetailViewModel
    private var descriptionExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoinDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val coinId = arguments?.getString("coin_id") ?: return
        val coinName = arguments?.getString("coin_name") ?: ""
        val coinSymbol = arguments?.getString("coin_symbol") ?: ""

        // Show name/symbol immediately from args
        binding.detailCoinName.text = coinName
        binding.detailCoinSymbol.text = coinSymbol.uppercase(Locale.getDefault())
        binding.detailIconText.text = coinSymbol.take(1).uppercase(Locale.getDefault())

        val factory = CoinDetailViewModelFactory(CoinRepository(), coinId)
        viewModel = ViewModelProvider(this, factory)[CoinDetailViewModel::class.java]

        binding.detailRetryButton.setOnClickListener {
            binding.detailError.visibility = View.GONE
            viewModel.loadCoinDetail()
        }

        lifecycleScope.launch {
            viewModel.state.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.detailLoading.visibility = View.VISIBLE
                        binding.detailContent.visibility = View.GONE
                        binding.detailError.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.detailLoading.visibility = View.GONE
                        binding.detailError.visibility = View.GONE
                        binding.detailContent.visibility = View.VISIBLE
                        resource.data?.let { populateUI(it) }
                    }
                    is Resource.Error -> {
                        binding.detailLoading.visibility = View.GONE
                        binding.detailContent.visibility = View.GONE
                        binding.detailError.visibility = View.VISIBLE
                        binding.detailErrorText.text = resource.message
                    }
                }
            }
        }
    }

    private fun populateUI(state: CoinDetailState) {
        val detail = state.coinDetail
        val md = detail.marketData

        // Header
        binding.detailCoinName.text = detail.name ?: arguments?.getString("coin_name") ?: ""
        binding.detailCoinSymbol.text = (detail.symbol ?: "").uppercase(Locale.getDefault())
        binding.detailIconText.text = (detail.symbol ?: "?").take(1).uppercase(Locale.getDefault())

        val price = md?.currentPrice?.get("usd")
        binding.detailPrice.text = price?.let { formatCurrency(it) } ?: getString(R.string.na)

        val change24h = md?.priceChangePercentage24h
        binding.detailChange24h.text = change24h?.let { formatPercent(it) } ?: getString(R.string.na)
        setPercentColor(binding.detailChange24h, change24h)

        // Price Statistics
        binding.detailHigh24h.text = md?.high24h?.get("usd")?.let { formatCurrency(it) } ?: getString(R.string.na)
        binding.detailLow24h.text = md?.low24h?.get("usd")?.let { formatCurrency(it) } ?: getString(R.string.na)

        bindPercentField(binding.detailChange7d, md?.priceChangePercentage7d)
        bindPercentField(binding.detailChange30d, md?.priceChangePercentage30d)
        bindPercentField(binding.detailChange1y, md?.priceChangePercentage1y)

        binding.detailAth.text = md?.ath?.get("usd")?.let { formatCurrency(it) } ?: getString(R.string.na)
        bindPercentField(binding.detailAthChange, md?.athChangePercentage?.get("usd"))
        binding.detailAtl.text = md?.atl?.get("usd")?.let { formatCurrency(it) } ?: getString(R.string.na)
        bindPercentField(binding.detailAtlChange, md?.atlChangePercentage?.get("usd"))

        // Greeks
        populateGreeks(state.greeks)

        // Market Data
        binding.detailRank.text = detail.marketCapRank?.let { "#$it" } ?: getString(R.string.na)
        binding.detailMarketCap.text = md?.marketCap?.get("usd")?.let { formatLargeNumber(it) } ?: getString(R.string.na)
        binding.detailVolume.text = md?.totalVolume?.get("usd")?.let { formatLargeNumber(it) } ?: getString(R.string.na)

        val mcap = md?.marketCap?.get("usd")
        val vol = md?.totalVolume?.get("usd")
        binding.detailVolMcap.text = if (mcap != null && mcap > 0 && vol != null) {
            String.format(Locale.US, "%.4f", vol / mcap)
        } else {
            getString(R.string.na)
        }

        // Supply
        populateSupply(md?.circulatingSupply, md?.totalSupply, md?.maxSupply)

        // About
        populateDescription(detail)
    }

    private fun populateGreeks(greeks: CryptoGreeks?) {
        if (greeks == null) {
            binding.greeksCard.visibility = View.GONE
            return
        }
        binding.greeksCard.visibility = View.VISIBLE

        binding.detailDelta.text = formatGreekValue(greeks.delta)
        setPercentColor(binding.detailDelta, greeks.delta)

        binding.detailGamma.text = formatGreekValue(greeks.gamma)
        binding.detailGamma.setTextColor(requireContext().getColor(R.color.text_primary))

        binding.detailTheta.text = formatPercent(greeks.theta * 100)
        setPercentColor(binding.detailTheta, greeks.theta)

        binding.detailVega.text = formatPercent(greeks.vega * 100)
        binding.detailVega.setTextColor(requireContext().getColor(R.color.text_primary))

        binding.detailRho.text = String.format(Locale.US, "%+.2f", greeks.rho)
        setPercentColor(binding.detailRho, greeks.rho)
    }

    private fun populateSupply(circulating: Double?, total: Double?, max: Double?) {
        binding.detailCirculating.text = circulating?.let { formatLargeNumber(it) } ?: getString(R.string.na)
        binding.detailTotalSupply.text = total?.let { formatLargeNumber(it) } ?: getString(R.string.na)
        binding.detailMaxSupply.text = max?.let { formatLargeNumber(it) } ?: getString(R.string.na)

        val supplyLimit = max ?: total
        if (circulating != null && supplyLimit != null && supplyLimit > 0) {
            val percent = (circulating / supplyLimit * 100).coerceIn(0.0, 100.0)
            binding.detailSupplyProgress.progress = percent.toInt()
            binding.detailSupplyProgress.visibility = View.VISIBLE
            binding.detailSupplyPercent.text = String.format(Locale.US, "%.1f%% circulating", percent)
            binding.detailSupplyPercent.visibility = View.VISIBLE
        } else {
            binding.detailSupplyProgress.visibility = View.GONE
            binding.detailSupplyPercent.visibility = View.GONE
        }
    }

    private fun populateDescription(detail: CoinDetail) {
        val desc = detail.description?.en
        if (desc.isNullOrBlank()) {
            binding.aboutCard.visibility = View.GONE
            return
        }
        binding.aboutCard.visibility = View.VISIBLE
        val plainText = Html.fromHtml(desc, Html.FROM_HTML_MODE_COMPACT).toString().trim()
        binding.detailDescription.text = plainText

        // Show "Show more" if text is long
        binding.detailDescription.post {
            if (binding.detailDescription.lineCount > 4) {
                binding.detailShowMore.visibility = View.VISIBLE
                binding.detailShowMore.setOnClickListener {
                    descriptionExpanded = !descriptionExpanded
                    if (descriptionExpanded) {
                        binding.detailDescription.maxLines = Int.MAX_VALUE
                        binding.detailShowMore.text = getString(R.string.show_less)
                    } else {
                        binding.detailDescription.maxLines = 4
                        binding.detailShowMore.text = getString(R.string.show_more)
                    }
                }
            } else {
                binding.detailShowMore.visibility = View.GONE
            }
        }
    }

    private fun bindPercentField(view: android.widget.TextView, value: Double?) {
        view.text = value?.let { formatPercent(it) } ?: getString(R.string.na)
        setPercentColor(view, value)
    }

    private fun setPercentColor(view: android.widget.TextView, value: Double?) {
        val color = when {
            value == null -> R.color.text_secondary
            value >= 0 -> R.color.accent_green
            else -> R.color.accent_red
        }
        view.setTextColor(requireContext().getColor(color))
    }

    private fun formatCurrency(value: Double): String {
        return if (value >= 1.0) {
            String.format(Locale.US, "$%,.2f", value)
        } else {
            String.format(Locale.US, "$%.6f", value)
        }
    }

    private fun formatPercent(value: Double): String {
        return String.format(Locale.US, "%+.2f%%", value)
    }

    private fun formatGreekValue(value: Double): String {
        return String.format(Locale.US, "%+.4f", value)
    }

    private fun formatLargeNumber(value: Double): String {
        return when {
            value >= 1_000_000_000_000 -> String.format(Locale.US, "$%.2fT", value / 1_000_000_000_000)
            value >= 1_000_000_000 -> String.format(Locale.US, "$%.2fB", value / 1_000_000_000)
            value >= 1_000_000 -> String.format(Locale.US, "$%.2fM", value / 1_000_000)
            value >= 1_000 -> String.format(Locale.US, "$%,.0f", value)
            else -> String.format(Locale.US, "$%.2f", value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
