package com.scouter.cruelsun.handlers;

import com.scouter.cruelsun.Configs;
import com.scouter.cruelsun.CruelSun;
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
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
    int waitToBurnTime = Configs.CONFIGS.getBurnSafetyTime()*TPS;

    @SubscribeEvent
    public void onPlayerTickEvent(PlayerTickEvent event)
    {
        if (!Configs.CONFIGS.doPlayerDamage()) return; //initial check to see if the configs even want to do damage to player
        if (!(event.player.getEntityWorld().getDayTime()%TPS==0)) return; //do not need to use the tick helper if nothing is happening this tick
        PlayerEntity player = event.player;

        if (player.getEntityWorld().getDimensionKey() != World.OVERWORLD) return; //this mod will only work in the Overworld
        if (Configs.CONFIGS.doFirstDayProtection() && player.world.getGameTime() < 13188) return; //protection for the first day of the world
        if (player.isCreative() || player.isSpectator() || player.ticksExisted <= waitToBurnTime) return; //general safety
        if (event.side.isClient() || event.phase == TickEvent.Phase.END) return; //back-end safety
        if (Configs.CONFIGS.doesWaterStopBurn() && player.isWet()) return; //check if the player is wet this tick, or check if the configs even support that
        damageConditionCheck(player); //do the damage
    }

    private void damageConditionCheck(PlayerEntity player)
    {
        if (!isSafeLocation(player))
        {
            if (Configs.CONFIGS.isDebugMode()) System.out.println("*&*&*&*&*&*&* Starting damageConditionCheck is new tick... info below");
            long time = player.getEntityWorld().getDayTime()%24000;
            if (Configs.CONFIGS.isDebugMode()) System.out.println("Tick time of day: "+time);
            //damage the armor if they are not protected
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.HEAD), time);
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.CHEST), time);
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.LEGS), time);
            damageSingleArmor(player.getItemStackFromSlot(EquipmentSlotType.FEET), time);

            //if the player is not protected by armor, and not protected by potions, then they are damaged
            if (!isProtectedByArmor(player) && !isProtectedByPotion(player)) damagePlayer(player);
        }
    }

    private boolean isSafeLocation(PlayerEntity player)
    {
        //if the player is in a block that is "dark" enough, eg. in a light level less than what is in the configs, then they are "safe"
        if (Configs.CONFIGS.isDebugMode()) System.out.println("Light:"+player.world.getLightFor(LightType.SKY,player.getPosition())+"/15 C:"+Configs.CONFIGS.getMinLightToDamagePlayer());
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
    private void damageSingleArmor(ItemStack armor, long time)
    {
        //check if the things in the armor slot is actually armor, otherwise it is air and nothing should be done
        if (armor.getItem() instanceof ArmorItem)
        {
            String armorName = armor.getItem().getName().getString().toLowerCase();
            //if the armor is not damageable... I don't know how to access it's energy level if it is charged armor, so just set the player on fire!
            if (!armor.isDamageable())
            {
                if (Configs.CONFIGS.isDebugMode()) System.out.println("A:"+armorName+" isDamageable, setting fire.");
                try {
                    assert armor.getAttachedEntity() != null;
                    armor.getAttachedEntity().setFire(1+Configs.CONFIGS.getBurnTimeMultiplier());}
                catch (Exception ignored) {}
                return;
            }

            //base amount that durability will be taken from the armor each tick check
            int protectionAmount = 1;

            //check if it is a modded hazmat-type armor piece

            //TODO: add config list for armor items that might be to protect the player additionally
            //this list should cover for all of the modded hazmat-type armor
            boolean isHazmat = armorName.contains("hazmat") || armorName.contains("rubber") || armorName.contains("scuba");
            if (isHazmat) protectionAmount = 3;

            //do configs allow enchantments to work
            if (Configs.CONFIGS.doEnchantmentsWork())
            {
                //check if it is an enchanted armor piece with fire protection
                ListNBT armorEnchantments = armor.getEnchantmentTagList();
                for (INBT enchantment : armorEnchantments) {
                    if (Configs.CONFIGS.isDebugMode()) System.out.println("ArmorEnch: "+armorName+": " + enchantment.getString());
                    if (enchantment.getString().contains("fire_protection")) {
                        //extract the level of the enchantment from the toString, and add it to the base protection amount of the armor
                        try {
                            protectionAmount += Integer.parseInt(enchantment.getString().replaceAll("\\D+", ""));
                            if (Configs.CONFIGS.isDebugMode()) System.out.println("Fire protection found on "+armorName);
                        } catch (Exception e) {
                            System.out.println("CruelSun: Error parsing int in enchantment substring. How did we get here?!");
                        }
                    }
                }
            }

            //if we get here, the armor is not protected in any way, and should take damage
            //every 1+(enchantment level of piece) seconds, the armor will take armorDamageRate damage
            if (time%(TPS*((long) protectionAmount * Configs.CONFIGS.getEnchantmentProtectionMultiplier()))==0) {
                armor.setDamage(armor.getDamage() + Configs.CONFIGS.getArmorDamageRate());
                if (Configs.CONFIGS.isDebugMode()) System.out.println(armorName+": "+armor.getDamage()+"/"+armor.getMaxDamage()+" (EnLvl:"+protectionAmount+")");
                if (armor.getMaxDamage() <= armor.getDamage()) armor.shrink(1); //if we are at the maximum damage level, destroy the armor
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
        if (Configs.CONFIGS.isDebugMode()) System.out.println("@DayTick:" + time + " Intensity:" + solarIntensity + " Burntime:" + burnTime + " Damage:" + sunDamage);

        player.attackEntityFrom(damageSource, (float) sunDamage);
        if (time <= 12000) player.setFire(burnTime * Configs.CONFIGS.getBurnTimeMultiplier());
    }
}
