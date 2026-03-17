package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.EvolutionData
import com.pokemonbp.data.SpriteUrls
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.data.TrainerManager
import com.pokemonbp.model.*
import java.util.UUID

class AddPlayerDialog(
    private val theme: AppTheme,
    private val existingTrainer: PlayerTrainer? = null,
    private val onTrainerSaved: (PlayerTrainer) -> Unit
) : DialogFragment() {

    private var selectedAvatarId: Int = existingTrainer?.avatarId ?: 1
    private var selectedGender: TrainerGender = existingTrainer?.gender ?: TrainerGender.MALE
    private val pokemonEntries = mutableListOf<TrainerPokemonEntry?>()
    private lateinit var avatarAdapter: AvatarAdapter
    private var avatarPickerVisible = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_player, null)
        view.setBackgroundColor(c.surface)

        val tvTitle           = view.findViewById<TextView>(R.id.tv_add_player_title)
        val etName            = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_trainer_name)
        val btnChooseAvatar   = view.findViewById<MaterialButton>(R.id.btn_choose_avatar)
        val layoutAvatarPicker= view.findViewById<LinearLayout>(R.id.layout_avatar_picker)
        val tabLayout         = view.findViewById<TabLayout>(R.id.tab_gender)
        val recyclerAvatars   = view.findViewById<RecyclerView>(R.id.recycler_avatars)
        val btnConfirmAvatar  = view.findViewById<MaterialButton>(R.id.btn_confirm_avatar)

        tvTitle.setTextColor(c.teamA)
        tvTitle.text = if (existingTrainer != null) "Edit Trainer" else "Add Trainer"
        etName.setTextColor(c.textPrimary)
        etName.setHintTextColor(c.textSecondary)
        existingTrainer?.let { etName.setText(it.name) }

        btnChooseAvatar.strokeColor = ColorStateList.valueOf(c.accent)
        btnChooseAvatar.setTextColor(c.accent)
        btnConfirmAvatar.backgroundTintList = ColorStateList.valueOf(c.accent)
        btnConfirmAvatar.setTextColor(Color.WHITE)

        avatarAdapter = AvatarAdapter(TrainerManager.maleAvatars, selectedAvatarId, c) { avatar ->
            selectedAvatarId = avatar.id
            selectedGender = avatar.gender
            btnChooseAvatar.text = "👤 Avatar ${if (avatar.gender == TrainerGender.MALE) "♂" else "♀"} #${if (avatar.gender == TrainerGender.MALE) avatar.id else avatar.id - 100} selected"
        }
        recyclerAvatars.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerAvatars.adapter = avatarAdapter

        tabLayout.addTab(tabLayout.newTab().setText("♂ Male"))
        tabLayout.addTab(tabLayout.newTab().setText("♀ Female"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val list = if (tab.position == 0) TrainerManager.maleAvatars else TrainerManager.femaleAvatars
                selectedGender = if (tab.position == 0) TrainerGender.MALE else TrainerGender.FEMALE
                avatarAdapter.updateList(list)
                selectedAvatarId = list.first().id
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnChooseAvatar.setOnClickListener {
            avatarPickerVisible = !avatarPickerVisible
            layoutAvatarPicker.visibility = if (avatarPickerVisible) View.VISIBLE else View.GONE
            btnChooseAvatar.text = if (avatarPickerVisible) "▲ Close Avatar Picker" else "👤 Choose Avatar"
        }
        btnConfirmAvatar.setOnClickListener {
            avatarPickerVisible = false
            layoutAvatarPicker.visibility = View.GONE
            val num = if (selectedGender == TrainerGender.MALE) selectedAvatarId else selectedAvatarId - 100
            btnChooseAvatar.text = "👤 Avatar ${if (selectedGender == TrainerGender.MALE) "♂" else "♀"} #$num — tap to change"
        }

        // Pre-fill from existing trainer, pad to 4 nulls
        existingTrainer?.pokemon?.forEach { pokemonEntries.add(TrainerPokemonEntry(preset = it, bp = it.baseBP)) }
        while (pokemonEntries.size < 4) pokemonEntries.add(null)

        val slots = listOf(
            view.findViewById<FrameLayout>(R.id.slot_0),
            view.findViewById<FrameLayout>(R.id.slot_1),
            view.findViewById<FrameLayout>(R.id.slot_2),
            view.findViewById<FrameLayout>(R.id.slot_3)
        )

        // Mutual-reference via var lambdas
        var refreshSlots: () -> Unit = {}
        var openPicker: (Int, Int) -> Unit = { _, _ -> }

        openPicker = { i, currentBp ->
            PokemonPickerDialog(theme) { picked ->
                pokemonEntries[i] = TrainerPokemonEntry(
                    preset = PokemonPreset(
                        name = picked.name, nameDE = picked.nameDE,
                        pokedexId = picked.id, types = picked.types, baseBP = currentBp
                    )
                )
                refreshSlots()
            }.show(parentFragmentManager, "PickerSlot$i")
        }

        refreshSlots = {
            slots.forEachIndexed { i, frame ->
                frame.removeAllViews()
                val sv = LayoutInflater.from(requireContext()).inflate(R.layout.item_slot, frame, true)

                val flEmpty    = frame.findViewById<FrameLayout>(R.id.fl_empty)
                val llFilled   = frame.findViewById<LinearLayout>(R.id.ll_filled)
                val ivSprite   = frame.findViewById<ImageView>(R.id.iv_slot_sprite)
                val llTypes    = frame.findViewById<LinearLayout>(R.id.ll_slot_types)
                val tvName     = frame.findViewById<TextView>(R.id.tv_slot_name)
                val btnBp      = frame.findViewById<MaterialButton>(R.id.btn_slot_bp)
                val btnRemove  = frame.findViewById<ImageButton>(R.id.btn_slot_remove)
                val btnEvolve  = frame.findViewById<ImageButton>(R.id.btn_slot_evolve)
                val btnDusk    = frame.findViewById<ImageButton>(R.id.btn_slot_duskstone)

                val entry = pokemonEntries[i]
                if (entry == null) {
                    flEmpty.visibility = View.VISIBLE
                    llFilled.visibility = View.GONE
                    frame.setOnClickListener { openPicker(i, 1) }
                } else {
                    flEmpty.visibility = View.GONE
                    llFilled.visibility = View.VISIBLE
                    val preset = entry.preset

                    // Sprite
                    ivSprite.loadPokemonSprite(requireContext(), preset.pokedexId)

                    // Types
                    llTypes.removeAllViews()
                    preset.types.forEach { type ->
                        val chip = ImageView(requireContext())
                        val size = (14 * resources.displayMetrics.density).toInt()
                        val lp = LinearLayout.LayoutParams(size, size)
                        lp.marginEnd = (2 * resources.displayMetrics.density).toInt()
                        chip.layoutParams = lp
                        val resId = requireContext().resources.getIdentifier(
                            "type_${type.name.lowercase()}", "drawable", requireContext().packageName)
                        if (resId != 0) chip.setImageResource(resId)
                        llTypes.addView(chip)
                    }

                    // Name
                    tvName.text = preset.nameDE

                    // BP button
                    btnBp.text = "${entry.bp}"
                    btnBp.setOnClickListener {
                        val next = if (entry.bp >= 12) 1 else entry.bp + 1
                        pokemonEntries[i] = entry.copy(bp = next)
                        refreshSlots()
                    }

                    // Delete button
                    Glide.with(requireContext())
                        .load(SpriteUrls.garbageBinUrl)
                        .placeholder(R.drawable.ic_garbage_bin)
                        .error(R.drawable.ic_garbage_bin)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(btnRemove)
                    btnRemove.setOnClickListener {
                        pokemonEntries[i] = null
                        refreshSlots()
                    }

                    // Evolve button
                    Glide.with(requireContext())
                        .load(SpriteUrls.dawnstoneUrl)
                        .placeholder(R.drawable.ic_dawnstone)
                        .error(R.drawable.ic_dawnstone)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(btnEvolve)
                    val nextEvos = EvolutionData.nextEvolutions(preset.pokedexId)
                    btnEvolve.isEnabled = nextEvos.isNotEmpty()
                    btnEvolve.alpha = if (nextEvos.isNotEmpty()) 1f else 0.3f
                    btnEvolve.setOnClickListener {
                        if (nextEvos.size == 1) {
                            val evo = PokedexData.allPokemon.find { it.id == nextEvos[0] } ?: return@setOnClickListener
                            pokemonEntries[i] = entry.copy(preset = preset.copy(
                                name = evo.name, nameDE = evo.nameDE,
                                pokedexId = evo.id, types = evo.types))
                            refreshSlots()
                        } else {
                            val popup = android.widget.PopupMenu(requireContext(), btnEvolve)
                            nextEvos.forEach { evoId ->
                                val evo = PokedexData.allPokemon.find { it.id == evoId }
                                popup.menu.add(evo?.let { "${it.nameDE} / ${it.name}" } ?: "#$evoId")
                                    .setOnMenuItemClickListener {
                                        if (evo != null) {
                                            pokemonEntries[i] = entry.copy(preset = preset.copy(
                                                name = evo.name, nameDE = evo.nameDE,
                                                pokedexId = evo.id, types = evo.types))
                                            refreshSlots()
                                        }
                                        true
                                    }
                            }
                            popup.show()
                        }
                    }

                    // Duskstone button — devolve Pokémon
                    Glide.with(requireContext())
                        .load(SpriteUrls.duskstoneUrl)
                        .placeholder(R.drawable.ic_duskstone)
                        .error(R.drawable.ic_duskstone)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .into(btnDusk)
                    val prevEvoId = EvolutionData.previousEvolution(preset.pokedexId)
                    btnDusk.isEnabled = prevEvoId != null
                    btnDusk.alpha = if (prevEvoId != null) 1f else 0.3f
                    btnDusk.setOnClickListener {
                        val prev = prevEvoId?.let { id -> PokedexData.allPokemon.find { it.id == id } }
                        if (prev != null) {
                            pokemonEntries[i] = entry.copy(preset = preset.copy(
                                name = prev.name, nameDE = prev.nameDE,
                                pokedexId = prev.id, types = prev.types))
                            refreshSlots()
                        } else {
                            Toast.makeText(requireContext(), "Already at base form!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Tap sprite area to swap Pokémon
                    ivSprite.setOnClickListener { openPicker(i, entry.bp) }
                    tvName.setOnClickListener { openPicker(i, entry.bp) }
                }
            }
        }

        // Initial render
        refreshSlots()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(if (existingTrainer != null) "Save Changes" else "Save Trainer", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val dm = resources.displayMetrics
            val width  = (dm.widthPixels  * 0.62).toInt()
            val height = (dm.heightPixels * 0.82).toInt()
            dialog.window?.setLayout(width, height)

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = etName.text.toString().trim()
                if (name.isEmpty()) { etName.error = "Enter a trainer name"; return@setOnClickListener }
                val trainer = PlayerTrainer(
                    id = existingTrainer?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    avatarId = selectedAvatarId,
                    gender = selectedGender,
                    pokemon = pokemonEntries.filterNotNull().map { it.preset.copy(baseBP = it.bp) }
                )
                if (existingTrainer == null) {
                    val all = TrainerManager.loadTrainers(requireContext())
                    all.add(trainer)
                    TrainerManager.saveTrainers(requireContext(), all)
                }
                onTrainerSaved(trainer)
                dialog.dismiss()
            }
        }

        return dialog
    }
}
