package com.pokemonbp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pokemonbp.R
import com.pokemonbp.data.AppTheme
import com.pokemonbp.data.ThemeManager
import com.pokemonbp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var currentTheme: AppTheme = AppTheme.DARK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentTheme = ThemeManager.load(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyThemeBackground()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TeamSetupFragment())
                .commit()
        }
    }

    fun applyThemeBackground() {
        val colors = ThemeManager.colorsFor(currentTheme)
        binding.root.setBackgroundColor(colors.background)
    }

    fun changeTheme(theme: AppTheme) {
        currentTheme = theme
        ThemeManager.save(this, theme)
        applyThemeBackground()
        // Refresh current fragment
        val frag = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (frag != null) {
            supportFragmentManager.beginTransaction()
                .detach(frag).attach(frag).commit()
        }
    }

    fun navigateToResults(resultFragment: ResultFragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, resultFragment)
            .addToBackStack(null)
            .commit()
    }

}
