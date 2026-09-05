# Egg Badges

Egg Badges is an independent client-side Fabric mod for Minecraft 1.21.1. It
places a small green dot on visible Pokémon Eggs whose six IVs are exactly 31.
It does not change the inventory, click behavior, server, or Pokémon data.

Egg Badges is not an official Cobblemon Town or Cobblemon project.

## Install

1. Use Minecraft `1.21.1` with Fabric Loader `>=0.18.0`, Fabric API
   `>=0.116.7+1.21.1`, and Java `21` or newer.
2. Download `egg-badges-0.2.4.jar` from the
   [Egg Badges release](https://github.com/wguDataNinja/cobblemon-town-client-mods/releases/tag/egg-badges-v0.2.4).
3. Put the JAR in the client instance's `mods` folder.
4. Restart Minecraft. No server installation is required.

Install only one Egg Badges JAR. Verify the download against the SHA-256 listed in
the release before copying it to another computer.

## Use

Open a chest, PC, or other handled inventory containing Pokémon Eggs. Matching eggs
show the green dot over their item slot. The settings button is available from the
native Minecraft Options screen and opens the in-game badge editor.

The default profile is intentionally narrow. Additional badge styles and matching
rules are being developed separately from the stable performance path.

## Performance behavior

Rendering reads cached badge masks only. Egg metadata classification is bounded and
runs on client ticks when a screen, handler, stack, or configuration changes. A
pending scan uses a separate buffer and swaps in only after classification completes;
unchanged badges remain visible while changed slots are refreshed.

The render callback does not parse lore, evaluate rule expressions, scan every slot,
write files, make network requests, or mutate inventory state. The mod uses the
visible egg metadata supplied by the supported client/server combination and fails
closed when the expected metadata is unavailable.

## Compatibility and privacy

- Client-only; the server does not need the mod.
- No network requests, telemetry, account identifiers, or evidence uploads.
- No commands, packet hooks, click interception, tooltip changes, or inventory
  mutation.
- A server or resource-pack change can make an egg's visible metadata unavailable;
  in that case the badge is omitted rather than guessed.

## Build from source

With JDK 21 or newer:

```sh
./gradlew test build
```

The remapped runtime JAR is produced under `build/libs/`. Do not distribute a
`-dev` JAR.

## License

Licensed under the repository's [MIT License](../LICENSE).
