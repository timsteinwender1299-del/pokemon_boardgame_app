package com.pokemonbp.data

import com.pokemonbp.model.BattleResult
import com.pokemonbp.model.Pokemon
import com.pokemonbp.model.Team
import com.pokemonbp.model.TeamBattleResult

object BattleCalculator {

    fun calculate(teamA: List<Pokemon>, teamB: List<Pokemon>): TeamBattleResult {
        val teamAResults = teamA.map { calculateForPokemon(it, teamB) }
        val teamBResults = teamB.map { calculateForPokemon(it, teamA) }

        val teamATotalBP = teamAResults.sumOf { it.finalBP }
        val teamBTotalBP = teamBResults.sumOf { it.finalBP }

        val winner = when {
            teamATotalBP > teamBTotalBP -> Team.TEAM_A
            teamBTotalBP > teamATotalBP -> Team.TEAM_B
            else -> null
        }

        return TeamBattleResult(teamAResults, teamBResults, teamATotalBP, teamBTotalBP, winner)
    }

    private fun calculateForPokemon(attacker: Pokemon, opponents: List<Pokemon>): BattleResult {
        val opponentNames = opponents.joinToString(", ") { it.name }
        val allDetails = mutableListOf<TypeChart.MatchupDetail>()
        var totalModifier = 0
        var forcedZero = false

        for (opponent in opponents) {
            val bpResult = TypeChart.getBpResult(attacker.baseBP, attacker.types, opponent.types)
            allDetails.addAll(bpResult.details)

            if (bpResult.zeroedOut) {
                // Single-type immunity: BP is forced to 0 regardless of other matchups
                forcedZero = true
            } else {
                totalModifier += bpResult.modifier
            }
        }

        val finalBP = if (forcedZero) 0 else maxOf(0, attacker.baseBP + totalModifier)

        return BattleResult(
            pokemon = attacker,
            finalBP = finalBP,
            bpModifier = if (forcedZero) -attacker.baseBP else totalModifier,
            matchupDetails = allDetails,
            opponentName = opponentNames,
            zeroedOut = forcedZero
        )
    }
}
