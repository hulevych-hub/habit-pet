package com.example.mobile.data.repository

import com.example.mobile.data.local.dao.PetDao
import com.example.mobile.data.local.entities.PetEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PetRepositoryImplTest {

    private lateinit var petDao: PetDao
    private lateinit var repository: PetRepositoryImpl

    private val petFlow = MutableStateFlow<PetEntity?>(null)

    @Before
    fun setup() {
        petDao = mock()
        whenever(petDao.getPet()).thenReturn(petFlow)

        // Note: equipItem/unequipItem require database.withTransaction which is a Room
        // extension function that cannot be mocked with Mockito. Those methods are
        // blocked from unit testing. We test getPet, updatePet, and resetPet here.
        repository = PetRepositoryImpl(
            database = mock(),
            petDao = petDao,
            inventoryItemRepository = mock(),
            challengeRepository = mock()
        )
    }

    // ==================== getPet ====================

    @Test
    fun `getPet — returns pet from dao`() {
        val pet = PetEntity(id = 1, name = "Luna", level = 5, xp = 100)
        petFlow.value = pet

        val result = repository.getPet()

        runTest {
            val value = result.firstOrNull()
            assertEquals("Luna", value?.name)
            assertEquals(5, value?.level)
            assertEquals(100L, value?.xp)
        }
    }

    @Test
    fun `getPet — returns default pet when dao returns null`() {
        petFlow.value = null

        runTest {
            val value = repository.getPet().firstOrNull()
            assertEquals(0L, value?.id)
            assertEquals("Luna", value?.name)
            assertEquals("Calm", value?.mood)
        }
    }

    // ==================== updatePet ====================

    @Test
    fun `updatePet — updates when row exists`() = runTest {
        val pet = PetEntity(id = 0, name = "Luna", level = 3, xp = 50)
        doReturn(1).whenever(petDao).updatePet(any())

        repository.updatePet(pet)

        verify(petDao).updatePet(pet.copy(id = 1))
        verify(petDao, never()).insertPet(any())
    }

    @Test
    fun `updatePet — inserts when row does not exist`() = runTest {
        val pet = PetEntity(id = 0, name = "Luna", level = 3, xp = 50)
        doReturn(0).whenever(petDao).updatePet(any())
        doReturn(1L).whenever(petDao).insertPet(any())

        repository.updatePet(pet)

        verify(petDao).updatePet(pet.copy(id = 1))
        verify(petDao).insertPet(pet.copy(id = 1))
    }

    @Test
    fun `updatePet — forces id to 1`() = runTest {
        val pet = PetEntity(id = 99, name = "Luna", level = 1)
        doReturn(1).whenever(petDao).updatePet(any())

        repository.updatePet(pet)

        val captor = org.mockito.kotlin.argumentCaptor<PetEntity>()
        verify(petDao).updatePet(captor.capture())
        assertEquals(1L, captor.firstValue.id)
    }

    // ==================== resetPet ====================

    @Test
    fun `resetPet — delegates to dao`() = runTest {
        repository.resetPet()
        verify(petDao).resetPet()
    }
}
