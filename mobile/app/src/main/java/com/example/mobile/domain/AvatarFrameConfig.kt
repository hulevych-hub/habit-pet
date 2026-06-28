package com.example.mobile.domain

object AvatarFrameConfig {

    const val NONE = "none"

    const val GOLDEN = "golden"
    const val FROST = "frost"
    const val FLAME = "flame"
    const val FOREST = "forest"
    const val CELESTIAL = "celestial"
    const val SHADOW = "shadow"
    const val LEGENDARY = "legendary"

    data class FrameDefinition(
        val id: String,
        val name: String,
        val description: String,
        val innerColor: Long,
        val outerColor: Long,
        val accentColor: Long,
        val glowStrength: Float = 0f
    )

    val frames: List<FrameDefinition> = listOf(
        FrameDefinition(
            id = GOLDEN,
            name = "Golden",
            description = "A warm golden glow",
            innerColor = 0xFFFFD700,
            outerColor = 0xFFB8860B,
            accentColor = 0xFFFFF8DC,
            glowStrength = 0.4f
        ),
        FrameDefinition(
            id = FROST,
            name = "Frost",
            description = "A cold blue shimmer",
            innerColor = 0xFF87CEEB,
            outerColor = 0xFF4682B4,
            accentColor = 0xFFF0F8FF,
            glowStrength = 0.4f
        ),
        FrameDefinition(
            id = FLAME,
            name = "Flame",
            description = "Dancing orange embers",
            innerColor = 0xFFFF4500,
            outerColor = 0xFF8B0000,
            accentColor = 0xFFFFFACD,
            glowStrength = 0.5f
        ),
        FrameDefinition(
            id = FOREST,
            name = "Forest",
            description = "Deep green leaves",
            innerColor = 0xFF228B22,
            outerColor = 0xFF006400,
            accentColor = 0xFF90EE90,
            glowStrength = 0.3f
        ),
        FrameDefinition(
            id = CELESTIAL,
            name = "Celestial",
            description = "Twinkling purple stars",
            innerColor = 0xFF9370DB,
            outerColor = 0xFF4B0082,
            accentColor = 0xFFE6E6FA,
            glowStrength = 0.5f
        ),
        FrameDefinition(
            id = SHADOW,
            name = "Shadow",
            description = "A dark mysterious aura",
            innerColor = 0xFF2F2F2F,
            outerColor = 0xFF000000,
            accentColor = 0xFF696969,
            glowStrength = 0.4f
        ),
        FrameDefinition(
            id = LEGENDARY,
            name = "Legendary",
            description = "A radiant rainbow shine",
            innerColor = 0xFFFF1493,
            outerColor = 0xFF00CED1,
            accentColor = 0xFFFFFF00,
            glowStrength = 0.7f
        )
    )

    fun frameById(id: String): FrameDefinition? = frames.firstOrNull { it.id == id }

    fun displayName(id: String): String = frameById(id)?.name ?: "None"
}
