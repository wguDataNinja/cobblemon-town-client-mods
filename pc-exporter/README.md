# PC Exporter

Client-side, read-only exporter for Cobblemon PC storage. Adds an **Export PC** button to the
Cobblemon PC screen and writes a private, timestamped JSON snapshot of your PC to your own
computer. Nothing is sent anywhere.

- **Read-only.** No gameplay automation, no inventory mutation, no server commands, no networking.
- **PC Plus required.** The Export button is placed inside Cobblemon PC Plus; PC Plus 1.3.0+ must
  be installed. PC Plus is not modified, bundled, or redistributed.
- **Private by default.** The snapshot is written locally and contains your PC Pokémon data.

## Requirements

- Minecraft **1.21.1**
- Fabric Loader **0.18.0+** and Fabric API **0.116.7+1.21.1+**
- **Java 21+**
- **Cobblemon 1.8.1** (`1.8.1+1.21.1`)
- **Cobblemon PC Plus 1.3.0** (`cobblemonpcplus`) — required

## Installation

1. Install Fabric Loader for Minecraft 1.21.1 and Fabric API.
2. Install Cobblemon and Cobblemon PC Plus.
3. Download `pc-exporter-0.3.0.jar` from the release.
4. Place it in your instance's `mods` folder.
5. Restart Minecraft.

Do not keep multiple versions of the same mod installed at once.

### Windows

Runtime testing on Windows is **pending**. This release was built and tested on macOS; the mod is
plain Fabric/Java 21 and the export itself is OS-independent, but the native folder picker and
"Open Folder" behavior have not been confirmed on Windows yet.

## Usage

1. Open your PC.
2. Click **Export** (left of PC Plus's filter controls).
3. **First export:** a folder picker opens. Choose where snapshots should go. The choice is
   remembered, and the snapshot is written there immediately.
   - If you cancel the picker, the export is cancelled — nothing is written and no folder is
     saved.
4. **Later exports:** snapshots go straight to the remembered folder. The picker does not appear
   again.
5. After every export, chat shows:

   ```
   PC Exporter: Exported 613 Pokémon.
   [Open Folder]  [Change Folder]
   ```

   - **Open Folder** opens the export folder in your file browser (Explorer/Finder).
   - **Change Folder** opens the picker again and remembers the new folder (it does not export
     again).

Snapshots are named `pc-<UTC timestamp>.json`.

### Advanced: pre-set the folder

You can set the folder without the picker by putting a single path (absolute, or relative to the
game directory) in:

```
config/pc-exporter/output-dir.txt
```

An existing, valid `output-dir.txt` counts as an already-configured location, so you will not be
prompted. This is also the fallback if the native picker is unavailable on your system.

## Snapshot format

One JSON file per export, schema version 1. Per Pokémon: species, form, aspects, gender, level,
experience, shiny, nature (+ mint), ability (+ hidden/slot), IVs/EVs/stats, moves, held/cosmetic
item, marks, species features, benched moves, tradeable, friendship, caught ball, original-trainer
fields (when available), Tera/Dmax/Gmax/Alpha, health/fullness, scale, and markings.

Known limitation: the `eggGroups` field is present but always empty — Cobblemon does not expose egg
groups to the client. It is retained for schema stability and may be removed in a future version.

## Checksum

SHA-256 of `pc-exporter-0.3.0.jar`:

```
fcc298a727003a62c0818b6b63f9fa592f90aabe80e79b10944d1b7906261c30
```

## License

MIT. See the repository [LICENSE](../LICENSE).
