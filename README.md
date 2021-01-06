# Cruelsun

A forge Minecraft mod that makes the surface air of the Overworld (or other worlds declared in the config) super-hot with solar radiation.

Encourages (rather, forces) players to live and survive underground. They can go on the surface for a limited time if they are protected in some way.

Every tick, the following checks are made:
* Check the current world of the player to the whitelisted worlds in the configs
* Check if it is nighttime, and check if the configs allow for damage in the nighttime
* Check if the /cs or /cruelsun command has been used OR if the safety period has passed
  * If it has not been run, the player will not be burnt until the first day of the world (or whatever is stated in the configs)
  * If the command has been triggered to start the burn, the ticksToFirstBurn will be ignored
* Check if the player is wet, and check if the configs even support that
* Check if the player is in a "safe location"

If all of them return false, then damage is applied to the player in some way:

* Check if the player is wearing any armor
* If they are, damage each armor piece (explained in next section)
* Check if the player has full armor protection, AND potion protection
* If the player has fire resistance potion applied (and if the configs allow it), the player does not take damage

Armor damage is applied with the following steps:
* Check if the thing in the armor slot is actually armor, otherwise it is air (or a pumpkin...) and nothing should be done, and offer no protection
* Check if armor is damageable (regular minecraft armor, for example)
* If the armor is damageable...
    * Check if it is a modded hazmat-type armor piece (looks at the name of the armor for keywords like hazmat, scuba, or rubber)
    * If the armor is a hazmat-type armor piece, increase its base "protection value"
    * Check if the armor is enchanted with fire protection, and check if enchantments do anything in the configs
    * Get the level of the enchantment, and add it to the "protection value"
    * Every added protection value that is added means that the armor will take damage in that many seconds
* If the armor is not damageable... (many modded armors, like armors that use energy rather than durability)
    * Check if it uses Forge Energy
    * If it uses (compatible) energy, try to drain an amount from it
        * If the armor cannot have power extracted, give damage to player... *cough* Mekanism Mekasuit *cough*
    * If after the damaging of the armor, it becomes fully drained, catch the player on fire
    * If the armor does not have energy storage and is not damageable, set fire to the player

Finally, if the player is not wearing full armor, AND no potions are applied, AND they are not in a safe location
* Get the solar intensity of the player location (the closer it is to noon, the higher the damage is)
* Damage the player based on that value
  * Also, if it is day time, catch the player on fire too