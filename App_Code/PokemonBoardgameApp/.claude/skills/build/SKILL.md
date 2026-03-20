---
name: build
description: Build the Pokemon Board Game app debug APK and report any errors
disable-model-invocation: true
allowed-tools: Bash
argument-hint: [assembleDebug|assembleRelease]
---

Build the Pokemon Board Game Android app.

## Steps

1. Set the working directory to the project root:
   `C:/Users/Tim/Dev/pokemon_boardgame_app/App_Code/PokemonBoardgameApp`

2. Determine the build task:
   - If $ARGUMENTS is provided, use that (e.g. `assembleRelease`)
   - Otherwise default to `assembleDebug`

3. Run the build using the local Gradle installation:
   ```
   "C:/Users/Tim/.gradle/wrapper/dists/gradle-9.3.1-bin/23ovyewtku6u96viwx3xl3oks/gradle-9.3.1/bin/gradle.bat" <task>
   ```
   Run from the project root directory.

4. Parse the output and report:
   - **Success**: show the APK path from `app/build/outputs/apk/debug/` and file size
   - **Failure**: extract and show only the actual error lines (e.g. `error:`, `e:`, `FAILED`) — skip Gradle noise
   - **Warnings**: summarise any warnings but don't list them all unless asked

5. If the build fails, suggest the most likely fix based on the error message.
