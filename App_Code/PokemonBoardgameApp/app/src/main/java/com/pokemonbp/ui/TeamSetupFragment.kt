package com.pokemonbp.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pokemonbp.R
import com.pokemonbp.data.*
import com.pokemonbp.databinding.FragmentTeamSetupBinding
import com.pokemonbp.model.*

class TeamSetupFragment : Fragment() {

    private var _binding: FragmentTeamSetupBinding? = null
    private val binding get() = _binding!!

    private val teamAList = mutableListOf<Pokemon>()
    private val teamBList = mutableListOf<Pokemon>()
    private lateinit var adapterA: PokemonListAdapter
    private lateinit var adapterB: PokemonListAdapter

    // Active (selected) Pokémon index per team — defaults to 0
    private var activeIndexA = 0
    private var activeIndexB = 0

    private var teamATrainer: PlayerTrainer? = null
    private var teamBSavedTrainer: PlayerTrainer? = null
    private var teamBLabel: String = "Enemy Trainer"
    private var currentEnemyTrainer: EnemyTrainer? = null
    private var wildMode = false

    // Fainted Pokémon tracking (in-memory per battle session)
    private val faintedIndicesA = mutableSetOf<Int>()
    private val faintedIndicesB = mutableSetOf<Int>()

    private val panelBackStack = ArrayDeque<() -> Unit>()

    private val mainActivity get() = activity as? MainActivity

    // Maps gym leader ID → badge number 1-8
    private val gymLeaderBadgeMap = mapOf(
        "roark" to 1, "gardenia" to 2, "fantina" to 3, "hilda" to 4,
        "crasherwake" to 5, "byron" to 6, "candice" to 7, "volkner" to 8
    )

    private val badgeViews: List<android.widget.ImageView> by lazy {
        listOf(
            binding.ivBadge1, binding.ivBadge2, binding.ivBadge3, binding.ivBadge4,
            binding.ivBadge5, binding.ivBadge6, binding.ivBadge7, binding.ivBadge8
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeamSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        applyTheme(theme)
        loadStaticIcons()

        adapterA = PokemonListAdapter(teamAList, theme,
            onDelete = { pos ->
                teamAList.removeAt(pos)
                // Clamp active index if needed
                if (activeIndexA >= teamAList.size) activeIndexA = maxOf(0, teamAList.size - 1)
                adapterA.activeIndex = activeIndexA
                adapterA.notifyDataSetChanged()
                updateBattleButton()
            },
            onSelected = { pos ->
                activeIndexA = pos
                updateBattleButton()
            },
            onRevive = { pos ->
                faintedIndicesA.remove(pos)
                adapterA.faintedIndices = faintedIndicesA.toSet()
                updateBattleButton()
            }
        )

        adapterB = PokemonListAdapter(teamBList, theme,
            onDelete = { pos ->
                teamBList.removeAt(pos)
                if (activeIndexB >= teamBList.size) activeIndexB = maxOf(0, teamBList.size - 1)
                adapterB.activeIndex = activeIndexB
                adapterB.notifyDataSetChanged()
                updateBattleButton()
            },
            onSelected = { pos ->
                activeIndexB = pos
                updateBattleButton()
            },
            onRevive = { pos ->
                faintedIndicesB.remove(pos)
                adapterB.faintedIndices = faintedIndicesB.toSet()
                updateBattleButton()
            }
        )

        adapterA.activeIndex = activeIndexA
        adapterB.activeIndex = activeIndexB

        binding.recyclerTeamA.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerTeamA.adapter = adapterA
        binding.recyclerTeamB.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerTeamB.adapter = adapterB

        // Fill section height: recalculate item heights when recycler size or data changes
        var prevHeightA = 0
        binding.recyclerTeamA.addOnLayoutChangeListener { _, _, t, _, b, _, _, _, _ ->
            val h = b - t; if (h != prevHeightA && h > 0) { prevHeightA = h; updateItemHeights() }
        }
        var prevHeightB = 0
        binding.recyclerTeamB.addOnLayoutChangeListener { _, _, t, _, b, _, _, _, _ ->
            val h = b - t; if (h != prevHeightB && h > 0) { prevHeightB = h; updateItemHeights() }
        }
        adapterA.registerAdapterDataObserver(object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(p: Int, c: Int) { binding.recyclerTeamA.post { updateItemHeights() } }
            override fun onItemRangeRemoved(p: Int, c: Int) { binding.recyclerTeamA.post { updateItemHeights() } }
        })
        adapterB.registerAdapterDataObserver(object : androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(p: Int, c: Int) { binding.recyclerTeamB.post { updateItemHeights() } }
            override fun onItemRangeRemoved(p: Int, c: Int) { binding.recyclerTeamB.post { updateItemHeights() } }
        })

        binding.ivUpdateButton.setOnClickListener {
            binding.ivUpdateButton.isEnabled = false
            Toast.makeText(requireContext(), "Updating data…", Toast.LENGTH_SHORT).show()
            com.pokemonbp.data.DataSyncManager.syncAll(requireContext()) { updated, failed ->
                binding.ivUpdateButton.isEnabled = true
                val msg = if (failed == 0) "Updated $updated files successfully!"
                          else "Updated $updated files, $failed failed."
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

        binding.ivReloadA.setOnClickListener {
            faintedIndicesA.clear()
            adapterA.faintedIndices = emptySet()
            if (teamAList.isNotEmpty()) { activeIndexA = 0; adapterA.activeIndex = 0 }
            updateBattleButton()
        }
        binding.ivReloadB.setOnClickListener {
            faintedIndicesB.clear()
            adapterB.faintedIndices = emptySet()
            if (teamBList.isNotEmpty()) { activeIndexB = 0; adapterB.activeIndex = 0 }
            updateBattleButton()
        }

        setupWildRouteGrid()

        binding.ivWildButton.setOnClickListener {
            wildMode = !wildMode
            if (wildMode) {
                binding.root.setBackgroundColor(Color.parseColor("#2983d3"))
                Glide.with(requireContext()).load(SpriteUrls.battleCalculatorUrl).placeholder(R.drawable.ic_battle_calculator_menu).error(R.drawable.ic_battle_calculator_menu).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(binding.ivWildButton)
                binding.layoutMainContent.visibility = android.view.View.GONE
                binding.layoutRouteDetail.visibility = android.view.View.GONE
                binding.layoutPanelAddPokemon.visibility    = android.view.View.GONE
                binding.layoutPanelAddTrainer.visibility    = android.view.View.GONE
                binding.layoutPanelChooseTrainer.visibility = android.view.View.GONE
                binding.layoutWildRoutes.visibility = android.view.View.VISIBLE
            } else {
                binding.root.setBackgroundColor(requireContext().getColor(R.color.pokedex_red))
                Glide.with(requireContext()).load(SpriteUrls.wildPokemonMenuUrl).placeholder(R.drawable.ic_wild_pokemon_menu).error(R.drawable.ic_wild_pokemon_menu).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(binding.ivWildButton)
                binding.layoutPanelAddPokemon.visibility    = android.view.View.GONE
                binding.layoutPanelAddTrainer.visibility    = android.view.View.GONE
                binding.layoutPanelChooseTrainer.visibility = android.view.View.GONE
                binding.layoutMainContent.visibility = android.view.View.VISIBLE
                binding.layoutWildRoutes.visibility = android.view.View.GONE
                binding.layoutRouteDetail.visibility = android.view.View.GONE
            }
        }

        binding.btnAddPokemonA.setOnClickListener { showAddPokemonPanel(Team.TEAM_A) }
        binding.btnAddPlayerA.setOnClickListener { showAddTrainerPanel() }
        binding.btnChooseTrainerA.setOnClickListener { showChooseTrainerPanel() }
        binding.btnChooseEnemyB.setOnClickListener { showEnemyTrainerDialog() }
        binding.ivDeloadEnemy.setOnClickListener { deloadEnemyTrainer() }
        binding.ivDeloadPlayer.setOnClickListener { deloadPlayerTrainer() }

        binding.btnCalculate.setOnClickListener {
            if (teamAList.isEmpty() || teamBList.isEmpty()) {
                Toast.makeText(requireContext(), "Each team needs at least one Pokémon!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Battle only the two selected Pokémon
            val pokemonA = teamAList[activeIndexA]
            val pokemonB = teamBList[activeIndexB]
            val result = BattleCalculator.calculate(listOf(pokemonA), listOf(pokemonB))
            val resultFrag = ResultFragment.newInstance(result)

            // Team A trainer info
            val aTrainer = teamATrainer
            if (aTrainer != null) {
                resultFrag.teamALabel = aTrainer.name
                resultFrag.teamALabelIconUrl = SpriteUrls.avatarUrl(aTrainer.avatarId)
                resultFrag.teamATrainerImageUrl = SpriteUrls.playerTrainerImageUrl(aTrainer.avatarId)
            }

            // Team B trainer info
            val enemy = currentEnemyTrainer
            if (enemy != null) {
                resultFrag.teamBLabel = teamBLabel
                resultFrag.teamBLabelIconUrl = when (enemy) {
                    is EnemyTrainer.GymLeader     -> SpriteUrls.trainerIconUrl(enemy.id)
                    is EnemyTrainer.Champion      -> SpriteUrls.trainerIconUrl(enemy.nameEN.lowercase())
                    is EnemyTrainer.RandomTrainer -> SpriteUrls.trainerIconUrl("random")
                    is EnemyTrainer.WildPokemon   -> SpriteUrls.trainerIconUrl("wild")
                    is EnemyTrainer.SavedTrainer  -> SpriteUrls.avatarUrl(enemy.trainer.avatarId)
                }
                resultFrag.teamBTrainerImageUrl = when (enemy) {
                    is EnemyTrainer.GymLeader     -> SpriteUrls.gymLeaderImageUrl(enemy.id)
                    is EnemyTrainer.Champion      -> SpriteUrls.championImageUrl(enemy.nameEN)
                    is EnemyTrainer.WildPokemon   -> SpriteUrls.trainerIconUrl("wild")
                    is EnemyTrainer.RandomTrainer -> SpriteUrls.randomTrainerImageUrl()
                    is EnemyTrainer.SavedTrainer  -> SpriteUrls.playerTrainerImageUrl(enemy.trainer.avatarId)
                }
            }

            // Faint callbacks
            val capturedA = activeIndexA
            val capturedB = activeIndexB
            val capturedEnemy = currentEnemyTrainer
            resultFrag.teamAPokemonCount = teamAList.size
            resultFrag.teamBPokemonCount = teamBList.size
            resultFrag.onYouLost  = { applyFaintA(capturedA) }
            resultFrag.onYouWon   = {
                val allFainted = applyFaintB(capturedB)
                // Award gym badge if applicable
                val gym = capturedEnemy as? EnemyTrainer.GymLeader
                val badgeNum = gym?.let { gymLeaderBadgeMap[it.id] }
                if (badgeNum != null) {
                    val trainer = teamATrainer
                    if (trainer != null && badgeNum !in trainer.badges) {
                        val updated = trainer.copy(badges = trainer.badges + badgeNum)
                        teamATrainer = updated
                        val all = TrainerManager.loadTrainers(requireContext())
                        val idx = all.indexOfFirst { it.id == updated.id }
                        if (idx >= 0) { all[idx] = updated; TrainerManager.saveTrainers(requireContext(), all) }
                        updateBadgeDisplay()
                    }
                }
                allFainted
            }
            resultFrag.onAllFaintedA = { resetAllFainted() }
            resultFrag.onAllFaintedB = { resetAllFainted() }

            (activity as MainActivity).navigateToResults(resultFrag)
        }

        updateBattleButton()
        restoreTrainerDisplay()
    }

    private fun updateItemHeights() {
        fun apply(recycler: androidx.recyclerview.widget.RecyclerView, list: List<*>, adapter: PokemonListAdapter) {
            val h = recycler.height
            if (h <= 0 || list.isEmpty()) return
            val marginPx = (10 * resources.displayMetrics.density).toInt()
            val rows = maxOf(1, kotlin.math.ceil(list.size / 2.0).toInt())
            val itemH = h / rows - marginPx
            if (itemH > 0) {
                adapter.forcedItemHeight = itemH
                adapter.notifyDataSetChanged()
            }
        }
        apply(binding.recyclerTeamA, teamAList, adapterA)
        apply(binding.recyclerTeamB, teamBList, adapterB)
    }

    private fun resetAllFainted() {
        faintedIndicesA.clear()
        faintedIndicesB.clear()
        adapterA.faintedIndices = emptySet()
        adapterB.faintedIndices = emptySet()
        // Auto-select first available Pokémon
        if (teamAList.isNotEmpty()) { activeIndexA = 0; adapterA.activeIndex = 0 }
        if (teamBList.isNotEmpty()) { activeIndexB = 0; adapterB.activeIndex = 0 }
        updateBattleButton()
    }

    private fun applyFaintA(pokemonIndex: Int): Boolean {
        faintedIndicesA.add(pokemonIndex)
        adapterA.faintedIndices = faintedIndicesA.toSet()
        // Advance active to next non-fainted
        val next = (0 until teamAList.size).firstOrNull { it !in faintedIndicesA }
        if (next != null) { activeIndexA = next; adapterA.activeIndex = next }
        updateBattleButton()
        return faintedIndicesA.size >= teamAList.size
    }

    private fun applyFaintB(pokemonIndex: Int): Boolean {
        faintedIndicesB.add(pokemonIndex)
        adapterB.faintedIndices = faintedIndicesB.toSet()
        val next = (0 until teamBList.size).firstOrNull { it !in faintedIndicesB }
        if (next != null) { activeIndexB = next; adapterB.activeIndex = next }
        updateBattleButton()
        return faintedIndicesB.size >= teamBList.size
    }

    private fun setupWildRouteGrid() {
        val routes = com.pokemonbp.data.RouteData.loadRoutes(requireContext())
        val c = com.pokemonbp.data.ThemeManager.colorsFor(mainActivity?.currentTheme ?: AppTheme.COLORFUL)
        val legendary = routes.filter { it.isLegendary }
        val normal    = routes.filter { !it.isLegendary }

        binding.rvLegendaryRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLegendaryRoutes.adapter = WildRouteAdapter(legendary, c) { route ->
            handleRouteClick(route)
        }
        binding.rvNormalRoutes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNormalRoutes.adapter = WildRouteAdapter(normal, c) { route ->
            handleRouteClick(route)
        }

        binding.btnRandomRoute.setOnClickListener {
            val allRoutes = com.pokemonbp.data.RouteData.loadRoutes(requireContext())
            val route = allRoutes.randomOrNull() ?: return@setOnClickListener
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("🎲 Random Route")
                .setMessage(route.displayName)
                .setPositiveButton("OK", null)
                .show()
        }
        binding.btnRandomTown.setOnClickListener {
            val towns = loadTowns()
            val town = towns.randomOrNull() ?: return@setOnClickListener
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("🏙️ Random Town")
                .setMessage(town)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun handleRouteClick(route: com.pokemonbp.data.RouteLocation) {
        if (route.isLegendary && route.tiers.size > 1) {
            showBadgeTierPicker(route)
        } else if (route.tiers.size > 1) {
            showRouteDetailAllTiers(route)
        } else {
            val tier = route.tiers.firstOrNull()
            showRouteDetail(route.displayName, tier?.pokemon ?: emptyList())
        }
    }

    private fun showRouteDetailAllTiers(route: com.pokemonbp.data.RouteLocation) {
        binding.layoutWildRoutes.visibility = android.view.View.GONE
        binding.layoutRouteDetail.visibility = android.view.View.VISIBLE
        binding.tvRouteDetailName.text = route.displayName

        binding.tvRouteBack.setOnClickListener {
            binding.layoutRouteDetail.visibility = android.view.View.GONE
            binding.layoutWildRoutes.visibility = android.view.View.VISIBLE
        }

        val items = mutableListOf<RouteDetailItem>()
        for (tier in route.tiers) {
            items.add(RouteDetailItem.Header(tier.label))
            tier.pokemon.forEach { items.add(RouteDetailItem.PokemonRow(it)) }
        }

        val c = com.pokemonbp.data.ThemeManager.colorsFor(mainActivity?.currentTheme ?: com.pokemonbp.data.AppTheme.COLORFUL)
        val glm = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)
        glm.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =
                if (items[position] is RouteDetailItem.Header) 3 else 1
        }
        binding.rvRoutePokemon.layoutManager = glm
        binding.rvRoutePokemon.adapter = RoutePokemonDetailAdapter(items, c)
    }

    private fun showBadgeTierPicker(route: com.pokemonbp.data.RouteLocation) {
        val labels = route.tiers.map { it.label }.toTypedArray()
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(route.displayName)
            .setItems(labels) { _, index ->
                val tier = route.tiers[index]
                showRouteDetail("${route.displayName} — ${tier.label}", tier.pokemon)
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun applyTheme(theme: AppTheme) {
        val c = ThemeManager.colorsFor(theme)
        binding.screenPanel.setBackgroundColor(c.background)
        binding.tvAppTitle.setTextColor(android.graphics.Color.WHITE)
        binding.tvTeamALabel.setTextColor(c.teamA)
        binding.tvTeamBLabel.setTextColor(c.teamB)
        binding.divider.setBackgroundColor(c.divider)

        fun styleOutlined(btn: com.google.android.material.button.MaterialButton, color: Int) {
            btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            btn.strokeColor = ColorStateList.valueOf(color)
            btn.strokeWidth = 2
            btn.setTextColor(color)
            if (theme == AppTheme.RETRO) btn.typeface = Typeface.MONOSPACE
        }
        fun styleFilled(btn: com.google.android.material.button.MaterialButton, color: Int) {
            btn.backgroundTintList = ColorStateList.valueOf(color)
            btn.setTextColor(Color.WHITE)
            if (theme == AppTheme.RETRO) btn.typeface = Typeface.MONOSPACE
        }

        styleFilled(binding.btnAddPokemonA, c.teamA)
        styleOutlined(binding.btnAddPlayerA, c.teamA)
        styleOutlined(binding.btnChooseTrainerA, c.teamA)
        styleFilled(binding.btnChooseEnemyB, c.teamB)

        if (theme == AppTheme.RETRO) {
            binding.tvAppTitle.typeface = Typeface.MONOSPACE
            binding.tvTeamALabel.typeface = Typeface.MONOSPACE
            binding.tvTeamBLabel.typeface = Typeface.MONOSPACE
        }
    }

    private fun updateTeamALabel() {
        binding.tvTeamALabel.text = teamATrainer?.name ?: "Player"
    }

    private fun loadStaticIcons() {
        val ctx = requireContext()
        fun iv(url: String, view: android.widget.ImageView, fallback: Int) =
            Glide.with(ctx).load(url).placeholder(fallback).error(fallback).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(view)
        fun mb(url: String, btn: com.google.android.material.button.MaterialButton) =
            Glide.with(ctx).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                override fun onResourceReady(r: android.graphics.drawable.Drawable, t: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?) { btn.icon = r }
                override fun onLoadCleared(p: android.graphics.drawable.Drawable?) {}
            })
        iv(SpriteUrls.reloadUrl,         binding.ivReloadA,       R.drawable.ic_reload)
        iv(SpriteUrls.reloadUrl,         binding.ivReloadB,       R.drawable.ic_reload)
        iv(SpriteUrls.removeUrl,         binding.ivDeloadPlayer,  R.drawable.ic_remove)
        iv(SpriteUrls.removeUrl,         binding.ivDeloadEnemy,   R.drawable.ic_remove)
        iv(SpriteUrls.wildPokemonMenuUrl, binding.ivWildButton,   R.drawable.ic_wild_pokemon_menu)
        iv(SpriteUrls.playerUrl,         binding.ivLabelA,        R.drawable.ic_player)
        iv(SpriteUrls.battleUrl,         binding.ivLabelB,        R.drawable.ic_battle)
        mb(SpriteUrls.battleUrl,         binding.btnChooseEnemyB)
        mb(SpriteUrls.battleUrl,         binding.btnCalculate)
    }

    private fun loadLabelIcon(url: String?, imageView: android.widget.ImageView, fallbackRes: Int) {
        if (url == null) { imageView.setImageResource(fallbackRes); return }
        Glide.with(imageView.context)
            .load(url)
            .placeholder(fallbackRes)
            .error(fallbackRes)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(imageView)
    }

    private fun loadTrainerImage(url: String?, imageView: android.widget.ImageView) {
        if (url == null) {
            imageView.visibility = android.view.View.GONE
            return
        }
        imageView.visibility = android.view.View.VISIBLE
        Glide.with(imageView.context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .into(imageView)
    }

    private fun updateBadgeDisplay() {
        val trainer = teamATrainer
        if (trainer == null) {
            binding.llBadgesDisplay.visibility = android.view.View.GONE
            return
        }
        binding.llBadgesDisplay.visibility = android.view.View.VISIBLE
        badgeViews.forEachIndexed { idx, iv ->
            val badgeNum = idx + 1
            Glide.with(iv.context).load(SpriteUrls.badgeUrl(badgeNum)).placeholder(R.drawable.ic_badge_1).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(iv)
            iv.alpha = if (badgeNum in trainer.badges) 1f else 0.25f
        }
    }

    // ── Inline panels ──────────────────────────────────────────────────────────

    private fun allSubPanels() = listOf(
        binding.layoutPanelAddPokemon,
        binding.layoutPanelAddTrainer,
        binding.layoutPanelChooseTrainer,
        binding.layoutPanelSub)

    private fun switchToPanel(panel: android.widget.FrameLayout, title: String) {
        binding.tvAppTitle.text = title
        binding.layoutMainContent.visibility = android.view.View.GONE
        binding.layoutWildRoutes.visibility  = android.view.View.GONE
        binding.layoutRouteDetail.visibility = android.view.View.GONE
        allSubPanels().forEach { it.visibility = android.view.View.GONE }
        panel.visibility = android.view.View.VISIBLE
        binding.ivLens.isClickable = true
        binding.ivLens.setOnClickListener { popBack() }
    }

    private fun showPanel(panel: android.widget.FrameLayout, title: String) {
        panelBackStack.clear()
        switchToPanel(panel, title)
    }

    private fun rebuildSubPanelContent(title: String, buildContent: () -> Unit) {
        binding.layoutPanelSub.removeAllViews()
        buildContent()
        binding.tvAppTitle.text = title
        allSubPanels().forEach { it.visibility = android.view.View.GONE }
        binding.layoutPanelSub.visibility = android.view.View.VISIBLE
    }

    private fun pushSubPanel(title: String, buildContent: () -> Unit, onBack: () -> Unit) {
        panelBackStack.addLast(onBack)
        rebuildSubPanelContent(title, buildContent)
        binding.ivLens.isClickable = true
        binding.ivLens.setOnClickListener { popBack() }
    }

    private fun popBack() {
        if (panelBackStack.isNotEmpty()) {
            panelBackStack.removeLast().invoke()
        } else {
            hidePanels()
        }
    }

    private fun hidePanels() {
        panelBackStack.clear()
        binding.tvAppTitle.text = "BP Calculator"
        binding.ivLens.setOnClickListener(null)
        binding.ivLens.isClickable = false
        allSubPanels().forEach { it.visibility = android.view.View.GONE }
        binding.layoutMainContent.visibility = android.view.View.VISIBLE
    }

    private fun dpPx(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun buildBackRow(): android.widget.LinearLayout {
        val tv = android.widget.TextView(requireContext()).apply {
            text = "← Back"; textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#2983d3"))
            setTypeface(null, android.graphics.Typeface.BOLD)
            isClickable = true; isFocusable = true
            setOnClickListener { popBack() }
        }
        return android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dpPx(8), dpPx(4), dpPx(8), dpPx(4))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(tv)
        }
    }

    /** Removes the nested Pokédex shell (header + hinge + screen background) from a dialog
     *  layout inflated inline, so the content blends into the outer screen panel. */
    private fun stripDialogChrome(root: android.view.View) {
        root.setBackgroundResource(0)
        root.setPadding(0, 0, 0, 0)
        (root as? android.widget.LinearLayout)?.let { ll ->
            ll.getChildAt(0)?.visibility = android.view.View.GONE  // mini-header
            ll.getChildAt(1)?.visibility = android.view.View.GONE  // hinge divider
            ll.getChildAt(2)?.setBackgroundResource(0)              // screen_panel background
        }
    }

    private fun updateDeloadButton() {
        val loaded = currentEnemyTrainer != null
        val vis = if (loaded) android.view.View.VISIBLE else android.view.View.GONE
        binding.ivDeloadEnemy.visibility = vis
        binding.ivReloadB.visibility = vis
        binding.btnChooseEnemyB.visibility = if (loaded) android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun deloadEnemyTrainer() {
        currentEnemyTrainer = null
        teamBLabel = "Enemy Trainer"
        teamBList.clear()
        adapterB.notifyDataSetChanged()
        binding.tvTeamBLabel.text = teamBLabel
        Glide.with(requireContext()).load(SpriteUrls.battleUrl).placeholder(R.drawable.ic_battle).error(R.drawable.ic_battle).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(binding.ivLabelB)
        binding.ivTrainerB.visibility = android.view.View.GONE
        updateDeloadButton()
        updateBattleButton()
    }

    private fun updateDeloadPlayerButton() {
        val vis = if (teamATrainer != null) android.view.View.VISIBLE else android.view.View.GONE
        binding.ivDeloadPlayer.visibility = vis
        binding.ivReloadA.visibility = vis
    }

    private fun deloadPlayerTrainer() {
        teamATrainer = null
        teamAList.clear()
        faintedIndicesA.clear()
        adapterA.faintedIndices = emptySet()
        adapterA.notifyDataSetChanged()
        updateTeamALabel()
        Glide.with(requireContext()).load(SpriteUrls.playerUrl).placeholder(R.drawable.ic_player).error(R.drawable.ic_player).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(binding.ivLabelA)
        binding.ivTrainerA.visibility = android.view.View.GONE
        updateDeloadPlayerButton()
        updateBadgeDisplay()
        updateBattleButton()
    }

    // ── Add Pokémon panel ──────────────────────────────────────────────────────

    private fun showAddPokemonPanel(
        team: Team,
        onAdded: ((name: String, nameDE: String, types: List<com.pokemonbp.data.PokemonType>, bp: Int, pokedexId: Int) -> Unit)? = null
    ) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val b = com.pokemonbp.databinding.DialogAddPokemonBinding.inflate(layoutInflater)
        binding.layoutPanelAddPokemon.removeAllViews()
        binding.layoutPanelAddPokemon.addView(b.root, android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT))
        stripDialogChrome(b.root)
        val panelTitle = if (team == Team.TEAM_A) "Add Pokémon" else "Add Pokémon — Enemy"
        // onAdded != null means we're in sub-panel mode (called from trainer slot):
        // preserve the back-stack so lens can navigate back to Add Trainer
        if (onAdded != null) switchToPanel(binding.layoutPanelAddPokemon, panelTitle)
        else showPanel(binding.layoutPanelAddPokemon, panelTitle)

        val teamColor = if (team == Team.TEAM_A) c.teamA else c.teamB
        val selectedTypes = mutableSetOf<com.pokemonbp.data.PokemonType>()
        var currentPokedexId = 0
        var currentName = ""
        var currentNameDE = ""
        var selectedBP = -1

        b.tvTypesLabel.setTextColor(c.textSecondary)
        b.tvPresetsLabel.setTextColor(c.textSecondary)
        b.btnPickPokemon.backgroundTintList  = android.content.res.ColorStateList.valueOf(Color.parseColor("#CC0000"))
        b.btnPickPokemon.setTextColor(Color.WHITE)
        b.btnPickFromRoute.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CC0000"))
        b.btnPickFromRoute.setTextColor(Color.WHITE)
        b.btnSavePreset.setTextColor(c.accent)
        if (team == Team.TEAM_A) {
            b.btnAddPlayerPreset.visibility = android.view.View.VISIBLE
            b.btnAddPlayerPreset.setTextColor(teamColor)
            b.btnAddPlayerPreset.strokeColor = android.content.res.ColorStateList.valueOf(teamColor)
        } else {
            b.btnAddPlayerPreset.visibility = android.view.View.GONE
        }

        val bpBtns = listOf(b.bp1, b.bp2, b.bp3, b.bp4, b.bp5, b.bp6,
                            b.bp7, b.bp8, b.bp9, b.bp10, b.bp11, b.bp12)

        fun styleBp(btn: com.google.android.material.button.MaterialButton, selected: Boolean) {
            val red = Color.parseColor("#CC0000")
            if (selected) {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(red)
                btn.setTextColor(Color.WHITE); btn.strokeWidth = 0
            } else {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                btn.setTextColor(red)
                btn.strokeColor = android.content.res.ColorStateList.valueOf(red); btn.strokeWidth = 2
            }
        }
        fun selectBP(v: Int) { selectedBP = v; bpBtns.forEachIndexed { i, b2 -> styleBp(b2, i + 1 == v) } }
        bpBtns.forEachIndexed { i, btn ->
            styleBp(btn, false)
            if (theme == AppTheme.RETRO) btn.typeface = android.graphics.Typeface.MONOSPACE
            btn.setOnClickListener { selectBP(i + 1) }
        }

        val typeAdapter = TypeSelectionAdapter(com.pokemonbp.data.PokemonType.values().toList(), selectedTypes, theme) { type, sel ->
            if (sel) {
                if (selectedTypes.size >= 2) { Toast.makeText(requireContext(), "Max 2 types!", Toast.LENGTH_SHORT).show() }
                else selectedTypes.add(type)
            } else selectedTypes.remove(type)
        }
        b.recyclerTypes.layoutManager = GridLayoutManager(requireContext(), 3)
        b.recyclerTypes.adapter = typeAdapter

        fun applyEntry(entry: PokedexEntry) {
            currentName = entry.name; currentNameDE = entry.nameDE; currentPokedexId = entry.spriteId
            b.btnPickPokemon.text = "  ${entry.name}  #${entry.id}"
            b.tvDexId.text = "#${entry.id}"
            selectedTypes.clear(); selectedTypes.addAll(entry.types); typeAdapter.notifyDataSetChanged()
        }

        fun loadPresets() {
            val presets = com.pokemonbp.data.PresetManager.load(requireContext(), team)
            b.presetContainer.removeAllViews()
            b.tvPresetsLabel.text = if (presets.isEmpty())
                (if (team == Team.TEAM_A) "Player Presets (none yet)" else "Enemy Presets (none yet)")
            else (if (team == Team.TEAM_A) "Player Presets" else "Enemy Presets")
            for (preset in presets) {
                val chip = com.google.android.material.chip.Chip(requireContext())
                chip.text = preset.name.ifBlank { preset.types.joinToString("/") { it.displayName } }
                chip.isCheckable = false
                chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(teamColor)
                chip.setTextColor(Color.WHITE)
                chip.setOnClickListener {
                    currentName = preset.name; currentPokedexId = preset.pokedexId
                    b.btnPickPokemon.text = preset.name.ifBlank { preset.types.joinToString("/") { it.displayName } }
                    selectedTypes.clear(); selectedTypes.addAll(preset.types); typeAdapter.notifyDataSetChanged()
                    if (preset.pokedexId > 0) b.tvDexId.text = "#${preset.pokedexId}"
                }
                b.presetContainer.addView(chip)
            }
        }

        fun savePreset() {
            if (selectedTypes.isEmpty()) { Toast.makeText(requireContext(), "Pick types first!", Toast.LENGTH_SHORT).show(); return }
            val presets = com.pokemonbp.data.PresetManager.load(requireContext(), team)
            val np = com.pokemonbp.model.PokemonPreset(name = currentName, pokedexId = currentPokedexId, types = selectedTypes.toList())
            if (presets.none { it.name == np.name && it.types == np.types }) {
                presets.add(np); com.pokemonbp.data.PresetManager.save(requireContext(), team, presets)
                Toast.makeText(requireContext(), "Preset saved!", Toast.LENGTH_SHORT).show(); loadPresets()
            } else Toast.makeText(requireContext(), "Already saved!", Toast.LENGTH_SHORT).show()
        }

        loadPresets()

        val backToAddPokemon = { switchToPanel(binding.layoutPanelAddPokemon, panelTitle) }
        b.btnPickPokemon.setOnClickListener {
            showPokemonPickerInline(onBack = backToAddPokemon) { entry -> applyEntry(entry); switchToPanel(binding.layoutPanelAddPokemon, panelTitle) }
        }
        b.btnPickFromRoute.setOnClickListener {
            showRoutePickerInline(onBack = backToAddPokemon) { nameDE, nameEN, bp ->
                val entry = PokedexData.allPokemon.find { it.name.equals(nameEN.trim(), ignoreCase = true) }
                val types = entry?.types ?: emptyList()
                if (types.isNotEmpty() && bp > 0) {
                    if (onAdded != null) {
                        onAdded(nameEN, nameDE, types, bp, entry?.spriteId ?: 0)
                    } else {
                        val pokemon = Pokemon(id = System.currentTimeMillis().toInt(), name = nameEN, nameDE = nameDE,
                            types = types, baseBP = bp, team = team, pokedexId = entry?.spriteId ?: 0)
                        if (team == Team.TEAM_A) { teamAList.add(pokemon); adapterA.notifyItemInserted(teamAList.size - 1) }
                        else { teamBList.add(pokemon); adapterB.notifyItemInserted(teamBList.size - 1) }
                        updateBattleButton(); hidePanels()
                    }
                } else {
                    currentName = nameEN; currentNameDE = nameDE
                    val e2 = PokedexData.allPokemon.find { it.name.equals(nameEN, ignoreCase = true) }
                    if (e2 != null) { currentPokedexId = e2.spriteId; b.btnPickPokemon.text = "  $nameEN  #${e2.id}"; b.tvDexId.text = "#${e2.id}"; selectedTypes.clear(); selectedTypes.addAll(e2.types) }
                    else { currentPokedexId = 0; b.btnPickPokemon.text = "  $nameEN"; b.tvDexId.text = "" }
                    typeAdapter.notifyDataSetChanged(); if (bp > 0) selectBP(bp)
                    switchToPanel(binding.layoutPanelAddPokemon, panelTitle)
                }
            }
        }
        b.btnPickStarter.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#CC0000"))
        b.btnPickStarter.setTextColor(Color.WHITE)
        b.btnPickStarter.setOnClickListener {
            showStarterPickerInline(onBack = backToAddPokemon) { nameDE, nameEN ->
                val entry = PokedexData.allPokemon.find { it.name.equals(nameEN, ignoreCase = true) }
                val types = entry?.types ?: emptyList()
                if (types.isNotEmpty()) {
                    if (onAdded != null) {
                        onAdded(nameEN, nameDE, types, 3, entry?.spriteId ?: 0)
                    } else {
                        val pokemon = Pokemon(id = System.currentTimeMillis().toInt(), name = nameEN, nameDE = nameDE,
                            types = types, baseBP = 3, team = team, pokedexId = entry?.spriteId ?: 0)
                        if (team == Team.TEAM_A) { teamAList.add(pokemon); adapterA.notifyItemInserted(teamAList.size - 1) }
                        else { teamBList.add(pokemon); adapterB.notifyItemInserted(teamBList.size - 1) }
                        updateBattleButton(); hidePanels()
                    }
                } else {
                    currentName = nameEN; currentNameDE = nameDE; currentPokedexId = entry?.spriteId ?: 0
                    b.btnPickPokemon.text = if (currentPokedexId > 0) "  $nameEN  #$currentPokedexId" else "  $nameEN"
                    if (currentPokedexId > 0) b.tvDexId.text = "#$currentPokedexId"
                    selectedTypes.clear(); selectedTypes.addAll(entry?.types ?: emptyList())
                    typeAdapter.notifyDataSetChanged(); selectBP(3)
                    switchToPanel(binding.layoutPanelAddPokemon, panelTitle)
                }
            }
        }
        b.btnSavePreset.setOnClickListener { savePreset() }
        b.btnAddPlayerPreset.setOnClickListener { savePreset() }
        b.btnCancelPokemon.setOnClickListener { popBack() }
        b.btnAddPokemon.setOnClickListener {
            if (selectedTypes.isEmpty()) { Toast.makeText(requireContext(), "Pick a Pokémon or select types!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (selectedBP < 1) { Toast.makeText(requireContext(), "Select a BP value!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (onAdded != null) {
                onAdded(currentName, currentNameDE, selectedTypes.toList(), selectedBP, currentPokedexId)
            } else {
                val pokemon = Pokemon(id = System.currentTimeMillis().toInt(), name = currentName, nameDE = currentNameDE,
                    types = selectedTypes.toList(), baseBP = selectedBP, team = team, pokedexId = currentPokedexId)
                if (team == Team.TEAM_A) { teamAList.add(pokemon); adapterA.notifyItemInserted(teamAList.size - 1) }
                else { teamBList.add(pokemon); adapterB.notifyItemInserted(teamBList.size - 1) }
                updateBattleButton(); hidePanels()
            }
        }
    }

    // ── Add Trainer panel ──────────────────────────────────────────────────────

    private fun showAddTrainerPanel(
        existingTrainer: com.pokemonbp.model.PlayerTrainer? = null,
        onSaved: (com.pokemonbp.model.PlayerTrainer) -> Unit = { hidePanels() },
        onCancel: () -> Unit = { hidePanels() }
    ) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val v = layoutInflater.inflate(R.layout.dialog_add_player, binding.layoutPanelAddTrainer, false)
        v.layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
        binding.layoutPanelAddTrainer.removeAllViews()
        binding.layoutPanelAddTrainer.addView(v)
        stripDialogChrome(v)
        val addTrainerTitle = if (existingTrainer != null) "Edit Trainer" else "Add Trainer"
        showPanel(binding.layoutPanelAddTrainer, addTrainerTitle)

        var selAvatarId = existingTrainer?.avatarId ?: 1
        var selGender = existingTrainer?.gender ?: com.pokemonbp.model.TrainerGender.MALE
        val pokemonEntries = mutableListOf<TrainerPokemonEntry?>()
        var avatarPickerVisible = false
        var teamBodyVisible = false

        val etName               = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_trainer_name)
        val btnChooseAvatar      = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_choose_avatar)
        val layoutAvatar         = v.findViewById<android.widget.LinearLayout>(R.id.layout_avatar_picker)
        val tabLayout            = v.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_gender)
        val recyclerAvatars      = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_avatars)
        val btnConfirmAvatar     = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm_avatar)
        val btnSave              = v.findViewById<android.widget.Button>(R.id.btn_save_player)
        val btnCancel            = v.findViewById<android.widget.Button>(R.id.btn_cancel_player)
        val llTeamHeader         = v.findViewById<android.widget.LinearLayout>(R.id.ll_pokemon_team_header)
        val tvTeamToggle         = v.findViewById<android.widget.TextView>(R.id.tv_pokemon_team_toggle)
        val layoutTeamBody       = v.findViewById<android.widget.LinearLayout>(R.id.layout_pokemon_team_body)
        val llBadgeHeader        = v.findViewById<android.widget.LinearLayout>(R.id.ll_badge_header)
        val previewViews         = listOf(
            v.findViewById<android.widget.ImageView>(R.id.iv_team_preview_1),
            v.findViewById<android.widget.ImageView>(R.id.iv_team_preview_2),
            v.findViewById<android.widget.ImageView>(R.id.iv_team_preview_3),
            v.findViewById<android.widget.ImageView>(R.id.iv_team_preview_4))

        etName.setTextColor(c.textPrimary); etName.setHintTextColor(c.textSecondary)
        existingTrainer?.let { etName.setText(it.name) }
        btnChooseAvatar.strokeColor = android.content.res.ColorStateList.valueOf(c.accent)
        btnChooseAvatar.setTextColor(c.accent)
        btnConfirmAvatar.backgroundTintList = android.content.res.ColorStateList.valueOf(c.accent)
        btnConfirmAvatar.setTextColor(Color.WHITE)
        btnSave.text = if (existingTrainer != null) "Save Changes" else "Save Trainer"

        val avatarAdapter = AvatarAdapter(TrainerManager.maleAvatars, selAvatarId, c) { avatar ->
            selAvatarId = avatar.id; selGender = avatar.gender
            val num = if (avatar.gender == com.pokemonbp.model.TrainerGender.MALE) avatar.id else avatar.id - 100
            btnChooseAvatar.text = "👤 Character ${if (avatar.gender == com.pokemonbp.model.TrainerGender.MALE) "♂" else "♀"} #$num selected"
        }
        recyclerAvatars.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerAvatars.adapter = avatarAdapter

        tabLayout.addTab(tabLayout.newTab().setText("♂ Male"))
        tabLayout.addTab(tabLayout.newTab().setText("♀ Female"))
        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                val list = if (tab.position == 0) TrainerManager.maleAvatars else TrainerManager.femaleAvatars
                selGender = if (tab.position == 0) com.pokemonbp.model.TrainerGender.MALE else com.pokemonbp.model.TrainerGender.FEMALE
                avatarAdapter.updateList(list); selAvatarId = list.first().id
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
        btnChooseAvatar.setOnClickListener {
            avatarPickerVisible = !avatarPickerVisible
            layoutAvatar.visibility = if (avatarPickerVisible) android.view.View.VISIBLE else android.view.View.GONE
            btnChooseAvatar.text = if (avatarPickerVisible) "▲ Close Character Picker" else "👤 Choose Character"
        }
        btnConfirmAvatar.setOnClickListener {
            avatarPickerVisible = false; layoutAvatar.visibility = android.view.View.GONE
            val num = if (selGender == com.pokemonbp.model.TrainerGender.MALE) selAvatarId else selAvatarId - 100
            btnChooseAvatar.text = "👤 Character ${if (selGender == com.pokemonbp.model.TrainerGender.MALE) "♂" else "♀"} #$num — tap to change"
        }
        llTeamHeader.setOnClickListener {
            teamBodyVisible = !teamBodyVisible
            layoutTeamBody.visibility = if (teamBodyVisible) android.view.View.VISIBLE else android.view.View.GONE
            tvTeamToggle.text = if (teamBodyVisible) "▲ Pokémon Team" else "▼ Pokémon Team"
        }
        llBadgeHeader.setOnClickListener {
            val imgView = android.widget.ImageView(requireContext())
            Glide.with(requireContext()).load(SpriteUrls.badgeCaseEmptyUrl).placeholder(R.drawable.ic_badge_case_empty).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(imgView)
            imgView.adjustViewBounds = true
            imgView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            android.app.AlertDialog.Builder(requireContext())
                .setView(imgView)
                .setPositiveButton("Close", null)
                .show()
        }

        existingTrainer?.pokemon?.forEach { pokemonEntries.add(TrainerPokemonEntry(preset = it, bp = it.baseBP)) }
        while (pokemonEntries.size < 4) pokemonEntries.add(null)

        val slots = listOf(
            v.findViewById<android.widget.FrameLayout>(R.id.slot_0),
            v.findViewById<android.widget.FrameLayout>(R.id.slot_1),
            v.findViewById<android.widget.FrameLayout>(R.id.slot_2),
            v.findViewById<android.widget.FrameLayout>(R.id.slot_3))

        var refreshSlots: () -> Unit = {}
        var openPicker: (Int, Int) -> Unit = { _, _ -> }

        openPicker = { i, _ ->
            // Push "back to Add Trainer" so the lens navigates correctly
            panelBackStack.addLast { switchToPanel(binding.layoutPanelAddTrainer, addTrainerTitle) }
            showAddPokemonPanel(Team.TEAM_A, onAdded = { name, nameDE, types, bp, pokedexId ->
                pokemonEntries[i] = TrainerPokemonEntry(
                    preset = com.pokemonbp.model.PokemonPreset(
                        name = name, nameDE = nameDE,
                        pokedexId = pokedexId, types = types, baseBP = bp),
                    bp = bp)
                refreshSlots()
                // Pop the "back to trainer" entry we added, then go back to trainer
                if (panelBackStack.isNotEmpty()) panelBackStack.removeLast()
                switchToPanel(binding.layoutPanelAddTrainer, addTrainerTitle)
            })
        }

        refreshSlots = {
            slots.forEachIndexed { i, frame ->
                frame.removeAllViews()
                layoutInflater.inflate(R.layout.item_slot, frame, true)
                val flEmpty  = frame.findViewById<android.widget.FrameLayout>(R.id.fl_empty)
                val llFilled = frame.findViewById<android.widget.LinearLayout>(R.id.ll_filled)
                val ivSprite = frame.findViewById<android.widget.ImageView>(R.id.iv_slot_sprite)
                val llTypes  = frame.findViewById<android.widget.LinearLayout>(R.id.ll_slot_types)
                val tvName     = frame.findViewById<android.widget.TextView>(R.id.tv_slot_name)
                val tvBpValue  = frame.findViewById<android.widget.TextView>(R.id.tv_slot_bp_value)
                val btnBpMinus = frame.findViewById<android.widget.TextView>(R.id.btn_slot_bp_minus)
                val btnBpPlus  = frame.findViewById<android.widget.TextView>(R.id.btn_slot_bp_plus)
                val btnRemove= frame.findViewById<android.widget.ImageButton>(R.id.btn_slot_remove)
                val btnEvolve= frame.findViewById<android.widget.ImageButton>(R.id.btn_slot_evolve)
                val btnDusk  = frame.findViewById<android.widget.ImageButton>(R.id.btn_slot_duskstone)
                val entry = pokemonEntries[i]
                if (entry == null) {
                    flEmpty.visibility = android.view.View.VISIBLE; llFilled.visibility = android.view.View.GONE
                    frame.setOnClickListener { openPicker(i, 1) }
                } else {
                    flEmpty.visibility = android.view.View.GONE; llFilled.visibility = android.view.View.VISIBLE
                    val preset = entry.preset
                    ivSprite.loadPokemonSprite(requireContext(), preset.pokedexId)
                    llTypes.removeAllViews()
                    preset.types.forEach { type ->
                        val cell = android.widget.FrameLayout(requireContext())
                        cell.layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
                        val icon = android.widget.ImageView(requireContext())
                        icon.layoutParams = android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
                        icon.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        icon.adjustViewBounds = true
                        Glide.with(requireContext())
                            .load(SpriteUrls.typeIconUrl(type.name))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(icon)
                        cell.addView(icon)
                        llTypes.addView(cell)
                    }
                    tvName.text = if (preset.nameDE == preset.name) preset.name else "${preset.nameDE} / ${preset.name}"
                    tvBpValue.text = "${entry.bp}"
                    btnBpMinus.setOnClickListener { pokemonEntries[i] = entry.copy(bp = if (entry.bp <= 1) 12 else entry.bp - 1); refreshSlots() }
                    btnBpPlus.setOnClickListener { pokemonEntries[i] = entry.copy(bp = if (entry.bp >= 12) 1 else entry.bp + 1); refreshSlots() }
                    Glide.with(requireContext()).load(SpriteUrls.garbageBinUrl).placeholder(R.drawable.ic_garbage_bin).error(R.drawable.ic_garbage_bin).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(btnRemove)
                    btnRemove.setOnClickListener { pokemonEntries[i] = null; refreshSlots() }
                    Glide.with(requireContext()).load(SpriteUrls.dawnstoneUrl).placeholder(R.drawable.ic_dawnstone).error(R.drawable.ic_dawnstone).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(btnEvolve)
                    val nextEvos = com.pokemonbp.data.EvolutionData.nextEvolutions(preset.pokedexId)
                    btnEvolve.isEnabled = nextEvos.isNotEmpty(); btnEvolve.alpha = if (nextEvos.isNotEmpty()) 1f else 0.3f
                    btnEvolve.setOnClickListener {
                        if (nextEvos.size == 1) {
                            val evo = PokedexData.allPokemon.find { it.id == nextEvos[0] } ?: return@setOnClickListener
                            pokemonEntries[i] = entry.copy(preset = preset.copy(name = evo.name, nameDE = evo.nameDE, pokedexId = evo.spriteId, types = evo.types)); refreshSlots()
                        } else {
                            val popup = android.widget.PopupMenu(requireContext(), btnEvolve)
                            nextEvos.forEach { evoId ->
                                val evo = PokedexData.allPokemon.find { it.id == evoId }
                                popup.menu.add(evo?.let { "${it.nameDE} / ${it.name}" } ?: "#$evoId").setOnMenuItemClickListener {
                                    if (evo != null) { pokemonEntries[i] = entry.copy(preset = preset.copy(name = evo.name, nameDE = evo.nameDE, pokedexId = evo.spriteId, types = evo.types)); refreshSlots() }; true
                                }
                            }; popup.show()
                        }
                    }
                    Glide.with(requireContext()).load(SpriteUrls.duskstoneUrl).placeholder(R.drawable.ic_duskstone).error(R.drawable.ic_duskstone).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(btnDusk)
                    val prevId = com.pokemonbp.data.EvolutionData.previousEvolution(preset.pokedexId)
                    btnDusk.isEnabled = prevId != null; btnDusk.alpha = if (prevId != null) 1f else 0.3f
                    btnDusk.setOnClickListener {
                        val prev = prevId?.let { id -> PokedexData.allPokemon.find { it.id == id } }
                        if (prev != null) { pokemonEntries[i] = entry.copy(preset = preset.copy(name = prev.name, nameDE = prev.nameDE, pokedexId = prev.spriteId, types = prev.types)); refreshSlots() }
                        else Toast.makeText(requireContext(), "Already at base form!", Toast.LENGTH_SHORT).show()
                    }
                    ivSprite.setOnClickListener { openPicker(i, entry.bp) }
                    tvName.setOnClickListener { openPicker(i, entry.bp) }
                }
            }
            // Update preview sprites in the collapsed header
            pokemonEntries.forEachIndexed { i, entry ->
                val preview = previewViews.getOrNull(i) ?: return@forEachIndexed
                if (entry != null) {
                    preview.loadPokemonSprite(requireContext(), entry.preset.pokedexId)
                } else {
                    Glide.with(requireContext()).load(SpriteUrls.pokeballUrl).placeholder(R.drawable.ic_pokeball_empty).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(preview)
                }
            }
        }
        refreshSlots()

        btnCancel.setOnClickListener { onCancel() }
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) { etName.error = "Enter a trainer name"; return@setOnClickListener }
            val trainer = com.pokemonbp.model.PlayerTrainer(
                id = existingTrainer?.id ?: java.util.UUID.randomUUID().toString(),
                name = name, avatarId = selAvatarId, gender = selGender,
                pokemon = pokemonEntries.filterNotNull().map { it.preset.copy(baseBP = it.bp) })
            val all = TrainerManager.loadTrainers(requireContext())
            if (existingTrainer == null) { all.add(trainer) }
            else { val idx = all.indexOfFirst { it.id == trainer.id }; if (idx >= 0) all[idx] = trainer else all.add(trainer) }
            TrainerManager.saveTrainers(requireContext(), all)
            if (existingTrainer == null) Toast.makeText(requireContext(), "Trainer '${trainer.name}' saved!", Toast.LENGTH_SHORT).show()
            onSaved(trainer)
        }
    }

    // ── Choose Trainer panel ───────────────────────────────────────────────────

    private fun showChooseTrainerPanel() {
        val trainers = TrainerManager.loadTrainers(requireContext())
        if (trainers.isEmpty()) {
            Toast.makeText(requireContext(), "No trainers saved yet! Use 'Add Trainer' first.", Toast.LENGTH_SHORT).show()
            return
        }
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val v = layoutInflater.inflate(R.layout.dialog_choose_trainer, binding.layoutPanelChooseTrainer, false)
        v.layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
        binding.layoutPanelChooseTrainer.removeAllViews()
        binding.layoutPanelChooseTrainer.addView(v)
        stripDialogChrome(v)
        showPanel(binding.layoutPanelChooseTrainer, "Choose Trainer")

        val recycler = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_trainer_chooser)
        // Expand recycler to fill remaining height
        (recycler.layoutParams as? android.widget.LinearLayout.LayoutParams)?.let {
            it.height = 0; it.weight = 1f; recycler.layoutParams = it
        }
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        fun reloadList() {
            val all = TrainerManager.loadTrainers(requireContext())
            recycler.adapter = TrainerRowAdapter(
                trainers = all.toMutableList(), theme = theme, c = c,
                onBattle = { trainer ->
                    teamATrainer = trainer; teamAList.clear()
                    // Reset fainted state when trainer is (re)loaded
                    faintedIndicesA.clear(); adapterA.faintedIndices = emptySet()
                    trainer.pokemon.forEach { preset ->
                        teamAList.add(Pokemon(id = System.currentTimeMillis().toInt() + teamAList.size,
                            name = preset.name, nameDE = preset.nameDE,
                            types = preset.types, baseBP = preset.baseBP,
                            team = Team.TEAM_A, pokedexId = preset.pokedexId))
                    }
                    activeIndexA = 0; adapterA.activeIndex = 0; adapterA.notifyDataSetChanged()
                    updateTeamALabel()
                    updateDeloadPlayerButton()
                    updateBadgeDisplay()
                    loadLabelIcon(SpriteUrls.avatarUrl(trainer.avatarId), binding.ivLabelA, R.drawable.ic_player)
                    loadTrainerImage(SpriteUrls.playerTrainerImageUrl(trainer.avatarId), binding.ivTrainerA)
                    updateBattleButton()
                    Toast.makeText(requireContext(), "${trainer.name}'s team loaded!", Toast.LENGTH_SHORT).show()
                    hidePanels()
                },
                onManage = { trainer ->
                    showAddTrainerPanel(
                        existingTrainer = trainer,
                        onSaved = { showChooseTrainerPanel() },
                        onCancel = { showChooseTrainerPanel() })
                },
                onDelete = { trainer ->
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Delete Trainer")
                        .setMessage("Remove \"${trainer.name}\" permanently?")
                        .setPositiveButton("Delete") { _, _ ->
                            val all2 = TrainerManager.loadTrainers(requireContext())
                            all2.removeAll { it.id == trainer.id }
                            TrainerManager.saveTrainers(requireContext(), all2)
                            reloadList()
                        }
                        .setNegativeButton("Cancel", null).show()
                })
        }
        reloadList()
        v.findViewById<android.widget.Button>(R.id.btn_close_dialog).setOnClickListener { hidePanels() }
    }

    private fun handleEnemySelected(enemyTrainer: EnemyTrainer, badge: Int?) {
        currentEnemyTrainer = enemyTrainer
        teamBList.clear()
        faintedIndicesB.clear(); adapterB.faintedIndices = emptySet()
        val trainerImageUrl: String?
        when (enemyTrainer) {
            is EnemyTrainer.GymLeader -> {
                val b = badge ?: 1
                val team = enemyTrainer.badgeTeams[b]
                    ?: enemyTrainer.badgeTeams.values.firstOrNull() ?: emptyList()
                teamBLabel = "${enemyTrainer.nameDE} / ${enemyTrainer.nameEN} (Badge $b)"
                team.forEach { gp ->
                    teamBList.add(Pokemon(
                        id = System.currentTimeMillis().toInt() + teamBList.size,
                        name = gp.nameEN, nameDE = gp.nameDE,
                        types = gp.types, baseBP = gp.baseBP,
                        team = Team.TEAM_B, pokedexId = gp.pokedexId
                    ))
                }
                trainerImageUrl = SpriteUrls.gymLeaderImageUrl(enemyTrainer.id)
            }
            is EnemyTrainer.Champion -> {
                teamBLabel = "Champion ${enemyTrainer.nameEN}"
                enemyTrainer.team.forEach { gp ->
                    teamBList.add(Pokemon(
                        id = System.currentTimeMillis().toInt() + teamBList.size,
                        name = gp.nameEN, nameDE = gp.nameDE,
                        types = gp.types, baseBP = gp.baseBP,
                        team = Team.TEAM_B, pokedexId = gp.pokedexId
                    ))
                }
                trainerImageUrl = SpriteUrls.championImageUrl(enemyTrainer.nameEN)
            }
            is EnemyTrainer.WildPokemon -> {
                teamBLabel = "Wild Pokémon"
                trainerImageUrl = SpriteUrls.trainerIconUrl("wild")
            }
            is EnemyTrainer.RandomTrainer -> {
                teamBLabel = "Random Trainer"
                trainerImageUrl = SpriteUrls.randomTrainerImageUrl()
            }
            is EnemyTrainer.SavedTrainer -> {
                teamBLabel = enemyTrainer.trainer.name
                enemyTrainer.trainer.pokemon.forEach { preset ->
                    teamBList.add(Pokemon(
                        id = System.currentTimeMillis().toInt() + teamBList.size,
                        name = preset.name, nameDE = preset.nameDE,
                        types = preset.types, baseBP = preset.baseBP,
                        team = Team.TEAM_B, pokedexId = preset.pokedexId
                    ))
                }
                trainerImageUrl = SpriteUrls.playerTrainerImageUrl(enemyTrainer.trainer.avatarId)
            }
        }
        val labelIconUrl = when (enemyTrainer) {
            is EnemyTrainer.GymLeader     -> SpriteUrls.trainerIconUrl(enemyTrainer.id)
            is EnemyTrainer.Champion      -> SpriteUrls.trainerIconUrl(enemyTrainer.nameEN.lowercase())
            is EnemyTrainer.RandomTrainer -> SpriteUrls.trainerIconUrl("random")
            is EnemyTrainer.WildPokemon   -> SpriteUrls.trainerIconUrl("wild")
            is EnemyTrainer.SavedTrainer  -> SpriteUrls.avatarUrl(enemyTrainer.trainer.avatarId)
        }
        activeIndexB = 0
        adapterB.activeIndex = 0
        adapterB.notifyDataSetChanged()
        binding.tvTeamBLabel.text = teamBLabel
        loadLabelIcon(labelIconUrl ?: SpriteUrls.battleUrl, binding.ivLabelB, R.drawable.ic_battle)
        loadTrainerImage(trainerImageUrl, binding.ivTrainerB)
        updateBattleButton()
        updateDeloadButton()
    }

    private fun showEnemyTrainerDialog() {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)

        fun buildEnemyGrid() {
            val view = layoutInflater.inflate(R.layout.dialog_enemy_trainer, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            view.findViewById<android.widget.LinearLayout>(R.id.screen_panel)?.setBackgroundColor(c.surface)

            val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_enemy_options)
            recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)

            val options: List<EnemyOption> = buildList {
                com.pokemonbp.data.TrainerParser.loadGymLeaders(requireContext()).forEach { add(EnemyOption.GymLeaderOption(it)) }
                add(EnemyOption.ChampionMenu)
                add(EnemyOption.WildOption)
                add(EnemyOption.RandomOption)
                add(EnemyOption.SavedTrainerMenu)
            }

            recycler.adapter = EnemyGridAdapter(options, c) { option ->
                when (option) {
                    is EnemyOption.GymLeaderOption -> showBadgePickerInline(option.gym,
                        onBack = { rebuildSubPanelContent("Enemy Trainer") { buildEnemyGrid() } })
                    is EnemyOption.ChampionMenu -> showChampionPickerInline(
                        onBack = { rebuildSubPanelContent("Enemy Trainer") { buildEnemyGrid() } })
                    is EnemyOption.WildOption -> {
                        handleEnemySelected(EnemyTrainer.WildPokemon, null)
                        showAddPokemonPanel(Team.TEAM_B)
                    }
                    is EnemyOption.RandomOption -> {
                        handleEnemySelected(EnemyTrainer.RandomTrainer, null)
                        showAddPokemonPanel(Team.TEAM_B)
                    }
                    is EnemyOption.SavedTrainerMenu -> showSavedTrainerInline(
                        onBack = { rebuildSubPanelContent("Enemy Trainer") { buildEnemyGrid() } })
                    is EnemyOption.ChampionOption -> { /* not shown in root grid */ }
                }
            }

            view.findViewById<android.widget.Button>(R.id.btn_close_dialog)?.setOnClickListener { hidePanels() }
            binding.layoutPanelSub.addView(view)
        }

        binding.layoutPanelSub.removeAllViews()
        buildEnemyGrid()
        showPanel(binding.layoutPanelSub, "Enemy Trainer")
    }

    // ── Inline sub-panel builders ──────────────────────────────────────────────

    private fun showPokemonPickerInline(onBack: () -> Unit, onPicked: (PokedexEntry) -> Unit) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)                                                                                                               
        pushSubPanel("Pokédex", buildContent = {
            val view = layoutInflater.inflate(R.layout.dialog_pokemon_picker, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            val sp = view.findViewById<android.widget.LinearLayout>(R.id.screen_panel)
            sp?.setBackgroundColor(c.surface)
            sp?.addView(buildBackRow(), 0)

            val filteredList = PokedexData.allPokemon.toMutableList()
            val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_picker)
            val etSearch = view.findViewById<android.widget.EditText>(R.id.et_picker_search)
            val tvTitle  = view.findViewById<android.widget.TextView>(R.id.tv_picker_title)

            tvTitle?.setTextColor(c.textPrimary)
            if (theme == AppTheme.RETRO) tvTitle?.typeface = android.graphics.Typeface.MONOSPACE
            etSearch.setTextColor(c.textPrimary)
            etSearch.setHintTextColor(c.textSecondary)
            etSearch.setBackgroundColor(c.surfaceVariant)

            val adapter = PickerAdapter(filteredList, theme) { entry -> onPicked(entry) }
            recycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            recycler.adapter = adapter

            etSearch.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    val q = s.toString().trim().lowercase()
                    filteredList.clear()
                    filteredList.addAll(if (q.isEmpty()) PokedexData.allPokemon
                    else PokedexData.allPokemon.filter {
                        it.name.lowercase().contains(q) || it.nameDE.lowercase().contains(q) ||
                        it.id.toString().contains(q) || it.types.any { t -> t.displayName.lowercase().contains(q) }
                    })
                    adapter.notifyDataSetChanged()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            view.findViewById<android.widget.Button>(R.id.btn_close_picker).setOnClickListener { popBack() }
            binding.layoutPanelSub.addView(view)
        }, onBack = onBack)
    }

    private fun showRoutePickerInline(onBack: () -> Unit, onPicked: (nameDE: String, nameEN: String, bp: Int) -> Unit) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val routes = com.pokemonbp.data.RouteData.loadRoutes(requireContext())

        fun buildRouteGrid() {
            val view = layoutInflater.inflate(R.layout.dialog_route_picker, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            val spRoute = view.findViewById<android.widget.LinearLayout>(R.id.screen_panel)
            spRoute?.setBackgroundColor(c.surface)
            spRoute?.addView(buildBackRow(), 0)

            val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_routes)
            recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)

            fun handleRoute(location: com.pokemonbp.data.RouteLocation) {
                if (!location.isLegendary) {
                    showRoutePokemonGridInline(location.displayName, location.tiers[0].pokemon,
                        onBack = { rebuildSubPanelContent("Route") { buildRouteGrid() } },
                        onPicked = { p -> onPicked(p.nameDE, p.nameEN, p.bp) })
                } else {
                    showRouteTierPickerInline(location,
                        onBack = { rebuildSubPanelContent("Route") { buildRouteGrid() } },
                        onPicked = onPicked)
                }
            }

            recycler.adapter = RouteGridAdapter(routes, c,
                onRouteClick = { location -> handleRoute(location) },
                onRandomClick = { handleRoute(routes.random()) })

            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_close_route)
                .setOnClickListener { popBack() }
            binding.layoutPanelSub.addView(view)
        }

        pushSubPanel("Route", buildContent = { buildRouteGrid() }, onBack = onBack)
    }

    private fun showRouteTierPickerInline(
        location: com.pokemonbp.data.RouteLocation,
        onBack: () -> Unit,
        onPicked: (nameDE: String, nameEN: String, bp: Int) -> Unit
    ) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)

        fun buildTierPicker() {
            val container = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dpPx(16), dpPx(10), dpPx(16), dpPx(10))
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            }
            container.addView(buildBackRow())
            android.widget.TextView(requireContext()).apply {
                text = location.displayName; textSize = 13f
                setTextColor(c.textPrimary); gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, dpPx(14))
                container.addView(this)
            }
            for (tier in location.tiers) {
                com.google.android.material.button.MaterialButton(requireContext()).apply {
                    text = tier.label; isAllCaps = false; textSize = 14f
                    setTextColor(android.graphics.Color.WHITE)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CC0000"))
                    cornerRadius = dpPx(22)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpPx(48)
                    ).also { it.bottomMargin = dpPx(8) }
                    setOnClickListener {
                        showRoutePokemonGridInline("${location.displayName} — ${tier.label}", tier.pokemon,
                            onBack = { rebuildSubPanelContent(location.displayName) { buildTierPicker() } },
                            onPicked = { p -> onPicked(p.nameDE, p.nameEN, p.bp) })
                    }
                    container.addView(this)
                }
            }
            binding.layoutPanelSub.addView(container)
        }

        pushSubPanel(location.displayName, buildContent = { buildTierPicker() }, onBack = onBack)
    }

    private fun showRoutePokemonGridInline(
        title: String,
        pokemon: List<com.pokemonbp.data.RoutePokemon>,
        onBack: () -> Unit,
        onPicked: (com.pokemonbp.data.RoutePokemon) -> Unit
    ) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        pushSubPanel(title, buildContent = {
            val view = layoutInflater.inflate(R.layout.dialog_route_pokemon_grid, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            val spGrid = view.findViewById<android.widget.LinearLayout>(R.id.screen_panel_grid)
            spGrid?.setBackgroundColor(c.surface)
            spGrid?.addView(buildBackRow(), 0)
            view.findViewById<android.widget.TextView>(R.id.tv_pokemon_grid_title)?.visibility = android.view.View.GONE

            val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_pokemon_grid)
            recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
            recycler.adapter = RoutePokemonGridAdapter(pokemon, c) { p -> onPicked(p) }

            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_back_pokemon_grid)
                .setOnClickListener { popBack() }
            binding.layoutPanelSub.addView(view)
        }, onBack = onBack)
    }

    private fun showStarterPickerInline(onBack: () -> Unit, onPicked: (nameDE: String, nameEN: String) -> Unit) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)

        fun buildTypeSelection() {
            val container = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dpPx(16), dpPx(16), dpPx(16), dpPx(12))
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            }
            container.addView(buildBackRow())
            android.widget.TextView(requireContext()).apply {
                text = "Choose Starter Type"; textSize = 14f
                setTextColor(c.textPrimary); gravity = android.view.Gravity.CENTER
                setPadding(0, 0, 0, dpPx(14))
                container.addView(this, android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT))
            }
            val typeRow = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
            }
            data class TypeOpt(val label: String, val file: String, val key: String)
            val types = listOf(TypeOpt("Grass","Starter_Grass.txt","GRASS"), TypeOpt("Water","Starter_Water.txt","WATER"), TypeOpt("Fire","Starter_Fire.txt","FIRE"))
            types.forEachIndexed { idx, opt ->
                val btn = android.widget.LinearLayout(requireContext()).apply {
                    orientation = android.widget.LinearLayout.VERTICAL; gravity = android.view.Gravity.CENTER
                    isClickable = true; isFocusable = true
                    val tv = android.util.TypedValue()
                    requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                    foreground = requireContext().getDrawable(tv.resourceId)
                    setPadding(dpPx(8), dpPx(8), dpPx(8), dpPx(8))
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, dpPx(80)).also {
                        it.weight = 1f; if (idx < types.size - 1) it.marginEnd = dpPx(4)
                    }
                }
                android.widget.ImageView(requireContext()).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(dpPx(48), dpPx(48))
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER; adjustViewBounds = true
                    Glide.with(requireContext()).load(SpriteUrls.typeIconUrl(opt.key))
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
                    btn.addView(this)
                }
                android.widget.TextView(requireContext()).apply {
                    text = opt.label; textSize = 12f; setTextColor(c.textPrimary)
                    gravity = android.view.Gravity.CENTER; setPadding(0, dpPx(4), 0, 0)
                    btn.addView(this, android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT))
                }
                btn.setOnClickListener {
                    val lines = parseStarterLines(opt.file)
                    showStarterLinesInline(opt.label, opt.key, lines,
                        onBack = { rebuildSubPanelContent("Starter") { buildTypeSelection() } },
                        onPicked = onPicked)
                }
                typeRow.addView(btn)
            }
            container.addView(typeRow, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT))
            binding.layoutPanelSub.addView(container)
        }

        pushSubPanel("Starter", buildContent = { buildTypeSelection() }, onBack = onBack)
    }

    private fun parseStarterLines(filename: String): List<StarterLine> {
        return try {
            val text = requireContext().assets.open(filename).bufferedReader().readText()
            val pokemonLines = text.lines().map { it.trim() }
                .filter { it.contains('/') && !it.trimEnd().endsWith(':') }
            pokemonLines.chunked(3).mapNotNull { group ->
                if (group.isEmpty()) null else {
                    fun parse(s: String): StarterPokemon {
                        val parts = s.split("/", limit = 2)
                        return StarterPokemon(parts[0].trim(), parts.getOrElse(1) { "" }.trim())
                    }
                    StarterLine(parse(group[0]), group.getOrNull(1)?.let { parse(it) }, group.getOrNull(2)?.let { parse(it) })
                }
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun showStarterLinesInline(
        typeName: String, typeKey: String,
        lines: List<StarterLine>,
        onBack: () -> Unit,
        onPicked: (nameDE: String, nameEN: String) -> Unit
    ) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)

        fun buildLines() {
            val outerContainer = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            }
            // Header: back + type icon + title
            val headerRow = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dpPx(8), dpPx(8), dpPx(8), dpPx(8))
            }
            headerRow.addView(buildBackRow().also {
                it.layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            })
            android.widget.ImageView(requireContext()).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(dpPx(26), dpPx(26)).also { it.marginEnd = dpPx(6) }
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                Glide.with(requireContext()).load(SpriteUrls.typeIconUrl(typeKey))
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
                headerRow.addView(this)
            }
            android.widget.TextView(requireContext()).apply {
                text = "$typeName Starters"; textSize = 13f; setTextColor(c.textPrimary)
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
                    .also { it.weight = 1f }
                headerRow.addView(this)
            }
            outerContainer.addView(headerRow, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT))
            // Divider
            outerContainer.addView(android.view.View(requireContext()).apply {
                setBackgroundColor(android.graphics.Color.parseColor("#33FFFFFF"))
            }, android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpPx(1)))

            val scrollView = android.widget.ScrollView(requireContext())
            val innerContainer = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dpPx(10), dpPx(8), dpPx(10), dpPx(8))
            }
            scrollView.addView(innerContainer, android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT))

            lines.forEach { line ->
                val lineRow = android.widget.LinearLayout(requireContext()).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = dpPx(6) }
                }
                val allPokemon = listOfNotNull(line.base, line.evo2, line.evo3)
                allPokemon.forEachIndexed { index, pokemon ->
                    val isBase = index == 0
                    val col = android.widget.LinearLayout(requireContext()).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        gravity = android.view.Gravity.CENTER_HORIZONTAL
                        isClickable = isBase; isFocusable = isBase
                        if (isBase) {
                            val tv = android.util.TypedValue()
                            requireContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
                            foreground = requireContext().getDrawable(tv.resourceId)
                            setOnClickListener { onPicked(pokemon.nameDE, pokemon.nameEN) }
                        }
                        alpha = if (isBase) 1f else 0.35f
                        setPadding(dpPx(4), dpPx(4), dpPx(4), dpPx(4))
                    }
                    val entry = PokedexData.allPokemon.find { it.name.equals(pokemon.nameEN, ignoreCase = true) }
                    android.widget.ImageView(requireContext()).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(dpPx(58), dpPx(58))
                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER; adjustViewBounds = true
                        if (entry != null && entry.spriteId > 0) loadPokemonSprite(requireContext(), entry.spriteId)
                        else Glide.with(requireContext()).load(SpriteUrls.pokeballUrl).placeholder(R.drawable.ic_pokeball).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(this)
                        col.addView(this)
                    }
                    if (entry != null) {
                        val typesRow = android.widget.LinearLayout(requireContext()).apply {
                            orientation = android.widget.LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER
                            layoutParams = android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = dpPx(2) }
                        }
                        entry.types.forEach { type ->
                            android.widget.ImageView(requireContext()).apply {
                                layoutParams = android.widget.LinearLayout.LayoutParams(dpPx(20), dpPx(20)).also { it.marginEnd = dpPx(2) }
                                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                Glide.with(requireContext()).load(SpriteUrls.typeIconUrl(type.name))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
                                typesRow.addView(this)
                            }
                        }
                        col.addView(typesRow)
                    }
                    android.widget.TextView(requireContext()).apply {
                        text = pokemon.nameDE; textSize = 8.5f; setTextColor(c.textPrimary)
                        gravity = android.view.Gravity.CENTER; maxLines = 1
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT).also { it.topMargin = dpPx(2) }
                        col.addView(this)
                    }
                    android.widget.TextView(requireContext()).apply {
                        text = pokemon.nameEN; textSize = 7.5f; setTextColor(c.textSecondary)
                        gravity = android.view.Gravity.CENTER; maxLines = 1
                        col.addView(this)
                    }
                    lineRow.addView(col, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
                        .also { it.weight = 1f; if (index < allPokemon.size - 1) it.marginEnd = dpPx(2) })
                }
                innerContainer.addView(lineRow)
            }
            outerContainer.addView(scrollView, android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0).also { it.weight = 1f })
            binding.layoutPanelSub.addView(outerContainer)
        }

        pushSubPanel("$typeName Starters", buildContent = { buildLines() }, onBack = onBack)
    }

    private fun showBadgePickerInline(gym: EnemyTrainer.GymLeader, onBack: () -> Unit) {
        val c = ThemeManager.colorsFor(mainActivity?.currentTheme ?: AppTheme.COLORFUL)
        pushSubPanel("${gym.nameDE} / ${gym.nameEN}", buildContent = {
            val view = layoutInflater.inflate(R.layout.dialog_badge_select, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            val spBadge = view.findViewById<android.widget.LinearLayout>(R.id.screen_panel)
            spBadge?.setBackgroundColor(c.surface)
            spBadge?.addView(buildBackRow(), 0)
            view.findViewById<android.widget.TextView>(R.id.tv_badge_title)?.text =
                "${gym.nameDE} / ${gym.nameEN} — Badge"

            val container = view.findViewById<android.widget.LinearLayout>(R.id.badge_container)
            for (i in 1..8) {
                com.google.android.material.button.MaterialButton(requireContext()).apply {
                    text = "Badge $i"; textSize = 14f; isAllCaps = false
                    setTextColor(android.graphics.Color.WHITE)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CC0000"))
                    cornerRadius = dpPx(22)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpPx(44)
                    ).also { it.bottomMargin = dpPx(6) }
                    setOnClickListener { handleEnemySelected(gym, i); hidePanels() }
                    container.addView(this)
                }
            }
            view.findViewById<android.widget.Button>(R.id.btn_back_badge)?.setOnClickListener { popBack() }
            binding.layoutPanelSub.addView(view)
        }, onBack = onBack)
    }

    private fun showChampionPickerInline(onBack: () -> Unit) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val champions = com.pokemonbp.data.TrainerParser.loadChampions(requireContext()).filter { it.nameEN != "Hilda" }
        pushSubPanel("Choose Champion", buildContent = {
            val view = layoutInflater.inflate(R.layout.dialog_enemy_trainer, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            val spChamp = view.findViewById<android.widget.LinearLayout>(R.id.screen_panel)
            spChamp?.setBackgroundColor(c.surface)
            spChamp?.addView(buildBackRow(), 0)

            val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_enemy_options)
            recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
            recycler.adapter = ChampionPickerAdapter(champions, c) { champion ->
                handleEnemySelected(champion, null)
                hidePanels()
            }
            view.findViewById<android.widget.Button>(R.id.btn_close_dialog)?.setOnClickListener { popBack() }
            binding.layoutPanelSub.addView(view)
        }, onBack = onBack)
    }

    private fun showSavedTrainerInline(onBack: () -> Unit) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val trainers = TrainerManager.loadTrainers(requireContext())
        if (trainers.isEmpty()) {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("No Trainers Saved")
                .setMessage("Create a trainer first using 'Add Trainer'.")
                .setPositiveButton("OK", null).show()
            return
        }
        pushSubPanel("Choose Trainer", buildContent = {
            val view = layoutInflater.inflate(R.layout.dialog_choose_trainer, binding.layoutPanelSub, false)
            view.layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            stripDialogChrome(view)
            val spSaved = view.findViewById<android.widget.LinearLayout>(R.id.screen_panel)
            spSaved?.setBackgroundColor(c.surface)
            spSaved?.addView(buildBackRow(), 0)

            val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_trainer_chooser)
            recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
            recycler.adapter = TrainerRowAdapter(
                trainers = trainers.toMutableList(), theme = theme, c = c,
                onBattle = { trainer ->
                    handleEnemySelected(EnemyTrainer.SavedTrainer(trainer), null)
                    hidePanels()
                },
                onManage = { /* not supported inline */ },
                onDelete = { /* not supported inline */ })

            view.findViewById<android.widget.Button>(R.id.btn_close_dialog)?.setOnClickListener { popBack() }
            binding.layoutPanelSub.addView(view)
        }, onBack = onBack)
    }

    private fun showSourcePickerInline(onBack: () -> Unit, onSource: (Int) -> Unit) {
        val c = ThemeManager.colorsFor(mainActivity?.currentTheme ?: AppTheme.COLORFUL)
        pushSubPanel("Add Pokémon", buildContent = {
            val container = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(dpPx(16), dpPx(14), dpPx(16), dpPx(10))
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
            }
            android.widget.TextView(requireContext()).apply {
                text = "Add Pokémon"; textSize = 14f; setTextColor(c.textPrimary)
                gravity = android.view.Gravity.CENTER; setPadding(0, 0, 0, dpPx(12))
                container.addView(this)
            }
            listOf("📖 Pokédex", "🌿 Route", "⭐ Starter").forEachIndexed { idx, label ->
                com.google.android.material.button.MaterialButton(requireContext()).apply {
                    text = label; isAllCaps = false; textSize = 13f
                    setTextColor(android.graphics.Color.WHITE)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CC0000"))
                    cornerRadius = dpPx(22)
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dpPx(44)
                    ).also { it.bottomMargin = dpPx(6) }
                    setOnClickListener { onSource(idx) }
                    container.addView(this)
                }
            }
            binding.layoutPanelSub.addView(container)
        }, onBack = onBack)
    }

    private fun showRouteDetail(title: String, pokemon: List<com.pokemonbp.data.RoutePokemon>) {
        binding.layoutWildRoutes.visibility = android.view.View.GONE
        binding.layoutRouteDetail.visibility = android.view.View.VISIBLE
        binding.tvRouteDetailName.text = title

        binding.tvRouteBack.setOnClickListener {
            binding.layoutRouteDetail.visibility = android.view.View.GONE
            binding.layoutWildRoutes.visibility = android.view.View.VISIBLE
        }

        val items = pokemon.map { RouteDetailItem.PokemonRow(it) }
        binding.rvRoutePokemon.layoutManager =
            androidx.recyclerview.widget.GridLayoutManager(requireContext(), 3)
        val c = com.pokemonbp.data.ThemeManager.colorsFor(mainActivity?.currentTheme ?: com.pokemonbp.data.AppTheme.COLORFUL)
        binding.rvRoutePokemon.adapter = RoutePokemonDetailAdapter(items, c)
    }

    private fun updateBattleButton() {
        val canBattle = teamAList.isNotEmpty() && teamBList.isNotEmpty()
        binding.btnCalculate.isEnabled = canBattle
        val c = ThemeManager.colorsFor(mainActivity?.currentTheme ?: AppTheme.COLORFUL)
        binding.btnCalculate.backgroundTintList =
            ColorStateList.valueOf(if (canBattle) c.accent else Color.GRAY)

        // Update button text to show which Pokémon will fight
        if (canBattle) {
            val nameA = teamAList.getOrNull(activeIndexA)?.displayName() ?: "?"
            val nameB = teamBList.getOrNull(activeIndexB)?.displayName() ?: "?"
            binding.btnCalculate.text = "  $nameA  vs  $nameB"
        } else {
            binding.btnCalculate.text = "  CALCULATE BATTLE"
        }
    }

    private fun loadTowns(): List<String> {
        return try {
            requireContext().assets.open("towns.txt").bufferedReader().readLines()
                .map { it.trim() }.filter { it.isNotEmpty() }
        } catch (e: Exception) { emptyList() }
    }

    private fun restoreTrainerDisplay() {
        val aTrainer = teamATrainer
        if (aTrainer != null) {
            binding.tvTeamALabel.text = aTrainer.name
            loadLabelIcon(SpriteUrls.avatarUrl(aTrainer.avatarId), binding.ivLabelA, R.drawable.ic_player)
            loadTrainerImage(SpriteUrls.playerTrainerImageUrl(aTrainer.avatarId), binding.ivTrainerA)
        }
        updateDeloadPlayerButton()
        val enemy = currentEnemyTrainer
        if (enemy != null) {
            binding.tvTeamBLabel.text = teamBLabel
            val labelIconUrl = when (enemy) {
                is EnemyTrainer.GymLeader     -> SpriteUrls.trainerIconUrl(enemy.id)
                is EnemyTrainer.Champion      -> SpriteUrls.trainerIconUrl(enemy.nameEN.lowercase())
                is EnemyTrainer.RandomTrainer -> SpriteUrls.trainerIconUrl("random")
                is EnemyTrainer.WildPokemon   -> SpriteUrls.trainerIconUrl("wild")
                is EnemyTrainer.SavedTrainer  -> SpriteUrls.avatarUrl(enemy.trainer.avatarId)
            }
            val trainerImageUrl = when (enemy) {
                is EnemyTrainer.GymLeader     -> SpriteUrls.gymLeaderImageUrl(enemy.id)
                is EnemyTrainer.Champion      -> SpriteUrls.championImageUrl(enemy.nameEN)
                is EnemyTrainer.WildPokemon   -> SpriteUrls.trainerIconUrl("wild")
                is EnemyTrainer.RandomTrainer -> SpriteUrls.randomTrainerImageUrl()
                is EnemyTrainer.SavedTrainer  -> SpriteUrls.playerTrainerImageUrl(enemy.trainer.avatarId)
            }
            loadLabelIcon(labelIconUrl ?: SpriteUrls.battleUrl, binding.ivLabelB, R.drawable.ic_battle)
            loadTrainerImage(trainerImageUrl, binding.ivTrainerB)
        }
        updateDeloadButton()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

data class StarterPokemon(val nameDE: String, val nameEN: String)
data class StarterLine(val base: StarterPokemon, val evo2: StarterPokemon?, val evo3: StarterPokemon?)

class WildRouteAdapter(
    private val routes: List<com.pokemonbp.data.RouteLocation>,
    private val c: com.pokemonbp.data.ThemeColors,
    private val onClick: (com.pokemonbp.data.RouteLocation) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<WildRouteAdapter.VH>() {

    inner class VH(val view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val card = view as com.google.android.material.card.MaterialCardView
        val tvName: android.widget.TextView = view.findViewById(R.id.tv_route_name)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
        VH(android.view.LayoutInflater.from(parent.context).inflate(R.layout.item_wild_route, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val route = routes[position]
        holder.tvName.text = route.displayName
        holder.tvName.setTextColor(c.textPrimary)
        val accentColor = if (route.isLegendary) Color.parseColor("#9b59b6") else Color.parseColor("#2983d3")
        holder.card.setCardBackgroundColor(c.surface)
        holder.card.strokeColor = accentColor
        holder.card.strokeWidth = if (route.isLegendary) 2 else 1
        holder.itemView.setOnClickListener { onClick(route) }
    }

    override fun getItemCount() = routes.size
}

// ── Route detail item types ────────────────────────────────────────────────────

sealed class RouteDetailItem {
    data class Header(val label: String) : RouteDetailItem()
    data class PokemonRow(val pokemon: com.pokemonbp.data.RoutePokemon) : RouteDetailItem()
}

// ── Route Pokemon detail adapter (3-col grid, sprite + types + BP) ─────────────

class RoutePokemonDetailAdapter(
    private val items: List<RouteDetailItem>,
    private val c: com.pokemonbp.data.ThemeColors
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER  = 0
        private const val TYPE_POKEMON = 1
    }

    inner class HeaderVH(v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val tv: android.widget.TextView = v as android.widget.TextView
    }

    inner class PokemonVH(v: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
        val ivSprite:  android.widget.ImageView = v.findViewById(R.id.iv_route_sprite)
        val ivType1:   android.widget.ImageView = v.findViewById(R.id.iv_route_type1)
        val ivType2:   android.widget.ImageView = v.findViewById(R.id.iv_route_type2)
        val tvBP:      android.widget.TextView  = v.findViewById(R.id.tv_route_bp)
        val tvName:    android.widget.TextView  = v.findViewById(R.id.tv_route_pokemon_name)
        val tvNameEn:  android.widget.TextView  = v.findViewById(R.id.tv_route_pokemon_name_en)
        val tvValue1:  android.widget.TextView  = v.findViewById(R.id.tv_route_value1)
        val tvValue2:  android.widget.TextView  = v.findViewById(R.id.tv_route_value2)
    }

    override fun getItemViewType(position: Int) =
        if (items[position] is RouteDetailItem.Header) TYPE_HEADER else TYPE_POKEMON

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int) =
        if (viewType == TYPE_HEADER) {
            val tv = android.widget.TextView(parent.context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(6, 8, 6, 4)
                textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(c.textPrimary)
            }
            HeaderVH(tv)
        } else {
            PokemonVH(android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_route_pokemon, parent, false))
        }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is RouteDetailItem.Header -> (holder as HeaderVH).tv.text = item.label
            is RouteDetailItem.PokemonRow -> {
                val vh  = holder as PokemonVH
                val p   = item.pokemon
                val ctx = holder.itemView.context

                vh.tvName.text = p.nameDE
                vh.tvNameEn.text = p.nameEN
                vh.tvName.setTextColor(c.textSecondary)
                vh.tvNameEn.setTextColor(c.textSecondary)
                vh.tvBP.text = if (p.bp > 0) "BP: ${p.bp}" else "BP: ?"
                val valueParts = p.value?.split("/")
                vh.tvValue1.text = valueParts?.getOrNull(0)?.trim() ?: ""
                vh.tvValue2.text = valueParts?.getOrNull(1)?.trim() ?: ""

                val entry = PokedexData.allPokemon.find { it.name.equals(p.nameEN.trim(), ignoreCase = true) }

                if (entry != null && entry.spriteId > 0) {
                    vh.ivSprite.loadPokemonSprite(ctx, entry.spriteId)
                } else {
                    Glide.with(ctx).load(SpriteUrls.pokeballUrl).placeholder(R.drawable.ic_pokeball).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(vh.ivSprite)
                }

                val types = entry?.types ?: emptyList()
                if (types.isNotEmpty()) {
                    com.bumptech.glide.Glide.with(ctx)
                        .load(com.pokemonbp.data.SpriteUrls.typeIconUrl(types[0].name))
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .into(vh.ivType1)
                }
                if (types.size >= 2) {
                    vh.ivType2.visibility = android.view.View.VISIBLE
                    com.bumptech.glide.Glide.with(ctx)
                        .load(com.pokemonbp.data.SpriteUrls.typeIconUrl(types[1].name))
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .into(vh.ivType2)
                } else {
                    vh.ivType2.visibility = android.view.View.GONE
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
