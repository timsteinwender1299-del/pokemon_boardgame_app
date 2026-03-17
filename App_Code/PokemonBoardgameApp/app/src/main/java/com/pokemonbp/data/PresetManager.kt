package com.pokemonbp.data

import android.content.Context
import com.pokemonbp.model.PokemonPreset
import com.pokemonbp.model.Team
import org.json.JSONArray
import org.json.JSONObject

object PresetManager {

    private const val PREF_FILE = "pokemonbp_prefs"
    private const val PRESET_KEY_A = "presets_player"
    private const val PRESET_KEY_B = "presets_enemy"

    private fun keyFor(team: Team) = if (team == Team.TEAM_A) PRESET_KEY_A else PRESET_KEY_B

    fun save(context: Context, team: Team, presets: List<PokemonPreset>) {
        val array = JSONArray()
        for (p in presets) {
            val obj = JSONObject()
            obj.put("name", p.name)
            obj.put("pokedexId", p.pokedexId)
            val typesArr = JSONArray()
            p.types.forEach { typesArr.put(it.name) }
            obj.put("types", typesArr)
            array.put(obj)
        }
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit().putString(keyFor(team), array.toString()).apply()
    }

    fun load(context: Context, team: Team): MutableList<PokemonPreset> {
        val json = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(keyFor(team), "[]") ?: "[]"
        val list = mutableListOf<PokemonPreset>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typesArr = obj.getJSONArray("types")
                val types = (0 until typesArr.length()).mapNotNull {
                    runCatching { PokemonType.valueOf(typesArr.getString(it)) }.getOrNull()
                }
                list.add(PokemonPreset(
                    name = obj.getString("name"),
                    pokedexId = obj.optInt("pokedexId", 0),
                    types = types
                ))
            }
        } catch (_: Exception) {}
        return list
    }
}
