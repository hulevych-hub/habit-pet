package com.example.mobile.domain

object PetTitleConfig {

    const val NONE = "none"

    const val FIRST_BLOOD = "first_blood"
    const val ROOKIE = "rookie"
    const val HABIT_BUILDER = "habit_builder"
    const val STREAK_STARTER = "streak_starter"
    const val DAILY_VISITOR = "daily_visitor"
    const val COMBO_MASTER = "combo_master"
    const val SET_COLLECTOR = "set_collector"
    const val CENTURION = "centurion"
    const val UNSTOPPABLE = "unstoppable"
    const val DRACONIC_LEGEND = "draconic_legend"

    data class TitleDefinition(
        val id: String,
        val displayText: String,
        val description: String
    )

    val titles: List<TitleDefinition> = listOf(
        TitleDefinition(FIRST_BLOOD, "First Blood", "Completed your very first habit"),
        TitleDefinition(ROOKIE, "Rookie", "Reached level 5"),
        TitleDefinition(HABIT_BUILDER, "Habit Builder", "Created 5 habits"),
        TitleDefinition(STREAK_STARTER, "Streak Starter", "Reached a 3-day streak"),
        TitleDefinition(DAILY_VISITOR, "Daily Visitor", "Logged in 7 days in a row"),
        TitleDefinition(COMBO_MASTER, "Combo Master", "Reached a 10-hit combo"),
        TitleDefinition(SET_COLLECTOR, "Set Collector", "Completed a customization set"),
        TitleDefinition(CENTURION, "Centurion", "Completed 100 habits total"),
        TitleDefinition(UNSTOPPABLE, "Unstoppable", "Reached a 30-day streak"),
        TitleDefinition(DRACONIC_LEGEND, "Draconic Legend", "Reached level 50")
    )

    fun titleById(id: String): TitleDefinition? = titles.firstOrNull { it.id == id }

    fun displayName(id: String): String = titleById(id)?.displayText ?: ""
}
