# Crop Biome Limiter Config App

For normal use on Windows, double-click:

`Open Config App.cmd`

The launcher starts a local app server and opens the config app in your default browser. Keep the launcher window open while using the app, then close it when you are done.

When installed through the mod jar, Minecraft creates this app at:

`config/cropbiomelimiter/config-app`

No Python, npm, or developer tools are required for normal use.

Manual fallback:

If the launcher cannot run, any static file server works. The app does not connect to Minecraft and does not require a running client or server.

Load any combination of:

- `general.json`
- `explicit-mode.json`
- `threshold-mode.json`
- `cropbiomelimiter-registry-snapshot.json`

The app edits the same split config files used by the Fabric mod. It keeps Explicit mode rules biome-first and Threshold mode rules crop-first. Use the download buttons to write updated JSON files back into Minecraft's `config/cropbiomelimiter` folder.

The app validates loaded files, supports copying the active dimension mode/rules to another dimension, and can use registry snapshots exported by `/cropbiomelimiter export-registry` so modded dimensions, biomes, and growable crop blocks appear in the grids.

Developer smoke test:

```powershell
npm test
```
