package com.pokemonbp.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
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

        binding.ivWildButton.setOnClickListener {
            wildMode = !wildMode
            val childVisibility = if (wildMode) android.view.View.GONE else android.view.View.VISIBLE
            if (wildMode) {
                binding.root.setBackgroundColor(Color.parseColor("#2983d3"))
                binding.ivWildButton.setImageResource(R.drawable.ic_battle_calculator_menu)
            } else {
                binding.root.setBackgroundColor(requireContext().getColor(R.color.pokedex_red))
                binding.ivWildButton.setImageResource(R.drawable.ic_wild_pokemon_menu)
            }
            for (i in 0 until binding.screenPanel.childCount) {
                binding.screenPanel.getChildAt(i).visibility = childVisibility
            }
        }

        binding.btnAddPokemonA.setOnClickListener { showAddPokemonDialog(Team.TEAM_A) }
        binding.btnAddPlayerA.setOnClickListener { showAddTrainerDialog() }
        binding.btnChooseTrainerA.setOnClickListener { showChooseTrainerDialog() }
        binding.btnChooseEnemyB.setOnClickListener { showEnemyTrainerDialog() }

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

    private fun showAddPokemonDialog(team: Team) {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        AddPokemonDialogFragment(team, theme) { pokemon ->
            if (team == Team.TEAM_A) {
                teamAList.add(pokemon)
                adapterA.notifyItemInserted(teamAList.size - 1)
                // New Pokémon doesn't change active selection
            } else {
                teamBList.add(pokemon)
                adapterB.notifyItemInserted(teamBList.size - 1)
            }
            updateBattleButton()
        }.show(parentFragmentManager, "AddPokemon")
    }

    private fun showAddTrainerDialog() {
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        AddPlayerDialog(theme) { trainer ->
            Toast.makeText(requireContext(), "Trainer '${trainer.name}' saved!", Toast.LENGTH_SHORT).show()
        }.show(parentFragmentManager, "AddPlayer")
    }

    private fun showChooseTrainerDialog() {
        val trainers = TrainerManager.loadTrainers(requireContext())
        if (trainers.isEmpty()) {
            Toast.makeText(requireContext(), "No trainers saved yet! Use 'Add Trainer' first.", Toast.LENGTH_SHORT).show()
            return
        }
        val theme = mainActivity?.currentTheme ?: AppTheme.COLORFUL
        ChooseTrainerDialog(theme, onBattle = { trainer ->
            teamATrainer = trainer
            teamAList.clear()
            trainer.pokemon.forEach { preset ->
                teamAList.add(Pokemon(
                    id = System.currentTimeMillis().toInt() + teamAList.size,
                    name = preset.name, nameDE = preset.nameDE,
                    types = preset.types, baseBP = preset.baseBP,
                    team = Team.TEAM_A, pokedexId = preset.pokedexId
                ))
            }
            activeIndexA = 0
            adapterA.activeIndex = 0
            adapterA.notifyDataSetChanged()
            updateTeamALabel()
            loadLabelIcon(SpriteUrls.avatarUrl(trainer.avatarId), binding.ivLabelA, R.drawable.ic_player)
            loadTrainerImage(SpriteUrls.playerTrainerImageUrl(trainer.avatarId), binding.ivTrainerA)
            updateBattleButton()
            Toast.makeText(requireContext(), "${trainer.name}'s team loaded!", Toast.LENGTH_SHORT).show()
        }).show(parentFragmentManager, "ChooseTrainer")
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
            },
            onAddSinglePokemon = {
                showAddPokemonDialog(Team.TEAM_B)
            }
        ).show(parentFragmentManager, "EnemyTrainer")
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
