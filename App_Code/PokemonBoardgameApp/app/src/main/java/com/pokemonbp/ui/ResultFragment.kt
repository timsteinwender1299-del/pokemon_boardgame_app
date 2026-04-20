package com.pokemonbp.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.databinding.FragmentResultBinding
import com.pokemonbp.model.Team
import com.pokemonbp.model.TeamBattleResult

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    var battleResult: TeamBattleResult? = null

    // Trainer display info — set by TeamSetupFragment before navigating
    var teamALabel: String = "Player"
    var teamALabelIconUrl: String? = null
    var teamATrainerImageUrl: String? = null
    var teamBLabel: String = "Enemy Trainer"
    var teamBLabelIconUrl: String? = null
    var teamBTrainerImageUrl: String? = null

    var reverseMode: Boolean = false
    var isChampionBattle: Boolean = false

    // Faint tracking — set by TeamSetupFragment
    var teamAPokemonCount: Int = 1
    var teamBPokemonCount: Int = 1
    // Returns true if ALL of that team's Pokémon are now fainted
    var onYouLost: (() -> Boolean)? = null
    var onYouWon: (() -> Boolean)? = null
    // Called after popup to reset fainted state for the wiped-out team
    var onAllFaintedA: (() -> Unit)? = null
    var onAllFaintedB: (() -> Unit)? = null

    companion object {
        fun newInstance(result: TeamBattleResult) = ResultFragment().also { it.battleResult = result }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val theme = (activity as? MainActivity)?.currentTheme ?: AppTheme.DARK
        val c = ThemeManager.colorsFor(theme)
        val result = battleResult ?: return

        binding.screenPanel.background = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(c.background)
            cornerRadius = 16f * resources.displayMetrics.density
        }

        binding.ivReverseIndicator.setImageResource(
            if (reverseMode) R.drawable.reverse_activated else R.drawable.reverse_deactivated
        )

        val isRetro = theme == AppTheme.RETRO
        if (isRetro) {
            binding.tvWinner.typeface = Typeface.MONOSPACE
        }

        when (result.winner) {
            Team.TEAM_A -> { binding.tvWinner.text = "🏆 Team A Wins!"; binding.tvWinner.setTextColor(c.teamA) }
            Team.TEAM_B -> { binding.tvWinner.text = "🏆 Team B Wins!"; binding.tvWinner.setTextColor(c.teamB) }
            null -> { binding.tvWinner.text = "⚔️ It's a Tie!"; binding.tvWinner.setTextColor(c.accent) }
        }

        binding.tvTeamAResultLabel.text = teamALabel
        binding.tvTeamAResultLabel.setTextColor(c.teamA)
        binding.tvTeamBResultLabel.text = teamBLabel
        binding.tvTeamBResultLabel.setTextColor(c.teamB)

        if (teamALabelIconUrl != null) {
            Glide.with(this).load(teamALabelIconUrl)
                .placeholder(R.drawable.ic_player).error(R.drawable.ic_player)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultLabelA)
        }
        if (teamATrainerImageUrl != null) {
            binding.ivResultTrainerA.visibility = android.view.View.VISIBLE
            Glide.with(this).load(teamATrainerImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultTrainerA)
        }
        if (teamBLabelIconUrl != null) {
            Glide.with(this).load(teamBLabelIconUrl)
                .placeholder(R.drawable.ic_battle).error(R.drawable.ic_battle)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultLabelB)
        }
        if (teamBTrainerImageUrl != null) {
            binding.ivResultTrainerB.visibility = android.view.View.VISIBLE
            Glide.with(this).load(teamBTrainerImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultTrainerB)
        }

        binding.recyclerResultA.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerResultA.adapter = BattleResultAdapter(result.teamA, theme)
        binding.recyclerResultB.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerResultB.adapter = BattleResultAdapter(result.teamB, theme)

        // Style buttons
        val cancelColor = Color.parseColor("#757575")
        val lostColor   = Color.parseColor("#C62828")
        val wonColor    = Color.parseColor("#2E7D32")
        binding.btnCancel.backgroundTintList  = android.content.res.ColorStateList.valueOf(cancelColor)
        binding.btnYouLost.backgroundTintList = android.content.res.ColorStateList.valueOf(lostColor)
        binding.btnYouWon.backgroundTintList  = android.content.res.ColorStateList.valueOf(wonColor)
        binding.btnCancel.setTextColor(Color.WHITE)
        binding.btnYouLost.setTextColor(Color.WHITE)
        binding.btnYouWon.setTextColor(Color.WHITE)
        if (isRetro) {
            binding.btnCancel.typeface  = Typeface.MONOSPACE
            binding.btnYouLost.typeface = Typeface.MONOSPACE
            binding.btnYouWon.typeface  = Typeface.MONOSPACE
        }

        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnYouLost.setOnClickListener {
            val allFainted = onYouLost?.invoke() ?: false
            if (allFainted) {
                showWinnerPopup("🏆 $teamBLabel Wins!") {
                    onAllFaintedA?.invoke()
                    requireActivity().onBackPressed()
                }
            } else {
                requireActivity().onBackPressed()
            }
        }

        binding.btnYouWon.setOnClickListener {
            val allFainted = onYouWon?.invoke() ?: false
            if (allFainted) {
                if (isChampionBattle) {
                    showVictoryScreen {
                        onAllFaintedB?.invoke()
                        requireActivity().onBackPressed()
                    }
                } else {
                    showWinnerPopup("🏆 $teamALabel Wins!") {
                        onAllFaintedB?.invoke()
                        requireActivity().onBackPressed()
                    }
                }
            } else {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun showWinnerPopup(message: String, onDismiss: () -> Unit) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("All Pokémon Fainted!")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> onDismiss() }
            .show()
    }

    private fun showVictoryScreen(onDismiss: () -> Unit) {
        val ctx = requireContext()
        val dialog = android.app.Dialog(ctx, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_victory)
        dialog.setCancelable(false)

        dialog.findViewById<android.widget.TextView>(R.id.tv_victory_player)?.text = teamALabel

        // Trainer image
        val trainerImageView = dialog.findViewById<android.widget.ImageView>(R.id.iv_victory_trainer)
        val imageUrl = teamATrainerImageUrl
        if (imageUrl != null && trainerImageView != null) {
            trainerImageView.visibility = android.view.View.VISIBLE
            com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .fitCenter()
                .into(trainerImageView)
        }

        // Pokemon grid — row1: slots 0-2, row2: slots 3-5
        val row1 = dialog.findViewById<android.widget.LinearLayout>(R.id.layout_victory_row1)
        val row2 = dialog.findViewById<android.widget.LinearLayout>(R.id.layout_victory_row2)
        val pokemonList = battleResult?.teamA?.filter { it.pokemon.pokedexId > 0 } ?: emptyList()
        val density = resources.displayMetrics.density
        val spritePx = (80 * density).toInt()

        fun makePokemonCell(br: com.pokemonbp.model.BattleResult): android.widget.LinearLayout {
            val cell = android.widget.LinearLayout(ctx)
            cell.orientation = android.widget.LinearLayout.VERTICAL
            cell.gravity = android.view.Gravity.CENTER_HORIZONTAL
            val cellParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            cell.layoutParams = cellParams

            val iv = android.widget.ImageView(ctx)
            val ivParams = android.widget.LinearLayout.LayoutParams(spritePx, spritePx)
            iv.layoutParams = ivParams
            iv.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            iv.adjustViewBounds = true
            cell.addView(iv)

            val tv = android.widget.TextView(ctx)
            tv.text = br.pokemon.name
            tv.textSize = 10f
            tv.setTextColor(android.graphics.Color.parseColor("#AAAAFF"))
            tv.gravity = android.view.Gravity.CENTER
            tv.layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            cell.addView(tv)

            val spriteUrl = br.pokemon.spriteUrl()
            if (spriteUrl != null) {
                com.bumptech.glide.Glide.with(this)
                    .load(spriteUrl)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .fitCenter()
                    .into(iv)
            }
            return cell
        }

        pokemonList.forEachIndexed { idx, br ->
            val cell = makePokemonCell(br)
            if (idx < 3) row1?.addView(cell) else row2?.addView(cell)
        }

        dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_victory_continue)
            ?.setOnClickListener {
                dialog.dismiss()
                onDismiss()
            }

        dialog.show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
