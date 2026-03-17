package com.pokemonbp.data

import android.content.Context

object SpriteUrls {

    private const val BASE_URL = "https://raw.githubusercontent.com/timsteinwender1299-del/pokemon_boardgame_app/main"

    // Mega IDs that exist in the mega_artwork folder
    private val megaSpriteIds = setOf(
        10033, 10034, 10035, 10036, 10037, 10038, 10039, 10040, 10041, 10042,
        10043, 10044, 10045, 10046, 10047, 10048, 10049, 10051, 10052, 10053,
        10054, 10055, 10057, 10058, 10059, 10060, 10061, 10062, 10063, 10064,
        10065, 10066, 10067, 10068, 10071, 10072, 10073, 10074, 10076, 10079,
        10080, 10081, 10082, 10083, 10084, 10090, 10091
    )

    // Map: Mega sprite ID -> filename in pokemon_artwork_mega folder
    private val megaFileNames: Map<Int, String> = mapOf(
        10033 to "Hauptartwork_003m1.png",
        10034 to "Hauptartwork_006m1.png",
        10035 to "Hauptartwork_006m2.png",
        10036 to "Hauptartwork_009m1.png",
        10037 to "Hauptartwork_065m1.png",
        10038 to "Hauptartwork_094m1.png",
        10039 to "Hauptartwork_115m1.png",
        10040 to "Hauptartwork_127m1.png",
        10041 to "Hauptartwork_130m1.png",
        10042 to "Hauptartwork_142m1.png",
        10043 to "Hauptartwork_150m1.png",
        10044 to "Hauptartwork_150m2.png",
        10045 to "Hauptartwork_181m1.png",
        10046 to "Hauptartwork_212m1.png",
        10047 to "Hauptartwork_229m1.png",
        10048 to "Hauptartwork_248m1.png",
        10049 to "Hauptartwork_257m1.png",
        10051 to "Hauptartwork_282m1.png",
        10052 to "Hauptartwork_303m1.png",
        10053 to "Hauptartwork_306m1.png",
        10054 to "Hauptartwork_308m1.png",
        10055 to "Hauptartwork_310m1.png",
        10057 to "Hauptartwork_359m1.png",
        10058 to "Hauptartwork_445m1.png",
        10059 to "Hauptartwork_448m1.png",
        10060 to "Hauptartwork_460m1.png",
        10061 to "Hauptartwork_531m1.png",
        10062 to "Hauptartwork_302m1.png",
        10063 to "Hauptartwork_475m1.png",
        10064 to "Hauptartwork_354m1.png",
        10065 to "Hauptartwork_254m1.png",
        10066 to "Hauptartwork_260m1.png",
        10067 to "Hauptartwork_319m1.png",
        10068 to "Hauptartwork_323m1.png",
        10071 to "Hauptartwork_080m1.png",
        10072 to "Hauptartwork_208m1.png",
        10073 to "Hauptartwork_214m1.png",
        10074 to "Hauptartwork_362m1.png",
        10076 to "Hauptartwork_376m1.png",
        10079 to "Hauptartwork_384m1.png",
        10080 to "Hauptartwork_334m1.png",
        10081 to "Hauptartwork_373m1.png",
        10082 to "Hauptartwork_380m1.png",
        10083 to "Hauptartwork_381m1.png",
        10084 to "Hauptartwork_719m1.png",
        10090 to "Hauptartwork_015m1.png",
        10091 to "Hauptartwork_018m1.png"
    )

    /**
     * Returns the GitHub raw URL for a Pokémon sprite.
     * Regular Pokémon: pokemon_artwork/Hauptartwork_XXX.png
     * Mega Evolutions: pokemon_artwork_mega/Hauptartwork_XXXmY.png
     * Falls back to PokeAPI for anything not in our repo.
     */
    fun urlFor(pokedexId: Int): String {
        // Mega Evolution
        if (pokedexId >= 10000) {
            val fileName = megaFileNames[pokedexId]
            return if (fileName != null) {
                "$BASE_URL/pokemon_artwork_mega/$fileName"
            } else {
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokedexId.png"
            }
        }

        // Regular Pokémon — pad to 3 digits (001, 025, 151, 1000+)
        val padded = "%03d".format(pokedexId)
        return "$BASE_URL/pokemon_artwork/Hauptartwork_$padded.png"
    }

    val duskstoneUrl: String = "$BASE_URL/Duskstone.png"

    /** No local drawables anymore — always use URL */
    fun localResId(context: Context, pokedexId: Int): Int = 0

    fun fallbackUrl(pokedexId: Int): String = urlFor(pokedexId)
}
