package com.example.liveticker

import com.example.liveticker.data.ClobPricePoint
import com.example.liveticker.data.parseFirstClobTokenId
import com.example.liveticker.data.toProbabilityPoints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProbabilityHistoryMappingTest {

    @Test
    fun `clob points map seconds to millis preserving probability`() {
        val points = listOf(ClobPricePoint(t = 1_700_000_000L, p = 0.42)).toProbabilityPoints()
        assertEquals(1_700_000_000_000L, points[0].timestampMs)
        assertEquals(0.42, points[0].probability, 1e-9)
    }

    @Test
    fun `empty clob list maps to empty list`() {
        assertEquals(emptyList<Any>(), emptyList<ClobPricePoint>().toProbabilityPoints())
    }

    @Test
    fun `token id parses first entry of json string array`() {
        assertEquals("123abc", parseFirstClobTokenId("""["123abc","456def"]"""))
    }

    @Test
    fun `token id parse returns null for null blank or malformed input`() {
        assertNull(parseFirstClobTokenId(null))
        assertNull(parseFirstClobTokenId(""))
        assertNull(parseFirstClobTokenId("not-json"))
        assertNull(parseFirstClobTokenId("[]"))
    }
}
