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

    // ── Galactic Commanders ──────────────────────────────────────────────────

    data class GalacticCommander(
        val id: String,
        val nameDE: String,
        val nameEN: String,
        val fights: Map<Int, List<GymPokemon>>   // 1=Fight1, 2=Fight2, 3=Fight3
    )

    private val commanderMeta = mapOf(
        "mars"    to ("Galaktik Mars"    to "Galactic Mars"),
        "jupiter" to ("Galaktik Jupiter" to "Galactic Jupiter"),
        "saturn"  to ("Galaktik Saturn"  to "Galactic Saturn")
    )

    private val commanderFiles = mapOf(
        "mars"    to "GalacticMars.txt",
        "jupiter" to "GalacticJupiter.txt",
        "saturn"  to "GalacticSaturn.txt"
    )

    fun loadGalacticCommander(context: Context, id: String): GalacticCommander? {
        val fileName = commanderFiles[id] ?: return null
        val file = File(DataSyncManager.trainerDir(context), fileName)
        if (!file.exists()) return null
        val (nameDE, nameEN) = commanderMeta[id] ?: return null
        return GalacticCommander(id, nameDE, nameEN, parseFightTeams(file))
    }

    fun loadGalacticCyrus(context: Context): List<GymPokemon> {
        val file = File(DataSyncManager.trainerDir(context), "GalacticCyrus.txt")
        if (!file.exists()) return emptyList()
        val lines = file.readLines().map { it.trim() }
        val pokemon = mutableListOf<GymPokemon>()
        var pendingName: Pair<String, String>? = null
        for (line in lines) {
            when {
                line.startsWith("_") || line.isEmpty() -> { /* skip */ }
                line.startsWith("BP") -> {
                    val bp = line.removePrefix("BP").trim().toIntOrNull() ?: 0
                    val (de, en) = pendingName ?: continue
                    pokemon.add(buildGymPokemon(de, en, bp))
                    pendingName = null
                }
                line.contains("/") -> pendingName = splitName(line)
            }
        }
        return pokemon
    }

    private fun parseFightTeams(file: File): Map<Int, List<GymPokemon>> {
        val lines = file.readLines().map { it.trim() }
        val fights = mutableMapOf<Int, MutableList<GymPokemon>>()
        var currentFight: Int? = null
        var pendingName: Pair<String, String>? = null

        for (line in lines) {
            val normalised = line.replace(" ", "").lowercase()
            when {
                normalised.startsWith("fight") -> {
                    val num = normalised.removePrefix("fight").toIntOrNull()
                    if (num != null) {
                        currentFight = num
                        fights.getOrPut(num) { mutableListOf() }
                        pendingName = null
                    }
                }
                line.startsWith("_") || line.isEmpty() -> { /* separator / blank */ }
                line.startsWith("BP") && currentFight != null -> {
                    val bp = line.removePrefix("BP").trim().toIntOrNull() ?: 0
                    val (de, en) = pendingName ?: continue
                    fights[currentFight!!]!!.add(buildGymPokemon(de, en, bp))
                    pendingName = null
                }
                line.contains("/") -> pendingName = splitName(line)
            }
        }
        return fights.mapValues { it.value.toList() }
    }

    // ─────────────────────────────────────────────────────────────────────────

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
