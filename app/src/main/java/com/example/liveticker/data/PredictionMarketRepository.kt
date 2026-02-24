package com.example.liveticker.data

import com.example.liveticker.network.KalshiClient
import com.example.liveticker.network.PolymarketClient
import com.example.liveticker.network.centsToDollars
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class PredictionMarketRepository {

    /**
     * Fetch prediction market positions from both Polymarket and Kalshi
     */
    suspend fun getPositions(walletAddress: String): Resource<List<PredictionMarketPosition>> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch from both sources in parallel
                val polymarketDeferred = async { fetchPolymarketPositions(walletAddress) }
                val kalshiDeferred = async { fetchKalshiPositions(walletAddress) }
                
                val polymarketResult = polymarketDeferred.await()
                val kalshiResult = kalshiDeferred.await()
                
                val allPositions = mutableListOf<PredictionMarketPosition>()
                
                polymarketResult.data?.let { allPositions.addAll(it) }
                kalshiResult.data?.let { allPositions.addAll(it) }
                
                if (allPositions.isEmpty()) {
                    // If both APIs failed, return fallback mock data for demo
                    Resource.Success(getFallbackMockData())
                } else {
                    Resource.Success(allPositions.sortedByDescending { it.currentValue })
                }
            } catch (e: Exception) {
                // Return mock data on any error for now (until real API keys are configured)
                Resource.Success(getFallbackMockData())
            }
        }
    }
    
    /**
     * Fetch positions from Polymarket
     */
    private suspend fun fetchPolymarketPositions(walletAddress: String): Resource<List<PredictionMarketPosition>> {
        return try {
            val response = PolymarketClient.api.getPositions(walletAddress)
            val positions = response.data?.map { position ->
                val invested = position.shares * position.avgPrice
                val currentValue = position.value ?: (position.shares * (position.currentPrice ?: position.avgPrice))
                val pnl = (position.unrealizedPnl ?: 0.0) + (position.realizedPnl ?: 0.0)
                val pnlPercent = if (invested > 0) (pnl / invested) * 100 else 0.0
                
                PredictionMarketPosition(
                    marketId = "polymarket-${position.id}",
                    marketQuestion = position.marketQuestion,
                    outcome = when (position.outcome.lowercase()) {
                        "yes" -> PredictionMarketPosition.Outcome.YES
                        "no" -> PredictionMarketPosition.Outcome.NO
                        else -> PredictionMarketPosition.Outcome.YES
                    },
                    shares = position.shares,
                    avgPrice = position.avgPrice,
                    currentPrice = position.currentPrice ?: position.avgPrice,
                    invested = invested,
                    currentValue = currentValue,
                    pnl = pnl,
                    pnlPercent = pnlPercent,
                    resolutionDate = "2025-12-31", // Would need to fetch from market details
                    liquidity = 1000000.0, // Mock
                    volume24h = 50000.0, // Mock
                    category = "Crypto", // Would need to parse from tags
                    chainName = "Polygon"
                )
            } ?: emptyList()
            Resource.Success(positions)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Polymarket API error")
        }
    }
    
    /**
     * Fetch positions from Kalshi
     * Note: Requires authentication token in production
     */
    private suspend fun fetchKalshiPositions(walletAddress: String): Resource<List<PredictionMarketPosition>> {
        return try {
            // In production, you'd need to get an auth token first
            // For now, return empty as we don't have auth
            // val token = getKalshiAuthToken(walletAddress)
            // val response = KalshiClient.api.getPositions("Bearer $token")
            
            // Mock Kalshi positions for demo
            delay(300) // Simulate network
            
            // Return empty for now - in production implement OAuth flow
            Resource.Success(emptyList())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kalshi API error")
        }
    }
    
    /**
     * Fetch active markets from Polymarket (for discovery)
     */
    suspend fun getPolymarketMarkets(): Resource<List<PolymarketMarketDisplay>> {
        return try {
            val response = PolymarketClient.api.getMarkets(active = true, limit = 50)
            val markets = response.data?.map { market ->
                PolymarketMarketDisplay(
                    id = market.id,
                    slug = market.slug,
                    question = market.question,
                    probability = market.lastPrice ?: market.bestBid ?: 0.0,
                    volume24h = market.volume24h ?: 0.0,
                    liquidity = market.liquidity ?: 0.0,
                    category = market.tags?.firstOrNull()?.label ?: "Other",
                    resolutionDate = market.resolutionDate ?: "TBD"
                )
            } ?: emptyList()
            Resource.Success(markets)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch Polymarket markets")
        }
    }
    
    /**
     * Fetch active markets from Kalshi (for discovery)
     */
    suspend fun getKalshiMarkets(): Resource<List<KalshiMarketDisplay>> {
        return try {
            val response = KalshiClient.api.getMarkets(status = "active", limit = 50)
            val markets = response.markets.map { market ->
                KalshiMarketDisplay(
                    ticker = market.ticker,
                    title = market.title,
                    probability = (market.lastPrice ?: market.yesBid ?: 50).centsToDollars(),
                    volume24h = market.volume24h?.centsToDollars() ?: 0.0,
                    liquidity = market.liquidityCents?.centsToDollars() ?: 0.0,
                    category = market.category ?: "Other",
                    closeTime = market.closeTime ?: "TBD"
                )
            }
            Resource.Success(markets)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch Kalshi markets")
        }
    }
    
    /**
     * Fallback mock data when APIs are unavailable
     */
    private fun getFallbackMockData(): List<PredictionMarketPosition> {
        return listOf(
            PredictionMarketPosition(
                marketId = "polymarket-001",
                marketQuestion = "Will Bitcoin hit $100k by end of 2025?",
                outcome = PredictionMarketPosition.Outcome.YES,
                shares = 500.0,
                avgPrice = 0.65,
                currentPrice = 0.72,
                invested = 325.0,
                currentValue = 360.0,
                pnl = 35.0,
                pnlPercent = 10.77,
                resolutionDate = "2025-12-31",
                liquidity = 2500000.0,
                volume24h = 150000.0,
                category = "Crypto",
                chainName = "Polygon"
            ),
            PredictionMarketPosition(
                marketId = "polymarket-002",
                marketQuestion = "Will ETH ETF be approved by March 2025?",
                outcome = PredictionMarketPosition.Outcome.YES,
                shares = 1000.0,
                avgPrice = 0.45,
                currentPrice = 0.38,
                invested = 450.0,
                currentValue = 380.0,
                pnl = -70.0,
                pnlPercent = -15.56,
                resolutionDate = "2025-03-31",
                liquidity = 5000000.0,
                volume24h = 320000.0,
                category = "Crypto",
                chainName = "Polygon"
            ),
            PredictionMarketPosition(
                marketId = "kalshi-001",
                marketQuestion = "Will Fed cut rates in next meeting?",
                outcome = PredictionMarketPosition.Outcome.NO,
                shares = 800.0,
                avgPrice = 0.40,
                currentPrice = 0.52,
                invested = 320.0,
                currentValue = 416.0,
                pnl = 96.0,
                pnlPercent = 30.0,
                resolutionDate = "2025-03-19",
                liquidity = 1200000.0,
                volume24h = 89000.0,
                category = "Politics",
                chainName = "Kalshi"
            ),
            PredictionMarketPosition(
                marketId = "polymarket-003",
                marketQuestion = "Will Trump win 2024 election?",
                outcome = PredictionMarketPosition.Outcome.YES,
                shares = 250.0,
                avgPrice = 0.48,
                currentPrice = 0.51,
                invested = 120.0,
                currentValue = 127.5,
                pnl = 7.5,
                pnlPercent = 6.25,
                resolutionDate = "2024-11-05",
                liquidity = 8000000.0,
                volume24h = 450000.0,
                category = "Politics",
                chainName = "Polygon"
            ),
            PredictionMarketPosition(
                marketId = "kalshi-002",
                marketQuestion = "Will there be a US recession in 2025?",
                outcome = PredictionMarketPosition.Outcome.NO,
                shares = 600.0,
                avgPrice = 0.35,
                currentPrice = 0.29,
                invested = 210.0,
                currentValue = 174.0,
                pnl = -36.0,
                pnlPercent = -17.14,
                resolutionDate = "2025-12-31",
                liquidity = 1800000.0,
                volume24h = 67000.0,
                category = "Economy",
                chainName = "Kalshi"
            )
        )
    }
}

// Display models for market discovery
data class PolymarketMarketDisplay(
    val id: String,
    val slug: String,
    val question: String,
    val probability: Double,
    val volume24h: Double,
    val liquidity: Double,
    val category: String,
    val resolutionDate: String
)

data class KalshiMarketDisplay(
    val ticker: String,
    val title: String,
    val probability: Double,
    val volume24h: Double,
    val liquidity: Double,
    val category: String,
    val closeTime: String
)

/**
 * Calculator for Prediction Market Metrics (Greeks equivalent)
 */
object PredictionMarketMetricsCalculator {
    
    /**
     * Calculate metrics for a single position
     */
    fun calculatePositionMetrics(position: PredictionMarketPosition): PredictionMarketMetrics {
        val prob = position.currentPrice
        val daysToResolution = calculateDaysToResolution(position.resolutionDate)
        
        // Probability Delta: How much $1 position changes with 1% probability change
        val probabilityDelta = when (position.outcome) {
            PredictionMarketPosition.Outcome.YES -> position.shares * 0.01
            PredictionMarketPosition.Outcome.NO -> -position.shares * 0.01
        }
        
        // Probability Gamma: How fast delta changes (highest near 50/50)
        val probabilityGamma = position.shares * 4 * prob * (1 - prob)
        
        // Time Theta: Value decay/accumulation as resolution approaches
        val timeDecayFactor = if (daysToResolution > 0) 1.0 / sqrt(daysToResolution.toDouble()) else 0.0
        val timeTheta = when (position.outcome) {
            PredictionMarketPosition.Outcome.YES -> -position.currentValue * timeDecayFactor * 0.01
            PredictionMarketPosition.Outcome.NO -> position.currentValue * timeDecayFactor * 0.005
        }
        
        // Liquidity Vega: Impact of liquidity on position
        val liquidityVega = if (position.liquidity > 0) {
            (1000000.0 / position.liquidity) * position.currentValue * 0.001
        } else 0.0
        
        // Volume Rho: Sensitivity to market activity
        val volumeRho = if (position.liquidity > 0) {
            (position.volume24h / position.liquidity) * position.currentValue * 0.01
        } else 0.0
        
        // Kelly Criterion: Optimal bet size
        val b = (1 - prob) / prob
        val p = 0.6 // User's estimated probability
        val q = 1 - p
        val kelly = ((b * p - q) / b).coerceIn(-1.0, 1.0)
        
        // Edge calculation
        val edgePercent = when (position.outcome) {
            PredictionMarketPosition.Outcome.YES -> ((p - prob) / prob) * 100
            PredictionMarketPosition.Outcome.NO -> (((1 - p) - (1 - prob)) / (1 - prob)) * 100
        }
        
        // Sharpe ratio approximation
        val expectedReturn = position.pnlPercent
        val risk = sqrt(prob * (1 - prob)) * 100
        val sharpe = if (risk > 0) expectedReturn / risk else 0.0
        
        // Max payout and loss
        val maxPayout = position.shares * 1.0
        val maxLoss = position.invested
        
        return PredictionMarketMetrics(
            probabilityDelta = probabilityDelta,
            probabilityGamma = probabilityGamma,
            timeTheta = timeTheta,
            liquidityVega = liquidityVega,
            volumeRho = volumeRho,
            impliedProbability = prob,
            kellyCriterion = kelly,
            edgePercent = edgePercent,
            sharpeRatio = sharpe,
            maxPayout = maxPayout,
            maxLoss = maxLoss
        )
    }
    
    /**
     * Calculate weighted portfolio metrics
     */
    fun calculatePortfolioMetrics(
        positions: List<PredictionMarketPosition>
    ): PredictionMarketMetrics {
        if (positions.isEmpty()) {
            return PredictionMarketMetrics(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
            )
        }
        
        val totalValue = positions.sumOf { it.currentValue }
        val weightedItems = positions.map { position ->
            val metrics = calculatePositionMetrics(position)
            val weight = position.currentValue / totalValue
            Pair(weight, metrics)
        }
        
        return PredictionMarketMetrics(
            probabilityDelta = weightedItems.sumOf { it.first * it.second.probabilityDelta },
            probabilityGamma = weightedItems.sumOf { it.first * it.second.probabilityGamma },
            timeTheta = weightedItems.sumOf { it.first * it.second.timeTheta },
            liquidityVega = weightedItems.sumOf { it.first * it.second.liquidityVega },
            volumeRho = weightedItems.sumOf { it.first * it.second.volumeRho },
            impliedProbability = weightedItems.sumOf { it.first * it.second.impliedProbability },
            kellyCriterion = weightedItems.sumOf { it.first * it.second.kellyCriterion },
            edgePercent = weightedItems.sumOf { it.first * it.second.edgePercent },
            sharpeRatio = weightedItems.sumOf { it.first * it.second.sharpeRatio },
            maxPayout = weightedItems.sumOf { it.second.maxPayout },
            maxLoss = weightedItems.sumOf { it.second.maxLoss }
        )
    }
    
    private fun calculateDaysToResolution(dateString: String): Long {
        return try {
            val parts = dateString.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            
            when {
                year == 2024 -> 30L
                year == 2025 && month <= 3 -> 90L
                year == 2025 -> 300L
                else -> 365L
            }
        } catch (e: Exception) {
            180L
        }
    }
}
