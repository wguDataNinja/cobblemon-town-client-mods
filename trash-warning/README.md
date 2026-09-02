# Trash Warning

Trash Warning is a small client-side Fabric mod for Cobblemon Town's `/trash` screen.

The Trash inventory looks almost exactly like a normal chest, making it easy to mistake
for storage. The mod adds a clear red `! TRASH !` heading and permanent-deletion warning
while leaving the normal Minecraft container and its behavior unchanged.

Trash Warning is independent and is not an official Cobblemon Town mod or server
feature.

![Before: standard Trash container. After: the same container with Trash Warning active.](docs/images/trash-warning-before-after.png)

*Before and after on Cobblemon Town.*

## Why it exists

Cobblemon Town's `/trash` opens as a normal-looking 9×3 inventory. That's not much
visual distinction for a container that permanently deletes items left inside it.

Trash Warning simply makes the screen obvious. It doesn't add confirmations, change
inventory behavior, or modify the server.

## How it recognizes Trash

A chest shouldn't get a warning just because somebody named it `Trash`.

To avoid that, all of these observed details must match:

- `GenericContainerScreen`
- `GenericContainerScreenHandler`
- 3 rows and 63 slots
- the observed 27 container + 36 player slot layout
- title exactly `Trash`
- structured title exactly `{"text":"Trash","color":"dark_gray"}`

Anything else is left alone.

Live testing confirms:

- normal chest → no warning
- anvil-renamed `Trash` chest → no warning
- Cobblemon Town `/trash` → warning

### Known limitation

Another server/plugin container with the **exact same client-visible fingerprint**
cannot be distinguished from `/trash`.

If that happens, it would receive the warning too. Nothing about the container itself
would change.

## How it works

Fabric's `ScreenEvents.AFTER_INIT` is used to check the screen once when it opens. The
result is cached for that screen.

`ScreenEvents.afterRender` then adds the title and warning when the cached screen is
Trash.

Minecraft still handles the container, slots, items, tooltips, clicks, and keyboard
input normally.

There are no mixins, packet or command hooks, inventory modification, gameplay
automation, network requests, telemetry, or server-side components.

## Compatibility

- Minecraft `1.21.1`
- Fabric Loader `>=0.18.0`
- Fabric API `>=0.116.7+1.21.1`
- Java 21

The mod is client-only. The server does not need it.

It has been live-tested in the development Cobblemon Town environment. Testing against
the current standard Cobblemon Town pack and a representative addon setup is also
planned before broader compatibility is claimed.

If a future server update changes the `/trash` fingerprint, the mod fails closed and
displays nothing.

## Installation

Install the supported Fabric Loader and Fabric API, then place:

`trash-warning-<version>.jar`

in the client's `mods` folder.

No configuration or server installation is required.

## Testing

Automated tests cover the Trash fingerprint and rejection of incorrect titles, classes,
dimensions, slot counts, and slot layouts.

Live tests have confirmed:

- ordinary chest unchanged
- anvil-renamed `Trash` chest unchanged
- `/trash` recognized correctly
- normal container behavior remains unchanged

New environments should also be checked at different GUI scales and with any installed
UI/overlay mods.

## Build from source

From the `trash-warning/` directory with JDK 21 or newer:

```sh
./gradlew build
```

## Source review

The runtime code is intentionally small:

- `TrashWarningClient` — Fabric event registration
- `TrashScreenClassifier` — reads the screen fingerprint
- `TrashFingerprint` — matching rules
- `TrashWarningRenderer` — draws the warning

There are no mixins, networking, gameplay automation, or inventory manipulation.

These files are the primary runtime audit surface. A complete review can also inspect
the Gradle configuration, Fabric metadata, dependencies, and built JAR.

Licensed under [MIT](../LICENSE).
