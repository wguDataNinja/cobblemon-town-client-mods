# Cobblemon-Town Game Menu Dashboard

Game Menu Dashboard is an independent, unofficial client-side Fabric mod that adds a
task-oriented panel to Minecraft's Escape / Game Menu while leaving vanilla controls
available. It is not produced by or affiliated with Cobblemon Town, Cobblemon, or any
server.

## Testing snapshot — 0.2.3-dev.19

This source snapshot is published for controlled testing, **not as a GitHub Release**.
The latest supported player download remains the [v0.2.1 release](../README.md).

The snapshot includes Homes and Server Travel plus an experimental own-Claims/Trust
panel and optional Xaero map overlays. Claims, Trust, resize preview, and map behavior
are incomplete and require manual validation on the current official modpack. Do not
rely on the testing snapshot for irreversible actions or treat its Claims display as
server truth.

The dashboard sends at most one ordinary server command per deliberate interaction. It
does not automate chat input, chain commands, modify inventory, or require a server mod.

## Build

```sh
./gradlew test build
```

Use Java 21 or newer. Only the remapped runtime JAR in `build/libs/` is usable in
Minecraft; no JAR has been published for this testing snapshot.

Compatibility target: Minecraft 1.21.1, Fabric Loader >= 0.18.0, and Fabric API
>= 0.116.7+1.21.1.

Licensed under the repository [MIT License](../LICENSE).
