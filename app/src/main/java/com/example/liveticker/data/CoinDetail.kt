package com.example.liveticker.data

import com.google.gson.annotations.SerializedName

data class CoinDetail(
    val id: String?,
    val symbol: String?,
    val name: String?,
    val description: Description?,
    val links: CoinLinks?,
    @SerializedName("market_cap_rank") val marketCapRank: Int?,
    @SerializedName("market_data") val marketData: MarketData?
)

data class Description(
    val en: String?
)

data class CoinLinks(
    val homepage: List<String>?
)

data class MarketData(
    @SerializedName("current_price") val currentPrice: Map<String, Double?>?,
    @SerializedName("market_cap") val marketCap: Map<String, Double?>?,
    @SerializedName("total_volume") val totalVolume: Map<String, Double?>?,
    @SerializedName("high_24h") val high24h: Map<String, Double?>?,
    @SerializedName("low_24h") val low24h: Map<String, Double?>?,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @SerializedName("price_change_percentage_7d") val priceChangePercentage7d: Double?,
    @SerializedName("price_change_percentage_30d") val priceChangePercentage30d: Double?,
    @SerializedName("price_change_percentage_1y") val priceChangePercentage1y: Double?,
    val ath: Map<String, Double?>?,
    @SerializedName("ath_change_percentage") val athChangePercentage: Map<String, Double?>?,
    val atl: Map<String, Double?>?,
    @SerializedName("atl_change_percentage") val atlChangePercentage: Map<String, Double?>?,
    @SerializedName("circulating_supply") val circulatingSupply: Double?,
    @SerializedName("total_supply") val totalSupply: Double?,
    @SerializedName("max_supply") val maxSupply: Double?
)
