package com.example.mobile.data.local.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PetEntityCompletedSetsTest {

    @Test
    fun `parseUnlockedIds returns empty set for empty json`() {
        val result = PetEntity.parseUnlockedIds("[]")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `parseUnlockedIds parses single id`() {
        val result = PetEntity.parseUnlockedIds("[\"sakura_set\"]")
        assertEquals(1, result.size)
        assertTrue(result.contains("sakura_set"))
    }

    @Test
    fun `parseUnlockedIds parses multiple ids`() {
        val result = PetEntity.parseUnlockedIds("[\"sakura_set\",\"crystal_set\"]")
        assertEquals(2, result.size)
        assertTrue(result.contains("sakura_set"))
        assertTrue(result.contains("crystal_set"))
    }

    @Test
    fun `unlockedIdsToJson produces valid json`() {
        val ids = setOf("sakura_set", "crystal_set")
        val json = PetEntity.unlockedIdsToJson(ids)
        assertTrue(json.contains("sakura_set"))
        assertTrue(json.contains("crystal_set"))
        assertTrue(json.startsWith("["))
        assertTrue(json.endsWith("]"))
    }

    @Test
    fun `unlockedIdsToJson handles empty set`() {
        val json = PetEntity.unlockedIdsToJson(emptySet())
        assertEquals("[]", json)
    }

    @Test
    fun `default completedSetsJson is empty array`() {
        val pet = PetEntity(id = 1)
        assertEquals("[]", pet.completedSetsJson)
    }

    @Test
    fun `completedSetsJson can be set and parsed`() {
        val pet = PetEntity(
            id = 1,
            completedSetsJson = "[\"sakura_set\",\"royal_set\"]"
        )
        val parsed = PetEntity.parseUnlockedIds(pet.completedSetsJson)
        assertEquals(2, parsed.size)
        assertTrue(parsed.contains("sakura_set"))
        assertTrue(parsed.contains("royal_set"))
    }
}
