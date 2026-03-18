package com.pokemonbp.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    private val mainActivity get() = activity as? MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTeamSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val theme = mainActivity?.currentTheme ?: AppTheme.DARK
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

        binding.btnTheme.setOnClickListener { showThemePicker() }
        updateBattleButton()
    }

    private fun applyTheme(theme: AppTheme) {
        val c = ThemeManager.colorsFor(theme)
        binding.root.setBackgroundColor(c.background)
        binding.tvAppTitle.setTextColor(c.accent)
        binding.tvTeamALabel.setTextColor(c.teamA)
        binding.tvTeamBLabel.setTextColor(c.teamB)
        binding.divider.setBackgroundColor(c.divider)
        binding.btnTheme.setTextColor(c.onSurfaceSecondary)

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

    private fun showAddPokemonDialog(team: Team) {
        val theme = mainActivity?.currentTheme ?: AppTheme.DARK
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
        val theme = mainActivity?.currentTheme ?: AppTheme.DARK
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
        val theme = mainActivity?.currentTheme ?: AppTheme.DARK
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
            updateBattleButton()
            Toast.makeText(requireContext(), "${trainer.name}'s team loaded!", Toast.LENGTH_SHORT).show()
        }).show(parentFragmentManager, "ChooseTrainer")
    }

    private fun showEnemyTrainerDialog() {
        val theme = mainActivity?.currentTheme ?: AppTheme.DARK
        EnemyTrainerDialog(theme,
            onTrainerSelected = { enemyTrainer, badge ->
            teamBList.clear()
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
                }
                is EnemyTrainer.WildPokemon -> { teamBLabel = "Wild Pokémon" }
                is EnemyTrainer.RandomTrainer -> { teamBLabel = "Random Trainer" }
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
                }
            }
            activeIndexB = 0
            adapterB.activeIndex = 0
            adapterB.notifyDataSetChanged()
            binding.tvTeamBLabel.text = "🔵 $teamBLabel"
            updateBattleButton()
            },
            onAddSinglePokemon = {
                showAddPokemonDialog(Team.TEAM_B)
            }
        ).show(parentFragmentManager, "EnemyTrainer")
    }

    private fun showThemePicker() {
        val themes = AppTheme.values()
        val labels = themes.map { "${it.emoji} ${it.displayName}" }.toTypedArray()
        val current = mainActivity?.currentTheme ?: AppTheme.DARK
        var selected = themes.indexOf(current)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(labels, selected) { _, which -> selected = which }
            .setPositiveButton("Apply") { _, _ -> mainActivity?.changeTheme(themes[selected]) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateBattleButton() {
        val canBattle = teamAList.isNotEmpty() && teamBList.isNotEmpty()
        binding.btnCalculate.isEnabled = canBattle
        val c = ThemeManager.colorsFor(mainActivity?.currentTheme ?: AppTheme.DARK)
        binding.btnCalculate.backgroundTintList =
            ColorStateList.valueOf(if (canBattle) c.accent else Color.GRAY)

        // Update button text to show which Pokémon will fight
        if (canBattle) {
            val nameA = teamAList.getOrNull(activeIndexA)?.displayName() ?: "?"
            val nameB = teamBList.getOrNull(activeIndexB)?.displayName() ?: "?"
            binding.btnCalculate.text = "⚔️  $nameA  vs  $nameB"
        } else {
            binding.btnCalculate.text = "⚔️  CALCULATE BATTLE"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
