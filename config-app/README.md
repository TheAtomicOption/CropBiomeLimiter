# Crop Biome Limiter Config App

Run the app as local static files, then open the printed local URL in a browser.

```powershell
python -m http.server 41731
```

If Python is not on `PATH`, any static file server works. The app does not connect to Minecraft and does not require a running client or server.

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
