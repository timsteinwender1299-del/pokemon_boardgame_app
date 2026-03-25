package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.RouteData
import com.pokemonbp.data.RouteLocation
import com.pokemonbp.data.RoutePokemon
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeColors
import com.pokemonbp.data.ThemeManager

class RoutePickerDialog(
    private val theme: AppTheme,
    private val onPokemonPicked: (nameDE: String, nameEN: String, bp: Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val routes = RouteData.loadRoutes(requireContext())
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_route_picker, null)
        view.findViewById<LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_routes)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = RouteGridAdapter(routes, c,
            onRouteClick = { location -> handleRouteClick(location) },
            onRandomClick = { handleRouteClick(routes.random()) }
        )

        view.findViewById<MaterialButton>(R.id.btn_close_route).setOnClickListener { dismiss() }

        view.findViewById<MaterialButton>(R.id.btn_rollsheet_route).setOnClickListener {
            showRollSheet("RollSheet — Route", listOf(
                RollSheetEntry("Pokemon-Kampf (Wild)",  "Du kämpfst gegen ein Wildes Pokemon auf deiner Route! (Würfel für Encounter)", "1–5"),
                RollSheetEntry("Pokemon-Kampf (Trainer)", "Du begegnest einem Trainer auf deiner Route! (Routenpokemon +1/Orden | Reset 4 Orden)", "6–8"),
                RollSheetEntry("Pokemon fangen",        "Du begegnest einem Wilden Pokemon auf deiner Route! (Würfel für Encounter)", "9–15"),
                RollSheetEntry("Itemrad (Consumables)", "Du findest ein Item! (Würfle für Item)", "16–20")
            ))
        }

        view.findViewById<MaterialButton>(R.id.btn_rollsheet_town).setOnClickListener {
            showRollSheet("RollSheet — Town EventTime", listOf(
                RollSheetEntry("Spacial Rend!",              "Du wirst zu einem zufälligen Ort teleportiert!", "1"),
                RollSheetEntry("Kleiner Meteor",             "Ein Meteor schlägt in eine Zufällige Route ein!", "2"),
                RollSheetEntry("Uno-Reverse!",               "Alle Type-Matchups sind verdreht diese Runde!", "3–4"),
                RollSheetEntry("Doppelteam!",                "Du darfst noch einmal würfeln!", "5–6"),
                RollSheetEntry("Finanzielle Hilfsmittel!",   "Du bekommst Geld anhand deiner Ordenanzahl (Badgesx10)", "7–8"),
                RollSheetEntry("TownSpecific-Event!",        "TownSpecific-Event!", "9–14"),
                RollSheetEntry("Itemrad (Type+)!",           "Du findest ein Item! (Würfle für Item)", "15–16"),
                RollSheetEntry("Itemrad (Permanent)!",       "Du findest ein Item! (Würfle für Item)", "17"),
                RollSheetEntry("NoSkippingLegDay!",          "Du kämpst trotzdem gegen die/den Arenaleiter/In", "18"),
                RollSheetEntry("Großer Meteor!",             "Ein Meteor schlägt in eine Zufällige Stadt ein!", "19"),
                RollSheetEntry("Roar of Time!",              "Dein Stärkstes Pokemon verliert 1BP (Es kann sich auch zurückentwickeln!)", "20")
            ))
        }

        view.findViewById<MaterialButton>(R.id.btn_rollsheet_galactic).setOnClickListener {
            showRollSheet("RollSheet — GalacticTime!", listOf(
                RollSheetEntry("Raubüberfall!",   "Jeder Spieler verliert Geld! (200G)", "1–4"),
                RollSheetEntry("Meteor!",         "Ein Meteor schlägt ein! Ein PKMN bei allen Spielern geht K.O.!", "5–8"),
                RollSheetEntry("Pokemon Kampf!",  "Würfle für einen Trainerkampf! (Von der Route wo du die Stadt betreten hast)", "9–12"),
                RollSheetEntry("Entführung!",     "Du wirst in das Geheimversteck von Team-Galaktik gebracht! (Schleife)", "13–16"),
                RollSheetEntry("Diebstahl!",      "Du verlierst ein Item!", "17–20")
            ))
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.setOnShowListener {
            val w = (requireContext().resources.displayMetrics.widthPixels * 0.92).toInt()
            dialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    private fun handleRouteClick(location: RouteLocation) {
        if (!location.isLegendary) {
            showPokemonGrid(location.displayName, location.tiers[0].pokemon)
        } else {
            showBadgeTierPicker(location)
        }
    }

    private fun showBadgeTierPicker(location: RouteLocation) {
        val c = ThemeManager.colorsFor(theme)
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(12))
            setBackgroundColor(c.surface)
        }

        TextView(requireContext()).apply {
            text = location.displayName
            textSize = 13f
            setTextColor(c.textPrimary)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, dp(14))
            container.addView(this)
        }

        var tierDialog: AlertDialog? = null

        for (tier in location.tiers) {
            MaterialButton(requireContext()).apply {
                text = tier.label
                isAllCaps = false
                textSize = 14f
                setTextColor(Color.WHITE)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC0000"))
                cornerRadius = dp(22)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
                ).also { it.bottomMargin = dp(8) }
                setOnClickListener {
                    tierDialog?.dismiss()
                    showPokemonGrid("${location.displayName} — ${tier.label}", tier.pokemon)
                }
                container.addView(this)
            }
        }

        MaterialButton(
            requireContext(), null,
            com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply {
            text = "Back"
            isAllCaps = false
            textSize = 12f
            setTextColor(Color.parseColor("#CC0000"))
            strokeColor = ColorStateList.valueOf(Color.parseColor("#CC0000"))
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            cornerRadius = dp(22)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40)
            ).also { it.topMargin = dp(4) }
            setOnClickListener { tierDialog?.dismiss() }
            container.addView(this)
        }

        tierDialog = AlertDialog.Builder(requireContext())
            .setView(container)
            .create()
        tierDialog.setOnShowListener {
            val w = (requireContext().resources.displayMetrics.widthPixels * 0.75).toInt()
            tierDialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
            tierDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        tierDialog.show()
    }

    private fun showPokemonGrid(title: String, pokemon: List<RoutePokemon>) {
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_route_pokemon_grid, null)

        view.findViewById<LinearLayout>(R.id.screen_panel_grid).setBackgroundColor(c.surface)
        view.findViewById<TextView>(R.id.tv_pokemon_grid_title).text = title

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_pokemon_grid)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = RoutePokemonGridAdapter(pokemon, c) { p ->
            onPokemonPicked(p.nameDE, p.nameEN, p.bp)
            dismiss()
        }

        var gridDialog: AlertDialog? = null
        view.findViewById<MaterialButton>(R.id.btn_back_pokemon_grid)
            .setOnClickListener { gridDialog?.dismiss() }

        gridDialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        gridDialog.setOnShowListener {
            val dm = requireContext().resources.displayMetrics
            val w = (dm.widthPixels * 0.44).toInt()
            val h = (dm.heightPixels * 0.78).toInt()
            gridDialog.window?.setLayout(w, h)
            gridDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        gridDialog.show()
    }

    private fun showRollSheet(title: String, entries: List<RollSheetEntry>) {
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rollsheet, null)
        view.findViewById<LinearLayout>(R.id.screen_panel_rollsheet).setBackgroundColor(c.surface)
        view.findViewById<TextView>(R.id.tv_rollsheet_title).text = title

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_rollsheet)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = RollSheetAdapter(entries, c)

        var rsDialog: AlertDialog? = null
        view.findViewById<MaterialButton>(R.id.btn_close_rollsheet).setOnClickListener { rsDialog?.dismiss() }

        rsDialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        rsDialog.setOnShowListener {
            val w = (requireContext().resources.displayMetrics.widthPixels * 0.92).toInt()
            rsDialog.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
            rsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        rsDialog.show()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

data class RollSheetEntry(val event: String, val text: String, val value: String)

// ── Route grid adapter (route name buttons, 2-col) ────────────────────────────

class RouteGridAdapter(
    private val routes: List<RouteLocation>,
    private val c: ThemeColors,
    private val onRouteClick: (RouteLocation) -> Unit,
    private val onRandomClick: () -> Unit
) : RecyclerView.Adapter<RouteGridAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tv_route_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_route_button, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        if (position == routes.size) {
            holder.tvName.text = "🎲 Random"
            holder.itemView.setOnClickListener { onRandomClick() }
        } else {
            val loc = routes[position]
            holder.tvName.text = loc.displayName
            holder.itemView.setOnClickListener { onRouteClick(loc) }
        }
    }

    override fun getItemCount() = routes.size + 1
}

// ── Pokémon grid adapter (sprites + types + BP, 3-col) ───────────────────────

class RoutePokemonGridAdapter(
    private val pokemon: List<RoutePokemon>,
    private val c: ThemeColors,
    private val onClick: (RoutePokemon) -> Unit
) : RecyclerView.Adapter<RoutePokemonGridAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivSprite: ImageView = v.findViewById(R.id.iv_route_sprite)
        val ivType1:  ImageView = v.findViewById(R.id.iv_route_type1)
        val ivType2:  ImageView = v.findViewById(R.id.iv_route_type2)
        val tvBP:     TextView  = v.findViewById(R.id.tv_route_bp)
        val tvName:   TextView  = v.findViewById(R.id.tv_route_pokemon_name)
        val tvNameEN: TextView  = v.findViewById(R.id.tv_route_pokemon_name_en)
        val tvValue1: TextView  = v.findViewById(R.id.tv_route_value1)
        val tvValue2: TextView  = v.findViewById(R.id.tv_route_value2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_route_pokemon, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p   = pokemon[position]
        val ctx = holder.itemView.context

        holder.tvName.text = p.nameDE
        holder.tvName.setTextColor(c.textPrimary)
        holder.tvNameEN.text = p.nameEN
        holder.tvNameEN.setTextColor(c.textSecondary)
        holder.tvBP.text = if (p.bp > 0) "BP: ${p.bp}" else "BP: ?"

        // Values
        val valueParts = p.value?.split("/")
        holder.tvValue1.text = valueParts?.getOrNull(0)?.trim() ?: ""
        holder.tvValue2.text = valueParts?.getOrNull(1)?.trim() ?: ""

        val entry = PokedexData.allPokemon.find { it.name.equals(p.nameEN.trim(), ignoreCase = true) }

        // Sprite
        if (entry != null && entry.spriteId > 0) {
            holder.ivSprite.loadPokemonSprite(ctx, entry.spriteId)
        } else {
            holder.ivSprite.setImageResource(R.drawable.ic_pokeball)
        }

        // Type icons — always show 2 slots; NoType.png for empty second slot
        val types = entry?.types ?: emptyList()
        val url1 = if (types.isNotEmpty()) SpriteUrls.typeIconUrl(types[0].name) else SpriteUrls.noTypeUrl
        val url2 = if (types.size >= 2) SpriteUrls.typeIconUrl(types[1].name) else SpriteUrls.noTypeUrl
        holder.ivType2.visibility = View.VISIBLE
        Glide.with(ctx).load(url1).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.ivType1)
        Glide.with(ctx).load(url2).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.ivType2)

        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount() = pokemon.size
}

// ── RollSheet adapter ─────────────────────────────────────────────────────────

class RollSheetAdapter(
    private val entries: List<RollSheetEntry>,
    private val c: ThemeColors
) : RecyclerView.Adapter<RollSheetAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvEvent: TextView = v.findViewById(R.id.tv_rs_event)
        val tvText:  TextView = v.findViewById(R.id.tv_rs_text)
        val tvValue: TextView = v.findViewById(R.id.tv_rs_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_rollsheet_row, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = entries[position]
        holder.tvEvent.text = e.event
        holder.tvEvent.setTextColor(c.textPrimary)
        holder.tvText.text = e.text
        holder.tvText.setTextColor(c.textSecondary)
        holder.tvValue.text = e.value
        holder.tvValue.setTextColor(c.accent)
        holder.itemView.setBackgroundColor(if (position % 2 == 0) 0x0AFFFFFF else 0x00000000)
    }

    override fun getItemCount() = entries.size
}
