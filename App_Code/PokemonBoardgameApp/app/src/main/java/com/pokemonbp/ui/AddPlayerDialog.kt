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
import com.pokemonbp.data.TrainerItem
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
    private val selectedBadges: MutableSet<Int> = existingTrainer?.badges?.toMutableSet() ?: mutableSetOf()
    // 8 ordered slots — null = empty
    private val itemSlots: MutableList<TrainerItem?> = MutableList<TrainerItem?>(8) { null }.also { slots ->
        existingTrainer?.trainerItems?.forEachIndexed { i, item -> if (i < 8) slots[i] = item }
    }
    private lateinit var avatarAdapter: AvatarAdapter
    private var avatarPickerVisible = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = ThemeManager.colorsFor(theme)
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_player, null)
        view.findViewById<android.widget.LinearLayout>(R.id.screen_panel).setBackgroundColor(c.surface)

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
            btnChooseAvatar.text = "👤 Character ${if (avatar.gender == TrainerGender.MALE) "♂" else "♀"} #${if (avatar.gender == TrainerGender.MALE) avatar.id else avatar.id - 100} selected"
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
            btnChooseAvatar.text = if (avatarPickerVisible) "▲ Close Character Picker" else "👤 Choose Character"
        }
        btnConfirmAvatar.setOnClickListener {
            avatarPickerVisible = false
            layoutAvatarPicker.visibility = View.GONE
            val num = if (selectedGender == TrainerGender.MALE) selectedAvatarId else selectedAvatarId - 100
            btnChooseAvatar.text = "👤 Character ${if (selectedGender == TrainerGender.MALE) "♂" else "♀"} #$num — tap to change"
        }

        // ── Badge row ──────────────────────────────────────────────────────────
        val badgeViews = listOf(
            view.findViewById<android.widget.ImageView>(R.id.iv_badge_1),
            view.findViewById(R.id.iv_badge_2),
            view.findViewById(R.id.iv_badge_3),
            view.findViewById(R.id.iv_badge_4),
            view.findViewById(R.id.iv_badge_5),
            view.findViewById(R.id.iv_badge_6),
            view.findViewById(R.id.iv_badge_7),
            view.findViewById<android.widget.ImageView>(R.id.iv_badge_8)
        )

        fun refreshBadges() {
            badgeViews.forEachIndexed { i, iv ->
                val num = i + 1
                Glide.with(requireContext()).load(SpriteUrls.badgeUrl(num))
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into(iv)
                iv.alpha = if (num in selectedBadges) 1f else 0.3f
            }
        }
        badgeViews.forEachIndexed { i, iv ->
            iv.setOnClickListener {
                val num = i + 1
                if (num in selectedBadges) selectedBadges.remove(num) else selectedBadges.add(num)
                refreshBadges()
            }
        }
        refreshBadges()

        // ── Trainer Items row ──────────────────────────────────────────────────
        val allTItemViews = listOf(
            view.findViewById<android.widget.ImageView>(R.id.iv_titem_1),
            view.findViewById(R.id.iv_titem_2),
            view.findViewById(R.id.iv_titem_3),
            view.findViewById(R.id.iv_titem_4),
            view.findViewById(R.id.iv_titem_5),
            view.findViewById(R.id.iv_titem_6),
            view.findViewById(R.id.iv_titem_7),
            view.findViewById<android.widget.ImageView>(R.id.iv_titem_8)
        )
        fun refreshTrainerItems() {
            allTItemViews.forEachIndexed { i, iv ->
                val item = itemSlots[i]
                iv.alpha = if (item != null) 1f else 0.3f
                if (item != null) {
                    Glide.with(requireContext()).load(item.iconUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_item_placeholder)
                        .error(R.drawable.ic_item_placeholder)
                        .into(iv)
                } else {
                    iv.setImageResource(R.drawable.ic_item_placeholder)
                }
            }
        }
        allTItemViews.forEachIndexed { slotIndex, iv ->
            iv.setOnClickListener {
                val ctx = requireContext()
                val dp = resources.displayMetrics.density

                val opts = mutableListOf<Pair<String, TrainerItem?>>()
                if (itemSlots[slotIndex] != null) opts.add("❌ Remove" to null)
                TrainerItem.values().forEach { item -> opts.add("${item.nameDE} / ${item.nameEN}" to item) }

                val container = android.widget.LinearLayout(ctx).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setBackgroundColor(0xFF1A1A2E.toInt())
                }

                var popup: android.widget.PopupWindow? = null
                opts.forEach { (label, item) ->
                    android.widget.TextView(ctx).apply {
                        text = label
                        textSize = 13f
                        setTextColor(0xFFEEEEEE.toInt())
                        setPadding((12 * dp).toInt(), (10 * dp).toInt(), (12 * dp).toInt(), (10 * dp).toInt())
                        setOnClickListener {
                            if (item == null) {
                                itemSlots[slotIndex] = null
                            } else {
                                for (j in itemSlots.indices) { if (itemSlots[j] == item) itemSlots[j] = null }
                                itemSlots[slotIndex] = item
                            }
                            refreshTrainerItems()
                            popup?.dismiss()
                        }
                    }.also { container.addView(it) }
                }

                popup = android.widget.PopupWindow(
                    container,
                    (160 * dp).toInt(),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
                ).also {
                    it.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0xFF1A1A2E.toInt()))
                    it.elevation = 16f * dp
                    it.showAsDropDown(iv, 0, 0)
                }
            }
        }
        refreshTrainerItems()

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
            AddTrainerPokemonDialog(theme) { name, nameDE, types, bp, pokedexId ->
                pokemonEntries[i] = TrainerPokemonEntry(
                    preset = PokemonPreset(
                        name = name, nameDE = nameDE,
                        pokedexId = pokedexId, types = types,
                        baseBP = bp
                    )
                )
                refreshSlots()
            }.show(parentFragmentManager, "TrainerPokemonSlot$i")
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
                val tvBpValue  = frame.findViewById<TextView>(R.id.tv_slot_bp_value)
                val btnBpMinus = frame.findViewById<TextView>(R.id.btn_slot_bp_minus)
                val btnBpPlus  = frame.findViewById<TextView>(R.id.btn_slot_bp_plus)
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

                    // Type icons — one per type, equal-height cells stacked vertically
                    llTypes.removeAllViews()
                    preset.types.forEach { type ->
                        val cell = android.widget.FrameLayout(requireContext())
                        cell.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
                        val icon = ImageView(requireContext())
                        icon.layoutParams = android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT)
                        icon.scaleType = ImageView.ScaleType.FIT_CENTER
                        icon.adjustViewBounds = true
                        Glide.with(requireContext())
                            .load(SpriteUrls.typeIconUrl(type.name))
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .into(icon)
                        cell.addView(icon)
                        llTypes.addView(cell)
                    }

                    tvName.text = if (preset.nameDE == preset.name) preset.name else "${preset.nameDE} / ${preset.name}"

                    // BP control
                    tvBpValue.text = "${entry.bp}"
                    btnBpMinus.setOnClickListener {
                        val next = if (entry.bp <= 0) 0 else entry.bp - 1
                        pokemonEntries[i] = entry.copy(bp = next)
                        refreshSlots()
                    }
                    btnBpPlus.setOnClickListener {
                        val next = if (entry.bp >= 20) 20 else entry.bp + 1
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
                            val evo = PokedexData.byId[nextEvos[0]] ?: return@setOnClickListener
                            pokemonEntries[i] = entry.copy(preset = preset.copy(
                                name = evo.name, nameDE = evo.nameDE,
                                pokedexId = evo.id, types = evo.types))
                            refreshSlots()
                        } else {
                            val popup = android.widget.PopupMenu(requireContext(), btnEvolve)
                            nextEvos.forEach { evoId ->
                                val evo = PokedexData.byId[evoId]
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
                        val prev = prevEvoId?.let { id -> PokedexData.byId[id] }
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
            .create()

        val btnSave   = view.findViewById<android.widget.Button>(R.id.btn_save_player)
        val btnCancel = view.findViewById<android.widget.Button>(R.id.btn_cancel_player)
        btnSave.text = if (existingTrainer != null) "Save Changes" else "Save Trainer"
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) { etName.error = "Enter a trainer name"; return@setOnClickListener }
            val trainer = PlayerTrainer(
                id = existingTrainer?.id ?: UUID.randomUUID().toString(),
                name = name,
                avatarId = selectedAvatarId,
                gender = selectedGender,
                pokemon = pokemonEntries.filterNotNull().map { it.preset.copy(baseBP = it.bp) },
                badges = selectedBadges.toSet(),
                trainerItems = itemSlots.filterNotNull().toSet()
            )
            if (existingTrainer == null) {
                val all = TrainerManager.loadTrainers(requireContext())
                all.add(trainer)
                TrainerManager.saveTrainers(requireContext(), all)
            }
            onTrainerSaved(trainer)
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            val dm = resources.displayMetrics
            val width  = (dm.widthPixels  * 0.80).toInt()
            val height = (dm.heightPixels * 0.82).toInt()
            dialog.window?.setLayout(width, height)
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }

        return dialog
    }
}
