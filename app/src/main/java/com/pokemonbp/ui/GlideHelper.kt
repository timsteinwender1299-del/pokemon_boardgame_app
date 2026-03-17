package com.pokemonbp.ui

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pokemonbp.data.SpriteUrls

/**
 * Loads a Pokémon sprite from GitHub raw URL.
 * Uses override(TARGET_SIZE) so Glide fetches and caches at a high enough
 * resolution that the image looks sharp even in larger slots.
 * Images are cached permanently after first load.
 */
fun ImageView.loadPokemonSprite(context: Context, pokedexId: Int) {
    if (pokedexId <= 0) return
    Glide.with(context)
        .load(SpriteUrls.urlFor(pokedexId))
        .apply(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(400, 400)   // decode at 400px — sharp at any slot size
                .fitCenter()
        )
        .into(this)
}
