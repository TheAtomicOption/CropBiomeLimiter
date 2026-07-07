# Fabric Rewrite Plan

## Source behavior to preserve

Crop Biome Limiter should keep the original user-facing promise: crops can only grow where they should. The CurseForge description calls out three main enforcement surfaces: natural crop growth, bonemeal use, and seed/block placement. The old Forge implementation also had dimension allow/deny behavior, excluded blocks, biome-specific crop overrides, biome type lists, temperature/precipitation biome groups, and optional chat feedback when placement or bonemeal is blocked.

## Top-level reliability requirement

Because this code runs inside Minecraft, no mod-owned top-level entrypoint should ever surface an uncaught exception to the game. Initializers, event callbacks, mixin injections, config loading, command handlers, and registry export code must fail closed around the exception boundary and then return a safe default behavior.

- The safe default is permissive gameplay: if config, registry lookup, biome lookup, parsing, or rule resolution fails, allow planting, natural growth, and bonemeal rather than crashing or trapping the game loop.
- Prefer `try...` patterned APIs for operations that can fail, such as `tryGetValue`, `tryResolve`, `tryParse`, or `tryCreate`. These should return `Optional`, nullable values with clear local handling, or explicit fallback result records instead of throwing for ordinary bad config or unknown registry ids.
- When a method signature is dictated by Minecraft/Fabric and cannot expose a try-pattern result, catch internal exceptions at the callback/mixin/command boundary, log once or at a rate-limited/debug level as appropriate, and return the safe permissive result.
- Reserve thrown exceptions for construction-time programmer errors that cannot reach Minecraft callbacks. Config and data supplied by users or modpacks should be treated as untrusted input and converted to defaults plus diagnostics.
- Tests should include malformed config, unknown crop ids, unknown biome ids, missing dimension rules, and resolver failures to prove the mod falls back without surfacing exceptions.

## API findings for Minecraft 26.1.2 / Fabric API 0.153.0

Fabric has useful player interaction callbacks, but it does not appear to have a Forge-style crop growth callback. The implementation should use public Fabric events for player actions and mixins only for the natural growth path.

- `ItemEvents.USE_ON` wraps `Item#useOn(UseOnContext)` before vanilla item use. This is the best first hook for seed/block placement and for server-side bonemeal denial because it has the clicked position, item stack, player, hand, and level.
- `BlockEvents.USE_ITEM_ON` runs when `BlockState#useItemOn(...)` is called. Use it as a secondary interaction hook for block-side item behavior that `ItemEvents.USE_ON` may not fully cover, while keeping placement enforcement owned by `ItemEvents.USE_ON`.
- `UseBlockCallback` and `UseItemCallback` still exist, but the grouped `BlockEvents` and `ItemEvents` callbacks are closer to the modern vanilla method names and expose better context for this mod.
- `BlockBehaviour.BlockStateBase#randomTick(ServerLevel, BlockPos, RandomSource)` dispatches to the block's own `randomTick`. A cancellable mixin at the head of this common method can stop natural growth for any tracked growable block without adding one mixin per crop class.
- `BoneMealItem#useOn` delegates to `BoneMealItem.growCrop(ItemStack, Level, BlockPos)`. `growCrop` checks `BonemealableBlock#isValidBonemealTarget`, calls `performBonemeal`, then shrinks the item stack. If `ItemEvents.USE_ON` is not precise enough for bonemeal feedback or client/server return behavior, mix into `BoneMealItem#useOn` before that delegate call.
- Vanilla block tags help but are not complete. `BlockTags.CROPS` covers farmland crops and stems, while cactus, sugar cane, nether wart, cocoa, mushrooms, saplings, and berry/vine-style plants need explicit defaults or config entries. Fabric's conventional item tags include `ConventionalItemTags.SEEDS` and `ConventionalItemTags.CROPS`, but those are item tags and should not be the only source of growable-block detection.
- Biome lookup should use `Level#getBiome(pos)` as a `Holder<Biome>`. Explicit mode matches by biome id. Threshold mode intentionally uses only the biome public base temperature plus `has_precipitation` bool in the first Fabric rewrite.
- Old `Biome#getRainfall()` is gone, and the Fabric rewrite should not chase the private numeric downfall value. Climate-group matching should use public `Biome#getBaseTemperature()` plus `Biome#hasPrecipitation()` / precipitation helpers only in the first pass.

## Phase 1: Build the decision core before wiring hooks

- Add `CropDecisionService` with methods for `canGrowNaturally(ServerLevel, BlockPos, BlockState)`, `canUseBonemeal(ServerLevel, BlockPos, BlockState, Player)`, and `canPlace(Level, BlockPos, Block, Player)`. Public decision methods must not throw; if resolution fails internally, they should log and return permissive `true`.
- Add a fast `GrowableBlockClassifier` that starts from explicit config ids, then checks vanilla/default block ids, `BlockTags.CROPS`, and known growable interfaces/classes such as `BonemealableBlock` only when needed.
- Keep the event/mixin classes thin: gather level, position, state/block/item, call the decision service, and cancel or pass. These boundary classes should wrap mod-owned logic in internal try blocks because Fabric/Minecraft callback signatures cannot express `try` results.
- Add logging around denied growth at debug level only; natural random ticks can be extremely noisy.

## Phase 2: Player action enforcement with Fabric callbacks

- Register `ItemEvents.USE_ON` from the main initializer. The initializer should catch config/default setup failures, install permissive fallback rules, and continue loading.
- Placement path: when the held item is a `BlockItem`, use `BlockItem#getBlock()` plus `new BlockPlaceContext(context).getClickedPos()` to identify the candidate block and position. If `affectsBlockPlacement` is enabled and the decision service denies it, return a non-null failure result on the server and optionally send the chat message.
- Bonemeal path: when the held item is bone meal, evaluate the clicked block state. If `affectsBonemeal` is enabled and the decision service denies it, return a non-null failure result on the server so the stack is not consumed. If client prediction or feedback is poor, replace this with a focused `BoneMealItem#useOn` mixin.
- Keep the client side conservative at first: return `null`/pass on the client and let the server be authoritative, then adjust only if testing shows confusing prediction artifacts.
- Use `BlockEvents.USE_ITEM_ON` as a focused fallback for block-side bone meal handling. Add broader plant interactions, such as harvesting/interacting with berry or vine blocks, only if they become explicit feature surfaces rather than incidental use cases.

## Phase 3: Natural growth enforcement with a common mixin

- Add `cropbiomelimiter.mixins.json` and a cancellable mixin into `BlockBehaviour.BlockStateBase#randomTick(ServerLevel, BlockPos, RandomSource)`. The mixin must catch mod-owned resolver exceptions and allow the original random tick to continue.
- At the head of `randomTick`, get the current `BlockState`, quickly skip untracked blocks, and call `CropDecisionService#canGrowNaturally`.
- Cancel the callback when denied. This should cover vanilla crops, stems, saplings, cocoa, sugar cane, cactus, mushrooms, nether wart, berries, pitcher/torchflower crops, and most modded random-ticking growables once the classifier recognizes them.
- Add a fallback compatibility note: if a mod grows plants outside random ticks or bonemeal, it may need a dedicated integration later.

## Phase 4: Config model

- Choose a small JSON/TOML loader or a dedicated config library that works cleanly on dedicated servers, and wrap it behind exception-free loader APIs such as `tryLoadConfig` and `tryWriteDefaultConfig`.
- Make configuration dimension-scoped, with each dimension using one of two official modes: Explicit mode or Threshold mode.
- Treat these names as canonical throughout the mod, not just the standalone app. Java types, config schema, commands, logs, generated defaults, tests, docs, and UI labels should all use Explicit mode and Threshold mode consistently.
- Store the mode explicitly in the shared config, for example as dimension entries of `"minecraft:overworld": "threshold"` or `"minecraft:the_nether": "explicit"`, and map those values to a Java enum such as `RuleMode.EXPLICIT` and `RuleMode.THRESHOLD`. Bad or missing mode values should produce diagnostics and fall back to a known default mode instead of throwing out of the loader.
- Name implementation records and services after the modes, for example `ExplicitModeRules`, `ThresholdModeRules`, `ExplicitModeResolver`, and `ThresholdModeResolver`, with `CropDecisionService` dispatching by `RuleMode`.
- Explicit mode defines crop behavior directly per crop, per biome, per dimension. Its core data shape is a matrix keyed by dimension id, crop block id, and biome id, with each cell resolving to `growable`, `bonemeal-required`, or `unplantable`.
- Threshold mode infers crop behavior from the biome's public climate data. Its core data shape is a set of crop rules keyed by dimension id and crop block id, where each rule uses threshold temperatures plus the biome `has_precipitation` bool to produce `growable`, `bonemeal-required`, or `unplantable`.
- Represent crop ids, biome ids, biome tags, dimension ids, and Threshold mode climate rules as typed records instead of raw strings scattered through the logic. A Threshold mode rule should use base-temperature ranges plus a precipitation requirement: required, forbidden, or ignored.
- Preserve the old toggles: affects bonemeal, affects block placement, chat feedback, and excluded blocks. Prefer the two named modes over reviving the old dimension/crop/biome whitelist and blacklist mode combinations.
- Store config under its own folder inside the normal Minecraft config directory: `config/cropbiomelimiter/general.json` for shared settings and dimension mode selection, `config/cropbiomelimiter/explicit-mode.json` for biome-first Explicit mode rules, and `config/cropbiomelimiter/threshold-mode.json` for crop-first Threshold mode rules. This is a clean rewrite, so no backwards compatibility with old single-file config layouts is required.
- During Minecraft startup, before the decision service is used, the mod must ensure the split config folder exists and must write default `general.json`, `explicit-mode.json`, and `threshold-mode.json` files for any of those files that are missing or were deleted before startup. Existing malformed files should be left untouched and replaced only in memory by safe defaults for that run.
- Default Threshold mode rules should use simple climate checks only: hot plus no precipitation for dry plants, moderate/warm plus precipitation for temperate and wet plants, low-to-moderate plus precipitation for cool wet plants, Nether hot/dry rules for Nether plants, and End dry-temperate rules for chorus plants.
- Default Threshold mode rules should be tuned for gameplay travel: every vanilla biome climate should have at least one vanilla crop that can naturally grow there, while nonmatching crop climates default to `bonemeal-required` so placement succeeds with a warning, bone meal still works, and successful natural growth attempts turn the immature plant into dead bush. The overlap should push players toward roughly four Overworld farming bases plus Nether and End farms rather than one universal farm.
- Village farm generation should optionally align vanilla farm processor lists with the Threshold mode food-crop defaults. The default setting is enabled and maps coldest to warmest village crops as beetroot, potato, wheat, then carrot, with overlap between nearby buckets. A shared `general.json` option must disable this hook for modpacks where another mod owns village generation.
- Cosmetic plant defaults should be checked against vanilla biome feature registration, not only hand-maintained climate notes. Tracked cosmetic plants such as grasses, ferns, bushes, moss, and wildflower-style plants must remain growable in every Overworld biome where vanilla worldgen can place them, even when Threshold mode has to broaden a climate bucket because two biomes share the same temperature and precipitation values.
- Do not model numeric rainfall/downfall in the first Fabric rewrite. If a future need appears, prefer explicit biome tags over accessor mixins unless there is a compelling compatibility case.

## Phase 5: Crop and biome matching

- Use registry identifiers and tags where possible rather than hard-coding old class checks such as `BlockCrops`.
- In Explicit mode, resolve directly from dimension id, crop block id, and biome id. In Threshold mode, resolve from dimension id, crop block id, biome base temperature, and biome precipitation presence. Resolver internals may use `tryResolve` helpers, but top-level decisions must fall back to `growable`.
- Support explicit block overrides by block id, with item ids used only for placement helpers and user-facing config convenience.
- Decide the default behavior for unknown modded crops early. A conservative first version should allow unknown growables unless they are explicitly listed, then add an optional strict mode for old-mod parity.
- Keep exclusions as a first-class escape hatch for compatibility problems.

## Phase 6: Standalone configuration app

- Build a simple standalone configuration editor that is not loaded by Minecraft and does not depend on a running client or server.
- Have the app read and write the same split config folder format as the mod, including the same Explicit mode and Threshold mode values in `general.json`, so the app is a safer visual editor rather than a second source of truth.
- Present one dimension page or tab at a time: Overworld, Nether, End, and any modded dimensions discovered from an imported registry/config snapshot.
- Put the mode selector at the dimension level: Explicit mode for direct crop-by-biome editing, or Threshold mode for temperature/precipitation-derived behavior.
- In Explicit mode, show crops as rows and biomes as columns. Rows and columns should support filtering/search so large modpacks remain usable.
- In Threshold mode, show crops as rows with threshold-temperature controls and precipitation requirements, plus a read-only or generated preview grid showing how each biome will resolve.
- Use a three-state behavior model in both modes: `growable` means planting, natural growth, and bonemeal are allowed; `bonemeal-required` means planting is allowed, natural growth is blocked, and bonemeal is allowed; `unplantable` means planting, natural growth, and bonemeal are denied.
- In Explicit mode, let users click a cell to cycle states, click a crop row header to apply a state across all biomes in the current dimension, and click a biome column header to apply a state across all crops in the current dimension.
- In Threshold mode, let users edit thresholds in bulk by crop row and preview the resulting biome classifications before saving.
- Add bulk actions for copying rules between dimensions, switching a dimension between Explicit mode and Threshold mode, resetting a dimension to defaults, and importing/exporting config files.
- Feed the app with vanilla defaults plus optional registry snapshot data generated by a dev/server command, so modded dimensions, biomes, and crops can appear without hard-coding them into the app. The snapshot should include enough summary metadata for the app to validate it loaded plausible data.
- Keep the first version plain and local: a small desktop app or local static web app is enough if it can load a config, edit the grid, validate unknown ids, and save clean JSON/TOML.

## Phase 7: Test and release loop

- Add unit tests for config parsing, `RuleMode` serialization, Explicit mode resolution, Threshold mode resolution, block classification, biome tag matching, dimension behavior, and exception-free fallback behavior.
- Add in-game smoke tests for natural random growth, bonemeal, and placement denial.
- Later, build a fully automated command-driven integration suite. It should create controlled test biomes and test crop blocks, simulate a player attempting seed placement and bone meal use for each behavior case, and assert that the observed result matches the configured Explicit mode or Threshold mode rule. Do this after the core mod and config app schema settle, not during the initial implementation slices.
- Smoke test vanilla wheat, carrots, potatoes, beetroots, melon/pumpkin stems, torchflower, pitcher crop, cactus, sugar cane, cocoa, nether wart, mushrooms, saplings, sweet berries, and cave vines across Explicit mode cells and Threshold mode hot/dry, temperate, cold, wet, and precipitation cases.
- Test dedicated server startup, client startup, config generation, chat feedback, and modded-crop fallback behavior.
- Only after the Fabric version reaches feature parity, revisit optional compatibility defaults for popular crop/biome mods.

## First implementation slice

1. Add the config shell with `RuleMode`, Explicit mode and Threshold mode records, exception-free `try` helpers, and hard-coded permissive defaults so the hooks can be tested before a full config UI/format is polished.
2. Implement `CropDecisionService` and `GrowableBlockClassifier` with vanilla defaults and temperature/precipitation Threshold mode matching.
3. Add `ItemEvents.USE_ON` handling for placement and bonemeal.
4. Add the common `randomTick` mixin for natural growth.
5. Add a minimal registry/config export command that the standalone configuration app can consume later.
6. Run a focused manual test world with one Explicit mode dimension and one Threshold mode dimension before expanding the default crop table.

## Config file schema v1

The Fabric mod now reads and writes a `cropbiomelimiter` folder inside Fabric Loader's normal config directory. The clean rewrite does not support the old single-file config layout. On Minecraft startup, the loader creates the folder if needed and writes default copies of any missing split config files, including files the user or a pack deleted before startup. The loader is intentionally tolerant: missing fields inherit defaults, invalid ids or values produce diagnostics, malformed files are left untouched, and the game uses safe in-memory defaults for that run.

Files:

- `config/cropbiomelimiter/general.json`: settings shared by both modes plus per-dimension mode selection.
- `config/cropbiomelimiter/explicit-mode.json`: biome-first Explicit mode rules.
- `config/cropbiomelimiter/threshold-mode.json`: crop-first Threshold mode rules.

`general.json`:

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
    "minecraft:overworld": "threshold",
    "minecraft:the_nether": "threshold",
    "minecraft:the_end": "threshold"
  }
}
```

`explicit-mode.json` is biome-first so it is readable as a biome/crop grid without the standalone app:

```json
{
  "schema_version": 1,
  "fallback": {
    "default_behavior": "bonemeal-required",
    "biomes": {}
  },
  "dimensions": {
    "minecraft:overworld": {
      "default_behavior": "bonemeal-required",
      "biomes": {
        "minecraft:desert": {
          "minecraft:wheat": "bonemeal-required",
          "minecraft:spruce_sapling": "unplantable"
        }
      }
    }
  }
}
```

`threshold-mode.json` is crop-first because Threshold mode is edited most naturally by crop. `precipitation` is one of `required`, `forbidden`, or `ignored`, and it is matched against the biome's public `has_precipitation` value.

```json
{
  "schema_version": 1,
  "fallback": {
    "default_rule": {
      "default_behavior": "bonemeal-required",
      "climate_rules": []
    },
    "crops": {}
  },
  "dimensions": {
    "minecraft:overworld": {
      "default_rule": {
        "default_behavior": "bonemeal-required",
        "climate_rules": []
      },
      "crops": {
        "minecraft:wheat": {
          "default_behavior": "bonemeal-required",
          "climate_rules": [
            {
              "min_temperature": 0.15,
              "max_temperature": 1.5,
              "precipitation": "required",
              "behavior": "growable"
            }
          ]
        }
      }
    }
  }
}
```
## Current implementation status

Completed in the first Fabric branch slice:

- Fabric 26.1.2 scaffold is in place with the mod id `cropbiomelimiter`, Fabric entrypoint, and `randomTick` mixin config.
- The core config shell exists with canonical Explicit mode and Threshold mode names, three crop behaviors, temperature plus `has_precipitation` Threshold mode data, dimension-scoped rules, bonemeal-required unknown-crop fallbacks, and expanded vanilla Threshold mode defaults for the tracked vanilla growable plant set, including all sapling varieties. The default vanilla table is now climate-strategic rather than globally permissive: cold, cool, temperate, hot dry, Nether, and End crop groups overlap enough to avoid one-base-per-biome farming while still requiring multiple farming bases.
- The decision service and Fabric hooks are wired for natural growth, bone meal, and placement. Top-level initializer, event callback, mixin, and command surfaces catch mod-owned failures and return permissive gameplay behavior instead of surfacing exceptions to Minecraft. The decision service also exposes testable dimension/biome overloads so core rule behavior can be verified without mocking a full `ServerLevel`, and growable block classification now fails closed to "not tracked" for null or unsafe block-state inspection.
- Config parser helpers now expose `tryFromSerializedName` / `tryId` style APIs and non-throwing defaults for ordinary bad config values. The JSON config loader/writer uses `tryLoadConfig` and `tryWriteDefaultConfig` around the split `config/cropbiomelimiter` folder, writes `general.json`, `explicit-mode.json`, and `threshold-mode.json` on first run, reports exactly which missing files were restored, and leaves malformed user files untouched while using in-memory defaults. The mod jar also restores or refreshes the bundled standalone config app into `config/cropbiomelimiter/config-app` when the app folder is missing, an app file is missing, or a managed app file is stale.
- A minimal `/cropbiomelimiter export-registry` server command exports dimensions, biomes, growable crop block ids, mode names, behavior names, precipitation requirements, summary counts, and a `config_schema` metadata block to `cropbiomelimiter-registry-snapshot.json` in the world folder for the future standalone configuration app. Its existing success response reports the exported dimension, biome, and growable-block counts.
- An admin `/cropbiomelimiter reload-config` command reloads the split config folder at runtime and swaps in a fresh decision service, falling back to permissive behavior if reload fails. Startup logging and the existing reload command response now report the exact missing default files restored and when the load completed with config warnings, without adding more diagnostic commands.
- Player action enforcement now separates bone meal checks from placement checks, uses the vanilla `BlockPlaceContext` target and resolved placement `BlockState` before denying placement, registers a focused `BlockEvents.USE_ITEM_ON` fallback for block-side bone meal handling, and allows invalid or non-placeable clicks to fall through to vanilla instead of producing false climate denials.
- Dependency-free unit tests now run from `runUnitTests` during `check` through a small `SmokeTestSuite` runner, with focused config-loader, core-behavior, village-farm processor, and vanilla worldgen cosmetic-default test classes. Coverage includes default config creation, deleted split-file recreation on startup, malformed config fallback, unknown serialized values, Explicit mode parsing, Threshold mode parsing, default schema serialization, config-app schema metadata, default coverage for every tracked vanilla growable plant, default Threshold mode climate strategy across representative vanilla biome climates, vanilla worldgen-compatible cosmetic plant defaults, vanilla village farm crop buckets, growable block classifier safety, the three crop behavior action surfaces, Explicit mode biome-cell resolution, Threshold mode temperature plus precipitation resolution, decision-service action decisions, placement-state decisions, enforcement toggles, exclusions, untracked blocks, and missing dimension/biome fail-open behavior.
- A manual in-game smoke-test checklist now exists at `docs/manual-smoke-test.md`. It covers first-start config generation, deleted split-file restoration, malformed config fallback, Explicit mode placement/natural-growth/bonemeal behavior, Threshold mode temperature plus precipitation behavior, config reload, and registry snapshot export. The checklist is ready for the first manual game pass; no extra diagnostic commands were added.
- The standalone configuration app now exists in `config-app/` as a dependency-free local static app and is bundled into the mod jar. Minecraft startup restores it into `config/cropbiomelimiter/config-app`, including Windows, Linux, and macOS launchers that start a Java helper from the installed mod jar and open the browser. The helper serves the app locally and exposes a loopback-only API for loading and saving `general.json`, `explicit-mode.json`, and `threshold-mode.json` directly. The app can load an optional registry snapshot; validate imported files and snapshot counts; edit shared settings and dimension modes; copy the active dimension mode/rules to another dimension; reset the active dimension; show an Explicit mode crop-by-biome grid with clickable crop rows, biome columns, and cells; show Threshold mode crop rows with temperature and `has_precipitation` controls plus a biome preview; save installed config files through the helper; and still download one or all split JSON files as a browser-only fallback.
- `gradlew build` passes after these changes.

Plan completion status:

- Core Fabric rewrite, split config generation/loading, Explicit mode, Threshold mode, vanilla default crop coverage, optional vanilla village farm alignment, player placement enforcement, bone meal enforcement, natural random-growth enforcement, registry export, runtime reload, standalone config editing, and dependency-free unit/smoke tests are implemented.
- The remaining work is validation and polish rather than core feature construction: run the manual game pass, fix any discovered interaction gaps, then build the later automated command-driven in-game integration suite once the behavior is stable.

Remaining validation and release slice:

1. Run the manual game pass from `docs/manual-smoke-test.md`, recording results and any interaction gaps without adding more in-game diagnostic commands.
2. Check plant interactions beyond planting, natural growth, and bone meal, such as berry or vine interactions, and add focused hooks only if they become explicit feature surfaces.
3. Build the later automated command-driven integration suite that creates controlled test biomes/blocks, simulates player placement and bone meal attempts, and asserts the configured Explicit mode and Threshold mode outcomes.
4. Do a release pass: confirm generated default configs in a fresh instance, confirm dedicated-server startup, review README instructions, and package the Fabric jar.
