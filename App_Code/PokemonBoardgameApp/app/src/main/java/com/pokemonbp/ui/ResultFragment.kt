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

        binding.screenPanel.setBackgroundColor(c.background)
        binding.dividerResult.setBackgroundColor(c.divider)

        val isRetro = theme == AppTheme.RETRO
        if (isRetro) {
            binding.tvWinner.typeface = Typeface.MONOSPACE
            binding.tvTotalA.typeface = Typeface.MONOSPACE
            binding.tvTotalB.typeface = Typeface.MONOSPACE
        }

        when (result.winner) {
            Team.TEAM_A -> { binding.tvWinner.text = "🏆 Team A Wins!"; binding.tvWinner.setTextColor(c.teamA) }
            Team.TEAM_B -> { binding.tvWinner.text = "🏆 Team B Wins!"; binding.tvWinner.setTextColor(c.teamB) }
            null -> { binding.tvWinner.text = "⚔️ It's a Tie!"; binding.tvWinner.setTextColor(c.accent) }
        }

        binding.tvTotalA.text = "$teamALabel: ${result.teamATotalBP} BP"
        binding.tvTotalA.setTextColor(c.teamA)
        binding.tvTotalB.text = "$teamBLabel: ${result.teamBTotalBP} BP"
        binding.tvTotalB.setTextColor(c.teamB)

        binding.tvTeamAResultLabel.text = teamALabel
        binding.tvTeamAResultLabel.setTextColor(c.teamA)
        binding.tvTeamBResultLabel.text = teamBLabel
        binding.tvTeamBResultLabel.setTextColor(c.teamB)

        // Team A label icon
        if (teamALabelIconUrl != null) {
            Glide.with(this).load(teamALabelIconUrl)
                .placeholder(R.drawable.ic_player).error(R.drawable.ic_player)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultLabelA)
        }
        // Team A trainer image
        if (teamATrainerImageUrl != null) {
            binding.ivResultTrainerA.visibility = android.view.View.VISIBLE
            Glide.with(this).load(teamATrainerImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultTrainerA)
        }

        // Team B label icon
        if (teamBLabelIconUrl != null) {
            Glide.with(this).load(teamBLabelIconUrl)
                .placeholder(R.drawable.ic_battle).error(R.drawable.ic_battle)
                .diskCacheStrategy(DiskCacheStrategy.ALL).fitCenter()
                .into(binding.ivResultLabelB)
        }
        // Team B trainer image
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

        binding.btnNewBattle.backgroundTintList = android.content.res.ColorStateList.valueOf(c.accent)
        binding.btnNewBattle.setTextColor(c.buttonText)
        if (isRetro) binding.btnNewBattle.typeface = Typeface.MONOSPACE
        binding.btnNewBattle.setOnClickListener { requireActivity().onBackPressed() }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
