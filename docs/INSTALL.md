# Installation

## Requirements
- Minecraft server: `1.16.5+`
- Core: `Spigot`, `Paper`, or `Purpur`
- Java: `8+` (Java 17/21 is supported)

## Optional Dependencies
- `Vault` + economy plugin (for `/clan bank deposit|withdraw` wallet transactions)
- `PlaceholderAPI` (for `%coreclans_*%` placeholders in tab/scoreboard/HUD)

## Install Steps
1. Stop the server.
2. Put `coreclans-1.0.0.jar` into `plugins/`.
3. Start the server once.
4. Configure files in `plugins/CoreClans/`:
   - `config.yml`
   - `messages.yml`
   - `menus/*.yml`
5. Restart the server or run `/clanadmin reload`.

## First Setup (Recommended)
1. `/clan create <name>`
2. `/clan claim set 64`
3. Set objectives inside claim:
   - `/clan objective set capture`
   - `/clan objective set beacon`
   - `/clan objective set crystal`
4. Configure siege windows:
   - `/clan siege window set friday 18:00 22:00`
5. Open GUI:
   - `/clan menu`

## PlaceholderAPI Notes
No eCloud expansion install is required for CoreClans placeholders.

If PlaceholderAPI plugin is present, CoreClans registers its own internal expansion automatically.

## Java Notes
CoreClans is compiled with Java 8 target for maximum compatibility, but runs on newer JVMs (17/21).
