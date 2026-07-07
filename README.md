# Crop Biome Limiter

A Fabric rewrite of the original Forge mod for Minecraft 26.1.2.

Crops can only grow where they should. The rewrite starts from a clean Fabric scaffold; the old Forge event/config implementation is intentionally not ported directly.

## Development

```powershell
.\gradlew.bat build
```

Manual in-game smoke-test notes are in [docs/manual-smoke-test.md](docs/manual-smoke-test.md).

The standalone config editor is in [config-app](config-app). It runs as a local static app; serve that folder with any static file server, then open the local URL in a browser.

## Config

The Fabric rewrite uses a clean split config folder at `config/cropbiomelimiter`:

- `general.json` for shared toggles and dimension mode selection.
- `explicit-mode.json` for biome-first Explicit mode rules.
- `threshold-mode.json` for crop-first Threshold mode rules.

The old single-file config format is intentionally not supported.

## Sources

- CurseForge: https://www.curseforge.com/minecraft/mc-mods/crop-biome-limiter
- Repository: https://github.com/TheAtomicOption/CropBiomeLimiter
- Fabric template: https://github.com/FabricMC/fabric-example-mod/tree/26.1.2
