package com.pokemonbp.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.ThemeManager

class PokemonPickerDialog(
    private val theme: AppTheme,
    private val onPicked: (PokedexEntry) -> Unit
) : DialogFragment() {

    private var filteredList = PokedexData.allPokemon.toMutableList()
    private lateinit var adapter: PickerAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = ThemeManager.colorsFor(theme)
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_pokemon_picker, null)

        view.setBackgroundColor(c.surface)

        val etSearch = view.findViewById<EditText>(R.id.et_picker_search)
        val recycler  = view.findViewById<RecyclerView>(R.id.recycler_picker)
        val tvTitle   = view.findViewById<TextView>(R.id.tv_picker_title)

        tvTitle.setTextColor(c.textPrimary)
        if (theme == AppTheme.RETRO) tvTitle.typeface = Typeface.MONOSPACE

        etSearch.setTextColor(c.textPrimary)
        etSearch.setHintTextColor(c.textSecondary)
        etSearch.setBackgroundColor(c.surfaceVariant)

        adapter = PickerAdapter(filteredList, theme) { entry ->
            onPicked(entry)
            dismiss()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
        recycler.setHasFixedSize(false)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim().lowercase()
                filteredList.clear()
                filteredList.addAll(
                    if (q.isEmpty()) PokedexData.allPokemon
                    else PokedexData.allPokemon.filter {
                        it.name.lowercase().contains(q) ||
                        it.nameDE.lowercase().contains(q) ||
                        it.id.toString().contains(q) ||
                        it.types.any { t -> t.displayName.lowercase().contains(q) }
                    }
                )
                adapter.notifyDataSetChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .create()
    }
}

class PickerAdapter(
    private val list: List<PokedexEntry>,
    private val theme: AppTheme,
    private val onPicked: (PokedexEntry) -> Unit
) : RecyclerView.Adapter<PickerAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val ivSprite:  ImageView    = v.findViewById(R.id.iv_picker_sprite)
        val tvDexNum:  TextView     = v.findViewById(R.id.tv_picker_dex_num)
        val tvName:    TextView     = v.findViewById(R.id.tv_picker_name)
        val llTypes:   LinearLayout = v.findViewById(R.id.ll_picker_types)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_picker, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = list[position]
        val ctx   = holder.itemView.context
        val c     = ThemeManager.colorsFor(theme)

        holder.itemView.setBackgroundColor(
            if (position % 2 == 0) c.surface else c.surfaceVariant
        )

        holder.tvDexNum.text = "#${entry.id.toString().padStart(4, '0')}"
        holder.tvDexNum.setTextColor(c.textSecondary)

        holder.tvName.text = entry.displayName()
        holder.tvName.setTextColor(c.textPrimary)
        if (theme == AppTheme.RETRO) holder.tvName.typeface = Typeface.MONOSPACE

        // Build type icon badges with rounded corners
        holder.llTypes.removeAllViews()
        for (type in entry.types) {
            val iconId = type.iconResId(ctx)
            if (iconId != 0) {
                val card = com.google.android.material.card.MaterialCardView(ctx)
                val dp32 = (32 * ctx.resources.displayMetrics.density).toInt()
                val cardParams = LinearLayout.LayoutParams(dp32, dp32)
                cardParams.marginEnd = (3 * ctx.resources.displayMetrics.density).toInt()
                card.layoutParams = cardParams
                card.radius = (6 * ctx.resources.displayMetrics.density)
                card.cardElevation = 0f
                card.setCardBackgroundColor(android.graphics.Color.parseColor(type.colorHex))

                val iv = ImageView(ctx)
                iv.layoutParams = android.view.ViewGroup.LayoutParams(dp32, dp32)
                iv.setImageResource(iconId)
                iv.scaleType = ImageView.ScaleType.CENTER_CROP
                card.addView(iv)
                holder.llTypes.addView(card)
            }
        }

        // Pokémon sprite — prefer local drawable, fall back to PokeAPI
        holder.ivSprite.loadPokemonSprite(ctx, entry.spriteId)

        holder.itemView.setOnClickListener { onPicked(entry) }
    }

    override fun getItemCount() = list.size
}
