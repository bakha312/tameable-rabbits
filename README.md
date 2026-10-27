# 🐰 Tameable Rabbits

**Tameable Rabbits** is a Minecraft Fabric mod that allows players to tame rabbits and keep them as companions.

The mod extends vanilla rabbit behavior with taming, owner-following, sitting, healing, and breeding mechanics.

## Features

- **Rabbit taming** — tame wild rabbits using carrots, golden carrots, or dandelions, with approximately a 1-in-3 success chance.
- **Follow owner** — tamed rabbits follow their owner around the world.
- **Sit and stay** — right-click a tamed rabbit with an empty hand to toggle sitting.
- **Healing and breeding** — use carrots to heal or breed tamed rabbits.
- **Crop protection** — tamed rabbits no longer destroy carrot crops.
- **Persistence** — tamed rabbits do not naturally despawn.

## Requirements

| Component | Version |
|---|---|
| Minecraft | 1.20.1 |
| Mod Loader | Fabric |
| Fabric API | Compatible with Minecraft 1.20.1 |
| Java | 17+ |

## Installation

1. Install Fabric Loader for Minecraft 1.20.1.
2. Install the compatible Fabric API.
3. Build the mod or obtain its JAR file.
4. Place the JAR in your Minecraft `mods` folder.
5. Launch Minecraft using the Fabric profile.

## Building from Source

Clone the repository:

```bash
git clone https://github.com/bakha312/tameable-rabbits.git
cd tameable-rabbits
```

Make sure Java 17 or a compatible JDK is installed.

**Windows (PowerShell):**

```powershell
.\gradlew.bat build
```

**Linux / macOS:**

```bash
./gradlew build
```

The compiled mod will be available in:

```text
build/libs/
```

## Tech Stack

- Java
- Fabric API
- Minecraft 1.20.1
- Gradle
- Mixins
- Git & GitHub

## Project Structure

```text
src/main/
├── java/com/bach/tameablerabbits/
│   ├── TameableRabbits.java
│   ├── RabbitTameable.java
│   ├── goal/
│   │   ├── RabbitFollowOwnerGoal.java
│   │   └── RabbitSitGoal.java
│   └── mixin/
│       ├── RabbitEntityMixin.java
│       └── RabbitEntityModelMixin.java
└── resources/
    ├── assets/tameable_rabbits/lang/
    ├── fabric.mod.json
    └── tameable_rabbits.mixins.json
```

## Development

This project explores extending Minecraft entity behavior using Java and the Fabric modding framework.

Key development areas include:

- Object-oriented programming in Java
- Entity behavior and AI goals
- Minecraft entity interaction handling
- Mixins and game behavior modification
- Gradle-based project builds

## License

See the [LICENSE](LICENSE) file for license information.