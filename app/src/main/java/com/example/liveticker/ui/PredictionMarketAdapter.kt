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
import com.example.liveticker.data.PredictionMarketPosition
import java.util.Locale

class PredictionMarketAdapter(
    private val onPositionClick: (PredictionMarketPosition) -> Unit
) : ListAdapter<PredictionMarketPosition, PredictionMarketAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.prediction_market_item, parent, false)
        return ViewHolder(view, onPositionClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onPositionClick: (PredictionMarketPosition) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val marketQuestion: TextView = itemView.findViewById(R.id.market_question)
        private val outcomeBadge: TextView = itemView.findViewById(R.id.outcome_badge)
        private val shares: TextView = itemView.findViewById(R.id.shares)
        private val avgPrice: TextView = itemView.findViewById(R.id.avg_price)
        private val currentPrice: TextView = itemView.findViewById(R.id.current_price)
        private val positionValue: TextView = itemView.findViewById(R.id.position_value)
        private val pnl: TextView = itemView.findViewById(R.id.pnl)
        private val pnlPercent: TextView = itemView.findViewById(R.id.pnl_percent)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val resolutionDate: TextView = itemView.findViewById(R.id.resolution_date)
        private val chainName: TextView = itemView.findViewById(R.id.chain_name)

        fun bind(position: PredictionMarketPosition) {
            marketQuestion.text = position.marketQuestion
            
            // Outcome badge styling
            outcomeBadge.text = when (position.outcome) {
                PredictionMarketPosition.Outcome.YES -> "YES"
                PredictionMarketPosition.Outcome.NO -> "NO"
            }
            val outcomeColor = when (position.outcome) {
                PredictionMarketPosition.Outcome.YES -> R.color.accent_green
                PredictionMarketPosition.Outcome.NO -> R.color.accent_red
            }
            outcomeBadge.setBackgroundColor(ContextCompat.getColor(itemView.context, outcomeColor))
            
            shares.text = String.format(Locale.US, "%.0f shares", position.shares)
            avgPrice.text = String.format(Locale.US, "Avg: $%.2f", position.avgPrice)
            currentPrice.text = String.format(Locale.US, "Current: $%.2f", position.currentPrice)
            positionValue.text = String.format(Locale.US, "$%.2f", position.currentValue)
            
            // PnL styling
            pnl.text = String.format(Locale.US, "$%.2f", position.pnl)
            pnlPercent.text = String.format(Locale.US, "(%+.2f%%)", position.pnlPercent)
            
            val pnlColor = if (position.pnl >= 0) R.color.accent_green else R.color.accent_red
            pnl.setTextColor(ContextCompat.getColor(itemView.context, pnlColor))
            pnlPercent.setTextColor(ContextCompat.getColor(itemView.context, pnlColor))
            
            category.text = position.category
            resolutionDate.text = "Resolves: ${position.resolutionDate}"
            chainName.text = position.chainName
            
            // Click listener
            itemView.setOnClickListener {
                onPositionClick(position)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PredictionMarketPosition>() {
        override fun areItemsTheSame(
            oldItem: PredictionMarketPosition,
            newItem: PredictionMarketPosition
        ): Boolean = oldItem.marketId == newItem.marketId

        override fun areContentsTheSame(
            oldItem: PredictionMarketPosition,
            newItem: PredictionMarketPosition
        ): Boolean = oldItem == newItem
    }
}

/**
 * Adapter for position-level metrics breakdown
 */
class PositionMetricsAdapter : ListAdapter<com.example.liveticker.data.PositionMetrics, PositionMetricsAdapter.ViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.position_metrics_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val marketName: TextView = itemView.findViewById(R.id.market_name)
        private val weight: TextView = itemView.findViewById(R.id.weight)
        private val probDelta: TextView = itemView.findViewById(R.id.prob_delta)
        private val timeTheta: TextView = itemView.findViewById(R.id.time_theta)
        private val edge: TextView = itemView.findViewById(R.id.edge)

        fun bind(item: com.example.liveticker.data.PositionMetrics) {
            marketName.text = item.marketQuestion.take(30) + if (item.marketQuestion.length > 30) "..." else ""
            weight.text = String.format(Locale.US, "%.1f%%", item.weight * 100)
            probDelta.text = String.format(Locale.US, "%+.3f", item.metrics.probabilityDelta)
            timeTheta.text = String.format(Locale.US, "%+.2f", item.metrics.timeTheta)
            edge.text = String.format(Locale.US, "%+.1f%%", item.metrics.edgePercent)
            
            // Color coding
            val context = itemView.context
            probDelta.setTextColor(ContextCompat.getColor(context, 
                if (item.metrics.probabilityDelta >= 0) R.color.accent_green else R.color.accent_red))
            timeTheta.setTextColor(ContextCompat.getColor(context,
                if (item.metrics.timeTheta >= 0) R.color.accent_green else R.color.accent_red))
            edge.setTextColor(ContextCompat.getColor(context,
                if (item.metrics.edgePercent >= 0) R.color.accent_green else R.color.accent_red))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<com.example.liveticker.data.PositionMetrics>() {
        override fun areItemsTheSame(
            oldItem: com.example.liveticker.data.PositionMetrics,
            newItem: com.example.liveticker.data.PositionMetrics
        ): Boolean = oldItem.marketId == newItem.marketId

        override fun areContentsTheSame(
            oldItem: com.example.liveticker.data.PositionMetrics,
            newItem: com.example.liveticker.data.PositionMetrics
        ): Boolean = oldItem == newItem
    }
}
