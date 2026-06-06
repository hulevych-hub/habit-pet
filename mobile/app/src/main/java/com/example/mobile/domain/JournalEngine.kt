package com.example.mobile.domain

import com.example.mobile.data.local.dao.JournalEntryDao
import com.example.mobile.data.local.entities.JournalEntryEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Engine responsible for generating journal entries based on game events.
 * Observes pet evolution, level, and streak milestones to create journal entries.
 */
class JournalEngine(
    private val journalEntryDao: JournalEntryDao,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository
) {

    // Private coroutine scope for observing changes
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Track the last evolution stage for which we generated an entry
    private var lastEvolutionStage: Int = -1
    // Track the last streak milestone for which we generated an entry
    private var lastStreakMilestone: Int = 0

    init {
        scope.launch {
            // Check for initial pet arrival (if no journal entries exist)
            if (journalEntryDao.getAllJournalEntries().first().isEmpty()) {
                scope.launch { generateArrivalEntry() }
            }

            // Initialize current state
            val pet = petRepository.getPet().first()
            lastEvolutionStage = pet.evolutionStage

            val stats = statisticsRepository.getStatistics().first()
            lastStreakMilestone = stats.currentStreak

            // Start observing for changes
            observePet()
            observeStreaks()
        }
    }

    private fun observePet() {
        scope.launch {
            petRepository.getPet()
                .map { it.evolutionStage }
                .distinctUntilChanged()
                .collect { evolutionStage ->
                    if (evolutionStage > lastEvolutionStage) {
                        scope.launch { generateEvolutionEntry(evolutionStage) }
                        lastEvolutionStage = evolutionStage
                    }
                }
        }
    }

    private fun observeStreaks() {
        scope.launch {
            statisticsRepository.getStatistics()
                .map { it.currentStreak }
                .distinctUntilChanged()
                .collect { streak ->
                    val milestones = intArrayOf(7, 14, 30, 60, 100)
                    for (milestone in milestones) {
                        if (streak >= milestone && lastStreakMilestone < milestone) {
                            scope.launch { generateStreakEntry(milestone) }
                            lastStreakMilestone = milestone
                        }
                    }
                }
        }
    }

    private suspend fun generateArrivalEntry() {
        val pet = petRepository.getPet().first()
        val dayNumber = calculateDayNumber(pet.creationDate)
        val entry = JournalEntryEntity(
            dayNumber = dayNumber,
            entryText = "${pet.name} arrived.",
            timestamp = System.currentTimeMillis()
        )
        journalEntryDao.insertJournalEntry(entry)
    }

    private suspend fun generateEvolutionEntry(evolutionStage: Int) {
        val pet = petRepository.getPet().first()
        val dayNumber = calculateDayNumber(pet.creationDate)
        val entryText = when (evolutionStage) {
            1 -> "${pet.name} hatched."
            2 -> "${pet.name} became a young dragon."
            3 -> "${pet.name} became an adult dragon."
            4 -> "${pet.name} became an ancient dragon."
            else -> "${pet.name} evolved to stage $evolutionStage."
        }
        val entry = JournalEntryEntity(
            dayNumber = dayNumber,
            entryText = entryText,
            timestamp = System.currentTimeMillis()
        )
        journalEntryDao.insertJournalEntry(entry)
    }

    private suspend fun generateStreakEntry(streak: Int) {
        val pet = petRepository.getPet().first()
        val dayNumber = calculateDayNumber(pet.creationDate)
        val entryText = "Kept a $streak-day streak!"
        val entry = JournalEntryEntity(
            dayNumber = dayNumber,
            entryText = entryText,
            timestamp = System.currentTimeMillis()
        )
        journalEntryDao.insertJournalEntry(entry)
    }

    private fun calculateDayNumber(creationTimeMillis: Long): Int {
        val now = System.currentTimeMillis()
        val diffMs = now - creationTimeMillis
        return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
    }
}