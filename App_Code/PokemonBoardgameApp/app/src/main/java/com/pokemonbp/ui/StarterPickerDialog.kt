package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeColors
import com.pokemonbp.data.ThemeManager

class StarterPickerDialog(
    private val theme: AppTheme,
    private val onPicked: (nameDE: String, nameEN: String) -> Unit
) : DialogFragment() {

    data class StarterPokemon(val nameDE: String, val nameEN: String)
    data class StarterLine(val base: StarterPokemon, val evo2: StarterPokemon?, val evo3: StarterPokemon?)

    private fun parseStarters(filename: String): List<StarterLine> {
        return try {
            val text = requireContext().assets.open(filename).bufferedReader().readText()
            val pokemonLines = text.lines()
                .map { it.trim() }
                .filter { it.contains('/') && !it.trimEnd().endsWith(':') }
            pokemonLines.chunked(3).mapNotNull { group ->
                if (group.isEmpty()) null
                else {
                    fun parse(s: String): StarterPokemon {
                        val parts = s.split("/", limit = 2)
                        return StarterPokemon(parts[0].trim(), parts.getOrElse(1) { "" }.trim())
                    }
                    StarterLine(
                        parse(group[0]),
                        group.getOrNull(1)?.let { parse(it) },
                        group.getOrNull(2)?.let { parse(it) }
                    )
                }
            }
        } catch (e: Exception) { emptyList() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = ThemeManager.colorsFor(theme)
        val view = buildTypeSelectionView(c)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.setOnShowListener {
            val dm = requireContext().resources.displayMetrics
            val w = (dm.widthPixels * 0.80).toInt()
            dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    private fun buildTypeSelectionView(c: ThemeColors): android.view.View {
        val ctx = requireContext()
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            setBackgroundColor(c.surface)
        }

        TextView(ctx).apply {
            text = "Choose Starter Type"
            textSize = 14f
            setTextColor(c.textPrimary)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(14))
            container.addView(this, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }

        val typeRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        data class TypeOption(val label: String, val file: String, val typeKey: String)
        val types = listOf(
            TypeOption("Grass", "Starter_Grass.txt", "GRASS"),
            TypeOption("Water", "Starter_Water.txt", "WATER"),
            TypeOption("Fire",  "Starter_Fire.txt",  "FIRE")
        )

        types.forEachIndexed { idx, opt ->
            val btn = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                val tv = android.util.TypedValue()
                ctx.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                foreground = ctx.getDrawable(tv.resourceId)
                setPadding(dp(8), dp(8), dp(8), dp(8))
                val lp = LinearLayout.LayoutParams(0, dp(80)).also {
                    it.weight = 1f
                    if (idx < types.size - 1) it.marginEnd = dp(4)
                }
                layoutParams = lp
            }

            val typeIv = ImageView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
                scaleType = ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                Glide.with(ctx).load(SpriteUrls.typeIconUrl(opt.typeKey))
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
            }
            btn.addView(typeIv)

            val tv = TextView(ctx).apply {
                text = opt.label
                textSize = 12f
                setTextColor(c.textPrimary)
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
            }
            btn.addView(tv, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

            btn.setOnClickListener {
                val lines = parseStarters(opt.file)
                showStarterLines(opt.label, opt.typeKey, lines, c)
            }
            typeRow.addView(btn)
        }

        container.addView(typeRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        MaterialButton(ctx, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "Cancel"
            isAllCaps = false
            textSize = 12f
            setTextColor(Color.parseColor("#CC0000"))
            strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#CC0000"))
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
            cornerRadius = dp(22)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)).also { it.topMargin = dp(12) }
            setOnClickListener { dismiss() }
            container.addView(this)
        }

        return container
    }

    private fun showStarterLines(typeName: String, typeKey: String, lines: List<StarterLine>, c: ThemeColors) {
        val ctx = requireContext()

        val outerContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(c.surface)
        }

        // Header row: back + type icon + title
        val headerRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(8))
        }
        var linesDialog: AlertDialog? = null
        TextView(ctx).apply {
            text = "← Back"
            textSize = 13f
            setTextColor(Color.parseColor("#2983d3"))
            setPadding(0, 0, dp(12), 0)
            isClickable = true
            isFocusable = true
            setOnClickListener { linesDialog?.dismiss() }
            headerRow.addView(this)
        }
        ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).also { it.marginEnd = dp(6) }
            scaleType = ImageView.ScaleType.FIT_CENTER
            Glide.with(ctx).load(SpriteUrls.typeIconUrl(typeKey))
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
            headerRow.addView(this)
        }
        TextView(ctx).apply {
            text = "$typeName Starters"
            textSize = 13f
            setTextColor(c.textPrimary)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                .also { it.weight = 1f }
            headerRow.addView(this)
        }
        outerContainer.addView(headerRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        // Divider
        outerContainer.addView(android.view.View(ctx).apply {
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)))

        // Scrollable evolution lines
        val scrollView = ScrollView(ctx)
        val innerContainer = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }
        scrollView.addView(innerContainer, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        lines.forEach { line ->
            val lineRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = dp(6) }
            }

            val allPokemon = listOfNotNull(line.base, line.evo2, line.evo3)
            allPokemon.forEachIndexed { index, pokemon ->
                val isBase = index == 0
                val col = buildPokemonColumn(pokemon, c, isBase) {
                    onPicked(pokemon.nameDE, pokemon.nameEN)
                    linesDialog?.dismiss()
                    dismiss()
                }
                col.alpha = if (isBase) 1f else 0.35f
                lineRow.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
                    .also { it.weight = 1f; if (index < allPokemon.size - 1) it.marginEnd = dp(2) })
            }
            innerContainer.addView(lineRow)
        }

        outerContainer.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0).also { it.weight = 1f })

        linesDialog = AlertDialog.Builder(ctx)
            .setView(outerContainer)
            .create()
        linesDialog.setOnShowListener {
            val dm = ctx.resources.displayMetrics
            val w = (dm.widthPixels * 0.92).toInt()
            val h = (dm.heightPixels * 0.80).toInt()
            linesDialog.window?.setLayout(w, h)
            linesDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        linesDialog.show()
    }

    private fun buildPokemonColumn(
        pokemon: StarterPokemon,
        c: ThemeColors,
        clickable: Boolean,
        onClick: () -> Unit
    ): LinearLayout {
        val ctx = requireContext()
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            isClickable = clickable
            isFocusable = clickable
            if (clickable) {
                val tv = android.util.TypedValue()
                ctx.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                foreground = ctx.getDrawable(tv.resourceId)
                setOnClickListener { onClick() }
            }
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }

        // Sprite
        val entry = PokedexData.byNameEN[pokemon.nameEN.lowercase()]
        val ivSprite = ImageView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(dp(58), dp(58))
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            if (entry != null && entry.spriteId > 0) loadPokemonSprite(ctx, entry.spriteId)
            else setImageResource(R.drawable.ic_pokeball)
        }
        col.addView(ivSprite)

        // Type icons
        if (entry != null) {
            val typesRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = dp(2) }
            }
            entry.types.forEach { type ->
                val iv = ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(20), dp(20))
                        .also { it.marginEnd = dp(2) }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    Glide.with(ctx).load(SpriteUrls.typeIconUrl(type.name))
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
                }
                typesRow.addView(iv)
            }
            col.addView(typesRow)
        }

        // German name
        TextView(ctx).apply {
            text = pokemon.nameDE
            textSize = 8.5f
            setTextColor(c.textPrimary)
            gravity = Gravity.CENTER
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = dp(2) }
            col.addView(this)
        }

        // English name
        TextView(ctx).apply {
            text = pokemon.nameEN
            textSize = 7.5f
            setTextColor(c.textSecondary)
            gravity = Gravity.CENTER
            maxLines = 1
            col.addView(this)
        }

        return col
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
