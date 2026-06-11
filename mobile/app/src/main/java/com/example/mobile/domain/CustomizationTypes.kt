package com.example.mobile.domain

object CustomizationTypes {
    const val OUTFIT = "OUTFIT"
    const val BACKGROUND = "BACKGROUND"
    const val AURA = "AURA"

    val TYPES = listOf(OUTFIT, BACKGROUND, AURA)

    fun displayName(type: String): String = when (type) {
        OUTFIT -> "Outfits"
        BACKGROUND -> "Backgrounds"
        AURA -> "Auras"
        else -> type
    }
}
