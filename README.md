# CodeTest

A Minecraft mod for prototyping and testing miscellaneous content. Currently, it mainly includes some QoL features, which may become one or more standalone mods in the future.

## Example Config

```json
{
  "enhancedMatchingEnabled": true,
  "enhancedCursorEnabled": true,
  "noAutoCreateWorldScreen": true,
  "glfwCommandEnabled": true,
  "offlineIntegratedServer": true,
  "customGameModeSwitcherScreen": true,
  "gameModes": [
    {
      "type": null,
      "name": {
        "translate": "gui.cancel"
      },
      "command": null,
      "item": null
    },
    {
      "type": "creative",
      "name": {
        "translate": "gameMode.creative"
      },
      "command": "gamemode creative",
      "item": "minecraft:grass_block"
    },
    {
      "type": "survival",
      "name": {
        "translate": "gameMode.survival"
      },
      "command": "gamemode survival",
      "item": "minecraft:iron_sword"
    },
    {
      "type": "adventure",
      "name": {
        "translate": "gameMode.adventure"
      },
      "command": "gamemode adventure",
      "item": "minecraft:map"
    },
    {
      "type": "spectator",
      "name": {
        "translate": "gameMode.spectator"
      },
      "command": "gamemode spectator",
      "item": "minecraft:ender_eye"
    },
    {
      "type": null,
      "name": "time set day",
      "command": "time set day",
      "item": "minecraft:clock"
    },
    {
      "type": null,
      "name": {
        "text": "Super fireball",
        "bold": true
      },
      "command": "summon minecraft:fireball ~ ~ ~ {ExplosionPower:127b}",
      "item": "minecraft:fire_charge"
    }
  ]
}
```