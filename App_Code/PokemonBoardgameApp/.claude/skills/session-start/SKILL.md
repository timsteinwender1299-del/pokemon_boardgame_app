---
name: session-start
description: Run at the start of every session — verifies git repo, checks file encoding, and summarises recent changes
disable-model-invocation: true
allowed-tools: Bash, Read, Glob
---

Run this at the start of every coding session to get oriented quickly.

## Steps

### 1. Verify git repo
From `C:/Users/Tim/Dev/pokemon_boardgame_app`:
```
git status --short
git log --oneline -5
```
- Confirm we are inside the repo (if `git status` fails, stop and report the issue)
- Show the current branch and the 5 most recent commits

### 2. Check for uncommitted source changes
List only non-build files that are modified or untracked:
```
git status --short | grep -v "app/build" | grep -v ".gradle" | grep -v ".idea"
```
Summarise: how many source files changed, which ones are new vs modified.

### 3. Check encoding of route asset files
Verify the two critical asset files are valid UTF-8:
```
python3 -c "
files = [
    'App_Code/PokemonBoardgameApp/app/src/main/assets/RoutesNormal.txt',
    'App_Code/PokemonBoardgameApp/app/src/main/assets/RoutesLegendary.txt',
]
for f in files:
    try:
        open(f, encoding='utf-8').read()
        print(f'OK: {f}')
    except Exception as e:
        print(f'ENCODING ISSUE: {f} — {e}')
"
```
Report any issues. If a file fails, run `/update-routes` to fix it.

### 4. Summary
Output a short session briefing:
- Current branch
- Last commit message and date
- Uncommitted source changes (if any)
- Encoding status of asset files
- Suggested next step based on what you see (e.g. "2 uncommitted files — consider committing before starting new work")
