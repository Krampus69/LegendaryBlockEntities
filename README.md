# Legendary Block Entities

A Forge 1.20.1 port of [Enhanced Block Entities](https://github.com/FoundationGames/EnhancedBlockEntities)
by FoundationGames, originally written for the Fabric mod loader.

**Licensed under LGPL-3.0** — same as the original. See `LICENSE.txt` and `NOTICE` for full attribution.

## What it does

Optimizes block entity rendering for:

- Chests (regular, trapped, ender)
- Signs (standing, wall, hanging)
- Bells
- Beds
- Shulker boxes
- Decorated pots

Instead of redrawing these every frame via vanilla's per-block-entity renderer,
they get baked into the chunk mesh as static block models. The animated renderer
only runs when there's actual motion (a lid opening, a bell swinging). The result
is a major FPS gain in builds with lots of chests or signs.

## Status

Early port — not yet feature-complete. See `NOTICE` for the list of changes from
the upstream Fabric source.

## Credit

All design credit goes to FoundationGames and the original Enhanced Block Entities
project. This port is just an adaptation to a different mod loader.

  - Original: https://github.com/FoundationGames/EnhancedBlockEntities
  - Original CurseForge: https://www.curseforge.com/minecraft/mc-mods/enhanced-block-entities
  - Original Modrinth: https://modrinth.com/mod/OVuFYfre

## License

GNU Lesser General Public License v3.0 (LGPL-3.0). Same as upstream.
You are free to fork, modify, and redistribute this mod under the terms of that
license. See `LICENSE.txt` for the full text.
