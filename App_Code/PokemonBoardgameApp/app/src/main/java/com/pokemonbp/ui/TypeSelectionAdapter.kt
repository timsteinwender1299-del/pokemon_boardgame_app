package com.pokemonbp.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.PokemonType
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.databinding.ItemTypeChipBinding

class TypeSelectionAdapter(
    private val types: List<PokemonType>,
    private val selectedTypes: MutableSet<PokemonType>,
    private val theme: AppTheme,
    private val onTypeToggled: (PokemonType, Boolean) -> Unit
) : RecyclerView.Adapter<TypeSelectionAdapter.TypeViewHolder>() {

    inner class TypeViewHolder(val binding: ItemTypeChipBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TypeViewHolder(ItemTypeChipBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: TypeViewHolder, position: Int) {
        val type = types[position]
        val ctx = holder.itemView.context
        val isSelected = selectedTypes.contains(type)
        val typeColor = Color.parseColor(type.colorHex)

        // Load icon from GitHub
        holder.binding.ivTypeIcon.visibility = android.view.View.VISIBLE
        Glide.with(ctx)
            .load(SpriteUrls.typeIconUrl(type.name))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.ivTypeIcon)

        if (theme == AppTheme.RETRO) holder.binding.tvTypeName.typeface = Typeface.MONOSPACE

        if (isSelected) {
            // Selected: full type color background, white text, full icon opacity
            holder.binding.cardType.setCardBackgroundColor(typeColor)
            holder.binding.tvTypeName.setTextColor(Color.WHITE)
            holder.binding.cardType.strokeWidth = 0
            holder.binding.cardType.cardElevation = 6f
            holder.binding.ivTypeIcon.alpha = 1.0f
        } else {
            // Unselected: soft pastel tint of the type color, type-colored text, full icon opacity
            val bgColor = blendWithWhite(typeColor, 0.25f)  // 25% type color, 75% white
            holder.binding.cardType.setCardBackgroundColor(bgColor)
            holder.binding.tvTypeName.setTextColor(darken(typeColor, 0.75f))
            holder.binding.cardType.strokeWidth = 0
            holder.binding.cardType.cardElevation = 0f
            holder.binding.ivTypeIcon.alpha = 0.85f
        }

        holder.binding.tvTypeName.text = type.displayName

        holder.binding.root.setOnClickListener {
            val nowSelected = !selectedTypes.contains(type)
            onTypeToggled(type, nowSelected)
            notifyItemChanged(position)
        }
    }

    /** Blend a color toward white by `factor` (0 = original, 1 = white) */
    private fun blendWithWhite(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val rf = (r + (255 - r) * (1f - factor)).toInt().coerceIn(0, 255)
        val gf = (g + (255 - g) * (1f - factor)).toInt().coerceIn(0, 255)
        val bf = (b + (255 - b) * (1f - factor)).toInt().coerceIn(0, 255)
        return Color.rgb(rf, gf, bf)
    }

    /** Darken a color by `factor` (1.0 = original, 0 = black) */
    private fun darken(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    fun forceDeselect(type: PokemonType) {
        val pos = types.indexOf(type)
        if (pos >= 0) notifyItemChanged(pos)
    }

    override fun getItemCount() = types.size
}
