package com.pokemonbp.data

import com.pokemonbp.model.GymPokemon
import com.pokemonbp.model.EnemyTrainer

object SinnohData {

    val gymLeaders: List<EnemyTrainer.GymLeader> = listOf(

        // ── ROARK / VEIT ──────────────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "roark",
            nameDE = "Veit",
            nameEN = "Roark",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Koknodon", "Cranidos", 408, listOf(PokemonType.ROCK), 3)
                ),
                2 to listOf(
                    GymPokemon("Aerodactyl", "Aerodactyl", 142, listOf(PokemonType.ROCK, PokemonType.FLYING), 4)
                ),
                3 to listOf(
                    GymPokemon("Rameidos", "Rampardos", 409, listOf(PokemonType.ROCK), 5)
                ),
                4 to listOf(
                    GymPokemon("Aerodactyl", "Aerodactyl", 142, listOf(PokemonType.ROCK, PokemonType.FLYING), 4),
                    GymPokemon("Rameidos", "Rampardos", 409, listOf(PokemonType.ROCK), 5)
                ),
                5 to listOf(
                    GymPokemon("Aerodactyl", "Aerodactyl", 142, listOf(PokemonType.ROCK, PokemonType.FLYING), 5),
                    GymPokemon("Rameidos", "Rampardos", 409, listOf(PokemonType.ROCK), 6)
                ),
                6 to listOf(
                    GymPokemon("Aerodactyl", "Aerodactyl", 142, listOf(PokemonType.ROCK, PokemonType.FLYING), 6),
                    GymPokemon("Rameidos", "Rampardos", 409, listOf(PokemonType.ROCK), 7)
                ),
                7 to listOf(
                    GymPokemon("Aerodactyl", "Aerodactyl", 142, listOf(PokemonType.ROCK, PokemonType.FLYING), 7),
                    GymPokemon("Despotar", "Tyranitar", 248, listOf(PokemonType.ROCK, PokemonType.DARK), 7),
                    GymPokemon("Rameidos", "Rampardos", 409, listOf(PokemonType.ROCK), 8)
                ),
                8 to listOf(
                    GymPokemon("Aerodactyl", "Aerodactyl", 142, listOf(PokemonType.ROCK, PokemonType.FLYING), 8),
                    GymPokemon("Despotar", "Tyranitar", 248, listOf(PokemonType.ROCK, PokemonType.DARK), 8),
                    GymPokemon("Rameidos", "Rampardos", 409, listOf(PokemonType.ROCK), 9)
                )
            )
        ),

        // ── GARDENIA / SILVANA ───────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "gardenia",
            nameDE = "Silvana",
            nameEN = "Gardenia",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Kikugi", "Cherubi", 420, listOf(PokemonType.GRASS), 3)
                ),
                2 to listOf(
                    GymPokemon("Kikugi", "Cherubi", 420, listOf(PokemonType.GRASS), 4)
                ),
                3 to listOf(
                    GymPokemon("Roselia", "Roselia", 315, listOf(PokemonType.GRASS, PokemonType.POISON), 5)
                ),
                4 to listOf(
                    GymPokemon("Folipurba", "Leafeon", 470, listOf(PokemonType.GRASS), 4),
                    GymPokemon("Roselia", "Roselia", 315, listOf(PokemonType.GRASS, PokemonType.POISON), 5)
                ),
                5 to listOf(
                    GymPokemon("Folipurba", "Leafeon", 470, listOf(PokemonType.GRASS), 5),
                    GymPokemon("Roserade", "Roserade", 407, listOf(PokemonType.GRASS, PokemonType.POISON), 6)
                ),
                6 to listOf(
                    GymPokemon("Folipurba", "Leafeon", 470, listOf(PokemonType.GRASS), 6),
                    GymPokemon("Roserade", "Roserade", 407, listOf(PokemonType.GRASS, PokemonType.POISON), 7)
                ),
                7 to listOf(
                    GymPokemon("Folipurba", "Leafeon", 470, listOf(PokemonType.GRASS), 7),
                    GymPokemon("Chelterrar", "Torterra", 389, listOf(PokemonType.GRASS, PokemonType.GROUND), 7),
                    GymPokemon("Roserade", "Roserade", 407, listOf(PokemonType.GRASS, PokemonType.POISON), 8)
                ),
                8 to listOf(
                    GymPokemon("Folipurba", "Leafeon", 470, listOf(PokemonType.GRASS), 8),
                    GymPokemon("Chelterrar", "Torterra", 389, listOf(PokemonType.GRASS, PokemonType.GROUND), 8),
                    GymPokemon("Roserade", "Roserade", 407, listOf(PokemonType.GRASS, PokemonType.POISON), 9)
                )
            )
        ),

        // ── MAYLENE / HILDA ──────────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "hilda",
            nameDE = "Maylene",
            nameEN = "Hilda",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Glibunkel", "Croagunk", 453, listOf(PokemonType.POISON, PokemonType.FIGHTING), 3)
                ),
                2 to listOf(
                    GymPokemon("Glibunkel", "Croagunk", 453, listOf(PokemonType.POISON, PokemonType.FIGHTING), 4)
                ),
                3 to listOf(
                    GymPokemon("Toxiquak", "Toxicroak", 454, listOf(PokemonType.POISON, PokemonType.FIGHTING), 5)
                ),
                4 to listOf(
                    GymPokemon("Panpyro", "Monferno", 391, listOf(PokemonType.FIRE, PokemonType.FIGHTING), 4),
                    GymPokemon("Toxiquak", "Toxicroak", 454, listOf(PokemonType.POISON, PokemonType.FIGHTING), 5)
                ),
                5 to listOf(
                    GymPokemon("Panpyro", "Monferno", 391, listOf(PokemonType.FIRE, PokemonType.FIGHTING), 5),
                    GymPokemon("Toxiquak", "Toxicroak", 454, listOf(PokemonType.POISON, PokemonType.FIGHTING), 6)
                ),
                6 to listOf(
                    GymPokemon("Panferno", "Infernape", 392, listOf(PokemonType.FIRE, PokemonType.FIGHTING), 6),
                    GymPokemon("Toxiquak", "Toxicroak", 454, listOf(PokemonType.POISON, PokemonType.FIGHTING), 7)
                ),
                7 to listOf(
                    GymPokemon("Toxiquak", "Toxicroak", 454, listOf(PokemonType.POISON, PokemonType.FIGHTING), 7),
                    GymPokemon("Panferno", "Infernape", 392, listOf(PokemonType.FIRE, PokemonType.FIGHTING), 7),
                    GymPokemon("Galagladi", "Gallade", 475, listOf(PokemonType.PSYCHIC, PokemonType.FIGHTING), 8)
                ),
                8 to listOf(
                    GymPokemon("Toxiquak", "Toxicroak", 454, listOf(PokemonType.POISON, PokemonType.FIGHTING), 8),
                    GymPokemon("Panferno", "Infernape", 392, listOf(PokemonType.FIRE, PokemonType.FIGHTING), 8),
                    GymPokemon("Galagladi", "Gallade", 475, listOf(PokemonType.PSYCHIC, PokemonType.FIGHTING), 9)
                )
            )
        ),

        // ── CRASHER WAKE / WELLENBRECHER MARINUS ─────────────────────────────
        EnemyTrainer.GymLeader(
            id = "crasherwake",
            nameDE = "Wellenbrecher Marinus",
            nameEN = "Crasher Wake",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Bamelin", "Buizel", 418, listOf(PokemonType.WATER), 3)
                ),
                2 to listOf(
                    GymPokemon("Bamelin", "Buizel", 418, listOf(PokemonType.WATER), 4)
                ),
                3 to listOf(
                    GymPokemon("Garados", "Gyarados", 130, listOf(PokemonType.WATER, PokemonType.FLYING), 5)
                ),
                4 to listOf(
                    GymPokemon("Bamelin", "Buizel", 418, listOf(PokemonType.WATER), 4),
                    GymPokemon("Garados", "Gyarados", 130, listOf(PokemonType.WATER, PokemonType.FLYING), 5)
                ),
                5 to listOf(
                    GymPokemon("Garados", "Gyarados", 130, listOf(PokemonType.WATER, PokemonType.FLYING), 5),
                    GymPokemon("Bojelin", "Floatzel", 419, listOf(PokemonType.WATER), 6)
                ),
                6 to listOf(
                    GymPokemon("Garados", "Gyarados", 130, listOf(PokemonType.WATER, PokemonType.FLYING), 6),
                    GymPokemon("Bojelin", "Floatzel", 419, listOf(PokemonType.WATER), 7)
                ),
                7 to listOf(
                    GymPokemon("Morlord", "Quagsire", 195, listOf(PokemonType.WATER, PokemonType.GROUND), 7),
                    GymPokemon("Garados", "Gyarados", 130, listOf(PokemonType.WATER, PokemonType.FLYING), 7),
                    GymPokemon("Bojelin", "Floatzel", 419, listOf(PokemonType.WATER), 8)
                ),
                8 to listOf(
                    GymPokemon("Morlord", "Quagsire", 195, listOf(PokemonType.WATER, PokemonType.GROUND), 8),
                    GymPokemon("Garados", "Gyarados", 130, listOf(PokemonType.WATER, PokemonType.FLYING), 8),
                    GymPokemon("Bojelin", "Floatzel", 419, listOf(PokemonType.WATER), 9)
                )
            )
        ),

        // ── FANTINA / LAMINA ─────────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "fantina",
            nameDE = "Lamina",
            nameEN = "Fantina",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Nebulak", "Gastly", 92, listOf(PokemonType.GHOST, PokemonType.POISON), 3)
                ),
                2 to listOf(
                    GymPokemon("Nebulak", "Gastly", 92, listOf(PokemonType.GHOST, PokemonType.POISON), 4)
                ),
                3 to listOf(
                    GymPokemon("Alpollo", "Haunter", 93, listOf(PokemonType.GHOST, PokemonType.POISON), 5)
                ),
                4 to listOf(
                    GymPokemon("Dritflon", "Drifloon", 425, listOf(PokemonType.GHOST, PokemonType.FLYING), 4),
                    GymPokemon("Alpollo", "Haunter", 93, listOf(PokemonType.GHOST, PokemonType.POISON), 5)
                ),
                5 to listOf(
                    GymPokemon("Dritflon", "Drifloon", 425, listOf(PokemonType.GHOST, PokemonType.FLYING), 5),
                    GymPokemon("Gengar", "Gengar", 94, listOf(PokemonType.GHOST, PokemonType.POISON), 6)
                ),
                6 to listOf(
                    GymPokemon("Drifzepeli", "Drifblim", 426, listOf(PokemonType.GHOST, PokemonType.FLYING), 6),
                    GymPokemon("Gengar", "Gengar", 94, listOf(PokemonType.GHOST, PokemonType.POISON), 7)
                ),
                7 to listOf(
                    GymPokemon("Drifzepeli", "Drifblim", 426, listOf(PokemonType.GHOST, PokemonType.FLYING), 7),
                    GymPokemon("Frosdedje", "Froslass", 478, listOf(PokemonType.ICE, PokemonType.GHOST), 7),
                    GymPokemon("Gengar", "Gengar", 94, listOf(PokemonType.GHOST, PokemonType.POISON), 8)
                ),
                8 to listOf(
                    GymPokemon("Drifzepeli", "Drifblim", 426, listOf(PokemonType.GHOST, PokemonType.FLYING), 8),
                    GymPokemon("Frosdedje", "Froslass", 478, listOf(PokemonType.ICE, PokemonType.GHOST), 8),
                    GymPokemon("Gengar", "Gengar", 94, listOf(PokemonType.GHOST, PokemonType.POISON), 9)
                )
            )
        ),

        // ── BYRON / ADAM ─────────────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "byron",
            nameDE = "Adam",
            nameEN = "Byron",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Schilterus", "Shieldon", 410, listOf(PokemonType.ROCK, PokemonType.STEEL), 3)
                ),
                2 to listOf(
                    GymPokemon("Schilterus", "Shieldon", 410, listOf(PokemonType.ROCK, PokemonType.STEEL), 4)
                ),
                3 to listOf(
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 5)
                ),
                4 to listOf(
                    GymPokemon("Panzaeron", "Panzaeron", 437, listOf(PokemonType.STEEL, PokemonType.PSYCHIC), 4),
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 5)
                ),
                5 to listOf(
                    GymPokemon("Panzaeron", "Panzaeron", 437, listOf(PokemonType.STEEL, PokemonType.PSYCHIC), 5),
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 6)
                ),
                6 to listOf(
                    GymPokemon("Panzaeron", "Panzaeron", 437, listOf(PokemonType.STEEL, PokemonType.PSYCHIC), 6),
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 7)
                ),
                7 to listOf(
                    GymPokemon("Panzaeron", "Panzaeron", 437, listOf(PokemonType.STEEL, PokemonType.PSYCHIC), 7),
                    GymPokemon("Impoleon", "Empoleon", 395, listOf(PokemonType.WATER, PokemonType.STEEL), 7),
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 8)
                ),
                8 to listOf(
                    GymPokemon("Panzaeron", "Panzaeron", 437, listOf(PokemonType.STEEL, PokemonType.PSYCHIC), 8),
                    GymPokemon("Impoleon", "Empoleon", 395, listOf(PokemonType.WATER, PokemonType.STEEL), 8),
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 9)
                )
            )
        ),

        // ── CANDICE / FRIDA ──────────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "candice",
            nameDE = "Frida",
            nameEN = "Candice",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Sniebel", "Sneasel", 215, listOf(PokemonType.ICE, PokemonType.DARK), 3)
                ),
                2 to listOf(
                    GymPokemon("Sniebel", "Sneasel", 215, listOf(PokemonType.ICE, PokemonType.DARK), 4)
                ),
                3 to listOf(
                    GymPokemon("Glaziola", "Glaceon", 471, listOf(PokemonType.ICE), 5)
                ),
                4 to listOf(
                    GymPokemon("Sniebel", "Sneasel", 215, listOf(PokemonType.ICE, PokemonType.DARK), 4),
                    GymPokemon("Glaziola", "Glaceon", 471, listOf(PokemonType.ICE), 5)
                ),
                5 to listOf(
                    GymPokemon("Glaziola", "Glaceon", 471, listOf(PokemonType.ICE), 5),
                    GymPokemon("Snibunna", "Weavile", 461, listOf(PokemonType.ICE, PokemonType.DARK), 6)
                ),
                6 to listOf(
                    GymPokemon("Glaziola", "Glaceon", 471, listOf(PokemonType.ICE), 6),
                    GymPokemon("Snibunna", "Weavile", 461, listOf(PokemonType.ICE, PokemonType.DARK), 7)
                ),
                7 to listOf(
                    GymPokemon("Glaziola", "Glaceon", 471, listOf(PokemonType.ICE), 7),
                    GymPokemon("Mamutel", "Mamoswine", 473, listOf(PokemonType.ICE, PokemonType.GROUND), 7),
                    GymPokemon("Snibunna", "Weavile", 461, listOf(PokemonType.ICE, PokemonType.DARK), 8)
                ),
                8 to listOf(
                    GymPokemon("Glaziola", "Glaceon", 471, listOf(PokemonType.ICE), 8),
                    GymPokemon("Mamutel", "Mamoswine", 473, listOf(PokemonType.ICE, PokemonType.GROUND), 8),
                    GymPokemon("Snibunna", "Weavile", 461, listOf(PokemonType.ICE, PokemonType.DARK), 9)
                )
            )
        ),

        // ── VOLKNER ──────────────────────────────────────────────────────────
        EnemyTrainer.GymLeader(
            id = "volkner",
            nameDE = "Volkner",
            nameEN = "Volkner",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Sheinux", "Shinx", 403, listOf(PokemonType.ELECTRIC), 3)
                ),
                2 to listOf(
                    GymPokemon("Sheinux", "Shinx", 403, listOf(PokemonType.ELECTRIC), 4)
                ),
                3 to listOf(
                    GymPokemon("Luxio", "Luxio", 404, listOf(PokemonType.ELECTRIC), 5)
                ),
                4 to listOf(
                    GymPokemon("Lampi", "Chinchou", 170, listOf(PokemonType.WATER, PokemonType.ELECTRIC), 4),
                    GymPokemon("Luxio", "Luxio", 404, listOf(PokemonType.ELECTRIC), 5)
                ),
                5 to listOf(
                    GymPokemon("Lanturn", "Lanturn", 171, listOf(PokemonType.WATER, PokemonType.ELECTRIC), 5),
                    GymPokemon("Luxtra", "Luxray", 405, listOf(PokemonType.ELECTRIC), 6)
                ),
                6 to listOf(
                    GymPokemon("Lanturn", "Lanturn", 171, listOf(PokemonType.WATER, PokemonType.ELECTRIC), 6),
                    GymPokemon("Luxtra", "Luxray", 405, listOf(PokemonType.ELECTRIC), 7)
                ),
                7 to listOf(
                    GymPokemon("Lanturn", "Lanturn", 171, listOf(PokemonType.WATER, PokemonType.ELECTRIC), 7),
                    GymPokemon("Elevoltek", "Electivire", 466, listOf(PokemonType.ELECTRIC), 7),
                    GymPokemon("Luxtra", "Luxray", 405, listOf(PokemonType.ELECTRIC), 8)
                ),
                8 to listOf(
                    GymPokemon("Lanturn", "Lanturn", 171, listOf(PokemonType.WATER, PokemonType.ELECTRIC), 8),
                    GymPokemon("Elevoltek", "Electivire", 466, listOf(PokemonType.ELECTRIC), 8),
                    GymPokemon("Luxtra", "Luxray", 405, listOf(PokemonType.ELECTRIC), 9)
                )
            )
        )
    )

    val champions: List<EnemyTrainer.Champion> = listOf(
        EnemyTrainer.Champion("Cynthia", listOf(
            GymPokemon("Kryppuk", "Spiritomb", 442, listOf(PokemonType.GHOST, PokemonType.DARK), 9),
            GymPokemon("Milotic", "Milotic", 350, listOf(PokemonType.WATER), 10),
            GymPokemon("Lucario", "Lucario", 448, listOf(PokemonType.FIGHTING, PokemonType.STEEL), 10),
            GymPokemon("Knackrack", "Garchomp", 445, listOf(PokemonType.DRAGON, PokemonType.GROUND), 11)
        )),
        EnemyTrainer.Champion("Hilda", listOf()),
        EnemyTrainer.Champion("Tim", listOf(
            GymPokemon("Bisasam", "Bulbasaur", 1, listOf(PokemonType.GRASS, PokemonType.POISON), 9),
            GymPokemon("Aggrostella", "Toxapex", 748, listOf(PokemonType.POISON, PokemonType.WATER), 10),
            GymPokemon("Colossand", "Palossand", 770, listOf(PokemonType.GHOST, PokemonType.GROUND), 10),
            GymPokemon("Aranestro", "Araquanid", 752, listOf(PokemonType.WATER, PokemonType.BUG), 11)
        ))
    )
}
