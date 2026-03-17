package com.pokemonbp.data

enum class PokemonType(val displayName: String, val colorHex: String, val iconRes: String) {
    NORMAL("Normal",   "#919BA3", "type_normal"),
    FIRE("Fire",       "#FF9741", "type_fire"),
    WATER("Water",     "#3692DC", "type_water"),
    ELECTRIC("Electric","#F4D23C","type_electric"),
    GRASS("Grass",     "#38BE4B", "type_grass"),
    ICE("Ice",         "#4CD1C0", "type_ice"),
    FIGHTING("Fighting","#B54015","type_fighting"),
    POISON("Poison",   "#B567CE", "type_poison"),
    GROUND("Ground",   "#FA7A2A", "type_ground"),
    FLYING("Flying",   "#8FA9DE", "type_flying"),
    PSYCHIC("Psychic", "#FF6675", "type_psychic"),
    BUG("Bug",         "#90C22D", "type_bug"),
    ROCK("Rock",       "#C8B686", "type_rock"),
    GHOST("Ghost",     "#4C6AB2", "type_ghost"),
    DRAGON("Dragon",   "#0A67C1", "type_dragon"),
    DARK("Dark",       "#574F5E", "type_dark"),
    STEEL("Steel",     "#92A3AA", "type_steel"),
    FAIRY("Fairy",     "#EC8FE6", "type_fairy");

    fun iconResId(context: android.content.Context): Int =
        context.resources.getIdentifier(iconRes, "drawable", context.packageName)
}

object TypeChart {

    fun getEffectiveness(attacker: PokemonType, defenderType: PokemonType): Double {
        return chart[attacker]?.get(defenderType) ?: 1.0
    }

    /**
     * Returns true if this attacker has ANY 0-effectiveness hit against the defending team.
     * Used to detect the "immune" scenario.
     */
    private fun typeHasZeroVs(atkType: PokemonType, defenderTypes: List<PokemonType>): Boolean {
        return defenderTypes.any { getEffectiveness(atkType, it) == 0.0 }
    }

    /**
     * Calculates the total BP modifier for an attacker against all defender types.
     *
     * Rules:
     * - Super effective (2x)  → +1 BP
     * - Not very effective (0.5x) → -1 BP
     * - No effect (0x), single-type attacker → BP becomes 0 (flagged via BpResult)
     * - No effect (0x), multi-type attacker  → -2 BP for that type; AND all other
     *   matchups from that same attacking type are cancelled (not counted).
     */
    fun getBpResult(
        baseBP: Int,
        attackerTypes: List<PokemonType>,
        defenderTypes: List<PokemonType>
    ): BpResult {
        val isSingleType = attackerTypes.size == 1

        // Single-type: if the one type has 0 effectiveness vs any defender → BP = 0
        if (isSingleType) {
            val atkType = attackerTypes.first()
            if (typeHasZeroVs(atkType, defenderTypes)) {
                val zeroDetail = defenderTypes
                    .filter { getEffectiveness(atkType, it) == 0.0 }
                    .map { MatchupDetail(atkType, it, 0.0, 0, BpChangeReason.ZERO_NULLIFIES_ALL) }
                return BpResult(finalBP = 0, modifier = -baseBP, details = zeroDetail, zeroedOut = true)
            }
        }

        // Multi-type (or single type with no immunity):
        val details = mutableListOf<MatchupDetail>()
        var modifier = 0

        for (atkType in attackerTypes) {
            val hasZero = typeHasZeroVs(atkType, defenderTypes)

            if (hasZero) {
                // This type is fully blocked: -2 BP, all matchups for this type are cancelled
                for (defType in defenderTypes) {
                    val eff = getEffectiveness(atkType, defType)
                    if (eff == 0.0) {
                        details.add(MatchupDetail(atkType, defType, 0.0, -2, BpChangeReason.ZERO_MINUS_TWO))
                        modifier -= 2
                    } else {
                        // These matchups are cancelled due to the immunity
                        val cancelledEff = eff
                        val cancelledChange = when {
                            cancelledEff >= 2.0 -> 1
                            cancelledEff <= 0.5 -> -1
                            else -> 0
                        }
                        if (cancelledChange != 0) {
                            details.add(MatchupDetail(atkType, defType, cancelledEff, cancelledChange, BpChangeReason.CANCELLED_BY_ZERO))
                        }
                    }
                }
            } else {
                // Normal calculation for this type
                for (defType in defenderTypes) {
                    val eff = getEffectiveness(atkType, defType)
                    val change = when {
                        eff >= 2.0 -> 1
                        eff <= 0.5 -> -1
                        else -> 0
                    }
                    if (change != 0) {
                        details.add(MatchupDetail(atkType, defType, eff, change, BpChangeReason.NORMAL))
                        modifier += change
                    }
                }
            }
        }

        return BpResult(
            finalBP = maxOf(0, baseBP + modifier),
            modifier = modifier,
            details = details,
            zeroedOut = false
        )
    }

    enum class BpChangeReason {
        NORMAL,               // Regular +1 / -1
        ZERO_NULLIFIES_ALL,   // Single-type, immunity → BP = 0
        ZERO_MINUS_TWO,       // Multi-type, immunity hit → -2 BP
        CANCELLED_BY_ZERO     // Would have been +1/-1 but cancelled by same-type immunity
    }

    data class MatchupDetail(
        val attackerType: PokemonType,
        val defenderType: PokemonType,
        val effectiveness: Double,
        val bpChange: Int,
        val reason: BpChangeReason = BpChangeReason.NORMAL
    )

    data class BpResult(
        val finalBP: Int,
        val modifier: Int,
        val details: List<MatchupDetail>,
        val zeroedOut: Boolean  // true = single-type immunity, BP forced to 0
    )

    // Legacy helper kept for compatibility — use getBpResult for full logic
    fun getDetailedMatchup(
        attackerTypes: List<PokemonType>,
        defenderTypes: List<PokemonType>
    ): List<MatchupDetail> = getBpResult(0, attackerTypes, defenderTypes).details

    private val chart: Map<PokemonType, Map<PokemonType, Double>> = mapOf(
        PokemonType.NORMAL to mapOf(
            PokemonType.ROCK to 0.5, PokemonType.GHOST to 0.0, PokemonType.STEEL to 0.5
        ),
        PokemonType.FIRE to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 0.5, PokemonType.GRASS to 2.0,
            PokemonType.ICE to 2.0, PokemonType.BUG to 2.0, PokemonType.ROCK to 0.5,
            PokemonType.DRAGON to 0.5, PokemonType.STEEL to 2.0
        ),
        PokemonType.WATER to mapOf(
            PokemonType.FIRE to 2.0, PokemonType.WATER to 0.5, PokemonType.GRASS to 0.5,
            PokemonType.GROUND to 2.0, PokemonType.ROCK to 2.0, PokemonType.DRAGON to 0.5
        ),
        PokemonType.ELECTRIC to mapOf(
            PokemonType.WATER to 2.0, PokemonType.ELECTRIC to 0.5, PokemonType.GRASS to 0.5,
            PokemonType.GROUND to 0.0, PokemonType.FLYING to 2.0, PokemonType.DRAGON to 0.5
        ),
        PokemonType.GRASS to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 2.0, PokemonType.GRASS to 0.5,
            PokemonType.POISON to 0.5, PokemonType.GROUND to 2.0, PokemonType.FLYING to 0.5,
            PokemonType.BUG to 0.5, PokemonType.ROCK to 2.0, PokemonType.DRAGON to 0.5,
            PokemonType.STEEL to 0.5
        ),
        PokemonType.ICE to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 0.5, PokemonType.GRASS to 2.0,
            PokemonType.ICE to 0.5, PokemonType.GROUND to 2.0, PokemonType.FLYING to 2.0,
            PokemonType.DRAGON to 2.0, PokemonType.STEEL to 0.5
        ),
        PokemonType.FIGHTING to mapOf(
            PokemonType.NORMAL to 2.0, PokemonType.ICE to 2.0, PokemonType.POISON to 0.5,
            PokemonType.FLYING to 0.5, PokemonType.PSYCHIC to 0.5, PokemonType.BUG to 0.5,
            PokemonType.ROCK to 2.0, PokemonType.GHOST to 0.0, PokemonType.DARK to 2.0,
            PokemonType.STEEL to 2.0, PokemonType.FAIRY to 0.5
        ),
        PokemonType.POISON to mapOf(
            PokemonType.GRASS to 2.0, PokemonType.POISON to 0.5, PokemonType.GROUND to 0.5,
            PokemonType.ROCK to 0.5, PokemonType.GHOST to 0.5, PokemonType.STEEL to 0.0,
            PokemonType.FAIRY to 2.0
        ),
        PokemonType.GROUND to mapOf(
            PokemonType.FIRE to 2.0, PokemonType.ELECTRIC to 2.0, PokemonType.GRASS to 0.5,
            PokemonType.POISON to 2.0, PokemonType.FLYING to 0.0, PokemonType.BUG to 0.5,
            PokemonType.ROCK to 2.0, PokemonType.STEEL to 2.0
        ),
        PokemonType.FLYING to mapOf(
            PokemonType.ELECTRIC to 0.5, PokemonType.GRASS to 2.0, PokemonType.FIGHTING to 2.0,
            PokemonType.BUG to 2.0, PokemonType.ROCK to 0.5, PokemonType.STEEL to 0.5
        ),
        PokemonType.PSYCHIC to mapOf(
            PokemonType.FIGHTING to 2.0, PokemonType.POISON to 2.0, PokemonType.PSYCHIC to 0.5,
            PokemonType.DARK to 0.0, PokemonType.STEEL to 0.5
        ),
        PokemonType.BUG to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.GRASS to 2.0, PokemonType.FIGHTING to 0.5,
            PokemonType.POISON to 0.5, PokemonType.FLYING to 0.5, PokemonType.PSYCHIC to 2.0,
            PokemonType.GHOST to 0.5, PokemonType.DARK to 2.0, PokemonType.STEEL to 0.5,
            PokemonType.FAIRY to 0.5
        ),
        PokemonType.ROCK to mapOf(
            PokemonType.FIRE to 2.0, PokemonType.ICE to 2.0, PokemonType.FIGHTING to 0.5,
            PokemonType.GROUND to 0.5, PokemonType.FLYING to 2.0, PokemonType.BUG to 2.0,
            PokemonType.STEEL to 0.5
        ),
        PokemonType.GHOST to mapOf(
            PokemonType.NORMAL to 0.0, PokemonType.PSYCHIC to 2.0, PokemonType.GHOST to 2.0,
            PokemonType.DARK to 0.5
        ),
        PokemonType.DRAGON to mapOf(
            PokemonType.DRAGON to 2.0, PokemonType.STEEL to 0.5, PokemonType.FAIRY to 0.0
        ),
        PokemonType.DARK to mapOf(
            PokemonType.FIGHTING to 0.5, PokemonType.PSYCHIC to 2.0, PokemonType.GHOST to 2.0,
            PokemonType.DARK to 0.5, PokemonType.FAIRY to 0.5
        ),
        PokemonType.STEEL to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.WATER to 0.5, PokemonType.ELECTRIC to 0.5,
            PokemonType.ICE to 2.0, PokemonType.ROCK to 2.0, PokemonType.STEEL to 0.5,
            PokemonType.FAIRY to 2.0
        ),
        PokemonType.FAIRY to mapOf(
            PokemonType.FIRE to 0.5, PokemonType.FIGHTING to 2.0, PokemonType.POISON to 0.5,
            PokemonType.DRAGON to 2.0, PokemonType.DARK to 2.0, PokemonType.STEEL to 0.5
        )
    )
}
