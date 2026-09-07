# Cobblemon Town Client Mods

Hi, I'm **SwampDad** in-game and **WGU Data Ninja** on GitHub.

I'm a Cobblemon Town player sharing unofficial client-side quality-of-life mods built
specifically for **Cobblemon Town and its modpack**. These are independent fan projects,
not official Cobblemon Town or Cobblemon releases.

**Cobblemon Town:** [Wiki](https://cobblemon-town.gitbook.io/cobblemon-town) · [CurseForge](https://www.curseforge.com/minecraft/modpacks/cobblemon-optimized-your-pokemon-journey) · [Modrinth](https://modrinth.com/modpack/cobblemon-town)

## Mods

These mods make existing actions easier to find, understand, and use without automating
gameplay or replacing server authority.

**Ground rule: One deliberate human action → at most one server action.**

They're built with the [Cobblemon Town Server Rules](https://cobblemon-town.gitbook.io/cobblemon-town/get-started/server-rules) in mind, especially the rules around automation and unfair advantages.

| Mod | What it does | Latest |
| --- | --- | --- |
| **Game Menu Dashboard** | Adds your Homes and useful Cobblemon Town server destinations directly to the Escape menu. | [**v0.2.1**](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/game-menu-dashboard-v0.2.1) |
| **Trash Warning** | Makes Cobblemon Town's `/trash` clearly distinguishable from a normal chest so you don't accidentally throw something away. | [**v0.1.3**](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/trash-warning-v0.1.3) |
| **Egg Badges** | Marks visible Pokémon Eggs with perfect 6×31 IVs with a small badge. | [**v0.2.4**](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/egg-badges-v0.2.4) |

Each mod is independent. Install only the ones you want.

## Installation

These mods are client-side only. Nothing needs to be installed on the server.

1. Go to the mod's **latest GitHub Release**.
2. Under **Assets**, download the mod's `.jar` file.
3. Open the Minecraft instance you use for **Cobblemon Town**.
4. Place the downloaded `.jar` in that instance's `mods` folder.
5. Restart Minecraft.

### Updating

Download the new `.jar` and replace the old version in your `mods` folder. Do not leave
multiple versions of the same mod installed at the same time.

### Which file should I download?

Download the normal mod `.jar` listed under **Assets** on the GitHub Release. You do
not need source ZIPs, development JARs, or the GitHub repository itself.

## Compatibility

These mods are developed specifically for the **Cobblemon Town server and its modpack**.

- **Minecraft:** 1.21.1
- **Fabric Loader:** 0.18.0 or newer
- **Fabric API:** 0.116.7+1.21.1 or newer
- **Java:** 21 or newer
- **Server/modpack:** Cobblemon Town

The mods are client-side only and do not require server installation. Server or modpack
changes may affect compatibility; check each mod's latest release notes after updates.

## Game Menu Dashboard

**Game Menu Dashboard** turns Minecraft's Escape menu into a Cobblemon Town control
surface while preserving the normal vanilla Game Menu. The current release provides
Homes and Home capacity, click-to-travel Homes, Home creation/deletion/refresh, Spawn,
RTP, and direct access to the server's fixed travel Warps. Server actions still use the
normal commands available to the player.

### Screenshot

<!-- Screenshot placeholder — replace this comment with the approved in-game Game Menu
Dashboard screenshot before the next public README push. Keep the image under
game-menu-dashboard/docs/images/ and use descriptive alt text. -->

## Trash Warning

**Trash Warning** makes Cobblemon Town's `/trash` inventory visually unmistakable from
ordinary storage, reducing the chance of accidental deletion. It does not change the
server's Trash behavior.

## Egg Badges

**Egg Badges** adds a small visual marker to visible Pokémon Eggs with perfect six-stat
31 IVs. It does not modify Pokémon, Eggs, inventories, breeding mechanics, or server
state.

## Scope

These are small client-side quality-of-life mods. They do not require server
installation, grant extra permissions, or replace server authority. Any ordinary server
action still goes through the normal command or interaction available to the player.

## Checksums

Published releases include a SHA-256 checksum for the release JAR. Most players do not
need it; it is provided for independent verification of a downloaded or copied JAR.

## Development

Each published mod maintains its own source, tests, version, release notes, and release
lifecycle.

```bash
cd trash-warning && ./gradlew test build
cd ../egg-badges && ./gradlew test build
cd ../game-menu-dashboard && ./gradlew test build
```

Only remapped runtime JARs from `build/libs/` are intended for release.

## License

Unless a project directory states otherwise, these mods are distributed under the
repository's MIT License.
