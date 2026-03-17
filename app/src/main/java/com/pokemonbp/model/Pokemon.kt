package com.pokemonbp.model

import com.pokemonbp.data.PokemonType

data class PokemonPreset(
    val name: String,
    val nameDE: String = "",
    val pokedexId: Int,
    val types: List<PokemonType>,
    val baseBP: Int = 1
) {
    fun spriteUrl(): String? = if (pokedexId > 0)
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokedexId.png"
    else null
    fun displayName() = if (nameDE.isNotBlank()) "$nameDE / $name" else name
}

data class Pokemon(
    val id: Int,
    val name: String,
    val nameDE: String = "",
    val types: List<PokemonType>,
    val baseBP: Int,
    val team: Team,
    val pokedexId: Int = 0
) {
    fun spriteUrl(): String? = if (pokedexId > 0)
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokedexId.png"
    else null
    fun displayName(): String {
        val n = if (nameDE.isNotBlank()) "$nameDE / $name" else name.ifBlank { types.joinToString("/") { it.displayName } }
        return n
    }
}

enum class Team(val displayName: String, val colorHex: String) {
    TEAM_A("Player", "#E53935"),
    TEAM_B("Enemy Trainer", "#1E88E5")
}

// Trainer avatar placeholder
data class TrainerAvatar(
    val id: Int,
    val gender: TrainerGender,
    val displayName: String
)

enum class TrainerGender { MALE, FEMALE }

// A saved player trainer (Team A)
data class PlayerTrainer(
    val id: String,
    val name: String,
    val avatarId: Int,
    val gender: TrainerGender,
    val pokemon: List<PokemonPreset>  // up to 4, stores preset info
)

// Enemy trainer options
sealed class EnemyTrainer {
    data class GymLeader(
        val id: String,
        val nameDE: String,
        val nameEN: String,
        val badgeTeams: Map<Int, List<GymPokemon>>  // badge 1-8 -> team
    ) : EnemyTrainer()

    data class Champion(val nameEN: String) : EnemyTrainer()
    object WildPokemon : EnemyTrainer()
    object RandomTrainer : EnemyTrainer()
    data class SavedTrainer(val trainer: PlayerTrainer) : EnemyTrainer()
}

data class GymPokemon(
    val nameDE: String,
    val nameEN: String,
    val pokedexId: Int,
    val types: List<PokemonType>,
    val baseBP: Int
)

data class BattleResult(
    val pokemon: Pokemon,
    val finalBP: Int,
    val bpModifier: Int,
    val matchupDetails: List<com.pokemonbp.data.TypeChart.MatchupDetail>,
    val opponentName: String,
    val zeroedOut: Boolean = false
)

data class TeamBattleResult(
    val teamA: List<BattleResult>,
    val teamB: List<BattleResult>,
    val teamATotalBP: Int,
    val teamBTotalBP: Int,
    val winner: Team?
)
