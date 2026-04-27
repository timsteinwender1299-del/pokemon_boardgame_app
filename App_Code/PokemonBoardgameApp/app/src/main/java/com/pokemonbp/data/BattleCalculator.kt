package com.pokemonbp.data

import com.pokemonbp.model.BattleResult
import com.pokemonbp.model.Pokemon
import com.pokemonbp.model.Team
import com.pokemonbp.model.TeamBattleResult

object BattleCalculator {

    fun calculate(teamA: List<Pokemon>, teamB: List<Pokemon>, reversed: Boolean = false, assaultVest: Boolean = false): TeamBattleResult {
        val teamAResults = teamA.map { calculateForPokemon(it, teamB, reversed, noNegatives = assaultVest) }
        val teamBResults = teamB.map { calculateForPokemon(it, teamA, reversed) }

        val teamATotalBP = teamAResults.sumOf { it.finalBP }
        val teamBTotalBP = teamBResults.sumOf { it.finalBP }

        val winner = when {
            teamATotalBP > teamBTotalBP -> Team.TEAM_A
            teamBTotalBP > teamATotalBP -> Team.TEAM_B
            else -> null
        }

        return TeamBattleResult(teamAResults, teamBResults, teamATotalBP, teamBTotalBP, winner)
    }

    private fun calculateForPokemon(attacker: Pokemon, opponents: List<Pokemon>, reversed: Boolean = false, noNegatives: Boolean = false): BattleResult {
        val opponentNames = opponents.joinToString(", ") { it.name }
        val allDetails = mutableListOf<TypeChart.MatchupDetail>()
        var totalModifier = 0
        var forcedZero = false

        for (opponent in opponents) {
            val bpResult = TypeChart.getBpResult(attacker.effectiveBp, attacker.types, opponent.types, reversed)

            if (bpResult.zeroedOut && !noNegatives) {
                allDetails.addAll(bpResult.details)
                forcedZero = true
            } else if (bpResult.zeroedOut && noNegatives) {
                // AV blocks single-type immunity — annotate details as blocked
                bpResult.details.forEach { d ->
                    allDetails.add(d.copy(bpChange = 0, reason = TypeChart.BpChangeReason.BLOCKED_BY_AV))
                }
            } else if (!bpResult.zeroedOut) {
                if (noNegatives) {
                    // AV: block each individual negative detail; positives still count
                    bpResult.details.forEach { d ->
                        when {
                            (d.reason == TypeChart.BpChangeReason.NORMAL || d.reason == TypeChart.BpChangeReason.ZERO_MINUS_TWO) && d.bpChange < 0 -> {
                                allDetails.add(d.copy(bpChange = 0, reason = TypeChart.BpChangeReason.BLOCKED_BY_AV))
                            }
                            d.reason == TypeChart.BpChangeReason.NORMAL && d.bpChange > 0 -> {
                                allDetails.add(d)
                                totalModifier += d.bpChange
                            }
                            else -> allDetails.add(d)  // CANCELLED_BY_ZERO and others pass through unchanged
                        }
                    }
                } else {
                    allDetails.addAll(bpResult.details)
                    totalModifier += bpResult.modifier
                }
            }
        }

        val finalBP = if (forcedZero) 0 else maxOf(0, attacker.effectiveBp + totalModifier)

        return BattleResult(
            pokemon = attacker,
            finalBP = finalBP,
            bpModifier = if (forcedZero) -attacker.effectiveBp else totalModifier,
            matchupDetails = allDetails,
            opponentName = opponentNames,
            zeroedOut = forcedZero
        )
    }
}
