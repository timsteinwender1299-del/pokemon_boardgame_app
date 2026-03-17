package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pokemonbp.R
import com.pokemonbp.data.*
import com.pokemonbp.model.*

class EnemyTrainerDialog(
    private val theme: AppTheme,
    private val onTrainerSelected: (EnemyTrainer, Int?) -> Unit,
    private val onAddSinglePokemon: () -> Unit  // for Wild/Random
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_enemy_trainer, null)
        view.setBackgroundColor(c.surface)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_enemy_options)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = EnemyOptionAdapter(buildOptions(), c) { option ->
            handleOptionSelected(option)
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun buildOptions(): List<EnemyOption> = buildList {
        SinnohData.gymLeaders.forEach { add(EnemyOption.GymLeaderOption(it)) }
        add(EnemyOption.ChampionMenu)
        add(EnemyOption.WildOption)
        add(EnemyOption.RandomOption)
        add(EnemyOption.SavedTrainerMenu)
    }

    private fun handleOptionSelected(option: EnemyOption) {
        when (option) {
            is EnemyOption.GymLeaderOption -> showBadgeDialog(option.gym)
            is EnemyOption.ChampionMenu    -> showChampionDialog()
            is EnemyOption.WildOption      -> { dismiss(); onAddSinglePokemon() }
            is EnemyOption.RandomOption    -> { dismiss(); onAddSinglePokemon() }
            is EnemyOption.SavedTrainerMenu -> showSavedTrainerDialog()
        }
    }

    private fun showBadgeDialog(gym: EnemyTrainer.GymLeader) {
        val badges = (1..8).map { "Badge $it" }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("${gym.nameDE} / ${gym.nameEN} — Choose Badge")
            .setItems(badges) { _, which ->
                onTrainerSelected(gym, which + 1)
                dismiss()
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun showChampionDialog() {
        val names = SinnohData.champions.map { it.nameEN }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Champion")
            .setItems(names) { _, which ->
                onTrainerSelected(SinnohData.champions[which], null)
                dismiss()
            }
            .setNegativeButton("Back", null)
            .show()
    }

    private fun showSavedTrainerDialog() {
        val trainers = TrainerManager.loadTrainers(requireContext())
        if (trainers.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("No Trainers Saved")
                .setMessage("Create a trainer first using 'Add Trainer'.")
                .setPositiveButton("OK", null).show()
            return
        }
        ChooseTrainerDialog(theme) { trainer ->
            onTrainerSelected(EnemyTrainer.SavedTrainer(trainer), null)
            dismiss()
        }.show(parentFragmentManager, "ChooseTrainer")
    }
}

sealed class EnemyOption {
    data class GymLeaderOption(val gym: EnemyTrainer.GymLeader) : EnemyOption()
    object ChampionMenu : EnemyOption()
    object WildOption : EnemyOption()
    object RandomOption : EnemyOption()
    object SavedTrainerMenu : EnemyOption()
}

class EnemyOptionAdapter(
    private val options: List<EnemyOption>,
    private val c: ThemeColors,
    private val onClick: (EnemyOption) -> Unit
) : RecyclerView.Adapter<EnemyOptionAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tv_enemy_option_title)
        val tvSub: TextView   = v.findViewById(R.id.tv_enemy_option_sub)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_enemy_option, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val opt = options[position]
        holder.itemView.setBackgroundColor(if (position % 2 == 0) c.surface else c.surfaceVariant)
        holder.tvTitle.setTextColor(c.textPrimary)
        holder.tvSub.setTextColor(c.textSecondary)
        when (opt) {
            is EnemyOption.GymLeaderOption -> {
                holder.tvTitle.text = "${opt.gym.nameDE} / ${opt.gym.nameEN}"
                holder.tvSub.text = "Gym Leader  ·  Tap to choose badge"
            }
            is EnemyOption.ChampionMenu -> {
                holder.tvTitle.text = "🏆 Champion"
                holder.tvSub.text = "Cynthia or Tim"
            }
            is EnemyOption.WildOption -> {
                holder.tvTitle.text = "🌿 Wild Pokémon"
                holder.tvSub.text = "Choose one Pokémon to battle"
            }
            is EnemyOption.RandomOption -> {
                holder.tvTitle.text = "🎲 Random Trainer"
                holder.tvSub.text = "Choose one Pokémon to battle"
            }
            is EnemyOption.SavedTrainerMenu -> {
                holder.tvTitle.text = "👤 Choose Trainer"
                holder.tvSub.text = "From your saved trainers"
            }
        }
        holder.itemView.setOnClickListener { onClick(opt) }
    }

    override fun getItemCount() = options.size
}
