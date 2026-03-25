package com.pokemonbp.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeManager
import android.widget.ImageView
import android.widget.LinearLayout
import com.pokemonbp.data.PokemonType
import com.pokemonbp.databinding.ItemPokemonBinding
import com.pokemonbp.model.Pokemon

class PokemonListAdapter(
    private val pokemonList: MutableList<Pokemon>,
    private val theme: AppTheme,
    private val onDelete: (Int) -> Unit,
    private val onSelected: (Int) -> Unit = {},
    private val onRevive: (Int) -> Unit = {}
) : RecyclerView.Adapter<PokemonListAdapter.PokemonViewHolder>() {

    var activeIndex: Int = 0
        set(value) { field = value; notifyDataSetChanged() }

    var faintedIndices: Set<Int> = emptySet()
        set(value) { field = value; notifyDataSetChanged() }

    var forcedItemHeight: Int = 0  // 0 = wrap_content

    inner class PokemonViewHolder(val binding: ItemPokemonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PokemonViewHolder(ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val lp = holder.itemView.layoutParams
        lp.height = if (forcedItemHeight > 0) forcedItemHeight else ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = lp

        // Scale sprite to fill card height minus padding
        if (forcedItemHeight > 0) {
            val density = holder.itemView.context.resources.displayMetrics.density
            val paddingPx = (8 * density).toInt() * 2
            val spriteSize = (forcedItemHeight - paddingPx).coerceAtLeast(24)
            val slp = holder.binding.ivSprite.layoutParams
            slp.width = spriteSize
            slp.height = spriteSize
            holder.binding.ivSprite.layoutParams = slp
        }

        val pokemon = pokemonList[position]
        val c = ThemeManager.colorsFor(theme)
        val typeColor = Color.parseColor(pokemon.types.first().colorHex)
        val isActive = position == activeIndex
        val isFainted = position in faintedIndices

        if (isActive) {
            holder.binding.cardPokemon.setCardBackgroundColor(c.surface)
            holder.binding.cardPokemon.strokeColor = typeColor
            holder.binding.cardPokemon.strokeWidth = 3
            holder.binding.cardPokemon.alpha = 1.0f
            holder.binding.cardPokemon.cardElevation = 6f
        } else {
            holder.binding.cardPokemon.setCardBackgroundColor(c.surfaceVariant)
            holder.binding.cardPokemon.strokeColor = Color.parseColor("#55888888")
            holder.binding.cardPokemon.strokeWidth = 1
            holder.binding.cardPokemon.alpha = 0.55f
            holder.binding.cardPokemon.cardElevation = 0f
        }

        holder.binding.tvPokemonName.text = pokemon.displayName()
        holder.binding.tvPokemonName.setTextColor(c.textPrimary)
        holder.binding.tvTypes.text = pokemon.types.joinToString(" / ") { it.displayName }
        holder.binding.tvTypes.setTextColor(typeColor)
        loadTypeIcons(holder.binding.llTypes, pokemon.types, holder.itemView)
        if (isFainted) {
            holder.binding.tvBaseBp.visibility = android.view.View.GONE
            holder.binding.ivFainted.visibility = android.view.View.VISIBLE
            holder.binding.btnRevive.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.tvBaseBp.visibility = android.view.View.VISIBLE
            holder.binding.ivFainted.visibility = android.view.View.GONE
            holder.binding.btnRevive.visibility = android.view.View.GONE
            holder.binding.tvBaseBp.text = "BP: ${pokemon.baseBP}"
            holder.binding.tvBaseBp.setTextColor(c.accent)
        }

        if (theme == AppTheme.RETRO) {
            holder.binding.tvPokemonName.typeface = Typeface.MONOSPACE
            holder.binding.tvTypes.typeface = Typeface.MONOSPACE
        }

        // Sprite
        val spriteUrl = pokemon.spriteUrl()
        if (spriteUrl != null) {
            holder.binding.ivSprite.loadPokemonSprite(holder.itemView.context, pokemon.pokedexId)
            holder.binding.ivSprite.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.ivSprite.visibility = android.view.View.GONE
        }

        // Tap card to select as active (fainted cards are not selectable)
        holder.binding.cardPokemon.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt() && pos != activeIndex && pos !in faintedIndices) {
                activeIndex = pos
                onSelected(pos)
            }
        }

        holder.binding.btnDelete.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) onDelete(pos)
        }

        holder.binding.btnRevive.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) onRevive(pos)
        }
    }

    override fun getItemCount() = pokemonList.size

    private fun loadTypeIcons(container: LinearLayout?, types: List<PokemonType>, itemView: android.view.View) {
        container ?: return
        container.removeAllViews()
        val ctx = itemView.context
        for (type in types) {
            val iv = ImageView(ctx)
            val dp28 = (28 * ctx.resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(dp28, dp28)
            params.marginEnd = (3 * ctx.resources.displayMetrics.density).toInt()
            iv.layoutParams = params
            iv.scaleType = ImageView.ScaleType.FIT_CENTER
            iv.adjustViewBounds = true
            Glide.with(ctx).load(SpriteUrls.typeIconUrl(type.name)).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv)
            container.addView(iv)
        }
    }
}
