# PC Exporter v0.3.0

Client-side, read-only Cobblemon PC exporter for Minecraft 1.21.1.

**What's new in 0.3.0**

- **Pick where to save on first use.** The first time you click Export, a native folder picker
  opens; your choice is remembered and the snapshot is written there. Cancel = no export, no folder
  saved.
- **Later exports go straight to that folder.**
- **Clickable chat actions** after each export:
  `[Open Folder]` (opens the folder in Explorer/Finder) and `[Change Folder]` (pick a new folder).
- Existing `config/pc-exporter/output-dir.txt` setups are respected (no prompt).
- Clearer error/cancel messages; a failed operation never overwrites your saved folder.

**What it does**

- Adds an **Export PC** button to the Cobblemon PC screen, immediately left of Cobblemon PC Plus's
  native filter controls.
- Writes one private, timestamped JSON snapshot of your PC (all occupied boxes/slots).
- Read-only: no networking, no gameplay automation, no server commands, no inventory mutation.

**Requirements**

- Minecraft 1.21.1 · Fabric Loader 0.18.0+ · Fabric API 0.116.7+1.21.1+ · Java 21+
- Cobblemon 1.8.1 (`1.8.1+1.21.1`)
- **Cobblemon PC Plus 1.3.0** (`cobblemonpcplus`) — required

**Install:** download `pc-exporter-0.3.0.jar`, place it in your instance `mods` folder, restart.

**Checksum (SHA-256):**

```
fcc298a727003a62c0818b6b63f9fa592f90aabe80e79b10944d1b7906261c30
```

**Notes**

- Windows runtime testing is pending (built/tested on macOS). The export itself is OS-independent;
  the native folder picker and "Open Folder" have not yet been confirmed on Windows.
- `eggGroups` is present in the schema but always empty (Cobblemon does not expose egg groups to the
  client); retained for schema stability.
