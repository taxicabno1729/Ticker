package com.example.liveticker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liveticker.R
import com.example.liveticker.data.PortfolioToken

class PortfolioAdapter : ListAdapter<PortfolioToken, PortfolioAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.portfolio_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val token = getItem(position)
        holder.tokenSymbol.text = token.symbol
        holder.tokenName.text = token.name
        holder.tokenIconText.text = token.symbol.take(1).uppercase()
        holder.tokenBalance.text = if (token.balance < 0.0001 && token.balance > 0) {
            "< 0.0001"
        } else {
            String.format("%.4f", token.balance)
        }
        holder.tokenValue.text = String.format("$%.2f", token.valueUsd)
        holder.tokenPrice.text = String.format("$%.2f", token.priceUsd)

        if (token.valueUsd > 0) {
            holder.tokenValue.setTextColor(holder.itemView.context.getColor(R.color.accent_green))
        } else {
            holder.tokenValue.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        }

        // Show chain name for non-Ethereum tokens
        if (token.chainName != "Ethereum") {
            holder.tokenChain.text = token.chainName
            holder.tokenChain.visibility = View.VISIBLE
        } else {
            holder.tokenChain.visibility = View.GONE
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tokenSymbol: TextView = itemView.findViewById(R.id.token_symbol)
        val tokenName: TextView = itemView.findViewById(R.id.token_name)
        val tokenBalance: TextView = itemView.findViewById(R.id.token_balance)
        val tokenValue: TextView = itemView.findViewById(R.id.token_value)
        val tokenPrice: TextView = itemView.findViewById(R.id.token_price)
        val tokenIconText: TextView = itemView.findViewById(R.id.token_icon_text)
        val tokenChain: TextView = itemView.findViewById(R.id.token_chain)
    }

    class DiffCallback : DiffUtil.ItemCallback<PortfolioToken>() {
        override fun areItemsTheSame(oldItem: PortfolioToken, newItem: PortfolioToken) =
            oldItem.symbol == newItem.symbol && oldItem.chainName == newItem.chainName

        override fun areContentsTheSame(oldItem: PortfolioToken, newItem: PortfolioToken) =
            oldItem == newItem
    }
}
