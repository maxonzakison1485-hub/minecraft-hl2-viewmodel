# HL2 Viewmodel

A client-side Fabric mod for Minecraft 26.3, aiming to make the first-person held-item viewmodel move with the weight and inertia of Half-Life 2.

## Current state

- Fabric 26.3 project scaffold and client entrypoint.
- `ViewmodelMotion`: a damped spring model for camera-turn inertia / sway.
- The motion model is not yet connected to Minecraft's held-item renderer; this is the next implementation step.

## Build

Requirements: JDK 25 and Gradle 9.6 or newer. Fabric currently recommends Loom 1.19-SNAPSHOT and Loader 0.19.5 for Minecraft 26.3; see the [Fabric version page](https://fabricmc.net/develop/).

From the repository root, run:

```bash
gradle build
```

The mod JAR will be in `build/libs/`.

## Development

Open the project in IntelliJ IDEA as a Gradle project. Client sources are under `src/client/java`.
