package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.data.TrainerManager
import com.pokemonbp.model.PlayerTrainer

class ChooseTrainerDialog(
    private val theme: AppTheme,
    private val onBattle: (PlayerTrainer) -> Unit   // load team for battle
) : DialogFragment() {

    private lateinit var adapter: TrainerRowAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_choose_trainer, null)
        view.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_trainer_chooser)
        recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)

        fun reloadAdapter() {
            val trainers = TrainerManager.loadTrainers(requireContext())
            adapter = TrainerRowAdapter(
                trainers = trainers.toMutableList(),
                theme = theme,
                c = c,
                onBattle = { trainer -> onBattle(trainer); dismiss() },
                onManage = { trainer -> openManageDialog(trainer) },
                onDelete = { trainer -> confirmDelete(trainer) { reloadAdapter(); recycler.adapter = adapter } }
            )
            recycler.adapter = adapter
        }

        reloadAdapter()

        view.findViewById<android.widget.Button>(R.id.btn_close_dialog).setOnClickListener { dismiss() }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.setOnShowListener {
            val dm = requireContext().resources.displayMetrics
            dialog.window?.setLayout(
                (dm.widthPixels * 0.95).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
        return dialog
    }

    private fun openManageDialog(trainer: PlayerTrainer) {
        val theme = this.theme
        AddPlayerDialog(theme, existingTrainer = trainer) { updated ->
            // Save updated trainer
            val all = TrainerManager.loadTrainers(requireContext())
            val idx = all.indexOfFirst { it.id == updated.id }
            if (idx >= 0) all[idx] = updated else all.add(updated)
            TrainerManager.saveTrainers(requireContext(), all)
            // Refresh list
            dismiss()
            ChooseTrainerDialog(theme, onBattle).show(parentFragmentManager, "ChooseTrainer")
        }.show(parentFragmentManager, "ManageTrainer")
    }

    private fun confirmDelete(trainer: PlayerTrainer, onDeleted: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Trainer")
            .setMessage("Remove \"${trainer.name}\" permanently?")
            .setPositiveButton("Delete") { _, _ ->
                val all = TrainerManager.loadTrainers(requireContext())
                all.removeAll { it.id == trainer.id }
                TrainerManager.saveTrainers(requireContext(), all)
                onDeleted()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class TrainerRowAdapter(
    private val trainers: MutableList<PlayerTrainer>,
    private val theme: AppTheme,
    private val c: com.pokemonbp.data.ThemeColors,
    private val onBattle: (PlayerTrainer) -> Unit,
    private val onManage: (PlayerTrainer) -> Unit,
    private val onDelete: (PlayerTrainer) -> Unit
) : RecyclerView.Adapter<TrainerRowAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView     = v.findViewById(R.id.card_trainer_row)
        val tvAvatar: TextView         = v.findViewById(R.id.tv_trainer_avatar)
        val ivAvatar: ImageView        = v.findViewById(R.id.iv_trainer_avatar_img)
        val tvName: TextView           = v.findViewById(R.id.tv_trainer_row_name)
        val btnManage: MaterialButton  = v.findViewById(R.id.btn_manage_team)
        val btnBattle: MaterialButton  = v.findViewById(R.id.btn_load_battle)
        val btnDelete: MaterialButton  = v.findViewById(R.id.btn_delete_trainer)
        val slots: List<ImageView>     = listOf(
            v.findViewById(R.id.iv_slot_1), v.findViewById(R.id.iv_slot_2),
            v.findViewById(R.id.iv_slot_3), v.findViewById(R.id.iv_slot_4)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trainer_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val trainer = trainers[position]
        val ctx = holder.itemView.context

        holder.card.setCardBackgroundColor(c.surface)
        holder.card.strokeColor = c.teamA

        // Avatar
        val avatarUrl = SpriteUrls.avatarUrl(trainer.avatarId)
        if (avatarUrl != null) {
            holder.ivAvatar.visibility = View.VISIBLE
            holder.tvAvatar.visibility = View.GONE
            Glide.with(ctx)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_player)
                .error(R.drawable.ic_player)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(holder.ivAvatar)
        } else {
            holder.tvAvatar.text = if (trainer.gender == com.pokemonbp.model.TrainerGender.FEMALE) "👩" else "👨"
            holder.tvAvatar.visibility = View.VISIBLE
            holder.ivAvatar.visibility = View.GONE
        }

        holder.tvName.text = trainer.name
        holder.tvName.setTextColor(c.textPrimary)

        // Pokémon sprite slots
        for (i in 0..3) {
            val slot = holder.slots[i]
            val preset = trainer.pokemon.getOrNull(i)
            slot.alpha = 1.0f
            if (preset != null && preset.pokedexId > 0) {
                // No padding — sprite fills the slot fully via fitCenter
                slot.setPadding(0, 0, 0, 0)
                slot.loadPokemonSprite(ctx, preset.pokedexId)
            } else {
                // Large padding keeps pokéball visually small as a placeholder
                val pad = (18 * slot.context.resources.displayMetrics.density).toInt()
                slot.setPadding(pad, pad, pad, pad)
                Glide.with(ctx).load(SpriteUrls.pokeballUrl).placeholder(R.drawable.ic_pokeball).error(R.drawable.ic_pokeball).diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter().into(slot)
                slot.alpha = 0.35f
            }
        }

        val red = Color.parseColor("#CC0000")

        // All three: white bg, red border
        for (btn in listOf(holder.btnManage, holder.btnBattle, holder.btnDelete)) {
            btn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            btn.strokeColor = ColorStateList.valueOf(red)
            btn.strokeWidth = 3
        }
        loadButtonIcon(ctx, SpriteUrls.pokeballUrl, holder.btnManage)
        holder.btnManage.iconTint = null
        holder.btnManage.iconPadding = 0
        holder.btnManage.setOnClickListener { onManage(trainer) }

        loadButtonIcon(ctx, SpriteUrls.battleUrl, holder.btnBattle)
        holder.btnBattle.iconTint = null
        holder.btnBattle.iconPadding = 0
        holder.btnBattle.setOnClickListener { onBattle(trainer) }

        loadButtonIcon(ctx, SpriteUrls.garbageBinUrl, holder.btnDelete)
        holder.btnDelete.iconTint = null
        holder.btnDelete.setOnClickListener { onDelete(trainer) }
    }

    override fun getItemCount() = trainers.size

    private fun loadButtonIcon(ctx: android.content.Context, url: String, button: MaterialButton) {
        Glide.with(ctx).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
            override fun onResourceReady(resource: android.graphics.drawable.Drawable, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?) { button.icon = resource }
            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
        })
    }
}
