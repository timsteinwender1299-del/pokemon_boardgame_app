---
description: Verify git repo, check encoding, and summarise recent changes to get oriented quickly
allowed-tools: Bash(git status:*), Bash(git log:*)
---

## Context

- Git status (source files only): !`cd C:/Users/Tim/Dev/pokemon_boardgame_app && git status --short | grep -v "app/build" | grep -v ".gradle" | grep -v ".idea"`
- Recent commits: !`cd C:/Users/Tim/Dev/pokemon_boardgame_app && git log --oneline -5`
- Asset encoding check: !`cd C:/Users/Tim/Dev/pokemon_boardgame_app && python3 -c "files=['App_Code/PokemonBoardgameApp/app/src/main/assets/RoutesNormal.txt','App_Code/PokemonBoardgameApp/app/src/main/assets/RoutesLegendary.txt'];[print('OK: '+f) if open(f,encoding='utf-8').read() or True else None for f in files]" 2>&1 || echo "ENCODING ISSUE detected"`

## Your task

Using the context above, output a short session briefing covering:
1. Current branch and last commit (message + date)
2. Uncommitted source changes — how many files, which are new vs modified
3. Encoding status of the two route asset files
4. A suggested next step based on what you see

Keep it concise. If there are encoding issues, recommend running `/update-routes` to fix them.
