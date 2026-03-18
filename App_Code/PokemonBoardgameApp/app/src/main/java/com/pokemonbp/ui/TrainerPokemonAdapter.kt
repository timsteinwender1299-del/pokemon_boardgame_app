package com.pokemonbp.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.model.PokemonPreset

data class TrainerPokemonEntry(
    val preset: PokemonPreset,
    var bp: Int = preset.baseBP.takeIf { it >= 1 } ?: 1,
    var bpPickerOpen: Boolean = false
)

class TrainerPokemonAdapter(
    private val list: MutableList<TrainerPokemonEntry>,
    private val theme: AppTheme,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<TrainerPokemonAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView       = v.findViewById(R.id.card_tp)
        val ivSprite: ImageView          = v.findViewById(R.id.iv_tp_sprite)
        val tvName: TextView             = v.findViewById(R.id.tv_tp_name)
        val llTypes: LinearLayout        = v.findViewById(R.id.ll_tp_types)
        val btnBpToggle: MaterialButton  = v.findViewById(R.id.btn_bp_toggle)
        val layoutBpPicker: LinearLayout = v.findViewById(R.id.layout_bp_picker)
        val btnEvolve: MaterialButton    = v.findViewById(R.id.btn_evolve)
        val btnRemove: MaterialButton    = v.findViewById(R.id.btn_tp_remove)
        val btnDuskstone: MaterialButton = v.findViewById(R.id.btn_duskstone)
        val bpBtns: List<MaterialButton> = listOf(
            v.findViewById(R.id.bp_1),  v.findViewById(R.id.bp_2),
            v.findViewById(R.id.bp_3),  v.findViewById(R.id.bp_4),
            v.findViewById(R.id.bp_5),  v.findViewById(R.id.bp_6),
            v.findViewById(R.id.bp_7),  v.findViewById(R.id.bp_8),
            v.findViewById(R.id.bp_9),  v.findViewById(R.id.bp_10),
            v.findViewById(R.id.bp_11), v.findViewById(R.id.bp_12)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trainer_pokemon, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = list[position]
        val preset = entry.preset
        val c = ThemeManager.colorsFor(theme)
        val typeColor = if (preset.types.isNotEmpty())
            Color.parseColor(preset.types.first().colorHex) else c.accent

        // Card styling
        holder.card.setCardBackgroundColor(c.surface)
        holder.card.strokeColor = typeColor

        // Name
        holder.tvName.text = if (preset.nameDE.isNotBlank()) "${preset.nameDE} / ${preset.name}"
                             else preset.name
        holder.tvName.setTextColor(c.textPrimary)

        // Sprite
        if (preset.pokedexId > 0) {
            holder.ivSprite.loadPokemonSprite(holder.itemView.context, preset.pokedexId)
        }

        // Type icons
        holder.llTypes.removeAllViews()
        for (type in preset.types) {
            val iv = ImageView(holder.itemView.context)
            val dp = (24 * holder.itemView.context.resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(dp, dp)
            params.marginEnd = (2 * holder.itemView.context.resources.displayMetrics.density).toInt()
            iv.layoutParams = params
            Glide.with(holder.itemView.context).load(SpriteUrls.typeIconUrl(type.name)).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv)
            iv.scaleType = ImageView.ScaleType.FIT_CENTER
            holder.llTypes.addView(iv)
        }

        // BP toggle pill — shows current BP, opens/closes picker
        updateBpToggle(holder.btnBpToggle, entry, typeColor)
        holder.layoutBpPicker.visibility = if (entry.bpPickerOpen) View.VISIBLE else View.GONE

        holder.btnBpToggle.setOnClickListener {
            entry.bpPickerOpen = !entry.bpPickerOpen
            holder.layoutBpPicker.visibility = if (entry.bpPickerOpen) View.VISIBLE else View.GONE
            updateBpToggle(holder.btnBpToggle, entry, typeColor)
        }

        // BP number buttons — selecting one saves BP and closes picker
        holder.bpBtns.forEachIndexed { idx, btn ->
            val bpVal = idx + 1
            val selected = bpVal == entry.bp
            styleBpBtn(btn, selected, typeColor)
            btn.setOnClickListener {
                entry.bp = bpVal
                entry.bpPickerOpen = false  // auto-close
                holder.layoutBpPicker.visibility = View.GONE
                updateBpToggle(holder.btnBpToggle, entry, typeColor)
                // Refresh all BP btn styles
                holder.bpBtns.forEachIndexed { i, b -> styleBpBtn(b, i + 1 == bpVal, typeColor) }
            }
        }

        // Evolve button
        val nextEvos = com.pokemonbp.data.EvolutionData.nextEvolutions(preset.pokedexId)
        if (nextEvos.isNotEmpty()) {
            holder.btnEvolve.isEnabled = true
            holder.btnEvolve.alpha = 1.0f
            holder.btnEvolve.strokeColor = android.content.res.ColorStateList.valueOf(typeColor)
            holder.btnEvolve.iconTint = null
            holder.btnEvolve.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener

                if (nextEvos.size == 1) {
                    // Straight evolution — just evolve
                    applyEvolution(pos, nextEvos[0], entry)
                } else {
                    // Multiple options — show popup menu
                    val popup = android.widget.PopupMenu(holder.itemView.context, holder.btnEvolve)
                    nextEvos.forEach { evoId ->
                        val evoEntry = com.pokemonbp.ui.PokedexData.allPokemon.find { it.id == evoId }
                        val label = if (evoEntry != null) "${evoEntry.nameDE} / ${evoEntry.name}" else "#$evoId"
                        popup.menu.add(label).setOnMenuItemClickListener {
                            applyEvolution(pos, evoId, entry)
                            true
                        }
                    }
                    popup.show()
                }
            }
        } else {
            holder.btnEvolve.isEnabled = false
            holder.btnEvolve.alpha = 0.3f
            holder.btnEvolve.iconTint = android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY)
        }

        // Remove
        holder.btnRemove.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_ID.toInt()) onRemove(pos)
        }

        // Duskstone — placeholder, no function yet
        holder.btnDuskstone.isEnabled = false
        holder.btnDuskstone.alpha = 0.4f
    }

    private fun updateBpToggle(btn: MaterialButton, entry: TrainerPokemonEntry, typeColor: Int) {
        btn.text = if (entry.bp >= 1) "BP: ${entry.bp} ▾" else "BP ▾"
        btn.minWidth = 0
        btn.minimumWidth = 0
        if (entry.bpPickerOpen) {
            btn.backgroundTintList = ColorStateList.valueOf(typeColor)
            btn.setTextColor(Color.WHITE)
            btn.strokeWidth = 0
        } else {
            btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            btn.setTextColor(typeColor)
            btn.strokeColor = ColorStateList.valueOf(typeColor)
            btn.strokeWidth = 3
        }
    }

    private fun styleBpBtn(btn: MaterialButton, selected: Boolean, typeColor: Int) {
        if (selected) {
            btn.backgroundTintList = ColorStateList.valueOf(typeColor)
            btn.setTextColor(Color.WHITE)
            btn.strokeWidth = 0
        } else {
            btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            btn.setTextColor(typeColor)
            btn.strokeColor = ColorStateList.valueOf(typeColor)
            btn.strokeWidth = 2
        }
    }

    private fun applyEvolution(pos: Int, evoId: Int, entry: TrainerPokemonEntry) {
        val nextEntry = com.pokemonbp.ui.PokedexData.allPokemon.find { it.id == evoId }
        if (nextEntry != null) {
            list[pos] = TrainerPokemonEntry(
                preset = com.pokemonbp.model.PokemonPreset(
                    name = nextEntry.name,
                    nameDE = nextEntry.nameDE,
                    pokedexId = nextEntry.id,
                    types = nextEntry.types,
                    baseBP = entry.preset.baseBP
                ),
                bp = entry.bp
            )
            notifyItemChanged(pos)
        }
    }

    override fun getItemCount() = list.size
}
