# Sable Lag Fix

**Performance fixes for Sable / Create: Aeronautics.** It cuts autosave freezes caused by sub-level (ship) saving and skips idle physics work in dimensions that have no ships.

Download: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/sable-lag-fix)

> Unofficial patch. Not affiliated with Sable or Create: Aeronautics. If something breaks with this mod installed, please report it **here** first, not to the Sable team.

## What it fixes

### 1. Autosave freezes with many ships
Every autosave, Sable re-serializes, re-compresses and rewrites **every loaded ship**, even ones that did not change since the last save. On worlds with many parked ships this freezes the server thread for seconds.

Sable Lag Fix:
- **Skips rewriting ships whose data did not change.** It compares a SHA-256 digest of the uncompressed data before compressing. Parked ships cost almost nothing on autosave.
- **Compresses changed ships faster** (GZIP level 1). The file format is unchanged.
- **Skips a redundant file-system delete** done for every ship write when no overflow files exist.

**Measured** (singleplayer, 1024 sub-levels, NVMe SSD, Sable 2.0.6):

| | Sable save time |
|---|---|
| Without the mod | ~2.4 s (up to 3.3 s) |
| With the mod, first save after start | ~1.9 s |
| With the mod, later saves (ships unchanged) | **~0.3 s** |

Default settings. Real gains depend on how many of your ships change between saves.

### 2. Physics running in empty dimensions
Sable runs its native physics step every tick in every loaded dimension, even when the dimension has no ships (usually the Nether and the End). Sable Lag Fix skips only the native step in that case. All physics events still fire, so other addons keep working.

## Safety
- Save data stays in the normal Sable format, so your world does not depend on this mod.
- Only the native physics step is skipped, and only when the dimension has no ships and no physics objects.
- If a future Sable update changes something the mod patches, that single fix turns itself off instead of crashing the game.

## Config
`config/sablelagfix-common.toml`:

| Option | Default | What it does |
|---|---|---|
| `skipIdlePhysics` | true | Skip the native physics step in dimensions with no ships |
| `skipUnchangedWrites` | true | Don't rewrite ships whose data didn't change |
| `fastCompression` | true | GZIP level 1 for changed ships |
| `skipRedundantExternalDelete` | true | Skip a pointless delete per write |
| `noPerWriteDiskSync` | **false** | Open ship storage without DSYNC. Faster, but a power loss during a save can corrupt ships written in that save. Only enable it if you accept that risk. |

## Commands
- `/sablelagfix stats`: what the mod skipped and how long saves took.
- `/sablelagfix reset`: reset the counters.

## Requirements
- Minecraft 1.21.1, NeoForge
- Sable 2.0.6 or newer (2.0.x)
- Install it where the world runs: on the server, or in your game for singleplayer.
## License
MIT (this mod's code only). Sable is a separate project under its own license; this mod does not include any Sable code.
