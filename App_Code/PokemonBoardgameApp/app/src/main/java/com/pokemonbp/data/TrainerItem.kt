package com.pokemonbp.data

enum class TrainerItem(
    val nameDE: String,
    val nameEN: String,
    val iconUrl: String
) {
    LIFE_ORB(
        "Life Orb", "Life Orb",
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/life-orb.png"
    ),
    BEULENHELM(
        "Beulenhelm", "Assault Vest",
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/assault-vest.png"
    ),
    EXPERTENGURT(
        "Expertengurt", "Expert Belt",
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/expert-belt.png"
    ),
    EXP_SHARE(
        "Exp. Share", "Exp. Share",
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/exp-share.png"
    );
}
