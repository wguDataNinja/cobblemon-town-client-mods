# Collection Exporter

Collection Exporter is an independent client-side Fabric mod for Minecraft 1.21.1. It
reads your client-synchronized Cobblemon PC and creates a readable collection report that
you can copy to the clipboard for Discord or your own notes.

It is read-only. It does not open the PC, request synchronization, send packets, run server
commands, or modify Pokémon, storage, inventory, or gameplay.

Collection Exporter is not an official Cobblemon Town or Cobblemon project.

## Status

This is a **tester build: 0.3.0-dev.1**. The UI and report format were revised after the
first live test. Treat it as a development build rather than a final stable release.

## Install

1. Use Minecraft `1.21.1`, Fabric Loader `>=0.18.0`, Fabric API
   `>=0.116.7+1.21.1`, Cobblemon `1.8.2`, and Java `21` or newer.
2. Download `collection-exporter-0.3.0-dev.1.jar` from the
   [Collection Exporter release](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/collection-exporter-v0.3.0-dev.1).
3. Put the JAR in the client instance's `mods` folder.
4. Restart Minecraft. No server installation is required.

Install only one Collection Exporter JAR. PC Plus and PC Exporter are not required.

## Use

Open the Minecraft Escape menu and choose **Collection Exporter** through the Swamp Mods
launcher. The screen presents four filters:

- All Pokémon
- Custom Skins
- Legendary & Mythical
- Shiny

The default is Custom Skins, Legendary & Mythical, and Shiny, with All Pokémon off. Filters
use logical OR. Selecting All Pokémon clears the individual filters; selecting an
individual filter clears All Pokémon.

The report includes:

- Custom Skins grouped by official family, with Shiny/Non-Shiny groups;
- Legendary & Mythical split into Mythical then Legendary;
- Shiny Pokémon;
- Other selected Pokémon;
- named unresolved aspects instead of an anonymous count;
- a compact summary with a unique selected total.

Unknown aspects remain visible as unresolved. An unrecognized custom skin does not become a
confirmed Custom Skin merely because it is present in the aspect list.

## Compatibility and privacy

- Client-only; the server does not need the mod.
- No networking, telemetry, account identifiers, or evidence uploads.
- No commands, packet hooks, inventory mutation, or gameplay automation.
- The report is generated locally and copied only when you press **Copy to Clipboard**.
- A captured PC snapshot is client-synchronized data, not proof of current server ownership.

## Build from source

With JDK 21 or newer:

```sh
./gradlew test build
```

The remapped runtime JAR is produced under `build/libs/`. Do not distribute a `-dev` JAR.

## License

Licensed under the repository's [MIT License](../LICENSE).
