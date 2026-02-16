package com.example.liveticker.data

data class CoinDetailState(
    val coinDetail: CoinDetail,
    val greeks: CryptoGreeks?,
    val priceHistory: List<List<Double>>?,
    val greeksDays: Int = 30
)
