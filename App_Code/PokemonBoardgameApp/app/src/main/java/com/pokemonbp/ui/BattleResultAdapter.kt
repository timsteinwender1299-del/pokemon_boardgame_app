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
import com.pokemonbp.R
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.data.TypeChart
import com.pokemonbp.databinding.ItemBattleResultBinding
import com.pokemonbp.model.BattleResult

class BattleResultAdapter(
    private val results: List<BattleResult>,
    private val theme: AppTheme
) : RecyclerView.Adapter<BattleResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(val binding: ItemBattleResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ResultViewHolder(ItemBattleResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        val c = ThemeManager.colorsFor(theme)
        val typeColor = Color.parseColor(result.pokemon.types.first().colorHex)

        holder.binding.cardResult.setCardBackgroundColor(c.surface)
        holder.binding.cardResult.strokeColor = typeColor

        holder.binding.tvResultName.text = result.pokemon.displayName()
        holder.binding.tvResultName.setTextColor(c.textPrimary)
        holder.binding.tvResultTypes.text = result.pokemon.types.joinToString(" / ") { it.displayName }
        holder.binding.tvResultTypes.setTextColor(typeColor)
        val types = result.pokemon.types
        val ctx = holder.itemView.context
        Glide.with(ctx).load(SpriteUrls.typeIconUrl(types[0].name)).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.ivType1)
        Glide.with(ctx).load(if (types.size > 1) SpriteUrls.typeIconUrl(types[1].name) else SpriteUrls.noTypeUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.binding.ivType2)
        holder.binding.tvBaseBp.text = "Base BP: ${result.pokemon.baseBP}"
        holder.binding.tvBaseBp.setTextColor(c.textSecondary)

        if (theme == AppTheme.RETRO) {
            listOf(holder.binding.tvResultName, holder.binding.tvResultTypes,
                holder.binding.tvBaseBp, holder.binding.tvFinalBp,
                holder.binding.tvBpModifier, holder.binding.tvMatchupDetails)
                .forEach { it.typeface = Typeface.MONOSPACE }
        }

        if (result.zeroedOut) {
            holder.binding.tvFinalBp.text = "Final BP: 0  ⛔ Immune"
            holder.binding.tvFinalBp.setTextColor(c.negative)
        } else {
            holder.binding.tvFinalBp.text = "Final BP: ${result.finalBP}"
            holder.binding.tvFinalBp.setTextColor(c.textPrimary)
        }

        val modText = when {
            result.bpModifier > 0 -> "+${result.bpModifier} BP"
            result.bpModifier < 0 -> "${result.bpModifier} BP"
            else -> "±0 BP"
        }
        holder.binding.tvBpModifier.text = modText
        holder.binding.tvBpModifier.setTextColor(when {
            result.bpModifier > 0 -> c.positive
            result.bpModifier < 0 -> c.negative
            else -> c.textSecondary
        })

        // Sprite — use artwork images
        val pokedexId = result.pokemon.pokedexId
        if (pokedexId > 0) {
            Glide.with(holder.itemView.context)
                .load(SpriteUrls.urlFor(pokedexId))
                .placeholder(R.drawable.ic_pokeball)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.binding.ivSprite)
            holder.binding.ivSprite.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.ivSprite.visibility = android.view.View.GONE
        }

        // Matchup details
        if (result.matchupDetails.isEmpty()) {
            holder.binding.tvMatchupDetails.text = "No type advantages/disadvantages"
            holder.binding.tvMatchupDetails.setTextColor(c.textSecondary)
        } else {
            val sb = StringBuilder()
            for (detail in result.matchupDetails) {
                val line = when (detail.reason) {
                    TypeChart.BpChangeReason.NORMAL -> {
                        val eff = if (detail.effectiveness >= 2.0) "Super Effective ✅" else "Not Very Effective ⚠️"
                        val ch = if (detail.bpChange > 0) "+${detail.bpChange}" else "${detail.bpChange}"
                        "${detail.attackerType.displayName} → ${detail.defenderType.displayName}: $eff ($ch BP)"
                    }
                    TypeChart.BpChangeReason.ZERO_NULLIFIES_ALL ->
                        "${detail.attackerType.displayName} → ${detail.defenderType.displayName}: ⛔ Immune — BP→0"
                    TypeChart.BpChangeReason.ZERO_MINUS_TWO ->
                        "${detail.attackerType.displayName} → ${detail.defenderType.displayName}: ⛔ Immune (−2 BP)"
                    TypeChart.BpChangeReason.CANCELLED_BY_ZERO -> {
                        val ch = if (detail.bpChange > 0) "+${detail.bpChange}" else "${detail.bpChange}"
                        "${detail.attackerType.displayName} → ${detail.defenderType.displayName}: ✖ Cancelled (would be $ch)"
                    }
                }
                sb.appendLine(line)
            }
            holder.binding.tvMatchupDetails.text = sb.toString().trimEnd()
            holder.binding.tvMatchupDetails.setTextColor(c.textSecondary)
        }
    }

    override fun getItemCount() = results.size

}
