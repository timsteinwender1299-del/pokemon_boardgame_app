package com.pokemonbp.data

import android.content.Context

object GalacticStateManager {

    private const val PREF_FILE    = "pokemonbp_prefs"
    private const val KEY_FIGHT    = "galactic_fight_number"
    private const val KEY_DEFEATED = "galactic_defeated_commanders"

    fun getFightNumber(context: Context): Int =
        prefs(context).getInt(KEY_FIGHT, 1)

    fun getDefeatedCommanders(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_DEFEATED, emptySet()) ?: emptySet()

    fun isAllCommandersDefeated(context: Context): Boolean =
        getDefeatedCommanders(context).containsAll(setOf("mars", "jupiter", "saturn"))

    /** Call after loading a commander's team to record them as defeated and advance the fight number. */
    fun markCommanderDefeated(context: Context, id: String) {
        val p = prefs(context)
        val defeated = p.getStringSet(KEY_DEFEATED, emptySet())?.toMutableSet() ?: mutableSetOf()
        defeated.add(id)
        val nextFight = minOf(p.getInt(KEY_FIGHT, 1) + 1, 3)
        p.edit()
            .putStringSet(KEY_DEFEATED, defeated)
            .putInt(KEY_FIGHT, nextFight)
            .apply()
    }

    fun reset(context: Context) {
        prefs(context).edit()
            .remove(KEY_FIGHT)
            .remove(KEY_DEFEATED)
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
}
