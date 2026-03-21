package com.pokemonbp.data

import android.content.Context

data class RoutePokemon(val nameDE: String, val nameEN: String, val bp: Int, val value: String? = null)

data class RouteTier(
    val label: String,     // "" for single-tier; "Badge 1-4"/"Badge 5+" for tiered routes; "Regular"/"Town Event" for GreatMarsh
    val pokemon: List<RoutePokemon>
)

data class RouteLocation(
    val displayName: String,
    val isLegendary: Boolean,
    val tiers: List<RouteTier>
)

object RouteData {

    // Ordered exactly as specified
    private val routeOrder = listOf(
        "See der Wahrheit / Lake Verity",
        "Route 201",
        "Route 206",
        "Route 208",
        "Route 212",
        "Ewigwald / Eterna Forest",
        "Kraterberg / Mt. Coronet",
        "Route 210",
        "Überlebensareal / Survivalarea",
        "Großmoor / GreatMarsh",
        "Route 213",
        "Kühnheitsufer / Valor Lakefront",
        "Route 216",
        "Route 217",
        "See der Kühnheit / Lake Valor",
        "See der Stärke / Lake Acuity",
        "Eiseninsel / Iron Island",
        "Kahlberg / Stark Mountain",
        "Ursprungshöhle / Turnback Cave"
    )

    fun loadRoutes(context: Context): List<RouteLocation> {
        val normalMap    = parseNormalFile(context)
        val legendaryMap = parseLegendaryFile(context)

        return routeOrder.mapNotNull { name ->
            when {
                normalMap.containsKey(name)    -> RouteLocation(name, false, normalMap[name]!!)
                legendaryMap.containsKey(name) -> RouteLocation(name, true,  legendaryMap[name]!!)
                else -> null
            }
        }
    }

    // Parses RoutesNormal.txt — supports optional | Badge X tier lines within each route
    private fun parseNormalFile(context: Context): Map<String, List<RouteTier>> {
        val cached = DataSyncManager.routesNormalFile(context)
        val text = if (cached.exists()) cached.readText()
                   else context.assets.open("RoutesNormal.txt").bufferedReader().readText()

        val result = linkedMapOf<String, MutableList<RouteTier>>()
        var currentKey: String? = null
        var currentTierLabel: String? = null
        var currentPokemon: MutableList<RoutePokemon>? = null

        fun flush() {
            if (currentKey != null && currentPokemon != null && currentPokemon!!.isNotEmpty()) {
                result.getOrPut(currentKey!!) { mutableListOf() }
                    .add(RouteTier(currentTierLabel ?: "", currentPokemon!!.toList()))
            }
        }

        for (line in text.lines()) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("Route:") -> {
                    flush()
                    currentKey = trimmed.removePrefix("Route:").trim()
                    currentTierLabel = null
                    currentPokemon = mutableListOf()
                }
                trimmed.startsWith("|") -> {
                    flush()
                    currentTierLabel = trimmed.removePrefix("|").trim()
                    currentPokemon = mutableListOf()
                }
                trimmed.isNotEmpty() && currentPokemon != null ->
                    parsePokemonLine(trimmed)?.let { currentPokemon!!.add(it) }
            }
        }
        flush()
        return result
    }

    private fun parseLegendaryFile(context: Context): Map<String, List<RouteTier>> {
        val cached = DataSyncManager.routesLegendaryFile(context)
        val text = if (cached.exists()) cached.readText()
                   else context.assets.open("RoutesLegendary.txt").bufferedReader().readText()
        val result = linkedMapOf<String, MutableList<RouteTier>>()
        var currentBase: String? = null
        var currentTierLabel: String? = null
        var currentPokemon: MutableList<RoutePokemon>? = null

        fun flush() {
            if (currentBase != null && currentTierLabel != null && currentPokemon != null) {
                result.getOrPut(currentBase!!) { mutableListOf() }
                    .add(RouteTier(currentTierLabel!!, currentPokemon!!.toList()))
            }
        }

        for (line in text.lines()) {
            when {
                line.startsWith("Location:") -> {
                    flush()
                    val rest = line.removePrefix("Location:").trim()
                    val pipeIdx = rest.indexOf("|")
                    if (pipeIdx >= 0) {
                        currentBase = rest.substring(0, pipeIdx).trim()
                        val tierRaw = rest.substring(pipeIdx + 1).trim()
                        currentTierLabel = if (tierRaw == "TownEvent") "Town Event" else tierRaw
                    } else {
                        currentBase = rest.trim()
                        currentTierLabel = "Regular"
                    }
                    currentPokemon = mutableListOf()
                }
                line.trim().isNotEmpty() && currentPokemon != null ->
                    parsePokemonLine(line.trim())?.let { currentPokemon!!.add(it) }
            }
        }
        flush()
        return result
    }

    private fun parsePokemonLine(line: String): RoutePokemon? {
        // Match (BP: X) anywhere in the line (not necessarily at end)
        val bpMatch = Regex("""\(BP:\s*(\d+|\?)\)""").find(line) ?: return null
        val bp = bpMatch.groupValues[1].toIntOrNull() ?: 0
        val namePart = line.substring(0, bpMatch.range.first).trim()
        val valueMatch = Regex("""\(Value:\s*([^)]+)\)""").find(line)
        val value = valueMatch?.groupValues?.get(1)?.trim()
        val (de, en) = splitName(namePart)
        return RoutePokemon(de.trim(), en.trim(), bp, value)
    }

    private fun splitName(raw: String): Pair<String, String> = when {
        " / " in raw -> raw.split(" / ", limit = 2).let { Pair(it[0], it[1]) }
        "/"   in raw -> raw.split("/",   limit = 2).let { Pair(it[0], it[1]) }
        "_"   in raw -> raw.split("_",   limit = 2).let { Pair(it[0], it[1]) }
        else         -> Pair(raw, raw)
    }
}
