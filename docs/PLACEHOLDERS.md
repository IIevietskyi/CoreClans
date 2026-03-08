# PlaceholderAPI

Prefix: `%coreclans_...%`

## Clan/Profile
| Placeholder | Meaning |
|---|---|
| `%coreclans_has_clan%` | `true/false` |
| `%coreclans_clan_name%` | Clan name |
| `%coreclans_clan_tag%` | Clan tag |
| `%coreclans_clan_tag_colored%` | Colored clan tag |
| `%coreclans_clan_role%` | `leader/co_leader/officer/member` |
| `%coreclans_clan_members_online%` | Online clan members |
| `%coreclans_clan_members_total%` | Total clan members |
| `%coreclans_clan_bank%` | Clan bank amount |
| `%coreclans_clan_points%` | Season points |
| `%coreclans_clan_division%` | `bronze/silver/gold` |
| `%coreclans_clan_season_rank%` | Current rank position |

## Tab/UI
| Placeholder | Meaning |
|---|---|
| `%coreclans_tab_tag%` | `[TAG]` |
| `%coreclans_tab_name%` | `[TAG] PlayerName` |
| `%coreclans_tab_division%` | Division |
| `%coreclans_tab_war_status%` | `peace` or war status |

## War
| Placeholder | Meaning |
|---|---|
| `%coreclans_war_active%` | `true/false` |
| `%coreclans_war_mode%` | `capture/beacon/crystal` |
| `%coreclans_war_side%` | `attacker/defender` |
| `%coreclans_war_enemy%` | Enemy clan name |
| `%coreclans_war_time_left%` | Seconds left |
| `%coreclans_war_queue_position%` | Queue position or `-1` |
| `%coreclans_war_queue_eta%` | Estimated seconds to active slot |
| `%coreclans_war_attackers_cap%` | Current attacker cap |
| `%coreclans_war_attackers_active%` | Active attackers in cap |
| `%coreclans_war_capture_progress%` | Capture progress in percent |
| `%coreclans_war_crystal_hp%` | Crystal HP |
| `%coreclans_war_crystal_hp_percent%` | Crystal HP percent |

## Bounty
| Placeholder | Meaning |
|---|---|
| `%coreclans_bounty_active%` | `true/false` (on your clan) |
| `%coreclans_bounty_target_clan%` | Issuer clan name |
| `%coreclans_bounty_reward%` | Reward amount |
| `%coreclans_bounty_time_left%` | Seconds left |
| `%coreclans_bounty_progress%` | Current progress format |

## Season/Leaderboard
| Placeholder | Meaning |
|---|---|
| `%coreclans_season_name%` | Season display name |
| `%coreclans_season_days_left%` | Days left |
| `%coreclans_season_points%` | Your clan points |
| `%coreclans_season_division%` | Your clan division |
| `%coreclans_top_1_name%` | Top 1 clan name |
| `%coreclans_top_1_points%` | Top 1 points |
| `%coreclans_top_2_name%` | Top 2 clan name |
| `%coreclans_top_2_points%` | Top 2 points |
| `%coreclans_top_3_name%` | Top 3 clan name |
| `%coreclans_top_3_points%` | Top 3 points |

Pattern support also exists for other positions:
- `%coreclans_top_<n>_name%`
- `%coreclans_top_<n>_points%`

## Notes
- No external eCloud expansion needed for CoreClans placeholders.
- Placeholder values fallback to config keys:
  - `placeholders.no-clan`
  - `placeholders.no-war`
