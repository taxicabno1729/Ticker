package com.example.liveticker.data

import java.util.Random

data class ProbabilityPoint(
    val timestampMs: Long,
    val probability: Double
)

/**
 * Deterministic backward random walk used when no real price history is
 * available (Kalshi markets, Polymarket mock fallbacks, API failures).
 * Same seed + endProbability + endTimeMs always yields the same series.
 */
object SyntheticHistoryGenerator {

    private const val DAY_MS = 24 * 60 * 60 * 1000L
    private const val MIN_P = 0.02
    private const val MAX_P = 0.98
    private const val MAX_DAILY_STEP = 0.06

    fun generate(
        seed: String,
        endProbability: Double,
        points: Int = 30,
        endTimeMs: Long = System.currentTimeMillis()
    ): List<ProbabilityPoint> {
        val rng = Random(seed.hashCode().toLong())
        val values = DoubleArray(points)
        values[points - 1] = endProbability.coerceIn(MIN_P, MAX_P)
        for (i in points - 2 downTo 0) {
            val step = (rng.nextDouble() - 0.5) * MAX_DAILY_STEP
            values[i] = (values[i + 1] + step).coerceIn(MIN_P, MAX_P)
        }
        return values.mapIndexed { i, v ->
            ProbabilityPoint(endTimeMs - (points - 1 - i) * DAY_MS, v)
        }
    }
}

// --- Polymarket CLOB price history ---

data class ClobPricePoint(
    val t: Long,   // unix seconds
    val p: Double  // price of the YES token == probability
)

data class ClobPriceHistoryResponse(
    val history: List<ClobPricePoint>?
)

fun List<ClobPricePoint>.toProbabilityPoints(): List<ProbabilityPoint> =
    map { ProbabilityPoint(timestampMs = it.t * 1000, probability = it.p) }

/**
 * Gamma returns clobTokenIds as a JSON-encoded string array
 * (e.g. "[\"123\",\"456\"]"). First entry is the YES outcome token.
 */
fun parseFirstClobTokenId(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        com.google.gson.Gson().fromJson(raw, Array<String>::class.java)?.firstOrNull()
    } catch (e: Exception) {
        null
    }
}
