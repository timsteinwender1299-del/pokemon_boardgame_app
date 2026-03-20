package com.pokemonbp.data

import android.content.Context
import com.pokemonbp.model.EnemyTrainer
import com.pokemonbp.model.GymPokemon
import com.pokemonbp.ui.PokedexData
import java.io.File

object TrainerParser {

    fun loadGymLeaders(context: Context): List<EnemyTrainer.GymLeader> {
        val dir = DataSyncManager.trainerDir(context)
        if (!dir.exists()) return SinnohData.gymLeaders
        val files = dir.listFiles { f -> f.name != "Champions.txt" }
        if (files.isNullOrEmpty()) return SinnohData.gymLeaders
        val parsed = files.mapNotNull { parseGymLeader(it) }
        return parsed.ifEmpty { SinnohData.gymLeaders }
    }

    fun loadChampions(context: Context): List<EnemyTrainer.Champion> {
        val file = File(DataSyncManager.trainerDir(context), "Champions.txt")
        if (!file.exists()) return SinnohData.champions
        val parsed = parseChampions(file)
        return parsed.ifEmpty { SinnohData.champions }
    }

    private fun parseGymLeader(file: File): EnemyTrainer.GymLeader? {
        val lines = file.readLines().map { it.trim() }

        // First non-empty, non-separator line is the trainer name header
        val headerLine = lines.firstOrNull { it.isNotEmpty() && !it.startsWith("_") } ?: return null
        val (nameDE, nameEN) = splitName(headerLine)
        val id = nameEN.lowercase().replace(" ", "")

        val badgeTeams = mutableMapOf<Int, MutableList<GymPokemon>>()
        var currentBadge: Int? = null
        var pendingName: Pair<String, String>? = null

        for (line in lines.drop(1)) {
            when {
                line.startsWith("Badge ") -> {
                    currentBadge = line.removePrefix("Badge ").trim().toIntOrNull()
                    if (currentBadge != null) badgeTeams.getOrPut(currentBadge!!) { mutableListOf() }
                    pendingName = null
                }
                line.startsWith("BP") && currentBadge != null -> {
                    val bp = line.removePrefix("BP").trim().toIntOrNull() ?: 0
                    val (de, en) = pendingName ?: continue
                    badgeTeams[currentBadge!!]?.add(buildGymPokemon(de, en, bp))
                    pendingName = null
                }
                line.contains("/") && !line.startsWith("_") -> {
                    pendingName = splitName(line)
                }
            }
        }

        if (badgeTeams.isEmpty()) return null
        return EnemyTrainer.GymLeader(
            id = id,
            nameDE = nameDE,
            nameEN = nameEN,
            badgeTeams = badgeTeams.mapValues { it.value.toList() }
        )
    }

    private fun parseChampions(file: File): List<EnemyTrainer.Champion> {
        val lines = file.readLines().map { it.trim() }
        val result = mutableListOf<EnemyTrainer.Champion>()
        var currentName: String? = null
        var currentPokemon = mutableListOf<GymPokemon>()
        var pendingName: Pair<String, String>? = null

        for (line in lines) {
            when {
                line.startsWith("Champion ") -> {
                    if (currentName != null) {
                        result.add(EnemyTrainer.Champion(currentName!!, currentPokemon.toList()))
                    }
                    currentName = line.removePrefix("Champion ").trim()
                    currentPokemon = mutableListOf()
                    pendingName = null
                }
                line.startsWith("BP") && currentName != null -> {
                    val bp = line.removePrefix("BP").trim().toIntOrNull() ?: 0
                    val (de, en) = pendingName ?: continue
                    currentPokemon.add(buildGymPokemon(de, en, bp))
                    pendingName = null
                }
                line.contains("/") && !line.startsWith("_") -> {
                    pendingName = splitName(line)
                }
            }
        }
        if (currentName != null) {
            result.add(EnemyTrainer.Champion(currentName!!, currentPokemon.toList()))
        }
        return result
    }

    private fun buildGymPokemon(nameDE: String, nameEN: String, bp: Int): GymPokemon {
        val entry = PokedexData.allPokemon.firstOrNull { it.name.equals(nameEN, ignoreCase = true) }
        return GymPokemon(
            nameDE = nameDE,
            nameEN = nameEN,
            pokedexId = entry?.id ?: 0,
            types = entry?.types ?: listOf(PokemonType.NORMAL),
            baseBP = bp
        )
    }

    private fun splitName(line: String): Pair<String, String> {
        val parts = line.split(" / ", limit = 2)
        return if (parts.size == 2) Pair(parts[0].trim(), parts[1].trim())
        else Pair(line.trim(), line.trim())
    }
}
