# Crop Biome Limiter

A Fabric rewrite of the original Forge mod for Minecraft 26.1.2.

Crops can only grow where they should. The rewrite starts from a clean Fabric scaffold; the old Forge event/config implementation is intentionally not ported directly.

## Development

```console
.\gradlew.bat build
```

Manual in-game smoke-test notes are in [docs/manual-smoke-test.md](docs/manual-smoke-test.md).

The standalone config editor is bundled inside the mod jar. After Minecraft starts once with the mod installed, the jar restores the app to:

`config/cropbiomelimiter/config-app`

Open the editor by running the launcher for your operating system in that folder:

- Windows: `Open Config App.cmd`
- Linux: `Open Config App.sh`
- macOS: `Open Config App.command`

The launcher starts the Java helper from the installed mod jar, opens the editor in your default browser, and lets the app save the split config files directly.

## Config

The Fabric rewrite uses a clean split config folder at `config/cropbiomelimiter`:

- `general.json` for shared toggles and dimension mode selection.
- `explicit-mode.json` for biome-first Explicit mode rules.
- `threshold-mode.json` for crop-first Threshold mode rules.
- `config-app` for the bundled standalone editor and one-click launcher.

The old single-file config format is intentionally not supported.

## Sources

- CurseForge: https://www.curseforge.com/minecraft/mc-mods/crop-biome-limiter
- Repository: https://github.com/TheAtomicOption/CropBiomeLimiter
- Fabric template: https://github.com/FabricMC/fabric-example-mod/tree/26.1.2
