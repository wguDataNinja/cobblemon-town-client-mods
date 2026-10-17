# PC Exporter v0.2.0

<!-- STAGING DRAFT: GitHub Release notes for tag `pc-exporter-v0.2.0`. -->

Client-side, read-only Cobblemon PC exporter for Minecraft 1.21.1.

**What it does**

- Adds an **Export PC** button to the Cobblemon PC screen, immediately left of Cobblemon PC Plus's
  native filter controls.
- Clicking writes one private, timestamped JSON snapshot of your PC (all occupied boxes/slots) to
  `config/pc-exporter/snapshots/`.
- Read-only: no networking, no gameplay automation, no server commands, no inventory mutation.

**Requirements**

- Minecraft 1.21.1 · Fabric Loader 0.18.0+ · Fabric API 0.116.7+1.21.1+ · Java 21+
- Cobblemon 1.8.1 (`1.8.1+1.21.1`)
- **Cobblemon PC Plus 1.3.0** (`cobblemonpcplus`) — required

**Install:** download `pc-exporter-0.2.0.jar`, place it in your instance `mods` folder, restart.

**Checksum (SHA-256):**

```
aad9b179a57e4a844b14516b4d13f023bb14c65835fed01b946a5f9ad2ef16f2
```

**Notes**

- Windows runtime testing is pending (built/tested on macOS).
- `eggGroups` is present in the schema but always empty (Cobblemon does not expose egg groups to the
  client); retained for schema stability.
