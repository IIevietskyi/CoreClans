# CoreClans

![CoreClans Cover](docs/assets/coreclans-cover.png)

![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5%2B-f08c2e?style=for-the-badge)
![Server](https://img.shields.io/badge/Spigot%20%7C%20Paper%20%7C%20Purpur-Supported-1a79c8?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-8%2B-e76f00?style=for-the-badge)
![Storage](https://img.shields.io/badge/Storage-Local%20JSON-46556b?style=for-the-badge)

CoreClans is a war-focused clan plugin designed for fair competitive gameplay.

It combines classic clan features with objective wars, siege time windows, anti-zerg balancing, war replay stats, season divisions, and a fully editable GUI menu system.

## Why CoreClans
- Objective-based wars: `capture`, `beacon`, `crystal`
- Scheduled raiding via siege windows (no forced 24/7 raids)
- Anti-zerg mechanics for online imbalance
- Attacker queue + attacker cap near objective
- Objective validation with reachability/path checks
- Local JSON data storage (no database setup)
- Ready GUI panels via `/menus/*.yml`
- PlaceholderAPI support for tab/scoreboard/HUD

![Separator](docs/assets/separator-thin-gradient-1200x40.png)

## Quick Start
1. Put `coreclans-1.0.0.jar` into `plugins/`.
2. Restart server (or start server once) to generate config files.
3. Optional: install `Vault` + economy plugin for wallet-based bank operations.
4. Optional: install `PlaceholderAPI` for `%coreclans_*%` placeholders.
5. In game:
   - `/clan create <name>`
   - `/clan claim set 64`
   - `/clan objective set capture` (and/or beacon/crystal)
   - `/clan siege window set friday 18:00 22:00`
   - `/clan menu`

## Compatibility
- Minecraft: `1.16.5+`
- Cores: `Spigot`, `Paper`, `Purpur`
- Java runtime: `8+` (Java 17/21 supported)
- API target: Spigot `1.16.5-R0.1-SNAPSHOT`

## Docs
- [Installation](docs/INSTALL.md)
- [Commands](docs/COMMANDS.md)
- [Permissions](docs/PERMISSIONS.md)
- [Placeholders](docs/PLACEHOLDERS.md)
- [Configuration](docs/CONFIG.md)
- [GUI Menus](docs/MENUS.md)
- [FAQ](docs/FAQ.md)
- [Update Post Template](docs/UPDATE_TEMPLATE.md)

## Visuals
![Core Features](docs/assets/banner-features.png)
![Objectives](docs/assets/banner-objectives.png)
![War](docs/assets/banner-war.png)
![GUI](docs/assets/banner-gui.png)
![Commands](docs/assets/banner-commands.png)
![Permissions](docs/assets/banner-permissions.png)
![Placeholders](docs/assets/banner-placeholders.png)
![Compatibility](docs/assets/banner-compatibility.png)

## Download Links
- Spigot: https://www.spigotmc.org/resources/%E2%9A%94%EF%B8%8Fcoreclans-%E2%9C%A8objective-wars-siege-windows-gui-json-1-16-5.133268/
- Modrinth: https://modrinth.com/plugin/coreclans

## Build From Source
```bash
mvn clean package
```

Built jar:
- `target/coreclans-1.0.0.jar`

## Storage
- Primary data file: `plugins/CoreClans/coreclans-data.json`
- Menus directory: `plugins/CoreClans/menus/`

## Support
- Bug report: [open issue](issues/new?template=bug_report.md)
- Feature request: [open issue](issues/new?template=feature_request.md)

If you find bugs or have suggestions, open an issue with your server version, CoreClans version, and reproduction steps.

## Author
- `IIevietskyi`




