# Commands

All player commands are under `/clan` (alias: `/cc`).

## General
| Command | Description | Access |
|---|---|---|
| `/clan help` | Show command help | Any player |
| `/clan menu` [main|war|objectives|admin]` | Open GUI menu by id | Any player (`admin` menu requires admin perm) |

## Clan Management
| Command | Description | Access |
|---|---|---|
| `/clan create <name>` | Create a clan | No clan |
| `/clan disband confirm` | Disband your clan | Leader |
| `/clan info [clan]` | Show clan info | Any clan member / target clan |
| `/clan list` | List all clans | Any player |
| `/clan top` | Season ranking top | Any player |
| `/clan online` | Online members of your clan | Clan member |
| `/clan invite <player>` | Invite player | Officer+ |
| `/clan join <clan>` | Join invited clan | Invited player |
| `/clan leave` | Leave clan | Clan member |
| `/clan kick <player>` | Kick member | Co-leader+ |
| `/clan promote <player>` | Promote member | Co-leader+ |
| `/clan demote <player>` | Demote member | Co-leader+ |
| `/clan owner <player> confirm` | Transfer ownership | Leader |

## Chat, Home, Claim, Bank
| Command | Description | Access |
|---|---|---|
| `/clan chat <message>` | Send clan chat message | Clan member |
| `/clan chat toggle` | Toggle clan chat mode | Clan member |
| `/clan sethome` | Set clan home | Co-leader+ |
| `/clan home` | Teleport to clan home | Clan member |
| `/clan claim set [radius]` | Set claim center and radius | Co-leader+ |
| `/clan bank` | Show clan bank | Clan member |
| `/clan bank deposit <amount>` | Deposit from player wallet via Vault | Clan member |
| `/clan bank withdraw <amount>` | Withdraw to player wallet via Vault | Co-leader+ |

## Diplomacy
| Command | Description | Access |
|---|---|---|
| `/clan ally <clan>` | Set relation to ally | Co-leader+ |
| `/clan enemy <clan>` | Set relation to enemy | Co-leader+ |
| `/clan neutral <clan>` | Set relation to neutral | Co-leader+ |

## Objectives
| Command | Description | Access |
|---|---|---|
| `/clan objective set <capture|beacon|crystal>` | Set objective at your location | Officer+ |
| `/clan objective remove <capture|beacon|crystal>` | Remove objective | Officer+ |
| `/clan objective validate` | Validate current location | Officer+ |
| `/clan objective preview [capture|beacon|crystal]` | Show reachable entries + particles | Officer+ |
| `/clan objective list` | List objective coordinates | Officer+ |

## War
| Command | Description | Access |
|---|---|---|
| `/clan war challenge <clan> <capture|beacon|crystal> [stake]` | Send war request | Officer+ |
| `/clan war accept <id>` | Accept incoming request | Officer+ (defender) |
| `/clan war deny <id>` | Deny incoming request | Officer+ (defender) |
| `/clan war info` | Show active war info | Clan member |
| `/clan war forfeit` | Forfeit active war | Officer+ |
| `/clan war log` | Show latest replays | Any clan member |

## Siege Windows
| Command | Description | Access |
|---|---|---|
| `/clan siege window set <day> <start> <end>` | Set custom day window | Officer+ |
| `/clan siege window view` | View custom windows and open state | Officer+ |
| `/clan siege window clear <day>` | Remove custom day window | Officer+ |

## Bounty
| Command | Description | Access |
|---|---|---|
| `/clan bounty create <targetClan> <reward>` | Place bounty contract | Officer+ |
| `/clan bounty info` | Show active bounty on your clan | Clan member |

## Admin (`/clanadmin`)
| Command | Description |
|---|---|
| `/clanadmin help` | Admin command help |
| `/clanadmin reload` | Reload config/messages/menus |
| `/clanadmin menu [admin|main|war|objectives]` | Open specific menu |
| `/clanadmin war stop <clan>` | Force-stop clan war |
| `/clanadmin war start <attacker> <defender> <capture|beacon|crystal>` | Force-start war |
| `/clanadmin season reset [name]` | Reset season |
| `/clanadmin points set <clan> <value>` | Set clan season points |
| `/clanadmin debug path <clan> <capture|beacon|crystal>` | Debug objective path validation |

