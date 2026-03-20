---
description: Build the Pokemon Board Game debug APK and report any errors
allowed-tools: Bash
---

## Your task

Build the Pokemon Board Game Android app.

1. Run the build from the project root `C:/Users/Tim/Dev/pokemon_boardgame_app/App_Code/PokemonBoardgameApp`:
   - If $ARGUMENTS is provided, use that as the Gradle task (e.g. `assembleRelease`)
   - Otherwise default to `assembleDebug`

   ```
   cd "C:/Users/Tim/Dev/pokemon_boardgame_app/App_Code/PokemonBoardgameApp" && "C:/Users/Tim/.gradle/wrapper/dists/gradle-9.3.1-bin/23ovyewtku6u96viwx3xl3oks/gradle-9.3.1/bin/gradle.bat" assembleDebug
   ```

2. Report results:
   - **Success**: show the APK path from `app/build/outputs/apk/debug/` and file size
   - **Failure**: show only the actual error lines (`error:`, `e:`, `FAILED`) — skip Gradle noise
   - If build fails, suggest the most likely fix based on the error
