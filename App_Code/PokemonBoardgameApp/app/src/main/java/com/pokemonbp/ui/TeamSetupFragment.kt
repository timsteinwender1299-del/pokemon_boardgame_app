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
    private var teamBLabel: String = "Enemy Trainer"
    private var currentEnemyTrainer: EnemyTrainer? = null
    private var wildMode = false

    private val mainActivity get() = activity as? MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeamSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        applyTheme(theme)

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
            }
        )

        adapterA.activeIndex = activeIndexA
        adapterB.activeIndex = activeIndexB

        binding.recyclerTeamA.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTeamA.adapter = adapterA
        binding.recyclerTeamB.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTeamB.adapter = adapterB

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

        setupWildRouteGrid()

        binding.ivWildButton.setOnClickListener {
            wildMode = !wildMode
            if (wildMode) {
                binding.root.setBackgroundColor(Color.parseColor("#2983d3"))
                binding.ivWildButton.setImageResource(R.drawable.ic_battle_calculator_menu)
                binding.layoutMainContent.visibility = android.view.View.GONE
                binding.layoutRouteDetail.visibility = android.view.View.GONE
                binding.layoutPanelAddPokemon.visibility    = android.view.View.GONE
                binding.layoutPanelAddTrainer.visibility    = android.view.View.GONE
                binding.layoutPanelChooseTrainer.visibility = android.view.View.GONE
                binding.layoutWildRoutes.visibility = android.view.View.VISIBLE
            } else {
                binding.root.setBackgroundColor(requireContext().getColor(R.color.pokedex_red))
                binding.ivWildButton.setImageResource(R.drawable.ic_wild_pokemon_menu)
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

        binding.btnCalculate.setOnClickListener {
            if (teamAList.isEmpty() || teamBList.isEmpty()) {
                Toast.makeText(requireContext(), "Each team needs at least one Pokémon!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Battle only the two selected Pokémon
            val pokemonA = teamAList[activeIndexA]
            val pokemonB = teamBList[activeIndexB]
            val result = BattleCalculator.calculate(listOf(pokemonA), listOf(pokemonB))
            (activity as MainActivity).navigateToResults(ResultFragment.newInstance(result))
        }

        updateBattleButton()
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
        binding.tvTeamALabel.text = "🔴 ${teamATrainer?.name ?: "Player"}"
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

    // ── Inline panels ──────────────────────────────────────────────────────────

    private fun showPanel(panel: android.widget.FrameLayout) {
        binding.layoutMainContent.visibility = android.view.View.GONE
        binding.layoutWildRoutes.visibility  = android.view.View.GONE
        binding.layoutRouteDetail.visibility = android.view.View.GONE
        binding.layoutPanelAddPokemon.visibility     = android.view.View.GONE
        binding.layoutPanelAddTrainer.visibility     = android.view.View.GONE
        binding.layoutPanelChooseTrainer.visibility  = android.view.View.GONE
        panel.visibility = android.view.View.VISIBLE
    }

    private fun hidePanels() {
        binding.layoutPanelAddPokemon.visibility     = android.view.View.GONE
        binding.layoutPanelAddTrainer.visibility     = android.view.View.GONE
        binding.layoutPanelChooseTrainer.visibility  = android.view.View.GONE
        binding.layoutMainContent.visibility = android.view.View.VISIBLE
    }

    private fun updateDeloadButton() {
        binding.ivDeloadEnemy.visibility =
            if (currentEnemyTrainer != null) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun deloadEnemyTrainer() {
        currentEnemyTrainer = null
        teamBLabel = "Enemy Trainer"
        teamBList.clear()
        adapterB.notifyDataSetChanged()
        binding.tvTeamBLabel.text = teamBLabel
        binding.ivLabelB.setImageResource(R.drawable.ic_battle)
        binding.ivTrainerB.visibility = android.view.View.GONE
        updateDeloadButton()
        updateBattleButton()
    }

    // ── Add Pokémon panel ──────────────────────────────────────────────────────

    private fun showAddPokemonPanel(team: Team) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        val c = ThemeManager.colorsFor(theme)
        val b = com.pokemonbp.databinding.DialogAddPokemonBinding.inflate(layoutInflater)
        binding.layoutPanelAddPokemon.removeAllViews()
        binding.layoutPanelAddPokemon.addView(b.root, android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT))
        showPanel(binding.layoutPanelAddPokemon)

        val teamColor = if (team == Team.TEAM_A) c.teamA else c.teamB
        val selectedTypes = mutableSetOf<com.pokemonbp.data.PokemonType>()
        var currentPokedexId = 0
        var currentName = ""
        var currentNameDE = ""
        var selectedBP = -1

        b.screenPanel.setBackgroundColor(c.surface)
        b.tvDialogTitle.setTextColor(teamColor)
        b.tvDialogTitle.text = if (team == Team.TEAM_A) "Add Pokémon — Player" else "Add Pokémon — Enemy"
        if (theme == AppTheme.RETRO) b.tvDialogTitle.typeface = android.graphics.Typeface.MONOSPACE
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
            currentName = entry.name; currentNameDE = entry.nameDE; currentPokedexId = entry.id
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

        b.btnPickPokemon.setOnClickListener {
            PokemonPickerDialog(theme) { entry -> applyEntry(entry) }.show(childFragmentManager, "PokPicker")
        }
        b.btnPickFromRoute.setOnClickListener {
            RoutePickerDialog(theme) { nameDE, nameEN, bp ->
                val entry = PokedexData.allPokemon.find { it.name.equals(nameEN.trim(), ignoreCase = true) }
                val types = entry?.types ?: emptyList()
                if (types.isNotEmpty() && bp > 0) {
                    val pokemon = Pokemon(id = System.currentTimeMillis().toInt(), name = nameEN, nameDE = nameDE,
                        types = types, baseBP = bp, team = team, pokedexId = entry?.id ?: 0)
                    if (team == Team.TEAM_A) { teamAList.add(pokemon); adapterA.notifyItemInserted(teamAList.size - 1) }
                    else { teamBList.add(pokemon); adapterB.notifyItemInserted(teamBList.size - 1) }
                    updateBattleButton(); hidePanels()
                } else {
                    currentName = nameEN; currentNameDE = nameDE
                    val e2 = PokedexData.allPokemon.find { it.name.equals(nameEN, ignoreCase = true) }
                    if (e2 != null) { currentPokedexId = e2.id; b.btnPickPokemon.text = "  $nameEN  #${e2.id}"; b.tvDexId.text = "#${e2.id}"; selectedTypes.clear(); selectedTypes.addAll(e2.types) }
                    else { currentPokedexId = 0; b.btnPickPokemon.text = "  $nameEN"; b.tvDexId.text = "" }
                    typeAdapter.notifyDataSetChanged(); if (bp > 0) selectBP(bp)
                }
            }.show(childFragmentManager, "RoutePicker")
        }
        b.btnSavePreset.setOnClickListener { savePreset() }
        b.btnAddPlayerPreset.setOnClickListener { savePreset() }
        b.btnCancelPokemon.setOnClickListener { hidePanels() }
        b.btnAddPokemon.setOnClickListener {
            if (selectedTypes.isEmpty()) { Toast.makeText(requireContext(), "Pick a Pokémon or select types!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (selectedBP < 1) { Toast.makeText(requireContext(), "Select a BP value!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val pokemon = Pokemon(id = System.currentTimeMillis().toInt(), name = currentName, nameDE = currentNameDE,
                types = selectedTypes.toList(), baseBP = selectedBP, team = team, pokedexId = currentPokedexId)
            if (team == Team.TEAM_A) { teamAList.add(pokemon); adapterA.notifyItemInserted(teamAList.size - 1) }
            else { teamBList.add(pokemon); adapterB.notifyItemInserted(teamBList.size - 1) }
            updateBattleButton(); hidePanels()
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
        showPanel(binding.layoutPanelAddTrainer)

        v.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)
        var selAvatarId = existingTrainer?.avatarId ?: 1
        var selGender = existingTrainer?.gender ?: com.pokemonbp.model.TrainerGender.MALE
        val pokemonEntries = mutableListOf<TrainerPokemonEntry?>()
        var avatarPickerVisible = false

        val tvTitle         = v.findViewById<android.widget.TextView>(R.id.tv_add_player_title)
        val etName          = v.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_trainer_name)
        val btnChooseAvatar = v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_choose_avatar)
        val layoutAvatar    = v.findViewById<android.widget.LinearLayout>(R.id.layout_avatar_picker)
        val tabLayout       = v.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_gender)
        val recyclerAvatars = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_avatars)
        val btnConfirmAvatar= v.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm_avatar)
        val btnSave         = v.findViewById<android.widget.Button>(R.id.btn_save_player)
        val btnCancel       = v.findViewById<android.widget.Button>(R.id.btn_cancel_player)

        tvTitle.setTextColor(c.teamA)
        tvTitle.text = if (existingTrainer != null) "Edit Trainer" else "Add Trainer"
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

        existingTrainer?.pokemon?.forEach { pokemonEntries.add(TrainerPokemonEntry(preset = it, bp = it.baseBP)) }
        while (pokemonEntries.size < 4) pokemonEntries.add(null)

        val slots = listOf(
            v.findViewById<android.widget.FrameLayout>(R.id.slot_0),
            v.findViewById<android.widget.FrameLayout>(R.id.slot_1),
            v.findViewById<android.widget.FrameLayout>(R.id.slot_2),
            v.findViewById<android.widget.FrameLayout>(R.id.slot_3))

        var refreshSlots: () -> Unit = {}
        var openPicker: (Int, Int) -> Unit = { _, _ -> }

        openPicker = { i, curBp ->
            PokemonPickerDialog(theme) { picked ->
                pokemonEntries[i] = TrainerPokemonEntry(preset = com.pokemonbp.model.PokemonPreset(
                    name = picked.name, nameDE = picked.nameDE,
                    pokedexId = picked.id, types = picked.types, baseBP = curBp))
                refreshSlots()
            }.show(childFragmentManager, "PickerSlot$i")
        }

        refreshSlots = {
            slots.forEachIndexed { i, frame ->
                frame.removeAllViews()
                layoutInflater.inflate(R.layout.item_slot, frame, true)
                val flEmpty  = frame.findViewById<android.widget.FrameLayout>(R.id.fl_empty)
                val llFilled = frame.findViewById<android.widget.LinearLayout>(R.id.ll_filled)
                val ivSprite = frame.findViewById<android.widget.ImageView>(R.id.iv_slot_sprite)
                val llTypes  = frame.findViewById<android.widget.LinearLayout>(R.id.ll_slot_types)
                val tvName   = frame.findViewById<android.widget.TextView>(R.id.tv_slot_name)
                val btnBp    = frame.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_slot_bp)
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
                        val chip = android.widget.ImageView(requireContext())
                        val sz = (14 * resources.displayMetrics.density).toInt()
                        chip.layoutParams = android.widget.LinearLayout.LayoutParams(sz, sz).also { it.marginEnd = (2 * resources.displayMetrics.density).toInt() }
                        Glide.with(requireContext()).load(SpriteUrls.typeIconUrl(type.name)).diskCacheStrategy(DiskCacheStrategy.ALL).into(chip)
                        llTypes.addView(chip)
                    }
                    tvName.text = preset.nameDE
                    btnBp.text = "${entry.bp}"
                    btnBp.setOnClickListener { pokemonEntries[i] = entry.copy(bp = if (entry.bp >= 12) 1 else entry.bp + 1); refreshSlots() }
                    Glide.with(requireContext()).load(SpriteUrls.garbageBinUrl).placeholder(R.drawable.ic_garbage_bin).error(R.drawable.ic_garbage_bin).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(btnRemove)
                    btnRemove.setOnClickListener { pokemonEntries[i] = null; refreshSlots() }
                    Glide.with(requireContext()).load(SpriteUrls.dawnstoneUrl).placeholder(R.drawable.ic_dawnstone).error(R.drawable.ic_dawnstone).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(btnEvolve)
                    val nextEvos = com.pokemonbp.data.EvolutionData.nextEvolutions(preset.pokedexId)
                    btnEvolve.isEnabled = nextEvos.isNotEmpty(); btnEvolve.alpha = if (nextEvos.isNotEmpty()) 1f else 0.3f
                    btnEvolve.setOnClickListener {
                        if (nextEvos.size == 1) {
                            val evo = PokedexData.allPokemon.find { it.id == nextEvos[0] } ?: return@setOnClickListener
                            pokemonEntries[i] = entry.copy(preset = preset.copy(name = evo.name, nameDE = evo.nameDE, pokedexId = evo.id, types = evo.types)); refreshSlots()
                        } else {
                            val popup = android.widget.PopupMenu(requireContext(), btnEvolve)
                            nextEvos.forEach { evoId ->
                                val evo = PokedexData.allPokemon.find { it.id == evoId }
                                popup.menu.add(evo?.let { "${it.nameDE} / ${it.name}" } ?: "#$evoId").setOnMenuItemClickListener {
                                    if (evo != null) { pokemonEntries[i] = entry.copy(preset = preset.copy(name = evo.name, nameDE = evo.nameDE, pokedexId = evo.id, types = evo.types)); refreshSlots() }; true
                                }
                            }; popup.show()
                        }
                    }
                    Glide.with(requireContext()).load(SpriteUrls.duskstoneUrl).placeholder(R.drawable.ic_duskstone).error(R.drawable.ic_duskstone).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(btnDusk)
                    val prevId = com.pokemonbp.data.EvolutionData.previousEvolution(preset.pokedexId)
                    btnDusk.isEnabled = prevId != null; btnDusk.alpha = if (prevId != null) 1f else 0.3f
                    btnDusk.setOnClickListener {
                        val prev = prevId?.let { id -> PokedexData.allPokemon.find { it.id == id } }
                        if (prev != null) { pokemonEntries[i] = entry.copy(preset = preset.copy(name = prev.name, nameDE = prev.nameDE, pokedexId = prev.id, types = prev.types)); refreshSlots() }
                        else Toast.makeText(requireContext(), "Already at base form!", Toast.LENGTH_SHORT).show()
                    }
                    ivSprite.setOnClickListener { openPicker(i, entry.bp) }
                    tvName.setOnClickListener { openPicker(i, entry.bp) }
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
        showPanel(binding.layoutPanelChooseTrainer)

        v.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)
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
                    trainer.pokemon.forEach { preset ->
                        teamAList.add(Pokemon(id = System.currentTimeMillis().toInt() + teamAList.size,
                            name = preset.name, nameDE = preset.nameDE,
                            types = preset.types, baseBP = preset.baseBP,
                            team = Team.TEAM_A, pokedexId = preset.pokedexId))
                    }
                    activeIndexA = 0; adapterA.activeIndex = 0; adapterA.notifyDataSetChanged()
                    updateTeamALabel()
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

    private fun showEnemyTrainerDialog() {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        EnemyTrainerDialog(theme,
            onTrainerSelected = { enemyTrainer, badge ->
            currentEnemyTrainer = enemyTrainer
            teamBList.clear()
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
                is EnemyTrainer.GymLeader    -> SpriteUrls.trainerIconUrl(enemyTrainer.id)
                is EnemyTrainer.Champion     -> SpriteUrls.trainerIconUrl(enemyTrainer.nameEN.lowercase())
                is EnemyTrainer.RandomTrainer -> SpriteUrls.trainerIconUrl("random")
                is EnemyTrainer.WildPokemon  -> SpriteUrls.trainerIconUrl("wild")
                is EnemyTrainer.SavedTrainer -> SpriteUrls.avatarUrl(enemyTrainer.trainer.avatarId)
            }
            activeIndexB = 0
            adapterB.activeIndex = 0
            adapterB.notifyDataSetChanged()
            binding.tvTeamBLabel.text = teamBLabel
            loadLabelIcon(labelIconUrl, binding.ivLabelB, R.drawable.ic_battle)
            loadTrainerImage(trainerImageUrl, binding.ivTrainerB)
            updateBattleButton()
            updateDeloadButton()
            },
            onAddSinglePokemon = {
                showAddPokemonPanel(Team.TEAM_B)
            }
        ).show(parentFragmentManager, "EnemyTrainer")
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

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
                    vh.ivSprite.setImageResource(R.drawable.ic_pokeball)
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
