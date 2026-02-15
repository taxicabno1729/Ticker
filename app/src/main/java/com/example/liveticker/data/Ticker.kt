package com.example.liveticker.data

data class Ticker(
    val id: String,
    val symbol: String,
    val name: String,
    val current_price: Double,
    val price_change_percentage_24h: Double? = null
)
