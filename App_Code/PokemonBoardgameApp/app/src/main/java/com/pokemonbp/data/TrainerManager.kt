package com.pokemonbp.data

import android.content.Context
import com.pokemonbp.model.*
import org.json.JSONArray
import org.json.JSONObject

object TrainerManager {

    private const val PREF_FILE  = "pokemonbp_prefs"
    private const val TRAINERS_KEY = "player_trainers"

    // Male trainers: id 1–12, maps to trainer_male_1.png … trainer_male_12.png
    val maleAvatars: List<TrainerAvatar> = listOf(
        TrainerAvatar(1,  TrainerGender.MALE, "Barry"),
        TrainerAvatar(2,  TrainerGender.MALE, "Blue"),
        TrainerAvatar(3,  TrainerGender.MALE, "Brendan"),
        TrainerAvatar(4,  TrainerGender.MALE, "Calem"),
        TrainerAvatar(5,  TrainerGender.MALE, "Ethan"),
        TrainerAvatar(6,  TrainerGender.MALE, "Hau"),
        TrainerAvatar(7,  TrainerGender.MALE, "Hilbert"),
        TrainerAvatar(8,  TrainerGender.MALE, "Hugh"),
        TrainerAvatar(9,  TrainerGender.MALE, "Lucas"),
        TrainerAvatar(10, TrainerGender.MALE, "Nate"),
        TrainerAvatar(11, TrainerGender.MALE, "Red"),
        TrainerAvatar(12, TrainerGender.MALE, "Silver")
    )

    // Female trainers: id 101–112, drawable trainer_female_1.png … trainer_female_12.png
    val femaleAvatars: List<TrainerAvatar> = listOf(
        TrainerAvatar(101, TrainerGender.FEMALE, "Bianca"),
        TrainerAvatar(102, TrainerGender.FEMALE, "Dawn"),
        TrainerAvatar(103, TrainerGender.FEMALE, "Hilda"),
        TrainerAvatar(104, TrainerGender.FEMALE, "Leaf"),
        TrainerAvatar(105, TrainerGender.FEMALE, "Lyra"),
        TrainerAvatar(106, TrainerGender.FEMALE, "Marnie"),
        TrainerAvatar(107, TrainerGender.FEMALE, "May"),
        TrainerAvatar(108, TrainerGender.FEMALE, "Nemona"),
        TrainerAvatar(109, TrainerGender.FEMALE, "Penny"),
        TrainerAvatar(110, TrainerGender.FEMALE, "Rosa"),
        TrainerAvatar(111, TrainerGender.FEMALE, "Serena"),
        TrainerAvatar(112, TrainerGender.FEMALE, "Shauna")
    )

    /** Returns the drawable resource name for a given avatarId */
    fun avatarDrawableName(avatarId: Int): String = when {
        avatarId in 1..12  -> "trainer_male_${avatarId}"
        avatarId in 101..112 -> "trainer_female_${avatarId - 100}"
        else -> ""
    }

    /** Returns the drawable resource ID for a given avatarId, or 0 if not found */
    fun avatarResId(context: Context, avatarId: Int): Int {
        val name = avatarDrawableName(avatarId)
        if (name.isEmpty()) return 0
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    fun saveTrainers(context: Context, trainers: List<PlayerTrainer>) {
        val arr = JSONArray()
        for (t in trainers) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("name", t.name)
            obj.put("avatarId", t.avatarId)
            obj.put("gender", t.gender.name)
            val pkArr = JSONArray()
            for (p in t.pokemon) {
                val pk = JSONObject()
                pk.put("name", p.name)
                pk.put("nameDE", p.nameDE)
                pk.put("pokedexId", p.pokedexId)
                pk.put("baseBP", p.baseBP)
                pk.put("level", p.level)
                pk.put("xp", p.xp)
                pk.put("xpLocked", p.xpLocked)
                val types = JSONArray()
                p.types.forEach { types.put(it.name) }
                pk.put("types", types)
                pkArr.put(pk)
            }
            obj.put("pokemon", pkArr)
            val badgeArr = JSONArray()
            t.badges.forEach { badgeArr.put(it) }
            obj.put("badges", badgeArr)
            val faintedArr = JSONArray()
            t.faintedPokemonIds.forEach { faintedArr.put(it) }
            obj.put("faintedPokemonIds", faintedArr)
            arr.put(obj)
        }
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit().putString(TRAINERS_KEY, arr.toString()).apply()
    }

    fun loadTrainers(context: Context): MutableList<PlayerTrainer> {
        val json = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(TRAINERS_KEY, "[]") ?: "[]"
        val list = mutableListOf<PlayerTrainer>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val pkArr = obj.getJSONArray("pokemon")
                val pokemon = (0 until pkArr.length()).map { j ->
                    val pk = pkArr.getJSONObject(j)
                    val tArr = pk.getJSONArray("types")
                    val types = (0 until tArr.length()).mapNotNull {
                        runCatching { PokemonType.valueOf(tArr.getString(it)) }.getOrNull()
                    }
                    PokemonPreset(
                        name = pk.getString("name"),
                        nameDE = pk.optString("nameDE", ""),
                        pokedexId = pk.optInt("pokedexId", 0),
                        baseBP = pk.optInt("baseBP", 1),
                        types = types,
                        level = pk.optInt("level", 1),
                        xp = pk.optInt("xp", 0),
                        xpLocked = pk.optBoolean("xpLocked", false)
                    )
                }
                val badgeArr = obj.optJSONArray("badges")
                val badges = if (badgeArr != null) {
                    (0 until badgeArr.length()).map { badgeArr.getInt(it) }.toSet()
                } else emptySet()
                val faintedArr = obj.optJSONArray("faintedPokemonIds")
                val faintedIds = if (faintedArr != null) {
                    (0 until faintedArr.length()).map { faintedArr.getInt(it) }.toSet()
                } else emptySet()
                list.add(PlayerTrainer(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    avatarId = obj.getInt("avatarId"),
                    gender = runCatching { TrainerGender.valueOf(obj.getString("gender")) }
                        .getOrDefault(TrainerGender.MALE),
                    pokemon = pokemon,
                    badges = badges,
                    faintedPokemonIds = faintedIds
                ))
            }
        } catch (_: Exception) {}
        return list
    }
}
