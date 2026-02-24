package com.example.liveticker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liveticker.R
import com.example.liveticker.data.PortfolioToken

sealed class PortfolioListItem {
    abstract val id: String

    data class Header(
        val chainName: String,
        val chainSymbol: String,
        val totalValue: Double,
        val isExpanded: Boolean = true
    ) : PortfolioListItem() {
        override val id: String get() = "header_$chainName"
    }

    data class Token(
        val portfolioToken: PortfolioToken
    ) : PortfolioListItem() {
        override val id: String get() = "${portfolioToken.symbol}_${portfolioToken.chainName}"
    }
}

class PortfolioAdapter(
    private val onChainClick: (chainName: String) -> Unit
) : ListAdapter<PortfolioListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TOKEN = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PortfolioListItem.Header -> TYPE_HEADER
            is PortfolioListItem.Token -> TYPE_TOKEN
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.chain_header_item, parent, false)
                HeaderViewHolder(view, onChainClick)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.portfolio_item, parent, false)
                TokenViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is PortfolioListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is PortfolioListItem.Token -> (holder as TokenViewHolder).bind(item.portfolioToken)
        }
    }

    class HeaderViewHolder(
        itemView: View,
        private val onChainClick: (chainName: String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val chainName: TextView = itemView.findViewById(R.id.chain_name)
        private val chainValue: TextView = itemView.findViewById(R.id.chain_value)
        private val chainIconText: TextView = itemView.findViewById(R.id.chain_icon_text)
        private val expandIcon: ImageView = itemView.findViewById(R.id.expand_icon)
        private val headerContainer: View = itemView.findViewById(R.id.header_container)

        fun bind(header: PortfolioListItem.Header) {
            chainName.text = header.chainName
            chainValue.text = String.format("$%,.2f", header.totalValue)
            chainIconText.text = header.chainSymbol.take(1).uppercase()

            // Update expand/collapse icon
            expandIcon.setImageResource(
                if (header.isExpanded) R.drawable.ic_expand_less
                else R.drawable.ic_expand_more
            )

            // Set click listener
            headerContainer.setOnClickListener {
                onChainClick(header.chainName)
            }
        }
    }

    class TokenViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tokenSymbol: TextView = itemView.findViewById(R.id.token_symbol)
        private val tokenName: TextView = itemView.findViewById(R.id.token_name)
        private val tokenBalance: TextView = itemView.findViewById(R.id.token_balance)
        private val tokenValue: TextView = itemView.findViewById(R.id.token_value)
        private val tokenPrice: TextView = itemView.findViewById(R.id.token_price)
        private val tokenIconText: TextView = itemView.findViewById(R.id.token_icon_text)
        private val tokenChain: TextView = itemView.findViewById(R.id.token_chain)

        fun bind(token: PortfolioToken) {
            tokenSymbol.text = token.symbol
            tokenName.text = token.name
            tokenIconText.text = token.symbol.take(1).uppercase()
            tokenBalance.text = if (token.balance < 0.0001 && token.balance > 0) {
                "< 0.0001"
            } else {
                String.format("%.4f", token.balance)
            }
            tokenValue.text = String.format("$%.2f", token.valueUsd)
            tokenPrice.text = String.format("$%.2f", token.priceUsd)

            if (token.valueUsd > 0) {
                tokenValue.setTextColor(itemView.context.getColor(R.color.accent_green))
            } else {
                tokenValue.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }

            // Hide chain tag since we now have section headers
            tokenChain.visibility = View.GONE
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PortfolioListItem>() {
        override fun areItemsTheSame(oldItem: PortfolioListItem, newItem: PortfolioListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PortfolioListItem, newItem: PortfolioListItem): Boolean {
            return oldItem == newItem
        }
    }
}
