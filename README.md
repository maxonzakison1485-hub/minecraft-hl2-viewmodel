# HL2 Viewmodel

Client-side Fabric mod for Minecraft 26.3. The goal is Half-Life 2-style first-person item weight, sway and inertia.

## Current implementation

- Fabric client entrypoint and build configuration.
- Damped spring state for camera-turn inertia.
- A client mixin wraps vanilla `ItemInHandRenderer.submitHandsWithItems` with an additive pose transform. Vanilla hand/item drawing is not replaced.
- GitHub Actions builds the mod and uploads the JAR as an artifact.

This is an early implementation. It needs an actual game launch and visual tuning; a successful compile alone does not prove renderer compatibility.

## Shader compatibility goal

The mod changes the first-person pose stack only. It does not call OpenGL, replace a shader, or modify a shader pipeline. That is the intended low-conflict approach for Vitrail (OptiFine-format packs translated for Minecraft's Vulkan renderer) and Sulkan (its own native Vulkan shader pipeline). Compatibility still needs in-game testing against the exact Minecraft, Sodium, Vitrail/Sulkan, and shader-pack versions. If a renderer changes or bypasses the vanilla hand-submission method, the mixin may need a backend-specific adjustment.

## Build

Requirements: JDK 25 and Gradle 9.6 or newer. Fabric's Minecraft 26.3 versions are listed at [Fabric Develop](https://fabricmc.net/develop/).

```bash
gradle build
```

The JAR is produced in `build/libs/`. GitHub Actions also runs this build on pushes and pull requests.

## Development

Open the project in IntelliJ IDEA as a Gradle project. Client sources are under `src/client/java`.
