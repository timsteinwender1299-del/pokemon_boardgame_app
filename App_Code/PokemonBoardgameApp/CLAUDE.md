# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Full build (includes lint, tests)
./gradlew build
```

There are no automated tests in this project. Android Studio is the recommended IDE for running the app on an emulator or device.

## Architecture

Single-Activity Android app (Kotlin, minSdk 24, targetSdk 34) using ViewBinding and fragment-based navigation. No ViewModel, LiveData, or Room — state is managed in fragments and persisted via SharedPreferences (JSON serialization).

**Package structure:** `com.pokemonbp`

### Three layers:

**`model/`** — All domain models in a single file (`Pokemon.kt`):
- `Pokemon` — instance with name (EN + DE), types, base BP
- `PokemonPreset` — reusable template
- `Team` enum — Team A (Player) vs Team B (Enemy)
- `PlayerTrainer` — saved profile with a team
- `EnemyTrainer` — sealed class covering GymLeader, Champion, Wild, Random, SavedTrainer
- `BattleResult` / `TeamBattleResult` — battle outcome data

**`data/`** — Business logic and data sources:
- `BattleCalculator.kt` — orchestrates team vs team battle resolution
- `TypeChart.kt` — `PokemonType` enum (18 types) + effectiveness matrix + immunity rules and BP modifier logic
- `EvolutionData.kt` — maps Pokédex IDs to next/previous evolutions (Gen 1–4 + Megas); supports evolve and devolve
- `PresetManager.kt` / `TrainerManager.kt` — SharedPreferences-backed persistence
- `ThemeManager.kt` — 4 themes persisted to SharedPreferences
- `SinnohData.kt` — hardcoded Sinnoh gym leader teams (badge-level variants)
- `SpriteUrls.kt` / `GlideConfig.kt` — PokeAPI sprite loading via Glide + OkHttp3

**`ui/`** — Single `MainActivity` hosts two fragments:
- `TeamSetupFragment` → main screen, team assembly, trainer selection
- `ResultFragment` → battle result display
- `PokedexData.kt` — full Pokédex (Gen 1–9 + Megas) as `PokedexEntry` list; source of truth for names, types, and sprite IDs
- `PokedexHelper.kt` — name-to-ID lookup map for search
- `PokemonPickerDialog.kt` — searchable picker backed by `PokedexData`
- Multiple other `DialogFragment`s for adding Pokémon, selecting trainers, picking avatars

### Battle calculation rules (TypeChart.kt)

1. Look up type effectiveness for each attacker type vs each defender type.
2. Effectiveness 1.5x → +1 BP modifier; 0.5x → -1 BP modifier.
3. **Single-type immunity**: if a Pokemon has one type and any matchup is 0x → final BP = 0.
4. **Multi-type immunity**: one type has 0x → -2 BP for that type, other type matchups cancelled.
5. Final BP = max(0, baseBP + totalModifier).

### Theme system

Four themes (Dark, Colorful, Retro Game-Boy, Modern) each define their own `style.xml` resource and are applied by restarting `MainActivity` via `ThemeManager`.

### Persistence

All persistence uses SharedPreferences with JSON string serialization — no SQLite or Room. Keys are defined within each Manager class.