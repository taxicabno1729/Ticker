package com.example.liveticker.network

import com.example.liveticker.data.ClobPriceHistoryResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Polymarket CLOB API — public, no auth.
 * https://clob.polymarket.com/prices-history?market=<tokenId>&interval=1m&fidelity=1440
 */
interface PolymarketClobApiService {

    @GET("prices-history")
    suspend fun getPriceHistory(
        @Query("market") tokenId: String,
        @Query("interval") interval: String = "1m",
        @Query("fidelity") fidelityMinutes: Int = 1440
    ): ClobPriceHistoryResponse

    companion object {
        const val BASE_URL = "https://clob.polymarket.com/"
    }
}
