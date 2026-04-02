package com.pokemonbp.data

import android.content.Context
import com.pokemonbp.model.*
import org.json.JSONArray
import org.json.JSONObject

data class GameSession(
    val teamA: MutableList<Pokemon>,
    val teamB: MutableList<Pokemon>,
    val isLockedA: Boolean,
    val isLockedB: Boolean,
    val activeIndexA: Int,
    val activeIndexB: Int,
    val faintedA: Set<Int>,
    val faintedB: Set<Int>,
    val teamATrainerId: String?,
    val teamBLabel: String,
    val reverseMode: Boolean,
    // Enemy trainer reconstruction: type + key
    val enemyType: String?,  // "GYMLEADER" | "CHAMPION" | "WILD" | "RANDOM" | "SAVED_TRAINER"
    val enemyKey: String?    // gym leader id | champion nameEN | saved trainer id
)

object SessionManager {

    private const val PREF_FILE = "pokemonbp_prefs"
    private const val SESSION_KEY = "game_session"

    fun save(context: Context, session: GameSession) {
        val obj = JSONObject()
        obj.put("teamA", serializePokemonList(session.teamA))
        obj.put("teamB", serializePokemonList(session.teamB))
        obj.put("isLockedA", session.isLockedA)
        obj.put("isLockedB", session.isLockedB)
        obj.put("activeIndexA", session.activeIndexA)
        obj.put("activeIndexB", session.activeIndexB)
        obj.put("faintedA", JSONArray(session.faintedA.toList()))
        obj.put("faintedB", JSONArray(session.faintedB.toList()))
        if (session.teamATrainerId != null) obj.put("teamATrainerId", session.teamATrainerId)
        obj.put("teamBLabel", session.teamBLabel)
        obj.put("reverseMode", session.reverseMode)
        if (session.enemyType != null) obj.put("enemyType", session.enemyType)
        if (session.enemyKey != null) obj.put("enemyKey", session.enemyKey)
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit().putString(SESSION_KEY, obj.toString()).apply()
    }

    fun load(context: Context): GameSession? {
        val json = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(SESSION_KEY, null) ?: return null
        return try {
            val obj = JSONObject(json)
            val faintedAArr = obj.optJSONArray("faintedA") ?: JSONArray()
            val faintedBArr = obj.optJSONArray("faintedB") ?: JSONArray()
            GameSession(
                teamA = deserializePokemonList(obj.getJSONArray("teamA"), Team.TEAM_A),
                teamB = deserializePokemonList(obj.getJSONArray("teamB"), Team.TEAM_B),
                isLockedA = obj.optBoolean("isLockedA", false),
                isLockedB = obj.optBoolean("isLockedB", false),
                activeIndexA = obj.optInt("activeIndexA", 0),
                activeIndexB = obj.optInt("activeIndexB", 0),
                faintedA = (0 until faintedAArr.length()).map { faintedAArr.getInt(it) }.toSet(),
                faintedB = (0 until faintedBArr.length()).map { faintedBArr.getInt(it) }.toSet(),
                teamATrainerId = obj.optString("teamATrainerId", "").takeIf { it.isNotEmpty() },
                teamBLabel = obj.optString("teamBLabel", "Enemy Trainer"),
                reverseMode = obj.optBoolean("reverseMode", false),
                enemyType = obj.optString("enemyType", "").takeIf { it.isNotEmpty() },
                enemyKey = obj.optString("enemyKey", "").takeIf { it.isNotEmpty() }
            )
        } catch (_: Exception) { null }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit().remove(SESSION_KEY).apply()
    }

    private fun serializePokemonList(list: List<Pokemon>): JSONArray {
        val arr = JSONArray()
        for (p in list) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("nameDE", p.nameDE)
            val types = JSONArray()
            p.types.forEach { types.put(it.name) }
            obj.put("types", types)
            obj.put("baseBP", p.baseBP)
            obj.put("pokedexId", p.pokedexId)
            arr.put(obj)
        }
        return arr
    }

    private fun deserializePokemonList(arr: JSONArray, team: Team): MutableList<Pokemon> {
        val list = mutableListOf<Pokemon>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val typesArr = obj.getJSONArray("types")
            val types = (0 until typesArr.length()).mapNotNull {
                runCatching { PokemonType.valueOf(typesArr.getString(it)) }.getOrNull()
            }
            list.add(Pokemon(
                id = obj.getInt("id"),
                name = obj.optString("name", ""),
                nameDE = obj.optString("nameDE", ""),
                types = types,
                baseBP = obj.optInt("baseBP", 0),
                team = team,
                pokedexId = obj.optInt("pokedexId", 0)
            ))
        }
        return list
    }
}
