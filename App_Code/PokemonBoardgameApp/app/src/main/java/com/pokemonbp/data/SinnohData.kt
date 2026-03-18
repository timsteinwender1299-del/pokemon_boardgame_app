package com.pokemonbp.data

import com.pokemonbp.model.GymPokemon
import com.pokemonbp.model.EnemyTrainer

object SinnohData {

    val gymLeaders: List<EnemyTrainer.GymLeader> = listOf(

        EnemyTrainer.GymLeader(
            id = "roark",
            nameDE = "Veit",
            nameEN = "Roark",
            badgeTeams = mapOf(
                1 to listOf(
                    GymPokemon("Krikling", "Geodude", 74, listOf(PokemonType.ROCK, PokemonType.GROUND), 2),
                    GymPokemon("Onix", "Onix", 95, listOf(PokemonType.ROCK, PokemonType.GROUND), 3),
                    GymPokemon("Krawumms", "Cranidos", 408, listOf(PokemonType.ROCK), 3)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "gardenia",
            nameDE = "Silvana",
            nameEN = "Gardenia",
            badgeTeams = mapOf(
                2 to listOf(
                    GymPokemon("Knofensa", "Turtwig", 387, listOf(PokemonType.GRASS), 2),
                    GymPokemon("Flegmon", "Cherrim", 421, listOf(PokemonType.GRASS), 3),
                    GymPokemon("Roserade", "Roserade", 407, listOf(PokemonType.GRASS, PokemonType.POISON), 4)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "hilda",
            nameDE = "Maylene",
            nameEN = "Hilda",
            badgeTeams = mapOf(
                3 to listOf(
                    GymPokemon("Meditie", "Meditite", 307, listOf(PokemonType.FIGHTING, PokemonType.PSYCHIC), 3),
                    GymPokemon("Machoke", "Machoke", 67, listOf(PokemonType.FIGHTING), 3),
                    GymPokemon("Lucario", "Lucario", 448, listOf(PokemonType.FIGHTING, PokemonType.STEEL), 4)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "crasherwake",
            nameDE = "Wellenbrecher Marinus",
            nameEN = "Crasher Wake",
            badgeTeams = mapOf(
                4 to listOf(
                    GymPokemon("Golduck", "Golduck", 55, listOf(PokemonType.WATER), 3),
                    GymPokemon("Quagsire", "Quagsire", 195, listOf(PokemonType.WATER, PokemonType.GROUND), 3),
                    GymPokemon("Toxiquak", "Floatzel", 419, listOf(PokemonType.WATER), 4)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "fantina",
            nameDE = "Lamina",
            nameEN = "Fantina",
            badgeTeams = mapOf(
                5 to listOf(
                    GymPokemon("Drifzepeli", "Drifblim", 426, listOf(PokemonType.GHOST, PokemonType.FLYING), 4),
                    GymPokemon("Traunfugil", "Haunter", 93, listOf(PokemonType.GHOST, PokemonType.POISON), 3),
                    GymPokemon("Mismagius", "Mismagius", 429, listOf(PokemonType.GHOST), 5)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "byron",
            nameDE = "Adam",
            nameEN = "Byron",
            badgeTeams = mapOf(
                6 to listOf(
                    GymPokemon("Kiesling", "Geodude", 74, listOf(PokemonType.ROCK, PokemonType.GROUND), 3),
                    GymPokemon("Stahlbeiß", "Magneton", 82, listOf(PokemonType.ELECTRIC, PokemonType.STEEL), 4),
                    GymPokemon("Bastiodon", "Bastiodon", 411, listOf(PokemonType.ROCK, PokemonType.STEEL), 5)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "candice",
            nameDE = "Frida",
            nameEN = "Candice",
            badgeTeams = mapOf(
                7 to listOf(
                    GymPokemon("Snorunt", "Snorunt", 361, listOf(PokemonType.ICE), 3),
                    GymPokemon("Piloswine", "Piloswine", 221, listOf(PokemonType.ICE, PokemonType.GROUND), 4),
                    GymPokemon("Froslass", "Froslass", 478, listOf(PokemonType.ICE, PokemonType.GHOST), 5),
                    GymPokemon("Abomasnow", "Abomasnow", 460, listOf(PokemonType.GRASS, PokemonType.ICE), 5)
                )
            )
        ),

        EnemyTrainer.GymLeader(
            id = "volkner",
            nameDE = "Volkner",
            nameEN = "Volkner",
            badgeTeams = mapOf(
                8 to listOf(
                    GymPokemon("Raichu", "Raichu", 26, listOf(PokemonType.ELECTRIC), 4),
                    GymPokemon("Ambipom", "Ambipom", 424, listOf(PokemonType.NORMAL), 4),
                    GymPokemon("Octillery", "Octillery", 224, listOf(PokemonType.WATER), 4),
                    GymPokemon("Luxray", "Luxray", 405, listOf(PokemonType.ELECTRIC), 6)
                )
            )
        )
    )

    val champions: List<EnemyTrainer.Champion> = listOf(
        EnemyTrainer.Champion("Cynthia"),
        EnemyTrainer.Champion("Hilda"),
        EnemyTrainer.Champion("Tim")
    )
}
