package com.example.liveticker.data

data class TokenGreeks(
    val coingeckoId: String,
    val symbol: String,
    val name: String,
    val weight: Double,
    val greeks: CryptoGreeks
)

data class PortfolioGreeksState(
    val portfolioGreeks: CryptoGreeks,
    val tokenGreeks: List<TokenGreeks>,
    val days: Int
)
