# Easier Ghost Block

A client-side Fabric mod for Minecraft: Java Edition **1.21.1 to 26.3**. It makes ghost blocks easy to create on low-ping servers and in singleplayer.

## How it works

When you break or place a block, your client shows the result right away and waits for the server to confirm it. If the server disagrees, the block snaps back once the confirmation ("block changed ack") arrives. That's the moment a ghost block disappears. With 50 ms ping you get about a 50 ms window, and in singleplayer you get almost none.

Easier Ghost Block holds that confirmation back for a time you choose, so the window becomes **ping + your delay**. The mod only delays the confirmation for blocks you predicted yourself. It doesn't touch any other world updates or packets.

## Configuration

You can open the config screen (built with [YACL](https://modrinth.com/mod/yacl)) in two ways:
- in **Mod Menu** (optional), or
- with the **`/easierghostblock`** command (alias `/egb`).

| Option | Description |
|---|---|
| Enabled | Master on/off switch |
| Delay Unit | `Ticks` or `Milliseconds`: only the value for the selected unit is used |
| Delay (Ticks) | Extra delay in ticks (1 tick = 50 ms), slider plus an exact field up to 200 |
| Delay (Milliseconds) | Extra delay in ms, slider plus an exact field up to 10000 |
| Subtract Ping | Treat the delay as a total target and subtract your current ping (so every server feels the same) |
| Singleplayer / Multiplayer | Choose where the delay applies |

Commands:
- `/egb`: open the config screen
- `/egb status`: show the current settings
- `/egb toggle`: turn the delay on or off
- `/egb ticks <n>`: switch to ticks and set the delay
- `/egb ms <n>`: switch to milliseconds and set the delay

The config file is `config/easierghostblock.json`.

## Requirements

- Fabric Loader
- Fabric API
- YetAnotherConfigLib (YACL) v3
- Mod Menu (optional)

## Downloads / supported versions

**Easiest:** use `easier-ghost-block-<version>-universal.jar`. One jar works on every supported version, from 1.21 to 26.3.x. It contains every per-version build (Jar-in-Jar), and Fabric Loader only loads the one that matches your game.

There are also per-version jars, and each one covers a range of Minecraft versions:

| Jar | Minecraft |
|---|---|
| `+1.21.1` | 1.21 – 1.21.1 |
| `+1.21.3` | 1.21.2 – 1.21.3 |
| `+1.21.4` | 1.21.4 |
| `+1.21.5` | 1.21.5 |
| `+1.21.8` | 1.21.6 – 1.21.8 |
| `+1.21.10` | 1.21.9 – 1.21.10 |
| `+1.21.11` | 1.21.11 |
| `+26.1` | 26.1 – 26.1.2 |
| `+26.2` | 26.2.x |
| `+26.3` | 26.3.x |

## Building

You need JDK 25 to run Gradle. JDK 21 is used for the 1.21.x targets, and Gradle downloads it automatically if it's missing.

```bash
./gradlew build                 # all versions -> dist/
./gradlew :1.21.3:build         # a single version
./gradlew build universalJar    # all versions + the universal jar -> dist/
```

Every version is its own Gradle subproject, defined by `versions/<name>.properties`, and all of them compile the shared sources in `src/`. Everything uses Mojang's official names: 1.21.x is remapped with Loom, and 26.x is unobfuscated. To add a version, add a properties file.

GitHub Actions (`.github/workflows/build.yml`) builds every version on each push and pull request, and uploads the jars as workflow artifacts. When you push a `v*` tag, it also attaches the jars to a GitHub release.
