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
        view.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_enemy_options)
        recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        recycler.adapter = EnemyGridAdapter(buildOptions(), c) { option ->
            handleOptionSelected(option)
        }

        view.findViewById<android.widget.Button>(R.id.btn_close_dialog).setOnClickListener { dismiss() }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.setOnShowListener {
            val width = (requireContext().resources.displayMetrics.widthPixels * 0.33).toInt()
            dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
        return dialog
    }

    private fun buildOptions(): List<EnemyOption> = buildList {
        com.pokemonbp.data.TrainerParser.loadGymLeaders(requireContext()).forEach { add(EnemyOption.GymLeaderOption(it)) }
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
            is EnemyOption.WildOption       -> { onTrainerSelected(EnemyTrainer.WildPokemon, null); dismiss(); onAddSinglePokemon() }
            is EnemyOption.RandomOption     -> { onTrainerSelected(EnemyTrainer.RandomTrainer, null); dismiss(); onAddSinglePokemon() }
            is EnemyOption.SavedTrainerMenu -> showSavedTrainerDialog()
            is EnemyOption.GalacticOption,
            is EnemyOption.GruntOption,
            is EnemyOption.CommanderOption  -> { /* handled inline in TeamSetupFragment */ }
        }
    }

    private fun showChampionDialog() {
        val champions = com.pokemonbp.data.TrainerParser.loadChampions(requireContext()).filter { it.nameEN != "Hilda" }
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_enemy_trainer, null)
        view.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(ThemeManager.colorsFor(theme).surface)
        view.findViewById<TextView>(R.id.tv_enemy_dialog_title).text = "Choose Champion"
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_enemy_options)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        var subDialog: android.app.AlertDialog? = null
        recycler.adapter = ChampionPickerAdapter(champions, ThemeManager.colorsFor(theme)) { champion ->
            onTrainerSelected(champion, null)
            subDialog?.dismiss()
            dismiss()
        }
        view.findViewById<android.widget.Button>(R.id.btn_close_dialog).also {
            it.text = "Back"
            it.setOnClickListener { subDialog?.dismiss() }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.setOnShowListener {
            val width = (requireContext().resources.displayMetrics.widthPixels * 0.45).toInt()
            dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
        subDialog = dialog
        dialog.show()
    }

    private fun showBadgeDialog(gym: EnemyTrainer.GymLeader) {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_badge_select, null)
        val c = ThemeManager.colorsFor(theme)
        view.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)
        view.findViewById<android.widget.TextView>(R.id.tv_badge_title).text =
            "${gym.nameDE} / ${gym.nameEN} — Badge"

        val container = view.findViewById<android.widget.LinearLayout>(R.id.badge_container)
        var badgeDialog: android.app.AlertDialog? = null

        for (i in 1..8) {
            val btn = com.google.android.material.button.MaterialButton(requireContext()).apply {
                text = "Badge $i"
                textSize = 14f
                isAllCaps = false
                setTextColor(android.graphics.Color.WHITE)
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#CC0000")
                )
                cornerRadius = (22 * resources.displayMetrics.density).toInt()
                val lp = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    (44 * resources.displayMetrics.density).toInt()
                )
                lp.bottomMargin = (6 * resources.displayMetrics.density).toInt()
                layoutParams = lp
                setOnClickListener {
                    onTrainerSelected(gym, i)
                    badgeDialog?.dismiss()
                    dismiss()
                }
            }
            container.addView(btn)
        }

        view.findViewById<android.widget.Button>(R.id.btn_back_badge)
            .setOnClickListener { badgeDialog?.dismiss() }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
        dialog.setOnShowListener {
            val width = (requireContext().resources.displayMetrics.widthPixels * 0.20).toInt()
            dialog.window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            )
        }
        badgeDialog = dialog
        dialog.show()
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
    object GalacticOption  : EnemyOption()
    object GruntOption     : EnemyOption()
    object CommanderOption : EnemyOption()
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

        val (iconId, label, fallback) = when (opt) {
            is EnemyOption.GymLeaderOption  -> Triple(opt.gym.id, "${opt.gym.nameDE}\n${opt.gym.nameEN}", R.drawable.ic_pokeball)
            is EnemyOption.ChampionOption   -> Triple(opt.champion.nameEN.lowercase(), opt.champion.nameEN, R.drawable.ic_pokeball)
            is EnemyOption.ChampionMenu     -> Triple("championmenu", "Champion", R.drawable.ic_pokeball)
            is EnemyOption.WildOption       -> Triple("wild", "Wild\nPokemon", R.drawable.ic_wild_pokemon)
            is EnemyOption.RandomOption     -> Triple("random", "Route\nEncounter", R.drawable.ic_random_trainer)
            is EnemyOption.SavedTrainerMenu -> Triple("saved", "Choose\nTrainer", R.drawable.ic_pokeball)
            is EnemyOption.GalacticOption    -> Triple("galactic",          "Team\nGalaktik",     R.drawable.ic_pokeball)
            is EnemyOption.GruntOption       -> Triple("galacticgrunt",    "Galaktik\nGrunt",    R.drawable.ic_pokeball)
            is EnemyOption.CommanderOption   -> Triple("galacticcommander","Galaktik\nCommander", R.drawable.ic_pokeball)
        }

        holder.tvName.text = label

        val url = SpriteUrls.trainerIconUrl(iconId)
        if (url != null) {
            Glide.with(holder.ivIcon.context)
                .load(url)
                .placeholder(fallback)
                .error(fallback)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.ivIcon)
        } else {
            holder.ivIcon.setImageResource(fallback)
        }

        holder.itemView.setOnClickListener { onClick(opt) }
    }

    override fun getItemCount() = options.size
}

class ChampionPickerAdapter(
    private val champions: List<EnemyTrainer.Champion>,
    private val c: ThemeColors,
    private val onClick: (EnemyTrainer.Champion) -> Unit
) : RecyclerView.Adapter<ChampionPickerAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val ivImage: ImageView = v.findViewById(R.id.iv_champion_image)
        val tvName: TextView   = v.findViewById(R.id.tv_champion_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_champion_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val champion = champions[position]
        holder.tvName.text = champion.nameEN
        holder.tvName.setTextColor(c.textPrimary)
        val url = SpriteUrls.championImageUrl(champion.nameEN)
        if (url != null) {
            Glide.with(holder.ivImage.context)
                .load(url)
                .placeholder(R.drawable.ic_pokeball)
                .error(R.drawable.ic_pokeball)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.ic_pokeball)
        }
        holder.itemView.setOnClickListener { onClick(champion) }
    }

    override fun getItemCount() = champions.size
}
