package com.example.mobile.util

import android.content.res.AssetManager
import android.util.Log
import com.example.mobile.domain.DragonPhase
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.domain.EquipableType
import java.util.Locale

object AssetResolver {
    private const val TAG = "AssetResolver"

    private val phaseFolders = listOf(
        "egg",
        "hatchling",
        "young_dragon",
        "adult_dragon",
        "ancient_dragon"
    )

    private const val backgroundFolder = "backgrounds"
    private const val defaultFileName = "default"
    private const val outfitSuffix = "_outfit"
    private const val auraSuffix = "_aura"
    private val supportedExtensions = setOf("png", "jpg", "jpeg", "webp")

    data class DiscoveredAsset(
        val folder: String,
        val name: String,
        val fileName: String
    )

    fun phaseFolderForStage(stage: Int): String? = phaseFolders.getOrNull(stage)

    fun phaseFolderForPhase(phase: DragonPhase?): String? = phase?.folderName

    fun defaultAssetPath(assetManager: AssetManager, stage: Int): String? {
        val folder = phaseFolderForStage(stage) ?: return null
        return findAssetPath(assetManager, folder, defaultFileName)
            ?: findAssetPath(assetManager, folder, folder)
    }

    fun dragonBaseAssetPath(
        assetManager: AssetManager,
        stage: Int,
        equippedAura: String?
    ): String? {
        val folder = phaseFolderForStage(stage) ?: return null

        if (!equippedAura.isNullOrBlank()) {
            val auraDefinition = EquipableConfig.definition(equippedAura.trim())
            val auraName = equippedAura.trim()
            val auraFolder = auraDefinition?.let { phaseFolderForPhase(it.phase) }
            val auraPath = when {
                auraFolder != null -> findAssetPath(assetManager, auraFolder, auraName)
                else -> phaseFolders.firstNotNullOfOrNull { folder ->
                    findAssetPath(assetManager, folder, auraName)
                }
            }
            if (auraPath != null) return auraPath
        }

        return defaultAssetPath(assetManager, stage)
    }

    fun outfitAssetPath(
        assetManager: AssetManager,
        stage: Int,
        equippedOutfit: String?
    ): String? {
        if (equippedOutfit.isNullOrBlank()) return null
        val folder = phaseFolderForStage(stage) ?: return null
        val outfitDefinition = EquipableConfig.definition(equippedOutfit.trim())
        val outfitFolder = outfitDefinition?.let { phaseFolderForPhase(it.phase) }
        val outfitName = itemNameFromAssetName(equippedOutfit, outfitSuffix)
        return when {
            outfitFolder != null -> findAssetPath(assetManager, outfitFolder, "$outfitName$outfitSuffix")
            else -> phaseFolders.firstNotNullOfOrNull { candidateFolder ->
                findAssetPath(assetManager, candidateFolder, "$outfitName$outfitSuffix")
            }
        }
    }

    fun firstOutfitAssetPath(assetManager: AssetManager, itemIdOrUrl: String?): String? {
        val outfitName = itemNameFromAssetName(itemIdOrUrl, outfitSuffix)
        if (outfitName.isBlank()) return null

        val configuredFolder = EquipableConfig.definition(outfitName)
            ?.let { phaseFolderForPhase(it.phase) }

        return (configuredFolder?.let { listOf(it) } ?: phaseFolders)
            .firstNotNullOfOrNull { folder ->
                findAssetPath(assetManager, folder, "$outfitName$outfitSuffix")
            }
    }

    fun firstAuraAssetPath(assetManager: AssetManager, itemIdOrUrl: String?): String? {
        val auraName = itemNameFromAssetName(itemIdOrUrl, auraSuffix)
        if (auraName.isBlank()) return null

        val configuredFolder = EquipableConfig.definition(auraName)
            ?.let { phaseFolderForPhase(it.phase) }

        return (configuredFolder?.let { listOf(it) } ?: phaseFolders)
            .firstNotNullOfOrNull { folder ->
                findAssetPath(assetManager, folder, "$auraName$auraSuffix")
            }
    }

    fun backgroundAssetPath(assetManager: AssetManager, itemIdOrUrl: String?): String? {
        val backgroundName = normalizeBaseName(itemIdOrUrl)
        if (backgroundName.isBlank()) return null
        return findAssetPath(assetManager, backgroundFolder, backgroundName)
    }

    fun assetPath(assetManager: AssetManager, folder: String, baseName: String): String? =
        findAssetPath(assetManager, folder, baseName)

    fun itemAssetPath(
        assetManager: AssetManager,
        itemType: String,
        itemId: String,
        imageUrl: String
    ): String? = when (EquipableType.fromValue(itemType)) {
        EquipableType.BACKGROUND -> backgroundAssetPath(assetManager, imageUrl.ifBlank { itemId })
        EquipableType.OUTFIT -> firstOutfitAssetPath(assetManager, itemId.ifBlank { imageUrl })
        EquipableType.AURA -> firstAuraAssetPath(assetManager, itemId.ifBlank { imageUrl })
        null -> null
    }

    fun discoverOutfits(assetManager: AssetManager, stage: Int): List<DiscoveredAsset> {
        val folder = phaseFolderForStage(stage) ?: return emptyList()
        return discoverAssetsBySuffix(assetManager, folder, outfitSuffix)
    }

    fun discoverAuras(assetManager: AssetManager, stage: Int): List<DiscoveredAsset> {
        val folder = phaseFolderForStage(stage) ?: return emptyList()
        return discoverAssetsBySuffix(assetManager, folder, auraSuffix)
    }

    fun discoverAllOutfits(assetManager: AssetManager): List<DiscoveredAsset> =
        phaseFolders.flatMap { folder -> discoverOutfits(assetManager, folder) }

    fun discoverAllAuras(assetManager: AssetManager): List<DiscoveredAsset> =
        phaseFolders.flatMap { folder -> discoverAuras(assetManager, folder) }

    fun discoverBackgrounds(assetManager: AssetManager): List<DiscoveredAsset> =
        listAssetFiles(assetManager, backgroundFolder).mapNotNull { fileName ->
            val name = removeExtension(fileName)
            if (name.isBlank()) null else DiscoveredAsset(backgroundFolder, name, fileName)
        }

    private fun discoverOutfits(assetManager: AssetManager, folder: String): List<DiscoveredAsset> =
        discoverAssetsBySuffix(assetManager, folder, outfitSuffix)

    private fun discoverAuras(assetManager: AssetManager, folder: String): List<DiscoveredAsset> =
        discoverAssetsBySuffix(assetManager, folder, auraSuffix)

    private fun discoverAssetsBySuffix(
        assetManager: AssetManager,
        folder: String,
        suffix: String
    ): List<DiscoveredAsset> = listAssetFiles(assetManager, folder)
        .mapNotNull { fileName ->
            val name = removeExtension(fileName)
            if (!name.endsWith(suffix)) return@mapNotNull null
            DiscoveredAsset(folder, name.removeSuffix(suffix), fileName)
        }
        .distinctBy { it.name }
        .sortedWith(compareBy<DiscoveredAsset> { it.folder }.thenBy { it.name })

    private fun findAssetPath(
        assetManager: AssetManager,
        folder: String,
        baseName: String
    ): String? {
        val normalizedBaseName = normalizeBaseName(baseName)
        if (normalizedBaseName.isBlank()) return null

        val matchingFile = listAssetFiles(assetManager, folder)
            .firstOrNull { removeExtension(it).equals(normalizedBaseName, ignoreCase = true) }

        if (matchingFile != null) {
            return "$folder/$matchingFile"
        }

        Log.w(TAG, "Missing asset: $folder/$normalizedBaseName")
        return null
    }

    private fun listAssetFiles(assetManager: AssetManager, folder: String): List<String> =
        assetManager.list(folder)
            ?.filter { fileName ->
                val extension = fileName.substringAfterLast('.', "")
                extension in supportedExtensions || fileName.contains('.')
            }
            ?.sorted()
            ?: emptyList()

    private fun itemNameFromAssetName(value: String?, suffix: String): String {
        val normalizedName = normalizeBaseName(value)
        return if (normalizedName.endsWith(suffix)) {
            normalizedName.removeSuffix(suffix)
        } else {
            normalizedName
        }
    }

    private fun normalizeBaseName(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return value
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .let(::removeExtension)
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun removeExtension(fileName: String): String {
        val extensionIndex = fileName.lastIndexOf('.')
        if (extensionIndex <= 0) return fileName
        return fileName.substring(0, extensionIndex)
    }
}
