package com.pokemonbp.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.data.TypeChart
import android.widget.ImageView
import android.widget.LinearLayout
import com.pokemonbp.data.PokemonType
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
        loadTypeIcons(holder.binding.llResultTypes, result.pokemon.types, holder.itemView)
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

        // Sprite
        val spriteUrl = result.pokemon.spriteUrl()
        if (spriteUrl != null) {
            Glide.with(holder.itemView.context).load(spriteUrl).into(holder.binding.ivSprite)
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

    private fun loadTypeIcons(container: LinearLayout?, types: List<PokemonType>, itemView: android.view.View) {
        container ?: return
        container.removeAllViews()
        val ctx = itemView.context
        for (type in types) {
            val iconId = type.iconResId(ctx)
            if (iconId != 0) {
                val card = com.google.android.material.card.MaterialCardView(ctx)
                val iv = ImageView(ctx)
                val dp30 = (30 * ctx.resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(dp30, dp30)
                params.marginEnd = (3 * ctx.resources.displayMetrics.density).toInt()
                card.layoutParams = params
                card.radius = (5 * ctx.resources.displayMetrics.density)
                card.cardElevation = 0f
                card.setCardBackgroundColor(android.graphics.Color.parseColor(type.colorHex))
                iv.layoutParams = android.view.ViewGroup.LayoutParams(dp30, dp30)
                iv.setImageResource(iconId)
                iv.scaleType = ImageView.ScaleType.CENTER_CROP
                card.addView(iv)
                container.addView(card)
            }
        }
    }
}
