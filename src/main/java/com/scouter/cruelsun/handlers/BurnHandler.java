package com.scouter.cruelsun.handlers;

import com.scouter.cruelsun.Configs;
import com.scouter.cruelsun.CruelSun;
import com.scouter.cruelsun.commands.CommandSetBurn;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LightType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = CruelSun.MODID)
public class BurnHandler
{
    DamageSource damageSource = new DamageSource("cruelsun").setDamageBypassesArmor().setDifficultyScaled();
    private final int TPS = 20;

    @SubscribeEvent
    public void onPlayerTickEvent(PlayerTickEvent event)
    {
        PlayerEntity player = event.player;
        if (event.side.isClient() || event.phase == TickEvent.Phase.END) return; //back-end safety
        if (!Configs.CONFIGS.doPlayerDamage()) return; //initial check to see if the configs even want to do damage to player
        if (!(event.player.getEntityWorld().getDayTime()%TPS==0)) return; //do not need to use the tick helper if nothing is happening this tick
        if (player.isCreative() || player.isSpectator()) return; //general safety

        if (player.ticksExisted <= Configs.CONFIGS.getBurnSafetyTime()*TPS)
        {
            int secondsToBurn = Configs.CONFIGS.getBurnSafetyTime() - (player.ticksExisted/20);
            if (secondsToBurn % Configs.CONFIGS.getSpawnProtectionWarningInterval() == 0)
                player.sendMessage(new TranslationTextComponent("cruelsun.timer.spawn.safety.status", secondsToBurn), player.getUniqueID());
            if (secondsToBurn <= 1)
                player.sendMessage(new TranslationTextComponent("cruelsun.timer.spawn.safety.start"), player.getUniqueID());
            return;
        }

        boolean isInSafeWorld = false;
        String currentWorld = event.player.getEntityWorld().getDimensionKey().getLocation().toString();
        for (String w : Configs.CONFIGS.getAllowedWorlds())
        {
            if (currentWorld.equals(w))
            {
                if (Configs.CONFIGS.isDebugMode()) System.out.println("Current world is in whitelist");
                break;
            }
            isInSafeWorld = true;
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Current world is not on whitelist");
        }
        if (isInSafeWorld) return; //if the world is not on the whitelist of burning worlds, stop checking

        if (event.player.world.isNightTime() && Configs.CONFIGS.doDayDamageOnly()) return; //check if it is night time, and if the configs call for damage during only the day

        if (CommandSetBurn.getCommandState() == CommandSetBurn.CommandState.PAUSE) return; //check if command has been activated this session
        if ((player.world.getGameTime() < Configs.CONFIGS.ticksToFirstBurn()) && CommandSetBurn.getCommandState() == CommandSetBurn.CommandState.NORMAL)
        {
            int secondsToBurn = (int)(Configs.CONFIGS.ticksToFirstBurn() - (player.world.getGameTime()))/20;
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Seconds until burn: " + secondsToBurn);
            if (secondsToBurn % Configs.CONFIGS.getFirstDayProtectionWarningInterval() == 0)
                player.sendMessage(new TranslationTextComponent("cruelsun.timer.firstday.safety.status", secondsToBurn), player.getUniqueID());
            if (secondsToBurn == 0)
                player.sendMessage(new TranslationTextComponent("cruelsun.timer.firstday.safety.start"), player.getUniqueID());
            return; //protection for the first day of the world
        }
        //if the command has been triggered to start the burn, the ticksToFirstBurn will be ignored

        //4 = new moon. Check if it is a new moon
        if ((Configs.CONFIGS.isNewMoonSafe() && player.getEntityWorld().getMoonPhase() == 4) && player.getEntityWorld().isNightTime())
        {
            if (Configs.CONFIGS.isDebugMode()) System.out.println("The player is safe in the new moon.");
            return;
        }

        System.out.println("Moon phase: " + player.getEntityWorld().getMoonPhase());
        if (Configs.CONFIGS.doesWaterStopBurn() && player.isWet()) return; //check if the player is wet this tick, or check if the configs even support that
        damageConditionCheck(player); //do the damage
    }

    private void damageConditionCheck(PlayerEntity player)
    {
        if (!isSafeLocation(player))
        {
            long time = player.getEntityWorld().getDayTime()%24000;
            if (Configs.CONFIGS.isDebugMode()) System.out.println("*** Starting damageConditionCheck is new tick@"+time+"... info below");
            //damage the armor if they are not protected
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.HEAD), time, player);
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.CHEST), time, player);
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.LEGS), time, player);
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.FEET), time, player);

            //if the player is not protected by armor, and not protected by potions, then they are damaged
            if (!isProtectedByArmor(player) && !isProtectedByPotion(player)) damagePlayer(player);
        }
    }

    private boolean isSafeLocation(PlayerEntity player)
    {
        //if the player is in a block that is "dark" enough, eg. in a light level less than what is in the configs, then they are "safe"
        if (Configs.CONFIGS.isDebugMode()) System.out.println("Light:"+player.world.getLightFor(LightType.SKY,player.getPosition())+"/15 Config: >"+Configs.CONFIGS.getMinLightToDamagePlayer());
        return player.world.getLightFor(LightType.SKY,player.getPosition()) < Configs.CONFIGS.getMinLightToDamagePlayer();
    }

    //check if they are protected by armor or potions
    private boolean isProtectedByPotion(PlayerEntity player)
    {
        //check if the configs allow for potions to be useful
        if (Configs.CONFIGS.doPotionsWork())
        {
            //iterate through list of active player potion effects and check if the fire resistance is one of them
            for (Map.Entry<Effect, EffectInstance> buff : player.getActivePotionMap().entrySet()) {
                if (Configs.CONFIGS.isDebugMode()) System.out.println("PotionEffect: "+buff.getKey().getDisplayName());
                if (buff.getKey() == Effects.FIRE_RESISTANCE) {
                    return true;
                }
            }
        }
        //if we get here, there are no potions applied, or no fire resistance potions applied, or the configs say that potions are useless
        return false;
    }

    private boolean isProtectedByArmor(PlayerEntity player)
    {
        //check if the player is wearing armor, and check the configs to see if armor is useful
        return player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof ArmorItem &&
                player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof ArmorItem &&
                player.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem() instanceof ArmorItem &&
                player.getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof ArmorItem &&
                Configs.CONFIGS.doesArmorWork();
    }

    //try to damage the armor that is passed in
    private void damageSingleArmor(ItemStack armor, long time, PlayerEntity player)
    {
        //check if the things in the armor slot is actually armor, otherwise it is air (or a pumpkin...) and nothing should be done
        if (!(armor.getItem() instanceof ArmorItem)) return;

        String armorName = armor.getItem().getName().getString().toLowerCase();

        //check if armor is damageable
        if (armor.isDamageable())
        {
            //if the armor is damageable...
            if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName + " is damageable.");

            //base amount that durability will be taken from the armor each tick check
            int protectionAmount = 1;

            //check if it is a modded hazmat-type armor piece
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Config list of keywords: " + Configs.CONFIGS.getHazmatStrings());
            for (String keyword : Configs.CONFIGS.getHazmatStrings()) {
                if (Configs.CONFIGS.isDebugMode())
                    System.out.println("Checking for keyword '" + keyword + "' in '" + armorName + "'");
                if (armorName.contains(keyword)) {
                    //if the armor is a hazmat-type armor piece, increase its protection value
                    protectionAmount = 3;
                    if (Configs.CONFIGS.isDebugMode()) System.out.println("Confirmed " + armorName + " isHazmat type because config:" + keyword);
                }
            }

            //check if the armor is enchanted with fire protection
            //do configs allow enchantments to work
            if (Configs.CONFIGS.doEnchantmentsWork()) {
                ListNBT armorEnchantments = armor.getEnchantmentTagList();
                for (INBT enchantment : armorEnchantments) {
                    if (Configs.CONFIGS.isDebugMode()) System.out.println("ArmorEnch: " + armorName + ": " + enchantment.getString());
                    if (enchantment.getString().contains("fire_protection")) {
                        try {
                            //extract the level of the enchantment from the toString, and add it to the base protection amount of the armor
                            protectionAmount += Integer.parseInt(enchantment.getString().replaceAll("\\D+", ""));
                            if (Configs.CONFIGS.isDebugMode())
                                System.out.println("Fire protection found on " + armorName);
                        } catch (Exception e) {
                            System.out.println("Error parsing int in enchantment substring. How did we get here?!");
                        }
                    }
                }
            }

            //every 'protectionAmount' seconds, the armor will take armorDamageRate damage
            if (time % (TPS * ((long) protectionAmount * Configs.CONFIGS.getEnchantmentProtectionMultiplier())) == 0)
            {
                armor.setDamage(armor.getDamage() + Configs.CONFIGS.getArmorDamageRate());
                if (Configs.CONFIGS.isDebugMode()) System.out.println("Damaging armor: "+armorName + ": " + armor.getDamage() + "/" + armor.getMaxDamage() + " (protection:" + protectionAmount + ")");
                if (armor.getMaxDamage() <= armor.getDamage())
                    armor.shrink(1); //if we are at the maximum damage level, destroy the armor
            }
        }
        else
        {
            //if the armor is not damageable...
            if (Configs.CONFIGS.isDebugMode()) System.out.println("A:"+armorName+" is not damageable");

            //check if it uses Forge Energy
            if (armor.getCapability(CapabilityEnergy.ENERGY).isPresent())
            {
                try {
                    if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName + " has energy");

                    //if it uses energy, try to drain an amount from it
                    LazyOptional<IEnergyStorage> optional = armor.getCapability(CapabilityEnergy.ENERGY);
                    if (optional.isPresent())
                    {
                        IEnergyStorage energyStorage = optional.orElseThrow(IllegalStateException::new);
                        if (Configs.CONFIGS.isDebugMode()) System.out.println("canReceive:"+energyStorage.canReceive()+" canExtract:"+energyStorage.canExtract());

                        //if the armor can have power extracted, 'damage' it by taking away some stored energy
                        if (energyStorage.canExtract())
                        {
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("canExtract:"+energyStorage.canExtract()+". Extracting energy from armor.");
                            energyStorage.extractEnergy(Configs.CONFIGS.getEnergyArmorDrainRate(), false);
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("Damaged " + Configs.CONFIGS.getEnergyArmorDrainRate() + ". Now " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored());
                        }
                        //if the armor cannot have power extracted, give damage to player... *cough* Mekanism Mekasuit
                        else
                        {
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("canExtract:"+energyStorage.canExtract()+". Damaging player directly.");
                            catchFireToPlayer(player, 2); //player.attackEntityFrom(new DamageSource("cruelsun.energyarmor"), 2);
                        }

                        //if after the damaging of the armor, it becomes fully drained, catch the player on fire
                        if (energyStorage.getEnergyStored() == 0)
                        {
                            if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName+" is out of charge. Player is not protected. Setting fire.");
                            catchFireToPlayer(player, 2);
                        }
                    }
                }
                //if we get here, something weird happened
                catch (Exception e)
                {
                    System.out.println("Exception caught trying to drain armor");
                    catchFireToPlayer(player, 2);
                }
            }
            else
            {
                //if the armor does not have energy storage and is not damageable, set fire to the player
                if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName+" does not have energy, and is not damageable. Just set fire to player");
                catchFireToPlayer(player, 2);
            }
        }
    }

    private void damagePlayer(PlayerEntity player)
    {
        long time = player.getEntityWorld().getDayTime()%24000;
        //define what solar intensity is when it comes to damaging the player
        float solarIntensity = Math.max(6000 - Math.abs((int) time - 6000), 0);
        int burnTime = Math.round(solarIntensity / 1000f);
        double sunDamage = (Math.ceil(solarIntensity / 3000) + 1) * Configs.CONFIGS.getDamageMultiplier();
        if (Configs.CONFIGS.isDebugMode()) System.out.println("Damaging player @DayTick:" + time + " Intensity:" + solarIntensity + " Burntime:" + burnTime + " Damage:" + sunDamage);

        player.attackEntityFrom(damageSource, (float) sunDamage);
        if (time <= 12000) catchFireToPlayer(player, burnTime);
    }

    private void catchFireToPlayer(PlayerEntity player, int duration)
    {
        player.setFire(duration * Configs.CONFIGS.getBurnTimeMultiplier());
    }
}
