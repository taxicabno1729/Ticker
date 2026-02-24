package com.example.liveticker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liveticker.R
import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay
import java.util.Locale

sealed class MarketListItem {
    abstract val id: String
    
    data class PolymarketItem(val market: PolymarketMarketDisplay) : MarketListItem() {
        override val id: String get() = "poly-${market.id}"
    }
    
    data class KalshiItem(val market: KalshiMarketDisplay) : MarketListItem() {
        override val id: String get() = "kal-${market.ticker}"
    }
}

class MarketBrowserAdapter(
    private val onMarketClick: (MarketListItem) -> Unit
) : ListAdapter<MarketListItem, MarketBrowserAdapter.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_POLYMARKET = 0
        private const val TYPE_KALSHI = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is MarketListItem.PolymarketItem -> TYPE_POLYMARKET
            is MarketListItem.KalshiItem -> TYPE_KALSHI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.market_browser_item, parent, false)
        return ViewHolder(view, onMarketClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onMarketClick: (MarketListItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val marketQuestion: TextView = itemView.findViewById(R.id.market_question)
        private val probabilityBadge: TextView = itemView.findViewById(R.id.probability_badge)
        private val volume24h: TextView = itemView.findViewById(R.id.volume_24h)
        private val liquidity: TextView = itemView.findViewById(R.id.liquidity)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val resolutionDate: TextView = itemView.findViewById(R.id.resolution_date)
        private val sourceBadge: TextView = itemView.findViewById(R.id.source_badge)

        fun bind(item: MarketListItem) {
            when (item) {
                is MarketListItem.PolymarketItem -> bindPolymarket(item.market)
                is MarketListItem.KalshiItem -> bindKalshi(item.market)
            }
            
            itemView.setOnClickListener {
                onMarketClick(item)
            }
        }

        private fun bindPolymarket(market: PolymarketMarketDisplay) {
            marketQuestion.text = market.question
            
            val prob = market.probability
            probabilityBadge.text = String.format(Locale.US, "%.1f%%", prob * 100)
            
            // Color based on probability
            val probColor = when {
                prob >= 0.7 -> R.color.accent_green
                prob <= 0.3 -> R.color.accent_red
                else -> R.color.primary
            }
            probabilityBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, probColor))
            
            volume24h.text = formatCurrency(market.volume24h)
            liquidity.text = "Liq: ${formatCurrency(market.liquidity)}"
            category.text = market.category
            resolutionDate.text = "Resolves: ${market.resolutionDate}"
            
            sourceBadge.text = "Polymarket"
            sourceBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary))
        }

        private fun bindKalshi(market: KalshiMarketDisplay) {
            marketQuestion.text = market.title
            
            val prob = market.probability
            probabilityBadge.text = String.format(Locale.US, "%.1f%%", prob * 100)
            
            // Color based on probability
            val probColor = when {
                prob >= 0.7 -> R.color.accent_green
                prob <= 0.3 -> R.color.accent_red
                else -> R.color.primary
            }
            probabilityBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, probColor))
            
            volume24h.text = formatCurrency(market.volume24h)
            liquidity.text = "Liq: ${formatCurrency(market.liquidity)}"
            category.text = market.category
            resolutionDate.text = "Closes: ${market.closeTime}"
            
            sourceBadge.text = "Kalshi"
            sourceBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.secondary))
        }

        private fun formatCurrency(value: Double): String {
            return when {
                value >= 1_000_000 -> String.format(Locale.US, "$%.1fM", value / 1_000_000)
                value >= 1_000 -> String.format(Locale.US, "$%.1fK", value / 1_000)
                else -> String.format(Locale.US, "$%.0f", value)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MarketListItem>() {
        override fun areItemsTheSame(oldItem: MarketListItem, newItem: MarketListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MarketListItem, newItem: MarketListItem): Boolean {
            return oldItem == newItem
        }
    }
}
