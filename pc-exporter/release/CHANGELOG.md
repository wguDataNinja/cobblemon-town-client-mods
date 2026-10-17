# PC Exporter changelog

## 0.3.0

- **First export asks where to save.** The first time you click Export, a native folder picker
  opens. Your choice is remembered and the snapshot is written there. Cancelling cancels the
  export — nothing is written and no folder is saved.
- **Later exports go straight to the remembered folder** (no picker).
- **Clickable chat actions** after every export: `[Open Folder]` opens the folder in your file
  browser, `[Change Folder]` lets you pick a new folder (without exporting again). Backed by
  client-side commands; nothing is sent to the server.
- Existing `config/pc-exporter/output-dir.txt` configurations are respected and are treated as an
  already-configured location (no prompt).
- Clearer messages for a cancelled picker, a missing or non-writable folder, and picker/browser
  failures. A failed operation never overwrites your saved folder.

## 0.2.0 — first public release

- Adds an **Export PC** button inside Cobblemon PC Plus, placed immediately left of PC Plus's
  native filter controls using PC Plus's own public widget geometry and native button widget.
- Clicking writes one private, timestamped JSON snapshot of the client-resident PC (all occupied
  boxes/slots) to `config/pc-exporter/snapshots/` (configurable via `config/pc-exporter/output-dir.txt`).
- Snapshot schema version 1: species/form/aspects, gender, level/experience, shiny, nature (+mint),
  ability (+hidden/slot), IVs/EVs/stats, moves, held/cosmetic item, marks, species features,
  benched moves, tradeable, friendship, caught ball, original-trainer fields, Tera/Dmax/Gmax/Alpha,
  health/fullness, scale, markings.
- Read-only: no networking, no gameplay automation, no server commands, no inventory mutation.
- Requires Cobblemon PC Plus 1.3.0+.
