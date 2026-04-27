package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
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
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.android.material.button.MaterialButton
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.RouteData
import com.pokemonbp.data.RouteLocation
import com.pokemonbp.data.RouteMapCoordinates
import com.pokemonbp.data.RoutePokemon
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeColors
import com.pokemonbp.data.ThemeManager
import kotlin.math.abs
import kotlin.math.sqrt

class RoutePickerDialog(
    private val theme: AppTheme,
    private val onPokemonPicked: (nameDE: String, nameEN: String, bp: Int) -> Unit
) : DialogFragment() {

    private lateinit var allRoutes: List<RouteLocation>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        allRoutes = RouteData.loadRoutes(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_route_map, null)

        val mapView    = view.findViewById<SubsamplingScaleImageView>(R.id.iv_route_map)
        val overlay    = view.findViewById<RouteMapOverlayView>(R.id.v_route_overlay)
        val tvHovName  = view.findViewById<android.widget.TextView>(R.id.tv_route_hover_name)
        val btnClose   = view.findViewById<MaterialButton>(R.id.btn_close_route_map)

        mapView.setImage(ImageSource.asset("MapFull.png"))
        mapView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
        mapView.maxScale = 6f

        overlay.mapView = mapView
        overlay.hotspots = RouteMapCoordinates.hotspots
        overlay.legendaryNames = allRoutes.filter { it.isLegendary }.map { it.displayName }.toSet()

        mapView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onReady() {
                overlay.setImageDimensions(mapView.sWidth, mapView.sHeight)
            }
            override fun onImageLoaded() {}
            override fun onPreviewLoadError(e: Exception) {}
            override fun onImageLoadError(e: Exception) {}
            override fun onTileLoadError(e: Exception) {}
            override fun onPreviewReleased() {}
        })

        mapView.setOnStateChangedListener(object : SubsamplingScaleImageView.OnStateChangedListener {
            override fun onScaleChanged(newScale: Float, origin: Int) = overlay.invalidate()
            override fun onCenterChanged(newCenter: PointF, origin: Int) = overlay.invalidate()
        })

        // Touch handling: return false so SSIV keeps pan/zoom; we get all events anyway
        val slop = ViewConfiguration.get(requireContext()).scaledTouchSlop.toFloat()
        var downX = 0f; var downY = 0f
        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x; downY = event.y
                    updateHover(event.x, event.y, mapView, overlay, tvHovName)
                }
                MotionEvent.ACTION_MOVE -> {
                    updateHover(event.x, event.y, mapView, overlay, tvHovName)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val isTap = event.action == MotionEvent.ACTION_UP &&
                        abs(event.x - downX) < slop * 3 && abs(event.y - downY) < slop * 3
                    if (isTap && mapView.isImageLoaded) {
                        val src = mapView.viewToSourceCoord(event.x, event.y)
                        if (src != null) tryHitHotspot(src.x, src.y, overlay)
                    }
                    tvHovName.visibility = View.INVISIBLE
                    overlay.hoveredName = null
                    overlay.invalidate()
                }
            }
            false
        }

        btnClose.setOnClickListener { dismiss() }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.setOnShowListener {
            val dm = requireContext().resources.displayMetrics
            val w = (dm.widthPixels * 0.95).toInt()
            val h = (dm.heightPixels * 0.90).toInt()
            dialog.window?.setLayout(w, h)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    private fun updateHover(
        viewX: Float, viewY: Float,
        mapView: SubsamplingScaleImageView,
        overlay: RouteMapOverlayView,
        tvName: android.widget.TextView
    ) {
        if (!mapView.isImageLoaded) return
        val src = mapView.viewToSourceCoord(viewX, viewY) ?: return
        val w = mapView.sWidth.toFloat(); val h = mapView.sHeight.toFloat()
        val hoverRadius = w * 0.07f
        val nearest = RouteMapCoordinates.hotspots.minByOrNull { hs ->
            val dx = src.x - hs.x * w; val dy = src.y - hs.y * h
            sqrt(dx * dx + dy * dy)
        }
        if (nearest != null) {
            val dx = src.x - nearest.x * w; val dy = src.y - nearest.y * h
            if (sqrt(dx * dx + dy * dy) < hoverRadius) {
                tvName.text = nearest.routeName
                tvName.visibility = View.VISIBLE
                overlay.hoveredName = nearest.routeName
            } else {
                tvName.visibility = View.INVISIBLE
                overlay.hoveredName = null
            }
        } else {
            tvName.visibility = View.INVISIBLE
            overlay.hoveredName = null
        }
        overlay.invalidate()
    }

    private fun tryHitHotspot(srcX: Float, srcY: Float, overlay: RouteMapOverlayView) {
        val w = overlay.mapView?.sWidth?.toFloat() ?: return
        val h = overlay.mapView?.sHeight?.toFloat() ?: return
        val tapRadius = w * 0.06f
        val hit = RouteMapCoordinates.hotspots.minByOrNull { hs ->
            val dx = srcX - hs.x * w
            val dy = srcY - hs.y * h
            sqrt(dx * dx + dy * dy)
        } ?: return
        val dx = srcX - hit.x * w
        val dy = srcY - hit.y * h
        if (sqrt(dx * dx + dy * dy) > tapRadius) return
        val location = allRoutes.find { it.displayName == hit.routeName } ?: return
        overlay.setSelected(hit.routeName)
        handleRouteClick(location)
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

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}

data class RollSheetEntry(val event: String, val text: String, val min: String, val max: String)

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

        val entry = PokedexData.byNameEN[p.nameEN.trim().lowercase()]

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

    private var selectedPosition = -1

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvEvent: TextView = v.findViewById(R.id.tv_rs_event)
        val tvText:  TextView = v.findViewById(R.id.tv_rs_text)
        val tvMin:   TextView = v.findViewById(R.id.tv_rs_min)
        val tvMax:   TextView = v.findViewById(R.id.tv_rs_max)
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
        holder.tvMin.text = e.min
        holder.tvMax.text = e.max

        val isSelected = position == selectedPosition
        holder.itemView.setBackgroundColor(when {
            isSelected          -> 0xCCFF8F00.toInt()
            position % 2 == 0  -> 0x0AFFFFFF
            else               -> 0x00000000
        })

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = if (selectedPosition == position) -1 else position
            if (prev != -1) notifyItemChanged(prev)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = entries.size
}
