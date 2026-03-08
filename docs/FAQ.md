# FAQ

## 1) Why do I get `Objective invalid: objective is not reachable from enough attack entries`?
This means objective validation failed.

Check:
- objective is inside claim and not too close to border,
- valid Y-level (`claim.min-y`..`claim.max-y`),
- enough free head/feet space,
- not near blocked materials (`war.blocked-materials-near-objective`),
- reachable from enough attack sectors (`war.required-paths`).

Use:
- `/clan objective preview capture`
- `/clan objective preview beacon`
- `/clan objective preview crystal`
- `/clanadmin debug path <clan> <capture|beacon|crystal>`

## 2) Capture/beacon does not progress during war.
Typical causes:
- objective for selected war mode is not set for defender,
- war still in warmup,
- attackers are not inside objective radius,
- attackers are in queue and not in active attacker cap,
- defenders count >= attackers in zone.

Use `/clan war info` and objective preview commands.

## 3) Can a clan challenge itself?
No. Self-war is blocked.

## 4) Why does `/clan claim set` default to 64?
If radius is not provided, plugin uses current clan radius or config default.

You can set explicit radius:
- `/clan claim set 32`
- `/clan claim set 64`
- `/clan claim set 96`
- `/clan claim set 128`

(inside min/max config limits)

## 5) Does CoreClans require MySQL or SQLite?
No. CoreClans uses local JSON:
- `plugins/CoreClans/coreclans-data.json`

## 6) Does Java 17 work?
Yes. Plugin is Java 8-targeted, but runs on newer JVM versions.

## 7) Do I need PlaceholderAPI eCloud for CoreClans placeholders?
No. CoreClans provides its own built-in PAPI expansion.

## 8) What rewards are given after war?
Default war rewards:
- Winner: `+100` season points and `+100` bank.
- Winner also receives full stake pot (if stake > 0).
- Loser: `-30` season points.
- Bounty progress is updated based on result.
