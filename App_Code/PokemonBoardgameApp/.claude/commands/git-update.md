---
description: Stage all source file changes, commit with an auto-generated message, and push to origin main
allowed-tools: Bash
---

## Your task

Commit and push all local source file changes to the git repo at `C:/Users/Tim/Dev/pokemon_boardgame_app`.

1. Run `git status --short` from `C:/Users/Tim/Dev/pokemon_boardgame_app` to find all changed/untracked files.

2. Stage ONLY meaningful source files — never build artifacts:
   - **Stage** anything under: `App_Code/PokemonBoardgameApp/app/src/`, `App_Code/PokemonBoardgameApp/.claude/`, `Data/`, `Images/`, `Font/`
   - **Never stage**: `App_Code/PokemonBoardgameApp/app/build/`, `App_Code/PokemonBoardgameApp/.gradle/`, `App_Code/PokemonBoardgameApp/build/`

3. Write a concise 1–2 sentence commit message summarising what changed, based on the diff.

4. Commit using a HEREDOC with the co-author trailer:
   ```
   Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
   ```

5. Push to `origin main`.

6. Report back: how many files were committed and the commit hash.
