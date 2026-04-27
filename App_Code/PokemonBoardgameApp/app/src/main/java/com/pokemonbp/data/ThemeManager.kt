package com.pokemonbp.data

import android.content.Context
import android.graphics.Color

enum class AppTheme(val displayName: String, val emoji: String) {
    DARK("Dark Polished", "🌑"),
    COLORFUL("Bright & Colorful", "🌈"),
    RETRO("Game-Boy Retro", "🕹️"),
    MODERN("Clean Modern", "✨")
}

data class ThemeColors(
    val background: Int,
    val surface: Int,
    val surfaceVariant: Int,
    val onSurface: Int,
    val onSurfaceSecondary: Int,
    val accent: Int,
    val teamA: Int,
    val teamB: Int,
    val positive: Int,
    val negative: Int,
    val cardStroke: Int,
    val divider: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val buttonText: Int
)

object ThemeManager {

    private const val PREF_KEY = "app_theme"
    private const val PREF_FILE = "pokemonbp_prefs"

    private val colorCache = mutableMapOf<AppTheme, ThemeColors>()

    fun save(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit().putString(PREF_KEY, theme.name).apply()
    }

    fun load(context: Context): AppTheme {
        val name = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString(PREF_KEY, AppTheme.DARK.name)
        return AppTheme.values().firstOrNull { it.name == name } ?: AppTheme.DARK
    }

    fun colorsFor(theme: AppTheme): ThemeColors = colorCache.getOrPut(theme) { buildColorsFor(theme) }

    private fun buildColorsFor(theme: AppTheme): ThemeColors = when (theme) {

        AppTheme.DARK -> ThemeColors(
            background       = Color.parseColor("#0D0D0D"),
            surface          = Color.parseColor("#1A1A2E"),
            surfaceVariant   = Color.parseColor("#16213E"),
            onSurface        = Color.parseColor("#E0E0E0"),
            onSurfaceSecondary = Color.parseColor("#888888"),
            accent           = Color.parseColor("#FF6F00"),
            teamA            = Color.parseColor("#EF5350"),
            teamB            = Color.parseColor("#42A5F5"),
            positive         = Color.parseColor("#66BB6A"),
            negative         = Color.parseColor("#EF5350"),
            cardStroke       = Color.parseColor("#2A2A4A"),
            divider          = Color.parseColor("#2A2A4A"),
            textPrimary      = Color.WHITE,
            textSecondary    = Color.parseColor("#AAAAAA"),
            buttonText       = Color.WHITE
        )

        AppTheme.COLORFUL -> ThemeColors(
            background       = Color.parseColor("#F0F4FF"),
            surface          = Color.WHITE,
            surfaceVariant   = Color.parseColor("#E8F0FE"),
            onSurface        = Color.parseColor("#1A1A1A"),
            onSurfaceSecondary = Color.parseColor("#555555"),
            accent           = Color.parseColor("#FF5722"),
            teamA            = Color.parseColor("#E53935"),
            teamB            = Color.parseColor("#1E88E5"),
            positive         = Color.parseColor("#2E7D32"),
            negative         = Color.parseColor("#C62828"),
            cardStroke       = Color.parseColor("#BBDEFB"),
            divider          = Color.parseColor("#E0E0E0"),
            textPrimary      = Color.parseColor("#1A1A1A"),
            textSecondary    = Color.parseColor("#666666"),
            buttonText       = Color.WHITE
        )

        AppTheme.RETRO -> ThemeColors(
            background       = Color.parseColor("#0F380F"),
            surface          = Color.parseColor("#306230"),
            surfaceVariant   = Color.parseColor("#8BAC0F"),
            onSurface        = Color.parseColor("#E0F8D0"),
            onSurfaceSecondary = Color.parseColor("#9BBC0F"),
            accent           = Color.parseColor("#9BBC0F"),
            teamA            = Color.parseColor("#E07050"),
            teamB            = Color.parseColor("#5090D0"),
            positive         = Color.parseColor("#9BBC0F"),
            negative         = Color.parseColor("#E07050"),
            cardStroke       = Color.parseColor("#8BAC0F"),
            divider          = Color.parseColor("#306230"),
            textPrimary      = Color.parseColor("#E0F8D0"),
            textSecondary    = Color.parseColor("#9BBC0F"),
            buttonText       = Color.parseColor("#0F380F")
        )

        AppTheme.MODERN -> ThemeColors(
            background       = Color.parseColor("#FAFAFA"),
            surface          = Color.WHITE,
            surfaceVariant   = Color.parseColor("#F5F5F5"),
            onSurface        = Color.parseColor("#212121"),
            onSurfaceSecondary = Color.parseColor("#757575"),
            accent           = Color.parseColor("#6200EE"),
            teamA            = Color.parseColor("#D32F2F"),
            teamB            = Color.parseColor("#1565C0"),
            positive         = Color.parseColor("#388E3C"),
            negative         = Color.parseColor("#D32F2F"),
            cardStroke       = Color.parseColor("#E0E0E0"),
            divider          = Color.parseColor("#EEEEEE"),
            textPrimary      = Color.parseColor("#212121"),
            textSecondary    = Color.parseColor("#757575"),
            buttonText       = Color.WHITE
        )
    }
}
