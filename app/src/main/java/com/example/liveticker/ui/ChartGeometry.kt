package com.example.liveticker.ui

import com.example.liveticker.data.ProbabilityPoint

/**
 * Pure geometry/trend helpers for ProbabilityChartView, kept free of
 * android.view dependencies so they run in plain unit tests.
 */
object ChartGeometry {

    /** True when the series ends at or above where it starts, ordered by time. */
    fun trendIsUp(points: List<ProbabilityPoint>): Boolean {
        if (points.size < 2) return true
        val sorted = points.sortedBy { it.timestampMs }
        return sorted.last().probability >= sorted.first().probability
    }

    /**
     * Index of the point whose timestamp is closest to [timestampMs].
     * Expects [points] sorted ascending by timestamp; returns -1 when empty.
     */
    fun nearestIndex(points: List<ProbabilityPoint>, timestampMs: Long): Int {
        if (points.isEmpty()) return -1
        var best = 0
        var bestDist = Long.MAX_VALUE
        points.forEachIndexed { i, pt ->
            val dist = kotlin.math.abs(pt.timestampMs - timestampMs)
            if (dist < bestDist) {
                bestDist = dist
                best = i
            }
        }
        return best
    }
}
