---
name: update-routes
description: Copy the latest route txt files from the Data folder into app assets and rebuild the app
disable-model-invocation: true
allowed-tools: Bash, Read
---

Sync route data files from the Data folder into the Android app's assets and trigger a rebuild.

## Source and destination

| File | Source | Destination |
|------|--------|-------------|
| RoutesNormal.txt | `C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesNormal.txt` | `app/src/main/assets/RoutesNormal.txt` |
| RoutesLegendary.txt | `C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesLegendary.txt` | `app/src/main/assets/RoutesLegendary.txt` |

Project root: `C:/Users/Tim/Dev/pokemon_boardgame_app/App_Code/PokemonBoardgameApp`

## Steps

1. Verify both source files exist in the Data folder. If either is missing, stop and report which one.

2. Check encoding of each source file — they must be valid UTF-8:
   ```
   python3 -c "open('C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesNormal.txt', encoding='utf-8').read(); print('OK')"
   ```
   If a file is not valid UTF-8, convert it first:
   ```
   python3 -c "
   with open('<source>', encoding='latin-1') as f: content = f.read()
   with open('<source>', 'w', encoding='utf-8') as f: f.write(content)
   print('Converted to UTF-8')
   "
   ```

3. Copy both files to the assets directory:
   ```
   cp "C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesNormal.txt" "app/src/main/assets/RoutesNormal.txt"
   cp "C:/Users/Tim/Dev/pokemon_boardgame_app/Data/RoutesLegendary.txt" "app/src/main/assets/RoutesLegendary.txt"
   ```

4. Confirm the copies succeeded (check file sizes match source).

5. Run a debug build to verify the app compiles with the updated assets:
   ```
   "C:/Users/Tim/.gradle/wrapper/dists/gradle-9.3.1-bin/23ovyewtku6u96viwx3xl3oks/gradle-9.3.1/bin/gradle.bat" assembleDebug
   ```

6. Report: files copied, sizes, and build result (success or errors).
