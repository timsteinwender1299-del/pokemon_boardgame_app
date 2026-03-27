package com.pokemonbp.data

import android.content.Context

object SpriteUrls {

    private const val BASE_URL = "https://raw.githubusercontent.com/timsteinwender1299-del/pokemon_boardgame_app/main/Images"

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

    private val maleAvatarNames = mapOf(
        1 to "Barry", 2 to "Blue", 3 to "Brendan", 4 to "Calem",
        5 to "Ethan", 6 to "Hau", 7 to "Hilbert", 8 to "Hugh",
        9 to "Lucas", 10 to "Nate", 11 to "Red", 12 to "Silver"
    )
    private val femaleAvatarNames = mapOf(
        101 to "Bianca", 102 to "Dawn", 103 to "Hilda", 104 to "Leaf",
        105 to "Lyra", 106 to "Marnie", 107 to "May", 108 to "Nemona",
        109 to "Penny", 110 to "Rosa", 111 to "Serena", 112 to "Shauna"
    )

    fun avatarUrl(avatarId: Int): String? = when {
        avatarId in 1..12 -> {
            val name = maleAvatarNames[avatarId] ?: return null
            "$BASE_URL/Trainer/PlayerTrainer/Male/MaleTrainer%20Icon/${name}_Icon.png"
        }
        avatarId in 101..112 -> {
            val name = femaleAvatarNames[avatarId] ?: return null
            "$BASE_URL/Trainer/PlayerTrainer/Female/FemaleTrainerIcon/${name}_Icon.png"
        }
        else -> null
    }

    fun trainerIconUrl(id: String): String? = when (id) {
        "roark"       -> "$BASE_URL/Trainer/Trainer_Icon/Roark_Icon.png"
        "gardenia"    -> "$BASE_URL/Trainer/Trainer_Icon/Silvana_Icon.png"
        "crasherwake" -> "$BASE_URL/Trainer/Trainer_Icon/Wake_Icon.png"
        "fantina"     -> "$BASE_URL/Trainer/Trainer_Icon/Fantina_Icon.png"
        "byron"       -> "$BASE_URL/Trainer/Trainer_Icon/Byron_Icon.png"
        "candice"     -> "$BASE_URL/Trainer/Trainer_Icon/Candice_Icon.png"
        "volkner"     -> "$BASE_URL/Trainer/Trainer_Icon/Volkner_Image.png"
        "cynthia"      -> "$BASE_URL/Trainer/Trainer_Icon/Cynthia_Icon.png"
        "hilda"        -> "$BASE_URL/Trainer/Trainer_Icon/Hilda_Icon.png"
        "tim"          -> "$BASE_URL/Trainer/Trainer_Icon/Tim_Icon.png"
        "championmenu" -> "$BASE_URL/Trainer/Trainer_Icon/ChampionIcon.png"
        "random"       -> "$BASE_URL/Trainer/Trainer_Icon/RandomTrainer.png?v=2"
        "wild"         -> "$BASE_URL/Icons/WildPokemon.png"
        "saved"        -> "$BASE_URL/Trainer/Trainer_Icon/Choose%20Trainer.png"
        "galactic"          -> galacticLogoUrl
        "galacticgrunt"     -> galacticGruntIconUrl
        "galacticcommander" -> galacticCommanderIconUrl
        else               -> null
    }

    fun championImageUrl(nameEN: String): String? = when (nameEN) {
        "Cynthia" -> "$BASE_URL/Trainer/TrainerImage/ChampCynthia.png"
        "Tim"     -> "$BASE_URL/Trainer/TrainerImage/ChampTim.png"
        else      -> null
    }

    fun gymLeaderImageUrl(id: String): String? = when (id) {
        "roark"        -> "$BASE_URL/Trainer/TrainerImage/GymLeaderRoark.png"
        "gardenia"     -> "$BASE_URL/Trainer/TrainerImage/GymLeaderSilvana.png"
        "maylene"      -> "$BASE_URL/Trainer/TrainerImage/GymLeaderMaylene.png"
        "crasherwake"  -> "$BASE_URL/Trainer/TrainerImage/GymLeaderWake.png"
        "fantina"      -> "$BASE_URL/Trainer/TrainerImage/GymLeaderFantina.png"
        "byron"        -> "$BASE_URL/Trainer/TrainerImage/GymLeaderByron.png"
        "candice"      -> "$BASE_URL/Trainer/TrainerImage/GymLeaderCandice.png"
        "volkner"      -> "$BASE_URL/Trainer/TrainerImage/GymLeaderVolkner.png"
        else           -> null
    }

    fun playerTrainerImageUrl(avatarId: Int): String? = when {
        avatarId in 1..12 -> {
            val name = maleAvatarNames[avatarId] ?: return null
            "$BASE_URL/Trainer/PlayerTrainer/Male/MaleTrainerImage/$name.png"
        }
        avatarId in 101..112 -> {
            val name = femaleAvatarNames[avatarId] ?: return null
            "$BASE_URL/Trainer/PlayerTrainer/Female/FemaleTrainerImage/$name.png"
        }
        else -> null
    }

    private val randomTrainerFileNames = listOf(
        "ORAS_Ace_Trainer_F.png", "ORAS_Ace_Trainer_M.png", "ORAS_Aroma_Lady.png",
        "ORAS_Bird_Keeper.png", "ORAS_Bug_Catcher.png", "ORAS_Dragon_Tamer.png",
        "ORAS_Lady.png", "ORAS_Lass.png", "ORAS_Pok%C3%A9mon_Breeder_M.png",
        "ORAS_Pok%C3%A9mon_Ranger_F.png", "ORAS_Pok%C3%A9mon_Ranger_M.png",
        "ORAS_Rich_Boy.png", "ORAS_Street_Thug.png",
        "XY_Ace_Trainer_F.png", "XY_Ace_Trainer_M.png", "XY_Backpacker.png",
        "XY_Lass.png", "XY_Pok%C3%A9mon_Ranger_M.png", "XY_Psychic.png",
        "XY_Roller_Skater_M.png"
    )

    fun randomTrainerImageUrl(): String =
        "$BASE_URL/Trainer/TrainerRandom/${randomTrainerFileNames.random()}"

    fun typeIconUrl(typeName: String): String =
        "$BASE_URL/TypeIcons/${typeName.lowercase()}.png"

    val megaBraceletUrl: String    = "$BASE_URL/Icons/MegaBraceletIcon.png?v=2"
    val faintedUrl: String         = "$BASE_URL/Icons/FaintedIcon.png"
    val reviveUrl: String          = "$BASE_URL/Icons/ReviveIcon.png"
    val reloadUrl: String          = "$BASE_URL/Icons/ReloadIcon.png"
    val removeUrl: String          = "$BASE_URL/Icons/RemoveIcon.png"
    val battleUrl: String          = "$BASE_URL/Icons/Battle_Icon.png"
    val battleCalculatorUrl: String= "$BASE_URL/Icons/BattleCalculator_MenuIcon.png"
    val playerUrl: String          = "$BASE_URL/Icons/Player.png"
    val wildPokemonMenuUrl: String = "$BASE_URL/Icons/WildPokemon_MenuIcon.png"
    val pokeballUrl: String        = "$BASE_URL/Icons/Pokeball.png"
    val badgeCaseEmptyUrl: String  = "$BASE_URL/Badges/BadgeCaseEmpty.png"

    private val badgeFileNames = mapOf(
        1 to "01_RoarkBadge", 2 to "02_GardeniaBadge", 3 to "03_FantinaBadge",
        4 to "04_HildaBadge", 5 to "05_CrasherWakeBadge", 6 to "06_ByronBadge",
        7 to "07_CandiceBadge", 8 to "08_VolknerBadge"
    )
    fun badgeUrl(num: Int): String = "$BASE_URL/Badges/${badgeFileNames[num]}.png"

    val galacticGruntMaleUrl: String   = "$BASE_URL/Trainer/TrainerImage/GalacticGruntMale.png"
    val galacticGruntFemaleUrl: String = "$BASE_URL/Trainer/TrainerImage/GalacticGruntFemale.png"
    val galacticLogoUrl: String           = "$BASE_URL/Trainer/Trainer_Icon/GalacticLogo.png"
    val galacticGruntIconUrl: String      = "$BASE_URL/Trainer/Trainer_Icon/GalacticGrunt_Icon.png"
    val galacticCommanderIconUrl: String  = "$BASE_URL/Trainer/Trainer_Icon/GalacticCommander_Icon.png"
    val galacticMarsUrl: String        = "$BASE_URL/Trainer/TrainerImage/GalacticMars.png"
    val galacticJupiterUrl: String     = "$BASE_URL/Trainer/TrainerImage/GalacticJupiter.png"
    val galacticSaturnUrl: String      = "$BASE_URL/Trainer/TrainerImage/GalacticSaturn.png"
    val galacticCyrusUrl: String       = "$BASE_URL/Trainer/TrainerImage/GalacticCyrus.png"
    val resetUrl: String               = "$BASE_URL/Icons/RestetIcon.png"

    val noTypeUrl: String     = "$BASE_URL/TypeIcons/NoType.png"
    val dawnstoneUrl: String  = "$BASE_URL/Icons/Dawnstone.png"
    val duskstoneUrl: String  = "$BASE_URL/Icons/Duskstone.png"
    val garbageBinUrl: String = "$BASE_URL/Icons/Garbage%20Bin.png"

    /** No local drawables anymore — always use URL */
    fun localResId(context: Context, pokedexId: Int): Int = 0

    fun fallbackUrl(pokedexId: Int): String = urlFor(pokedexId)
}
