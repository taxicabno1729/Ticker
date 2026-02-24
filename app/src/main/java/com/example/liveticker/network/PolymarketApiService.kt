package com.example.liveticker.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Polymarket Gamma API Service
 * Docs: https://docs.polymarket.com/
 * Base URL: https://gamma-api.polymarket.com/
 */
interface PolymarketApiService {

    /**
     * Get all active markets
     * Returns a direct array of markets
     */
    @GET("markets")
    suspend fun getMarkets(
        @Query("active") active: Boolean = true,
        @Query("archived") archived: Boolean = false,
        @Query("closed") closed: Boolean = false,
        @Query("limit") limit: Int = 100
    ): List<PolymarketMarket>

    /**
     * Get specific market by slug
     */
    @GET("markets/{market_slug}")
    suspend fun getMarket(
        @Path("market_slug") slug: String
    ): PolymarketMarket

    /**
     * Get market orderbook/prices
     */
    @GET("markets/{market_slug}/orderbook")
    suspend fun getOrderBook(
        @Path("market_slug") slug: String
    ): PolymarketOrderBook

    /**
     * Get events (grouped markets)
     */
    @GET("events")
    suspend fun getEvents(
        @Query("active") active: Boolean = true,
        @Query("limit") limit: Int = 100
    ): List<PolymarketEvent>

    companion object {
        // Gamma API is the public read-only API for Polymarket
        const val BASE_URL = "https://gamma-api.polymarket.com/"
    }
}

// Polymarket Data Models
data class PolymarketMarket(
    val id: String,
    val slug: String,
    val question: String,
    val description: String?,
    @SerializedName("market_type") val marketType: String?,
    val status: String?, // active, closed, resolved
    @SerializedName("resolution_date") val resolutionDate: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("resolved_at") val resolvedAt: String?,
    @SerializedName("best_bid") val bestBid: Double?,
    @SerializedName("best_ask") val bestAsk: Double?,
    @SerializedName("last_price") val lastPrice: Double?,
    val volume: Double?,
    @SerializedName("volume_24h") val volume24h: Double?,
    val liquidity: Double?,
    @SerializedName("open_interest") val openInterest: Double?,
    @SerializedName("comment_count") val commentCount: Int?,
    @SerializedName("tags") val tags: List<PolymarketTag>?,
    val outcomes: List<PolymarketOutcome>?,
    @SerializedName("event_id") val eventId: String?,
    @SerializedName("events") val events: List<PolymarketEventBrief>?
)

data class PolymarketEvent(
    val id: String,
    val slug: String,
    val title: String,
    val description: String?,
    @SerializedName("start_date") val startDate: String?,
    @SerializedName("end_date") val endDate: String?,
    @SerializedName("resolution_date") val resolutionDate: String?,
    @SerializedName("markets") val markets: List<PolymarketMarket>?,
    val tags: List<PolymarketTag>?
)

data class PolymarketEventBrief(
    val id: String,
    val slug: String,
    val title: String?
)

data class PolymarketTag(
    val id: String?,
    val label: String?,
    val slug: String?
)

data class PolymarketOutcome(
    val id: String?,
    val name: String?,
    val probability: Double?,
    @SerializedName("market_id") val marketId: String?
)

data class PolymarketOrderBook(
    val bids: List<PolymarketOrder>?,
    val asks: List<PolymarketOrder>?
)

data class PolymarketOrder(
    val price: Double?,
    val size: Double?
)

// Position data - may need CLOB API for actual positions
data class PolymarketPosition(
    val id: String?,
    @SerializedName("market_slug") val marketSlug: String?,
    @SerializedName("market_question") val marketQuestion: String?,
    val outcome: String?, // "Yes" or "No"
    val shares: Double?,
    @SerializedName("avg_price") val avgPrice: Double?,
    @SerializedName("current_price") val currentPrice: Double?,
    @SerializedName("realized_pnl") val realizedPnl: Double?,
    @SerializedName("unrealized_pnl") val unrealizedPnl: Double?,
    val value: Double?
)
