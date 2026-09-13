# Cobblemon-Town Game Menu Dashboard

Game Menu Dashboard is an independent, unofficial client-side Fabric mod that adds a
task-oriented panel to Minecraft's Escape / Game Menu while leaving vanilla controls
available. It is not produced by or affiliated with Cobblemon Town, Cobblemon, or any
server.

## Tester build — 0.2.3-dev

This build is published for controlled tester sharing, **not as a GitHub Release**.
Download the normal runtime JAR from [`release/`](release/); do not use a development
JAR or source ZIP.

It includes Homes and Server Travel plus the current Claims/Trust panel and optional
Xaero map overlays. Claims, Trust, resize preview, and map behavior still need manual
validation on the current official modpack. Do not treat the Claims display as server
truth or use it for irreversible actions.

When Essential has mounted its pause-menu controls, the dashboard avoids that layout
and provides a compact **Swamp Menu** launcher. It opens a Swamp-owned dashboard screen
and returns to the original Game Menu with Back or Esc. The normal inline dashboard is
used when those Essential controls are not present.

The dashboard sends at most one ordinary server command per deliberate interaction. It
does not automate chat input, chain commands, modify inventory, or require a server mod.

## Build

```sh
./gradlew test build
```

Use Java 21 or newer. Only the remapped runtime JAR in `build/libs/` is usable in
Minecraft.

Compatibility target: Minecraft 1.21.1, Fabric Loader >= 0.18.0, and Fabric API
>= 0.116.7+1.21.1.

Licensed under the repository [MIT License](../LICENSE).
