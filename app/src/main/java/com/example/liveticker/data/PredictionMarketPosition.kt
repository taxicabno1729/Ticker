package com.example.liveticker.data

import java.math.BigDecimal

data class PredictionMarketPosition(
    val marketId: String,
    val marketQuestion: String,
    val outcome: Outcome,
    val shares: Double,
    val avgPrice: Double, // Price paid per share (0-1)
    val currentPrice: Double, // Current market price (0-1)
    val invested: Double, // Total amount invested
    val currentValue: Double, // Current value of position
    val pnl: Double, // Profit/Loss in USD
    val pnlPercent: Double, // Profit/Loss percentage
    val resolutionDate: String, // When the market resolves
    val liquidity: Double, // Market liquidity
    val volume24h: Double, // 24h volume
    val category: String, // Politics, Crypto, Sports, etc.
    val chainName: String // Polygon, Ethereum, etc.
) {
    enum class Outcome {
        YES, NO
    }
}

/**
 * Prediction Market Metrics - Equivalent to Greeks for options
 * These measure various sensitivities and risks in prediction market positions
 */
data class PredictionMarketMetrics(
    /**
     * PROBABILITY_DELTA (Δ): Sensitivity of position value to probability changes
     * Range: -1 to 1 (negative for NO positions)
     * Like Delta in options - how much value changes when probability changes by 1%
     */
    val probabilityDelta: Double,
    
    /**
     * PROBABILITY_GAMMA (Γ): Rate of change of Delta
     * Measures how sensitive your position is to large probability swings
     * High gamma = higher risk/reward near 50/50 outcomes
     */
    val probabilityGamma: Double,
    
    /**
     * TIME_THETA (Θ): Time decay of the position
     * How much value is lost/gained as resolution approaches
     * Negative for YES positions in far-off events (time value decay)
     * Positive for NO positions (probability increases as time passes without resolution)
     */
    val timeTheta: Double,
    
    /**
     * LIQUIDITY_VEGA (V): Sensitivity to liquidity changes
     * How much position value changes when market liquidity changes
     * Important for exit pricing - low liquidity = high slippage
     */
    val liquidityVega: Double,
    
    /**
     * VOLUME_RHO (P): Sensitivity to trading volume/volatility
     * How much position value changes with market activity
     * High volume can indicate new information entering the market
     */
    val volumeRho: Double,
    
    /**
     * IMPLIED_PROBABILITY: Current market-implied probability of YES outcome
     * Range: 0 to 1 (0% to 100%)
     */
    val impliedProbability: Double,
    
    /**
     * KELLY_CRITERION: Optimal bet size based on edge
     * Calculated from your estimated probability vs market probability
     */
    val kellyCriterion: Double,
    
    /**
     * EDGE_PERCENT: Your perceived edge vs market
     * Positive if you believe the true probability is higher than market (for YES)
     */
    val edgePercent: Double,
    
    /**
     * SHARP_RATIO: Risk-adjusted return metric
     * Higher is better - indicates good risk/reward balance
     */
    val sharpeRatio: Double,
    
    /**
     * MAX_PAYOUT: Maximum possible return if correct
     */
    val maxPayout: Double,
    
    /**
     * MAX_LOSS: Maximum possible loss if wrong
     */
    val maxLoss: Double
)

data class PredictionMarketMetricsState(
    val metrics: PredictionMarketMetrics,
    val positionMetrics: List<PositionMetrics>,
    val days: Int
)

data class PositionMetrics(
    val marketId: String,
    val marketQuestion: String,
    val weight: Double, // Portfolio weight
    val metrics: PredictionMarketMetrics
)
