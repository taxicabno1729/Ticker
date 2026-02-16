package com.example.liveticker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.liveticker.R
import com.example.liveticker.data.TokenGreeks
import com.example.liveticker.databinding.TokenGreeksItemBinding
import java.util.Locale

class TokenGreeksAdapter : ListAdapter<TokenGreeks, TokenGreeksAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TokenGreeksItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: TokenGreeksItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TokenGreeks) {
            binding.tokenGreeksSymbol.text = item.symbol.uppercase(Locale.US)
            binding.tokenGreeksWeight.text = String.format(Locale.US, "%.1f%%", item.weight * 100)

            binding.tokenDelta.text = String.format(Locale.US, "%+.4f", item.greeks.delta)
            setColor(binding.tokenDelta, item.greeks.delta)

            binding.tokenGamma.text = String.format(Locale.US, "%.4f", item.greeks.gamma)

            binding.tokenTheta.text = String.format(Locale.US, "%+.1f%%", item.greeks.theta * 100)
            setColor(binding.tokenTheta, item.greeks.theta)

            binding.tokenVega.text = String.format(Locale.US, "%.1f%%", item.greeks.vega * 100)

            binding.tokenRho.text = String.format(Locale.US, "%+.2f", item.greeks.rho)
            setColor(binding.tokenRho, item.greeks.rho)
        }

        private fun setColor(view: android.widget.TextView, value: Double) {
            val color = if (value >= 0) R.color.accent_green else R.color.accent_red
            view.setTextColor(view.context.getColor(color))
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TokenGreeks>() {
            override fun areItemsTheSame(a: TokenGreeks, b: TokenGreeks) =
                a.coingeckoId == b.coingeckoId

            override fun areContentsTheSame(a: TokenGreeks, b: TokenGreeks) = a == b
        }
    }
}
