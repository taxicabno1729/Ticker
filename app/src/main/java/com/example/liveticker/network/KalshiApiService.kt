package com.example.liveticker.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Kalshi API Service
 * Docs: https://trading-api.readme.io/
 * Base URL: https://trading-api.kalshi.com/v1
 */
interface KalshiApiService {

    /**
     * Get all active markets
     */
    @GET("markets")
    suspend fun getMarkets(
        @Query("status") status: String = "active",
        @Query("limit") limit: Int = 100
    ): KalshiMarketsResponse

    /**
     * Get specific market
     */
    @GET("markets/{ticker}")
    suspend fun getMarket(
        @Path("ticker") ticker: String
    ): KalshiMarketResponse

    /**
     * Get market orderbook
     */
    @GET("markets/{ticker}/orderbook")
    suspend fun getOrderBook(
        @Path("ticker") ticker: String,
        @Query("depth") depth: Int = 10
    ): KalshiOrderBookResponse

    /**
     * Get user portfolio positions
     * Requires Authorization header with Bearer token
     */
    @GET("portfolio/positions")
    suspend fun getPositions(
        @Header("Authorization") authToken: String,
        @Query("limit") limit: Int = 100
    ): KalshiPositionsResponse

    /**
     * Get user balance
     */
    @GET("portfolio/balance")
    suspend fun getBalance(
        @Header("Authorization") authToken: String
    ): KalshiBalanceResponse

    companion object {
        // Updated Kalshi API endpoint
        const val BASE_URL = "https://api.elections.kalshi.com/trade-api/v2/"
    }
}

// Kalshi Data Models
data class KalshiMarketsResponse(
    val markets: List<KalshiMarket>,
    val cursor: String?
)

data class KalshiMarketResponse(
    val market: KalshiMarket
)

data class KalshiMarket(
    val ticker: String,
    val title: String,
    val description: String?,
    @SerializedName("open_time") val openTime: String?,
    @SerializedName("close_time") val closeTime: String?,
    @SerializedName("settlement_time") val settlementTime: String?,
    @SerializedName("expiration_time") val expirationTime: String?,
    val status: String, // active, closed, settled
    val category: String?,
    @SerializedName("yes_bid") val yesBid: Int?, // Price in cents (0-100)
    @SerializedName("yes_ask") val yesAsk: Int?,
    @SerializedName("last_price") val lastPrice: Int?,
    @SerializedName("volume_24h") val volume24h: Int?,
    @SerializedName("volume") val volume: Int?,
    @SerializedName("open_interest") val openInterest: Int?,
    @SerializedName("liquidity_cents") val liquidityCents: Int?,
    @SerializedName("rules_primary") val rulesPrimary: String?,
    @SerializedName("rules_secondary") val rulesSecondary: String?,
    @SerializedName("settlement_value") val settlementValue: Int?, // 0 or 100
    val subtitle: String?,
    @SerializedName("event_ticker") val eventTicker: String?,
    @SerializedName("event_title") val eventTitle: String?,
    val series: KalshiSeries?
)

data class KalshiSeries(
    val ticker: String,
    val title: String,
    val category: String?
)

data class KalshiOrderBookResponse(
    @SerializedName("market_id") val marketId: String,
    @SerializedName("order_book") val orderBook: KalshiOrderBook
)

data class KalshiOrderBook(
    val yes: List<KalshiOrder>?,
    val no: List<KalshiOrder>?
)

data class KalshiOrder(
    val price: Int, // Price in cents
    val count: Int // Number of contracts
)

data class KalshiPositionsResponse(
    val positions: List<KalshiPosition>
)

data class KalshiPosition(
    @SerializedName("market_id") val marketId: String,
    @SerializedName("market_title") val marketTitle: String?,
    val ticker: String,
    val side: String, // "yes" or "no"
    val count: Int, // Number of contracts
    val cost: Int, // Cost in cents
    @SerializedName("entry_price") val entryPrice: Int?, // Entry price in cents
    @SerializedName("current_price") val currentPrice: Int?, // Current price in cents
    @SerializedName("unrealized_pnl") val unrealizedPnl: Int?, // P&L in cents
    @SerializedName("realized_pnl") val realizedPnl: Int?
)

data class KalshiBalanceResponse(
    @SerializedName("balance_cents") val balanceCents: Int,
    @SerializedName("available_cents") val availableCents: Int,
    @SerializedName("portfolio_value_cents") val portfolioValueCents: Int
)

// Helper to convert cents to dollars
fun Int?.centsToDollars(): Double = (this ?: 0) / 100.0
