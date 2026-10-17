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
3. Download `pc-exporter-0.2.0.jar` from the release.
4. Place it in your instance's `mods` folder.
5. Restart Minecraft.

Do not keep multiple versions of the same mod installed at once.

### Windows

Runtime testing on Windows is **pending**. This release was built and tested on macOS; the mod is
plain Fabric/Java 21 and has no OS-specific code, but Windows is not yet confirmed.

## Usage

1. Open your PC.
2. Click **Export** (left of PC Plus's filter controls).
3. Your snapshot is written to `config/pc-exporter/snapshots/pc-<UTC timestamp>.json` in your
   instance folder.
4. To change the output folder, put a single path (absolute, or relative to the game directory) in
   `config/pc-exporter/output-dir.txt`.

## Snapshot format

One JSON file per export, schema version 1. Per Pokémon: species, form, aspects, gender, level,
experience, shiny, nature (+ mint), ability (+ hidden/slot), IVs/EVs/stats, moves, held/cosmetic
item, marks, species features, benched moves, tradeable, friendship, caught ball, original-trainer
fields (when available), Tera/Dmax/Gmax/Alpha, health/fullness, scale, and markings.

Known limitation: the `eggGroups` field is present but always empty — Cobblemon does not expose egg
groups to the client. It is retained for schema stability and may be removed in a future version.

## Checksum

SHA-256 of `pc-exporter-0.2.0.jar`:

```
aad9b179a57e4a844b14516b4d13f023bb14c65835fed01b946a5f9ad2ef16f2
```

## License

MIT. See the repository [LICENSE](../LICENSE).
