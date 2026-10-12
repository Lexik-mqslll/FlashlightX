# 🔦 FlashlightX

**A real flashlight for Minecraft.** A directional beam that goes where you look, runs on batteries you have to carry, and that other players can see — no glowing aura stuck to your character.

---

## 💡 The beam

- **Lights what you point at** — a cone reaching **14 blocks**, bright in the middle, softer at the edges
- **Works point-blank** — press it against a wall and the wall stays lit instead of going dark
- **Shines through glass, doors, trapdoors and plants** — anything that doesn't really block light
- **Stops at solid blocks** — no light leaking into the room next door
- **Works underwater**

---

## 🔋 Batteries

- One battery lasts **3 minutes** of light, and they stack to 8
- Runs dry? The flashlight **grabs a fresh one from your inventory** and keeps going
- A **charge bar** on the item shows what's left, green to red
- **Switched off, it drains nothing** — and it switches itself off when you put it away
- No battery at all? Just a dry click

---

## 🌐 Multiplayer

- **Other players' beams are visible** — you see their light land exactly where they do
- Up to **4 nearby flashlights** at once, within 48 blocks
- **Light is drawn on each client**, so the server does no work for it and sends no lighting packets

---

## ⚙️ Configurable

Two config files, both generated on first launch:

- `flashlightx-client.toml` — beam range, cone angle, brightness of each ray, falloff, update rate, how many other players' beams to draw, glowing lens, raised arm and its angle
- `flashlightx-common.toml` — battery life, drain interval, auto-swap, whether it switches off when stowed, creative behaviour, click volume

---

## 🔨 Crafting

**Flashlight** — vertically, in any column

|  |
|:-:|
| Redstone Lamp |
| Redstone Dust |
| Iron Ingot |

**Battery** (makes 2) — vertically, in any column

|  |
|:-:|
| Iron Nugget |
| Lime Wool |
| Iron Nugget |

Both are in the mod's own creative tab.

---

## 📋 Versions & requirements

**[GeckoLib](https://modrinth.com/mod/geckolib) is required** — it drives the animated model. Pick the build that matches your game:

| Minecraft | Loader | GeckoLib |
|---|---|---|
| **1.21.1** | NeoForge 21.1+ | **4.6 – 4.9.3** (any release for 1.21.1) |
| **1.20.1** | Forge 47+ | **4.4 – 4.4.9** or **4.7 – 4.8.4** |

Two things worth knowing about the 1.20.1 column:

- GeckoLib **4.5 and 4.6 were never released for 1.20.1**, which is why the range has a gap — nothing is missing on this mod's side.
- Versions **4.2.x and 4.3.x are not supported**, so grab 4.4 or newer.

Install on **both the server and the client**.

---

## 👥 Credits

Developed by **Lexik**.
