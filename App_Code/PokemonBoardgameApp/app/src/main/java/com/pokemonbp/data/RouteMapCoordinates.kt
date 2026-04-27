package com.pokemonbp.data

data class RouteHotspot(
    val routeName: String,
    val x: Float,   // 0.0 = left edge, 1.0 = right edge of MapFull.png
    val y: Float    // 0.0 = top edge,  1.0 = bottom edge of MapFull.png
)

object RouteMapCoordinates {

    // Coordinates taken directly from MapFull_Layout.png (user-marked positions).
    val hotspots = listOf(
        // ── Normal routes ───────────────────────────────────────────────────────
        RouteHotspot("Route 201",                             0.167f, 0.683f),
        RouteHotspot("Route 206",                             0.270f, 0.474f),
        RouteHotspot("Route 208",                             0.387f, 0.614f),
        RouteHotspot("Route 210",                             0.595f, 0.355f),
        RouteHotspot("Route 212",                             0.540f, 0.762f),
        RouteHotspot("Route 213",                             0.761f, 0.747f),
        RouteHotspot("Route 216",                             0.392f, 0.222f),
        RouteHotspot("Route 217",                             0.292f, 0.216f),

        // ── Legendary / special locations ───────────────────────────────────────
        RouteHotspot("See der Wahrheit / Lake Verity",        0.053f, 0.728f),
        RouteHotspot("Ewigwald / Eterna Forest",              0.273f, 0.345f),
        RouteHotspot("Kraterberg / Mt. Coronet",              0.386f, 0.477f),
        RouteHotspot("Überlebensareal / Survivalarea",        0.670f, 0.074f),
        RouteHotspot("Großmoor / GreatMarsh",                 0.613f, 0.652f),
        RouteHotspot("Kühnheitsufer / Valor Lakefront",       0.755f, 0.546f),
        RouteHotspot("See der Kühnheit / Lake Valor",         0.644f, 0.522f),
        RouteHotspot("See der Stärke / Lake Acuity",          0.289f, 0.099f),
        RouteHotspot("Eiseninsel / Iron Island",              0.103f, 0.292f),
        RouteHotspot("Kahlberg / Stark Mountain",             0.810f, 0.078f),
        RouteHotspot("Ursprungshöhle / Turnback Cave",        0.864f, 0.425f)
    )

    fun shortLabel(routeName: String): String = when {
        routeName.startsWith("Route ") -> routeName.removePrefix("Route ")
        " / " in routeName             -> routeName.substringAfter(" / ").split(" ").last().take(7)
        else                           -> routeName.split(" ").last().take(7)
    }
}
