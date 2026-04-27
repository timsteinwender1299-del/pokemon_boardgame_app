package com.pokemonbp.data

enum class PokemonItem(
    val nameEN: String,
    val nameDE: String,
    val drawableResName: String,
    val requiredType: PokemonType?,
    val bpBonus: Int,
    val battleOnly: Boolean = false
) {
    SILK_SCARF("Silk Scarf",     "Seidentuch",      "item_silk_scarf",     PokemonType.NORMAL,   1),
    BLACK_BELT("Black Belt",     "Schwarzgurt",     "item_black_belt",     PokemonType.FIGHTING, 1),
    SHARP_BEAK("Sharp Beak",     "Spitzschnabel",   "item_sharp_beak",     PokemonType.FLYING,   1),
    TWISTED_SPOON("Twisted Spoon","Verbog. Löffel", "item_twisted_spoon",  PokemonType.PSYCHIC,  1),
    SILVER_POWDER("Silver Powder","Silberpuder",    "item_silver_powder",  PokemonType.BUG,      1),
    HARD_STONE("Hard Stone",     "Hartstein",       "item_hard_stone",     PokemonType.ROCK,     1),
    SPELL_TAG("Spell Tag",       "Geistband",       "item_spell_tag",      PokemonType.GHOST,    1),
    MAGNET("Magnet",             "Magnet",          "item_magnet",         PokemonType.ELECTRIC, 1),
    CHARCOAL("Charcoal",         "Holzkohle",       "item_charcoal",       PokemonType.FIRE,     1),
    MYSTIC_WATER("Mystic Water", "Magiewasser",     "item_mystic_water",   PokemonType.WATER,    1),
    MIRACLE_SEED("Miracle Seed", "Wundersame",      "item_miracle_seed",   PokemonType.GRASS,    1),
    NEVER_MELT_ICE("Never-Melt Ice","Immereis",     "item_never_melt_ice", PokemonType.ICE,      1),
    POISON_BARB("Poison Barb",   "Giftstachel",     "item_poison_barb",    PokemonType.POISON,   1),
    SOFT_SAND("Soft Sand",       "Weicher Sand",    "item_soft_sand",      PokemonType.GROUND,   1),
    DRAGON_FANG("Dragon Fang",   "Drachenzahn",     "item_dragon_fang",    PokemonType.DRAGON,   1),
    BLACK_GLASSES("Black Glasses","Schwarzbrille",  "item_black_glasses",  PokemonType.DARK,     1),
    METAL_COAT("Metal Coat",     "Metallmantel",    "item_metal_coat",     PokemonType.STEEL,    1),
    FAIRY_FEATHER("Fairy Feather","Feenfeder",      "item_fairy_feather",  PokemonType.FAIRY,    1),
    PROTEIN("Protein",           "Protein",         "item_protein",        null,                 2, battleOnly = true);

    fun effectiveBpBonus(types: List<PokemonType>): Int =
        if (requiredType == null || types.contains(requiredType)) bpBonus else 0

    fun iconResId(context: android.content.Context): Int =
        context.resources.getIdentifier(drawableResName, "drawable", context.packageName)
}
