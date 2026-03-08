# GUI Menus

CoreClans menu files are in:
- `plugins/CoreClans/menus/`

Default files:
- `main.yml`
- `war.yml`
- `objectives.yml`
- `admin.yml`

## Open Menus
- `/clan menu` -> opens `main`
- `/clan menu <id>` -> opens by menu id
- `/clanadmin menu <id>` -> admin open

## Menu Schema
```yaml
id: main
title: "&8CoreClans - Main"
size: 45

items:
  some_item:
    slot: 10
    material: BEACON
    amount: 1
    glow: false
    permission: "coreclans.admin"
    skull-owner: "{player}"
    name: "&eTitle"
    lore:
      - "&7Line"
    action:
      type: command
      value: "clan info"
```

## Supported Item Actions
- `command` (aliases: `cmd`)
- `open_menu` (aliases: `menu`, `open`)
- `close`
- `none`

## Menu Placeholders (`{...}`)
- `{player}`
- `{clan_name}`
- `{clan_tag}`
- `{clan_members}`
- `{clan_online}`
- `{clan_bank}`
- `{clan_points}`
- `{clan_rank}`
- `{objective_capture}`
- `{objective_beacon}`
- `{objective_crystal}`
- `{war_active}`
- `{war_mode}`
- `{war_status}`
- `{war_time_left}`
- `{war_capture}`
- `{siege_open}`

## Notes
- Menu `size` must be multiple of 9 (`9..54`).
- `slot` must be inside menu size.
- For command action, slash is optional (`clan info` or `/clan info`).
- Use item `permission` to hide protected actions from non-admin users.

After edits run:
- `/clanadmin reload`
