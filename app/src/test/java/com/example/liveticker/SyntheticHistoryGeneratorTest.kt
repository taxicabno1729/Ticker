package com.example.liveticker

import com.example.liveticker.data.SyntheticHistoryGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyntheticHistoryGeneratorTest {

    @Test
    fun `same seed produces identical series`() {
        val a = SyntheticHistoryGenerator.generate("mock-1", 0.72, endTimeMs = 1_000_000_000_000L)
        val b = SyntheticHistoryGenerator.generate("mock-1", 0.72, endTimeMs = 1_000_000_000_000L)
        assertEquals(a, b)
    }

    @Test
    fun `different seeds produce different series`() {
        val a = SyntheticHistoryGenerator.generate("mock-1", 0.72, endTimeMs = 1_000_000_000_000L)
        val b = SyntheticHistoryGenerator.generate("mock-2", 0.72, endTimeMs = 1_000_000_000_000L)
        assertNotEquals(a, b)
    }

    @Test
    fun `series has requested length and ends at current probability`() {
        val series = SyntheticHistoryGenerator.generate("seed", 0.65, points = 30, endTimeMs = 1_000_000_000_000L)
        assertEquals(30, series.size)
        assertEquals(0.65, series.last().probability, 1e-9)
        assertEquals(1_000_000_000_000L, series.last().timestampMs)
    }

    @Test
    fun `values are clamped to 0_02 to 0_98`() {
        val series = SyntheticHistoryGenerator.generate("seed", 0.99, points = 60, endTimeMs = 1_000_000_000_000L)
        assertTrue(series.all { it.probability in 0.02..0.98 })
    }

    @Test
    fun `timestamps ascend one day apart`() {
        val series = SyntheticHistoryGenerator.generate("seed", 0.5, points = 3, endTimeMs = 200_000_000_000L)
        val dayMs = 24 * 60 * 60 * 1000L
        assertEquals(200_000_000_000L - 2 * dayMs, series[0].timestampMs)
        assertEquals(200_000_000_000L - dayMs, series[1].timestampMs)
    }
}
