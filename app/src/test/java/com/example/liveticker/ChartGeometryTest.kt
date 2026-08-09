package com.example.liveticker

import com.example.liveticker.data.ProbabilityPoint
import com.example.liveticker.ui.ChartGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartGeometryTest {

    private fun point(ts: Long, p: Double) = ProbabilityPoint(timestampMs = ts, probability = p)

    // --- trendIsUp ---

    @Test
    fun `rising series trends up`() {
        val points = listOf(point(1L, 0.30), point(2L, 0.45), point(3L, 0.60))
        assertTrue(ChartGeometry.trendIsUp(points))
    }

    @Test
    fun `falling series trends down`() {
        val points = listOf(point(1L, 0.85), point(2L, 0.80), point(3L, 0.72))
        assertFalse(ChartGeometry.trendIsUp(points))
    }

    @Test
    fun `flat series counts as up`() {
        val points = listOf(point(1L, 0.50), point(2L, 0.50))
        assertTrue(ChartGeometry.trendIsUp(points))
    }

    @Test
    fun `trend uses timestamps not list order`() {
        // Falling over time, but delivered newest-first (e.g. an API that
        // returns descending history). Must still read as trending down.
        val points = listOf(point(3L, 0.72), point(1L, 0.85), point(2L, 0.80))
        assertFalse(ChartGeometry.trendIsUp(points))
    }

    @Test
    fun `fewer than two points counts as up`() {
        assertTrue(ChartGeometry.trendIsUp(emptyList()))
        assertTrue(ChartGeometry.trendIsUp(listOf(point(1L, 0.10))))
    }

    // --- nearestIndex ---

    private val sorted = listOf(
        point(1000L, 0.40),
        point(2000L, 0.50),
        point(3000L, 0.60),
        point(4000L, 0.70)
    )

    @Test
    fun `exact timestamp returns its index`() {
        assertEquals(2, ChartGeometry.nearestIndex(sorted, 3000L))
    }

    @Test
    fun `between points returns the nearer one`() {
        assertEquals(1, ChartGeometry.nearestIndex(sorted, 2400L))
        assertEquals(2, ChartGeometry.nearestIndex(sorted, 2600L))
    }

    @Test
    fun `before range clamps to first`() {
        assertEquals(0, ChartGeometry.nearestIndex(sorted, 0L))
    }

    @Test
    fun `after range clamps to last`() {
        assertEquals(3, ChartGeometry.nearestIndex(sorted, 99999L))
    }

    @Test
    fun `empty list returns -1`() {
        assertEquals(-1, ChartGeometry.nearestIndex(emptyList(), 1000L))
    }
}
