# Permissions

## Bukkit Permissions
| Permission | Default | Description |
|---|---|---|
| `coreclans.user` | `true` | Basic CoreClans command access |
| `coreclans.admin` | `op` | Full admin access (`/clanadmin`, admin menus) |
| `coreclans.bypass` | `op` | Bypass war restrictions |

## Internal Clan Roles
CoreClans also has in-clan roles (not Bukkit permissions):

- `LEADER`
- `CO_LEADER`
- `OFFICER`
- `MEMBER`

Role-based abilities:
- Invite/manage war: `LEADER`, `CO_LEADER`, `OFFICER`
- Manage members/claim/home/bank withdraw: `LEADER`, `CO_LEADER`
- Transfer ownership/disband: `LEADER`

## LuckPerms Example
```text
/lp group default permission set coreclans.user true
/lp group admin permission set coreclans.admin true
/lp group admin permission set coreclans.bypass true
```
