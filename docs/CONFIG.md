# Configuration

Main file: `plugins/CoreClans/config.yml`

## Full Key Reference
```yaml
prefix

storage.file
storage.autosave-seconds

clan.max-members
clan.min-name-length
clan.max-name-length
clan.default-tag

claim.default-radius
claim.min-radius
claim.max-radius
claim.objective-border-margin
claim.min-y
claim.max-y

war.duration-minutes
war.warmup-seconds
war.capture-radius
war.capture-target-seconds
war.crystal-default-hp
war.attacker-entry-distance
war.required-paths
war.max-path-distance
war.attacker-cap-offset
war.enable-attacker-queue
war.blocked-materials-near-objective

anti-zerg.enabled
anti-zerg.max-penalty-percent
anti-zerg.tiers
anti-zerg.crystal-damage-scaling
anti-zerg.capture-speed-scaling
anti-zerg.underdog-defense-bonus-percent

season.name
season.length-days
season.divisions.bronze-min
season.divisions.silver-min
season.divisions.gold-min

siege-windows.monday
siege-windows.tuesday
siege-windows.wednesday
siege-windows.thursday
siege-windows.friday
siege-windows.saturday
siege-windows.sunday

bounty.enabled
bounty.default-duration-hours

placeholders.no-clan
placeholders.no-war
```

## Most Important Tuning

### Objective Validation
- `claim.objective-border-margin`: objective must not be too close to claim border.
- `claim.min-y` / `claim.max-y`: valid Y-range for objectives.
- `war.blocked-materials-near-objective`: blocked blocks near objective (lava/water/cobweb, etc).
- `war.required-paths`: minimum number of reachable attacker sectors.
- `war.max-path-distance`: maximum path search radius.
- `war.attacker-entry-distance`: distance used to generate candidate entries.

### War Flow
- `war.duration-minutes`: max war duration.
- `war.warmup-seconds`: warmup before war turns active.
- `war.capture-target-seconds`: seconds needed to complete capture/beacon.
- `war.crystal-default-hp`: crystal HP for crystal mode.

### Anti-Zerg
- `anti-zerg.tiers`: online-difference tiers and penalties.
- `anti-zerg.max-penalty-percent`: hard cap for penalty.
- `anti-zerg.crystal-damage-scaling`: reduce larger side crystal damage.
- `anti-zerg.capture-speed-scaling`: reduce larger side capture speed.
- `anti-zerg.underdog-defense-bonus-percent`: damage reduction for smaller side.

### Queue/Cap
- `war.attacker-cap-offset`: attacker cap is defender online + this value.
- `war.enable-attacker-queue`: puts extra attackers in queue.

## Messages and Menus
- Message file: `plugins/CoreClans/messages.yml`
- Menu files: `plugins/CoreClans/menus/*.yml`

Use `/clanadmin reload` after editing.
