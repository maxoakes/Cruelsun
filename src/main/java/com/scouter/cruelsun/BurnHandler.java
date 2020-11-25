package com.scouter.cruelsun;

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
    public final int TPS = 20;
    int waitToBurnTime = Configs.CONFIGS.getBurnSafetyTime()*TPS; //literal is in seconds

    @SubscribeEvent
    public void onPlayerTickEvent(PlayerTickEvent event)
    {
        PlayerEntity player = event.player;

        if (player.getEntityWorld().getDimensionKey() != World.OVERWORLD) return; //this mod will only work in the Overworld
        if (player.isCreative() || player.isSpectator() || player.ticksExisted <= waitToBurnTime) return; //general safety
        if (event.side.isClient() || event.phase == TickEvent.Phase.END) return; //back-end safety
        if (Configs.CONFIGS.doesWaterStopBurn() && player.isWet()) return;

        damageConditionCheck(player);
    }

    private void damageConditionCheck(PlayerEntity player)
    {
        if (!isSafeLocation(player))
        {
            long time = player.getEntityWorld().getDayTime()%24000;

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
        return player.world.getLightFor(LightType.SKY,player.getPosition()) < Configs.CONFIGS.getMinLightToDamagePlayer(); //variable hard-mode
    }

    //check if they are protected by armor or potions
    private boolean isProtectedByPotion(PlayerEntity player)
    {
        //check if the configs allow for potions to be useful
        if (Configs.CONFIGS.doPotionsWork())
        {
            //check if the fire resistance buff is active on player
            for (Map.Entry<Effect, EffectInstance> buff : player.getActivePotionMap().entrySet()) {
                //System.out.println("K:" + buff.getKey() + " V:" + buff.getValue());
                if (buff.getKey() == Effects.FIRE_RESISTANCE) {
                    //System.out.println("Player is protected via potion:"+buff.getKey());
                    return true;
                }
            }
        }
        //if we get here, there are no potions applied, or no fire resistance potions applied
        return false;
    }

    private boolean isProtectedByArmor(PlayerEntity player){
        //check if the player is wearing armor
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
            if (!armor.isDamageable()) return; //we can figure out what to do with powered armor later...

            int protectionAmount = 1;

            //check if it is a modded hazmat-type armor piece
            String armorName = armor.getItem().getName().getString().toLowerCase();
            boolean isHazmat = armorName.contains("hazmat") || armorName.contains("rubber") || armorName.contains("scuba");
            if (isHazmat) protectionAmount = 3;

            //do configs allow enchantments to work
            if (Configs.CONFIGS.doEnchantmentsWork()) {
                //check if it is an enchanted armor piece with fire protection
                ListNBT armorEnchantments = armor.getEnchantmentTagList();

                for (INBT enchantment : armorEnchantments) {
                    if (enchantment.getString().contains("fire_protection")) {
                        //System.out.println("Armor protected via enchantment:"+enchantment.getString());
                        try {
                            protectionAmount += Integer.parseInt(enchantment.getString().replaceAll("\\D+", ""));
                            //System.out.println("E:" + enchantment.getString() + " Found Level: "+enchantedDamageRateMod);
                        } catch (Exception e) {
                            System.out.println("Error parsing int in enchantment substring");
                        }
                    }
                }
            }

            //if we get here, the armor is not protected in any way, and should take damage
            //every 1+(enchantment level of piece), the armor will take armorDamageRate damage
            if (time%(TPS*(protectionAmount*Configs.CONFIGS.getEnchantmentProtectionMultiplier()))==0) {
                armor.setDamage(armor.getDamage() + Configs.CONFIGS.getArmorDamageRate());
                System.out.println(armor.getItem().getName().getString() + ": " + armor.getDamage() + "/" + armor.getMaxDamage()+" (EnLvl:"+protectionAmount+")");
                if (armor.getMaxDamage() <= armor.getDamage()) armor.shrink(1);
            }
        }
    }

    private void damagePlayer(PlayerEntity player)
    {
        long time = player.getEntityWorld().getDayTime()%24000;
        if (time%TPS == 0) {
            float solarIntensity = Math.max(6000 - Math.abs((int) time - 6000), 0);
            int burnTime = Math.round(solarIntensity / 1000f);
            double sunDamage = (Math.ceil(solarIntensity / 3000) + 1) * Configs.CONFIGS.getDamageMultiplier();
            System.out.println("@DayTick:" + time + " Intensity:" + solarIntensity + " Burntime:" + burnTime + " Damage:" + sunDamage);

            //damage every one second rather than every tick
            player.attackEntityFrom(damageSource, (float) sunDamage);
            if (time <= 12000) player.setFire(burnTime * Configs.CONFIGS.getBurnTimeMultiplier());
        }
    }
}
