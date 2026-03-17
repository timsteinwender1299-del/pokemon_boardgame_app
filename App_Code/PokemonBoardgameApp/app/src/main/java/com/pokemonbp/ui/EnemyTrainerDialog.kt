package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        recycler.adapter = EnemyGridAdapter(buildOptions(), c) { option ->
            handleOptionSelected(option)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val width = (requireContext().resources.displayMetrics.widthPixels * 0.80).toInt()
            dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        return dialog
    }

    private fun buildOptions(): List<EnemyOption> = buildList {
        val hilda = SinnohData.champions.first { it.nameEN == "Hilda" }
        SinnohData.gymLeaders.take(2).forEach { add(EnemyOption.GymLeaderOption(it)) }
        add(EnemyOption.ChampionOption(hilda))
        SinnohData.gymLeaders.drop(2).forEach { add(EnemyOption.GymLeaderOption(it)) }
        add(EnemyOption.ChampionMenu)
        add(EnemyOption.WildOption)
        add(EnemyOption.RandomOption)
        add(EnemyOption.SavedTrainerMenu)
    }

    private fun handleOptionSelected(option: EnemyOption) {
        when (option) {
            is EnemyOption.GymLeaderOption  -> showBadgeDialog(option.gym)
            is EnemyOption.ChampionOption   -> { onTrainerSelected(option.champion, null); dismiss() }
            is EnemyOption.ChampionMenu     -> showChampionDialog()
            is EnemyOption.WildOption       -> { dismiss(); onAddSinglePokemon() }
            is EnemyOption.RandomOption     -> { dismiss(); onAddSinglePokemon() }
            is EnemyOption.SavedTrainerMenu -> showSavedTrainerDialog()
        }
    }

    private fun showChampionDialog() {
        val champions = SinnohData.champions.filter { it.nameEN != "Hilda" }
        val names = champions.map { it.nameEN }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Champion")
            .setItems(names) { _, which ->
                onTrainerSelected(champions[which], null)
                dismiss()
            }
            .setNegativeButton("Back", null)
            .show()
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
    data class ChampionOption(val champion: EnemyTrainer.Champion) : EnemyOption()
    object ChampionMenu : EnemyOption()
    object WildOption : EnemyOption()
    object RandomOption : EnemyOption()
    object SavedTrainerMenu : EnemyOption()
}

class EnemyGridAdapter(
    private val options: List<EnemyOption>,
    private val c: ThemeColors,
    private val onClick: (EnemyOption) -> Unit
) : RecyclerView.Adapter<EnemyGridAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivIcon: ImageView  = v.findViewById(R.id.iv_trainer_icon)
        val tvName: TextView   = v.findViewById(R.id.tv_trainer_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_enemy_grid, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val opt = options[position]
        holder.tvName.setTextColor(c.textPrimary)

        val (iconId, label) = when (opt) {
            is EnemyOption.GymLeaderOption  -> opt.gym.id to opt.gym.nameDE
            is EnemyOption.ChampionOption   -> opt.champion.nameEN.lowercase() to opt.champion.nameEN
            is EnemyOption.ChampionMenu     -> "cynthia" to "Champion"
            is EnemyOption.WildOption       -> "wild" to "Wild"
            is EnemyOption.RandomOption     -> "random" to "Random"
            is EnemyOption.SavedTrainerMenu -> "saved" to "Trainer"
        }

        holder.tvName.text = label

        val url = SpriteUrls.trainerIconUrl(iconId)
        if (url != null) {
            Glide.with(holder.ivIcon.context)
                .load(url)
                .placeholder(R.drawable.ic_pokeball)
                .error(R.drawable.ic_pokeball)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.ivIcon)
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_pokeball)
        }

        holder.itemView.setOnClickListener { onClick(opt) }
    }

    override fun getItemCount() = options.size
}
