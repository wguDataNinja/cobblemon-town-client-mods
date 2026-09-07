# Cobblemon Town Client Mods

Independent client-side Fabric mods for Cobblemon Town. These are community
projects, not official Cobblemon Town or Cobblemon releases.

## Mods

| Mod | Purpose | Documentation | Latest release |
| --- | --- | --- | --- |
| Game Menu Dashboard | Adds an unofficial, client-side Homes and Server Travel dashboard to the Escape menu while preserving vanilla controls. | [README](game-menu-dashboard/README.md) | 0.2.0 |
| Trash Warning | Makes `/trash` clearly distinguishable from a normal chest without changing container behavior. | [README](trash-warning/README.md) | [0.1.3](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/trash-warning-v0.1.3) |
| Egg Badges | Marks visible six-31-IV Pokémon Eggs with a small badge without changing inventory or server behavior. | [README](egg-badges/README.md) | [0.2.4](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/egg-badges-v0.2.4) |

The published mod set is Egg Badges, Trash Warning, and Game Menu Dashboard. Each mod
keeps its own source, tests, version, release notes, and release lifecycle.

## Installation

All mods are client-only. For a supported release, download the JAR from that mod's
GitHub Release and place it in the Minecraft instance's `mods` folder. Restart
Minecraft after adding or replacing a JAR. Do not install development JARs, source
ZIPs, or duplicate versions of the same mod.

The current compatibility target is Minecraft `1.21.1`, Fabric Loader `>=0.18.0`,
Fabric API `>=0.116.7+1.21.1`, and Java `21` or newer unless a mod's README says
otherwise. No mod requires server installation.

Each release includes a SHA-256 checksum. Verify it before copying a JAR to another
computer.

## Development

Each project can be built independently from its directory:

```sh
cd trash-warning && ./gradlew test build
cd ../egg-badges && ./gradlew test build
```

Only remapped runtime JARs from `build/libs/` should be released. Project-specific
documentation is the authoritative source for behavior, compatibility, limitations,
and safety guarantees.

## License

Unless a project directory states otherwise, these mods are distributed under the
repository's [MIT License](LICENSE).
