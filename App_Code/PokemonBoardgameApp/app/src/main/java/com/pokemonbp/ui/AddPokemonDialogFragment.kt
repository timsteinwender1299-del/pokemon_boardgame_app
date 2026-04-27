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
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.PokemonType
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.databinding.DialogAddPokemonBinding
import com.pokemonbp.model.Pokemon
import com.pokemonbp.model.Team

class AddPokemonDialogFragment(
    private val team: Team,
    private val theme: AppTheme,
    private val onPokemonAdded: (Pokemon) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddPokemonBinding? = null
    private val binding get() = _binding!!
    private val selectedTypes = mutableSetOf<PokemonType>()
    private lateinit var typeAdapter: TypeSelectionAdapter
    private var currentPokedexId = 0
    private var currentName = ""
    private var currentNameDE = ""
    private var selectedBP: Int = -1

    private val bpButtons: List<MaterialButton> by lazy {
        listOf(
            binding.bp0, binding.bp1, binding.bp2, binding.bp3, binding.bp4,
            binding.bp5, binding.bp6, binding.bp7, binding.bp8, binding.bp9,
            binding.bp10, binding.bp11, binding.bp12, binding.bp13, binding.bp14,
            binding.bp15, binding.bp16, binding.bp17, binding.bp18, binding.bp19, binding.bp20
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddPokemonBinding.inflate(LayoutInflater.from(requireContext()))
        val c = ThemeManager.colorsFor(theme)
        val teamColor = if (team == Team.TEAM_A) c.teamA else c.teamB

        binding.screenPanel.setBackgroundColor(c.surface)
        binding.tvDialogTitle.setTextColor(teamColor)
        // Different title depending on team
        binding.tvDialogTitle.text = if (team == Team.TEAM_A)
            "Add Pokémon — Player" else "Add Pokémon — Enemy Trainer"
        if (theme == AppTheme.RETRO) binding.tvDialogTitle.typeface = Typeface.MONOSPACE

        binding.tvTypesLabel.setTextColor(c.textSecondary)

        binding.btnPickPokemon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC0000"))
        binding.btnPickPokemon.setTextColor(Color.WHITE)
        binding.btnPickPokemon.setOnClickListener { openPicker() }

        binding.btnPickFromRoute.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#CC0000"))
        binding.btnPickFromRoute.setTextColor(Color.WHITE)
        binding.btnPickFromRoute.setOnClickListener { openRoutePicker() }

        // BP buttons
        bpButtons.forEachIndexed { index, btn ->
            val bpValue = index
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

        binding.btnCancelPokemon.setOnClickListener { dismiss() }
        binding.btnAddPokemon.setOnClickListener {
            if (selectedTypes.isEmpty()) {
                Toast.makeText(requireContext(), "Pick a Pokémon or select types!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedBP < 0) {
                Toast.makeText(requireContext(), "Select a BP value!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onPokemonAdded(Pokemon(
                id = System.currentTimeMillis().toInt(),
                name = currentName,
                nameDE = currentNameDE,
                types = selectedTypes.toList(),
                baseBP = selectedBP,
                team = team,
                pokedexId = currentPokedexId
            ))
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
            styleBpButton(btn, selected = (index == value), c.accent)
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
        val picker = PokemonPickerDialog(theme) { entry -> applyEntry(entry) }
        picker.show(parentFragmentManager, "PokemonPicker")
    }

    private fun openRoutePicker() {
        RoutePickerDialog(theme) { nameDE, nameEN, bp ->
            val entry = PokedexData.byNameEN[nameEN.trim().lowercase()]
            val types = entry?.types ?: emptyList()
            if (types.isNotEmpty() && bp > 0) {
                onPokemonAdded(Pokemon(
                    id = System.currentTimeMillis().toInt(),
                    name = nameEN,
                    nameDE = nameDE,
                    types = types,
                    baseBP = bp,
                    team = team,
                    pokedexId = entry?.id ?: 0
                ))
                dismiss()
            } else {
                applyRouteEntry(nameDE, nameEN, bp)
            }
        }.show(parentFragmentManager, "RoutePicker")
    }

    private fun applyRouteEntry(nameDE: String, nameEN: String, bp: Int) {
        currentName = nameEN
        currentNameDE = nameDE
        val entry = PokedexData.byNameEN[nameEN.lowercase()]
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
