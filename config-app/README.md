# Crop Biome Limiter Config App

After Minecraft starts once with the mod installed, the mod jar restores this app at:

`config/cropbiomelimiter/config-app`

Use the launcher for your operating system:

- Windows: `Open Config App.cmd`
- Linux: `Open Config App.sh`
- macOS: `Open Config App.command`

The launcher starts the Java helper from the installed mod jar, opens the config app in your default browser, and lets the app save `general.json`, `explicit-mode.json`, and `threshold-mode.json` directly. Keep the launcher window open while using the app, then close it when you are done.

No Python, npm, PowerShell, or developer tools are required for normal use.

Manual fallback:

If the launcher cannot run, any static file server works, but direct saving is available only through the Java helper. The browser-only fallback can still load JSON files and download edited replacements. The app does not connect to Minecraft and does not require a running client or server.

Load any combination of:

- `general.json`
- `explicit-mode.json`
- `threshold-mode.json`
- `cropbiomelimiter-registry-snapshot.json`

The app edits the same split config files used by the Fabric mod. It keeps Explicit mode rules biome-first and Threshold mode rules crop-first. When launched through the Java helper, use `Save Installed` to write updated JSON files back into Minecraft's `config/cropbiomelimiter` folder. In browser-only fallback mode, use the download buttons and replace the files manually.

The app validates loaded files, supports copying the active dimension mode/rules to another dimension, and can use registry snapshots exported by `/cropbiomelimiter export-registry` so modded dimensions, biomes, and growable crop blocks appear in the grids.

Developer smoke test:

```powershell
npm test
```
