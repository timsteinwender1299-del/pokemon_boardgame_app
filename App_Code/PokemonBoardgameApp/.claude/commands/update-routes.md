---
description: Copy the latest route txt files from Data/ into app assets and rebuild
allowed-tools: Bash
---

## Your task

Sync route data files from the Data folder into the Android app's assets.

**Source → Destination:**
- `C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesNormal.txt` → `app/src/main/assets/RoutesNormal.txt`
- `C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesLegendary.txt` → `app/src/main/assets/RoutesLegendary.txt`

Run all commands from `C:/Users/Tim/Dev/pokemon_boardgame_app`.

**Steps:**

1. Verify both source files exist. Stop and report if either is missing.

2. Check UTF-8 encoding of each file:
   ```
   python3 -c "open('Data/RoutesNormal.txt', encoding='utf-8').read(); print('OK')"
   python3 -c "open('Data/RoutesLegendary.txt', encoding='utf-8').read(); print('OK')"
   ```
   If a file fails, convert it first (latin-1 → utf-8) before copying.

3. Copy both files to assets:
   ```
   cp Data/RoutesNormal.txt App_Code/PokemonBoardgameApp/app/src/main/assets/RoutesNormal.txt
   cp Data/RoutesLegendary.txt App_Code/PokemonBoardgameApp/app/src/main/assets/RoutesLegendary.txt
   ```

4. Confirm file sizes match source files.

5. Run a debug build:
   ```
   cd App_Code/PokemonBoardgameApp && "C:/Users/Tim/.gradle/wrapper/dists/gradle-9.3.1-bin/23ovyewtku6u96viwx3xl3oks/gradle-9.3.1/bin/gradle.bat" assembleDebug
   ```

6. Report: files copied, sizes, and build result.
