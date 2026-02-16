package com.example.liveticker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liveticker.R
import com.example.liveticker.data.Ticker

class TickerAdapter(
    private val onItemClick: (Ticker) -> Unit = {}
) : ListAdapter<Ticker, TickerAdapter.TickerViewHolder>(TickerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TickerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ticker_item, parent, false)
        return TickerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TickerViewHolder, position: Int) {
        val ticker = getItem(position)
        holder.tickerName.text = ticker.name
        holder.tickerSymbol.text = ticker.symbol.uppercase()
        holder.tickerPrice.text = String.format("$%,.2f", ticker.current_price)
        holder.tickerIconText.text = ticker.symbol.take(1).uppercase()

        val change = ticker.price_change_percentage_24h
        if (change != null) {
            holder.tickerChange.visibility = View.VISIBLE
            holder.tickerChange.text = String.format("%+.2f%%", change)
            val color = if (change >= 0) {
                holder.itemView.context.getColor(R.color.accent_green)
            } else {
                holder.itemView.context.getColor(R.color.accent_red)
            }
            holder.tickerChange.setTextColor(color)
        } else {
            holder.tickerChange.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(ticker) }
    }

    class TickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tickerName: TextView = itemView.findViewById(R.id.ticker_name)
        val tickerSymbol: TextView = itemView.findViewById(R.id.ticker_symbol)
        val tickerPrice: TextView = itemView.findViewById(R.id.ticker_price)
        val tickerChange: TextView = itemView.findViewById(R.id.ticker_change)
        val tickerIconText: TextView = itemView.findViewById(R.id.ticker_icon_text)
    }
}

class TickerDiffCallback : DiffUtil.ItemCallback<Ticker>() {
    override fun areItemsTheSame(oldItem: Ticker, newItem: Ticker): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Ticker, newItem: Ticker): Boolean {
        return oldItem == newItem
    }
}
