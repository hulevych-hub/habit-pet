package com.example.mobile.data.local.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticsEntityTest {

    // ==================== freezeDatesToJson ====================

    @Test
    fun `freezeDatesToJson — empty set returns empty brackets`() {
        assertEquals("[]", StatisticsEntity.freezeDatesToJson(emptySet()))
    }

    @Test
    fun `freezeDatesToJson — single date`() {
        val dates = setOf(1001L)
        assertEquals("[1001]", StatisticsEntity.freezeDatesToJson(dates))
    }

    @Test
    fun `freezeDatesToJson — multiple dates`() {
        val dates = setOf(1001L, 1005L, 1010L)
        // joinToString uses ", " as default separator
        assertEquals("[1001, 1005, 1010]", StatisticsEntity.freezeDatesToJson(dates))
    }

    // ==================== parseFreezeDates ====================

    @Test
    fun `parseFreezeDates — empty json returns empty set`() {
        val result = StatisticsEntity.parseFreezeDates("[]")
        assertTrue("empty json should parse to empty set", result.isEmpty())
    }

    @Test
    fun `parseFreezeDates — single date`() {
        val result = StatisticsEntity.parseFreezeDates("[1001]")
        assertEquals(setOf(1001L), result)
    }

    @Test
    fun `parseFreezeDates — multiple dates`() {
        val result = StatisticsEntity.parseFreezeDates("[1001, 1005, 1010]")
        assertEquals(setOf(1001L, 1005L, 1010L), result)
    }

    // ==================== roundtrip ====================

    @Test
    fun `freezeDatesToJson + parseFreezeDates roundtrip`() {
        val dates = setOf(1001L, 1005L, 1010L)
        val json = StatisticsEntity.freezeDatesToJson(dates)
        val parsed = StatisticsEntity.parseFreezeDates(json)
        assertEquals(dates, parsed)
    }

    @Test
    fun `roundtrip — empty set`() {
        val dates = emptySet<Long>()
        val json = StatisticsEntity.freezeDatesToJson(dates)
        val parsed = StatisticsEntity.parseFreezeDates(json)
        assertEquals(dates, parsed)
    }

    @Test
    fun `roundtrip — single date`() {
        val dates = setOf(9999L)
        val json = StatisticsEntity.freezeDatesToJson(dates)
        val parsed = StatisticsEntity.parseFreezeDates(json)
        assertEquals(dates, parsed)
    }

    // ==================== malformed json ====================

    @Test
    fun `parseFreezeDates — malformed json returns empty set`() {
        val result = StatisticsEntity.parseFreezeDates("not json")
        assertTrue("malformed json should return empty set", result.isEmpty())
    }

    @Test
    fun `parseFreezeDates — partial json skips non-numeric`() {
        // mapNotNull silently skips non-numeric tokens
        val result = StatisticsEntity.parseFreezeDates("[1001,abc")
        assertEquals(setOf(1001L), result)
    }
}
