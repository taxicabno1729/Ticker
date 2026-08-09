package com.example.liveticker

import com.example.liveticker.data.KalshiMarketDisplay
import com.example.liveticker.data.PolymarketMarketDisplay
import org.junit.Assert.assertEquals
import org.junit.Test

class MarketUrlTest {

    @Test
    fun `polymarket web url is built from slug`() {
        val market = PolymarketMarketDisplay(
            id = "mock-1",
            slug = "bitcoin-100k-2025",
            question = "Will Bitcoin hit $100k by end of 2025?",
            probability = 0.72,
            volume24h = 150000.0,
            liquidity = 2500000.0,
            category = "Crypto",
            resolutionDate = "2025-12-31"
        )
        assertEquals("https://polymarket.com/event/bitcoin-100k-2025", market.webUrl)
    }

    @Test
    fun `kalshi web url is built from ticker`() {
        val market = KalshiMarketDisplay(
            ticker = "KXBTC-100K-2025",
            title = "Will Bitcoin reach $100,000 in 2025?",
            probability = 0.68,
            volume24h = 125000.0,
            liquidity = 1800000.0,
            category = "Crypto",
            closeTime = "2025-12-31"
        )
        assertEquals("https://kalshi.com/markets/KXBTC-100K-2025", market.webUrl)
    }
}
