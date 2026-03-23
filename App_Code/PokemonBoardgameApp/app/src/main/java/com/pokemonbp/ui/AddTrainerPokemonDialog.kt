package com.pokemonbp.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.PresetManager
import com.pokemonbp.data.PokemonType
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.databinding.DialogAddTrainerPokemonBinding
import com.pokemonbp.model.PokemonPreset
import com.pokemonbp.model.Team

class AddTrainerPokemonDialog(
    private val theme: AppTheme,
    private val onPokemonSelected: (name: String, nameDE: String, types: List<PokemonType>, bp: Int, pokedexId: Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddTrainerPokemonBinding? = null
    private val binding get() = _binding!!
    private val selectedTypes = mutableSetOf<PokemonType>()
    private lateinit var typeAdapter: TypeSelectionAdapter
    private var currentPokedexId = 0
    private var currentName = ""
    private var currentNameDE = ""
    private var selectedBP: Int = -1

    private val bpButtons: List<MaterialButton> by lazy {
        listOf(
            binding.bp1, binding.bp2, binding.bp3, binding.bp4,
            binding.bp5, binding.bp6, binding.bp7, binding.bp8,
            binding.bp9, binding.bp10, binding.bp11, binding.bp12
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddTrainerPokemonBinding.inflate(LayoutInflater.from(requireContext()))
        val c = ThemeManager.colorsFor(theme)

        binding.screenPanel.setBackgroundColor(c.surface)
        binding.tvDialogTitle.setTextColor(c.accent)
        binding.tvDialogTitle.text = "Add Pokémon — Trainer"
        if (theme == AppTheme.RETRO) binding.tvDialogTitle.typeface = Typeface.MONOSPACE

        binding.tvTypesLabel.setTextColor(c.textSecondary)
        binding.tvPresetsLabel.setTextColor(c.textSecondary)

        binding.btnPickPokemon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC0000"))
        binding.btnPickPokemon.setTextColor(Color.WHITE)
        binding.btnPickPokemon.setOnClickListener { openPicker() }

        binding.btnPickFromRoute.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC0000"))
        binding.btnPickFromRoute.setTextColor(Color.WHITE)
        binding.btnPickFromRoute.setOnClickListener { openRoutePicker() }

        binding.btnAddPlayerPreset.visibility = android.view.View.GONE

        // BP buttons
        bpButtons.forEachIndexed { index, btn ->
            val bpValue = index + 1
            styleBpButton(btn, selected = false, Color.parseColor("#CC0000"))
            if (theme == AppTheme.RETRO) btn.typeface = Typeface.MONOSPACE
            btn.setOnClickListener { selectBP(bpValue) }
        }

        // Type grid
        typeAdapter = TypeSelectionAdapter(PokemonType.values().toList(), selectedTypes, theme) { type, selected ->
            if (selected) {
                if (selectedTypes.size >= 2) {
                    typeAdapter.forceDeselect(type)
                    Toast.makeText(requireContext(), "Max 2 types!", Toast.LENGTH_SHORT).show()
                } else selectedTypes.add(type)
            } else selectedTypes.remove(type)
        }
        binding.recyclerTypes.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerTypes.adapter = typeAdapter

        loadPresets()

        binding.btnSavePreset.setOnClickListener { saveCurrentAsPreset() }
        binding.btnSavePreset.setTextColor(c.accent)

        binding.btnCancelPokemon.setOnClickListener { dismiss() }
        binding.btnAddPokemon.setOnClickListener {
            if (selectedTypes.isEmpty()) {
                Toast.makeText(requireContext(), "Pick a Pokémon or select types!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedBP < 1) {
                Toast.makeText(requireContext(), "Select a BP value!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onPokemonSelected(currentName, currentNameDE, selectedTypes.toList(), selectedBP, currentPokedexId)
            dismiss()
        }

        val dialog = AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Dialog_MinWidth)
            .setView(binding.root)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }
        return dialog
    }

    private fun selectBP(value: Int) {
        selectedBP = value
        val c = ThemeManager.colorsFor(theme)
        bpButtons.forEachIndexed { index, btn ->
            styleBpButton(btn, selected = (index + 1 == value), c.accent)
        }
    }

    private fun styleBpButton(btn: MaterialButton, selected: Boolean, accentColor: Int) {
        val pokeRed = Color.parseColor("#CC0000")
        if (selected) {
            btn.backgroundTintList = ColorStateList.valueOf(pokeRed)
            btn.setTextColor(Color.WHITE)
            btn.strokeWidth = 0
        } else {
            btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            btn.setTextColor(pokeRed)
            btn.strokeColor = ColorStateList.valueOf(pokeRed)
            btn.strokeWidth = 2
        }
    }

    private fun openPicker() {
        PokemonPickerDialog(theme) { entry -> applyEntry(entry) }
            .show(parentFragmentManager, "TrainerPokemonPicker")
    }

    private fun openRoutePicker() {
        RoutePickerDialog(theme) { nameDE, nameEN, bp ->
            val entry = PokedexData.allPokemon.find { it.name.equals(nameEN.trim(), ignoreCase = true) }
            val types = entry?.types ?: emptyList()
            if (types.isNotEmpty() && bp > 0) {
                onPokemonSelected(nameEN, nameDE, types, bp, entry?.id ?: 0)
                dismiss()
            } else {
                applyRouteEntry(nameDE, nameEN, bp)
            }
        }.show(parentFragmentManager, "TrainerRoutePicker")
    }

    private fun applyRouteEntry(nameDE: String, nameEN: String, bp: Int) {
        currentName = nameEN
        currentNameDE = nameDE
        val entry = PokedexData.allPokemon.find { it.name.equals(nameEN, ignoreCase = true) }
        if (entry != null) {
            currentPokedexId = entry.id
            binding.btnPickPokemon.text = "  $nameEN  #${entry.id}"
            binding.tvDexId.text = "#${entry.id}"
            selectedTypes.clear()
            selectedTypes.addAll(entry.types)
        } else {
            currentPokedexId = 0
            binding.btnPickPokemon.text = "  $nameEN"
            binding.tvDexId.text = ""
        }
        typeAdapter.notifyDataSetChanged()
        if (bp > 0) selectBP(bp)
    }

    private fun applyEntry(entry: PokedexEntry) {
        currentName = entry.name
        currentNameDE = entry.nameDE
        currentPokedexId = entry.id
        binding.btnPickPokemon.text = "  ${entry.name}  #${entry.id}"
        binding.tvDexId.text = "#${entry.id}"
        selectedTypes.clear()
        selectedTypes.addAll(entry.types)
        typeAdapter.notifyDataSetChanged()
    }

    private fun loadPresets() {
        val c = ThemeManager.colorsFor(theme)
        val presets = PresetManager.load(requireContext(), Team.TEAM_A)
        binding.presetContainer.removeAllViews()
        if (presets.isEmpty()) {
            binding.tvPresetsLabel.text = "Presets (none yet)"
            return
        }
        binding.tvPresetsLabel.text = "Presets"
        for (preset in presets) {
            val chip = com.google.android.material.chip.Chip(requireContext())
            chip.text = preset.name.ifBlank { preset.types.joinToString("/") { it.displayName } }
            chip.isCheckable = false
            chip.chipBackgroundColor = ColorStateList.valueOf(c.accent)
            chip.setTextColor(Color.WHITE)
            chip.setOnClickListener { applyPreset(preset) }
            binding.presetContainer.addView(chip)
        }
    }

    private fun applyPreset(preset: PokemonPreset) {
        currentName = preset.name
        currentNameDE = preset.name
        currentPokedexId = preset.pokedexId
        binding.btnPickPokemon.text = preset.name.ifBlank { preset.types.joinToString("/") { it.displayName } }
        selectedTypes.clear()
        selectedTypes.addAll(preset.types)
        typeAdapter.notifyDataSetChanged()
        if (preset.pokedexId > 0) {
            binding.tvDexId.text = "#${preset.pokedexId}"
        }
    }

    private fun saveCurrentAsPreset() {
        if (selectedTypes.isEmpty()) {
            Toast.makeText(requireContext(), "Pick types first!", Toast.LENGTH_SHORT).show()
            return
        }
        val presets = PresetManager.load(requireContext(), Team.TEAM_A)
        val newPreset = PokemonPreset(name = currentName, pokedexId = currentPokedexId, types = selectedTypes.toList())
        if (presets.none { it.name == newPreset.name && it.types == newPreset.types }) {
            presets.add(newPreset)
            PresetManager.save(requireContext(), Team.TEAM_A, presets)
            Toast.makeText(requireContext(), "Preset saved!", Toast.LENGTH_SHORT).show()
            loadPresets()
        } else {
            Toast.makeText(requireContext(), "Already saved!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
