# PC Exporter changelog

<!-- STAGING DRAFT for the public repository: intended path `pc-exporter/release/CHANGELOG.md`. -->

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
