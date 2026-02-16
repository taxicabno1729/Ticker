package com.example.liveticker.data

import kotlin.math.sqrt

object GreeksCalculator {

    fun calculate(prices: List<List<Double>>): CryptoGreeks? {
        if (prices.size < 2) return null

        val closingPrices = prices.map { it.getOrNull(1) ?: return null }
        val dailyReturns = closingPrices.zipWithNext { prev, curr ->
            if (prev == 0.0) return null
            (curr - prev) / prev
        }

        if (dailyReturns.isEmpty()) return null

        val days = dailyReturns.size
        val delta = dailyReturns.average()
        val gamma = stdDev(dailyReturns, delta)

        val firstPrice = closingPrices.first()
        val lastPrice = closingPrices.last()
        val theta = if (firstPrice != 0.0) (lastPrice / firstPrice - 1.0) * 365.0 / days else 0.0

        val vega = gamma * sqrt(365.0)

        val annualizedDailyReturn = delta * 365.0
        val rho = if (vega != 0.0) annualizedDailyReturn / vega else 0.0

        return CryptoGreeks(
            delta = delta,
            gamma = gamma,
            theta = theta,
            vega = vega,
            rho = rho
        )
    }

    fun calculateWeighted(items: List<Pair<Double, CryptoGreeks>>): CryptoGreeks {
        val totalWeight = items.sumOf { it.first }
        if (totalWeight == 0.0) return CryptoGreeks(0.0, 0.0, 0.0, 0.0, 0.0)

        var delta = 0.0
        var gamma = 0.0
        var theta = 0.0
        var vega = 0.0
        var rho = 0.0

        for ((weight, greeks) in items) {
            val w = weight / totalWeight
            delta += w * greeks.delta
            gamma += w * greeks.gamma
            theta += w * greeks.theta
            vega += w * greeks.vega
            rho += w * greeks.rho
        }

        return CryptoGreeks(delta, gamma, theta, vega, rho)
    }

    private fun stdDev(values: List<Double>, mean: Double): Double {
        if (values.size < 2) return 0.0
        val variance = values.sumOf { (it - mean) * (it - mean) } / (values.size - 1)
        return sqrt(variance)
    }
}
