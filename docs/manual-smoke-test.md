# Manual Smoke Test

This checklist covers the current Fabric rewrite behavior before the standalone config app and later automated integration suite exist. It uses only normal gameplay plus the existing admin commands:

- `/cropbiomelimiter reload-config`
- `/cropbiomelimiter export-registry`

Do not add new in-game diagnostic commands for this pass. The goal is to confirm that normal user-visible surfaces work and that failures remain permissive rather than crashing Minecraft.

## Setup

Run a development client or server with the mod loaded:

```powershell
.\gradlew.bat runClient
```

Use a creative test world with cheats enabled. After first startup, confirm that Minecraft created:

- `run/config/cropbiomelimiter/general.json`
- `run/config/cropbiomelimiter/explicit-mode.json`
- `run/config/cropbiomelimiter/threshold-mode.json`

If any of those files are deleted before startup, the mod should recreate only the missing file and continue using safe in-memory defaults.

## Fixture Config

Use this fixture when testing the three behavior states in the Overworld. The Nether and End can stay on their generated Threshold mode defaults for this pass.

`run/config/cropbiomelimiter/general.json`:

```json
{
  "schema_version": 1,
  "affects_bonemeal": true,
  "affects_block_placement": true,
  "affects_village_farm_generation": true,
  "chat_info": true,
  "excluded_blocks": [],
  "fallback_mode": "threshold",
  "dimensions": {
    "minecraft:overworld": "explicit",
    "minecraft:the_nether": "threshold",
    "minecraft:the_end": "threshold"
  }
}
```

`run/config/cropbiomelimiter/explicit-mode.json`:

```json
{
  "schema_version": 1,
  "fallback": {
    "default_behavior": "growable",
    "biomes": {}
  },
  "dimensions": {
    "minecraft:overworld": {
      "default_behavior": "growable",
      "biomes": {
        "minecraft:plains": {
          "minecraft:wheat": "growable",
          "minecraft:carrots": "bonemeal-required",
          "minecraft:potatoes": "unplantable"
        }
      }
    }
  }
}
```

Keep the generated `threshold-mode.json` in place for this Explicit mode pass. Run `/cropbiomelimiter reload-config` after editing.

For Threshold mode, switch `minecraft:overworld` back to `"threshold"` in `general.json`, then test the generated defaults without hand-editing `threshold-mode.json`.

## Explicit Mode Checks

All checks in this section should be done in a plains biome.

| Case | Action | Expected result |
| --- | --- | --- |
| `growable` wheat placement | Place wheat seeds on farmland | Placement succeeds |
| `growable` wheat bonemeal | Use bone meal on planted wheat | Bone meal succeeds and may advance growth |
| `growable` wheat random growth | Set random tick speed high and wait | Wheat can grow naturally |
| `bonemeal-required` carrot placement | Place carrot on farmland | Placement succeeds and prints "The young plant begins to wilt in this foreign biome." |
| `bonemeal-required` carrot bonemeal | Use bone meal on planted carrots | Bone meal succeeds and may advance growth |
| `bonemeal-required` carrot random growth | Set random tick speed high and wait | When a natural growth tick would advance the immature carrot, it is replaced with dead bush |
| `unplantable` potato placement | Place potato on farmland | Placement is denied and the item is not consumed |
| `unplantable` potato bonemeal | Use bone meal on an already placed potato crop | Bone meal is denied and the item is not consumed |
| fallback crop | Place and bone meal beetroot seeds | Uses the dimension default, so it succeeds |
| fallback biome | Repeat wheat/carrot/potato checks outside plains | Uses the dimension default, so configured plains-only denials do not apply |
| dead bush placement | Place dead bush on a valid vanilla support block | Dead bush placement is not blocked by the mod |
| creative mode bypass | Switch to creative and repeat denied potato placement/bonemeal | Placement and bone meal are not blocked by the mod |

Useful vanilla commands while testing:

```text
/gamemode creative
/gamerule randomTickSpeed 200
/gamerule randomTickSpeed 3
/locate biome minecraft:plains
```

## Threshold Mode Checks

Use generated defaults and test from biomes with clear temperature and precipitation differences.

| Biome type | Example biome | Crop | Expected result |
| --- | --- | --- | --- |
| temperate wet | `minecraft:plains` | wheat | Planting and bone meal succeed; natural growth is allowed |
| hot dry | `minecraft:desert` | cactus | Planting and bone meal behavior are allowed by the hot/dry rule; natural growth is allowed |
| hot wet | `minecraft:jungle` | cocoa or jungle sapling | Planting and bone meal succeed; natural growth is allowed |
| cold wet | `minecraft:snowy_plains` | spruce sapling or sweet berry bush | Planting and bone meal succeed; natural growth is allowed |
| wrong climate | `minecraft:desert` | wheat | Planting succeeds with the wilt warning and bone meal succeeds; when natural growth would advance the immature wheat, it is replaced with dead bush |
| wrong climate | `minecraft:snowy_plains` | cactus | Placement succeeds with the wilt warning; when natural growth would advance it, it is replaced with dead bush |
| wrong dimension crop | Overworld | nether wart | Default Overworld Threshold mode makes it bonemeal-required, so placement warns and natural growth withers on a successful growth tick |
| creative mode bypass | Any prohibited crop/biome pair | any denied crop | Placement and bone meal are not blocked by the mod while the player is in creative mode |

The rewrite intentionally uses `temperature` plus `has_precipitation` only. Do not inspect or depend on numeric rainfall/downfall in this pass.

## Startup And Reload Checks

| Case | Steps | Expected result |
| --- | --- | --- |
| first startup | Delete `run/config/cropbiomelimiter`, then start the game | The folder and all three config files are created |
| one deleted file | Delete only `threshold-mode.json`, then start the game | Only `threshold-mode.json` is recreated; existing files are not overwritten |
| malformed file | Put invalid JSON in `threshold-mode.json`, then start the game | The bad file remains on disk, the mod logs a warning, and gameplay uses defaults |
| reload after valid edit | Edit a rule and run `/cropbiomelimiter reload-config` | New decisions apply without restarting |
| reload after bad edit | Save malformed JSON and run `/cropbiomelimiter reload-config` | The command reports warnings or failure, and gameplay remains permissive/default rather than crashing |
| registry snapshot | Run `/cropbiomelimiter export-registry` | `run/config/cropbiomelimiter` gets `cropbiomelimiter-registry-snapshot.json` with dimensions, biomes, crops, counts, and `config_schema` |

## Results

Record manual runs here until the automated suite exists.

| Date | Minecraft | Fabric API | Environment | Result | Notes |
| --- | --- | --- | --- | --- | --- |
| pending | 26.1.2 | 0.153.0 | dev client | not run | Checklist added before first manual pass |
