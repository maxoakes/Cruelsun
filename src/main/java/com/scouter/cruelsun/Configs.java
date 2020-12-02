package com.scouter.cruelsun;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Configs {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Configs CONFIGS = new Configs(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();

    private static boolean loaded = false;
    private static List<Runnable> loadActions = new ArrayList<>();

    public static void setLoaded() {
        if (!loaded)
            loadActions.forEach(Runnable::run);
        loaded = true;
    }

    public static boolean isLoaded() {return loaded;}

    public static void onLoad(Runnable action) {
        if (loaded)
            action.run();
        else
            loadActions.add(action);
    }

    public final ForgeConfigSpec.IntValue secondsAfterSpawnUntilBurn;
    public final ForgeConfigSpec.IntValue armorDamageRate;
    public final ForgeConfigSpec.IntValue minLightToDamagePlayer;
    public final ForgeConfigSpec.IntValue burnTimeMultiplier;
    public final ForgeConfigSpec.IntValue damageMultiplier;
    public final ForgeConfigSpec.IntValue enchantmentProtectionMultiplier;
    public final ForgeConfigSpec.BooleanValue wetStopsBurn;
    public final ForgeConfigSpec.BooleanValue armorWorks;
    public final ForgeConfigSpec.BooleanValue enchantmentsWork;
    public final ForgeConfigSpec.BooleanValue potionsWork;
    public final ForgeConfigSpec.BooleanValue debug;
    public final ForgeConfigSpec.BooleanValue doPlayerDamage;
    public final ForgeConfigSpec.BooleanValue doWorldDamage;
    public final ForgeConfigSpec.BooleanValue doMobDamage;

    public int getBurnSafetyTime() {return secondsAfterSpawnUntilBurn.get();}
    public int getArmorDamageRate() {return armorDamageRate.get();}
    public int getMinLightToDamagePlayer() {return minLightToDamagePlayer.get();}
    public int getBurnTimeMultiplier() {return burnTimeMultiplier.get();}
    public int getDamageMultiplier() {return damageMultiplier.get();}
    public int getEnchantmentProtectionMultiplier() {return enchantmentProtectionMultiplier.get();}
    public boolean doesWaterStopBurn() {return wetStopsBurn.get();}
    public boolean doesArmorWork() {return armorWorks.get();}
    public boolean doEnchantmentsWork() {return enchantmentsWork.get();}
    public boolean doPotionsWork() {return potionsWork.get();}
    public boolean isDebugMode() {return debug.get();}
    public boolean doPlayerDamage() {return doPlayerDamage.get();}
    public boolean doWorldDamage() {return  doWorldDamage.get();}
    public boolean doMobDamage() {return doMobDamage.get();}

    Configs(ForgeConfigSpec.Builder builder) {
        builder.push("Configs");

        secondsAfterSpawnUntilBurn = builder
                .comment("Seconds after spawning (into server or after respawn) that damage and/or burning starts.\n" +
                        "20 is default.")
                .defineInRange("secondsAfterSpawnUntilBurn", 20, 0, Integer.MAX_VALUE);

        armorDamageRate = builder
                .comment("Whenever an armor piece gets damaged, it takes this many durability damage.\n"+
                        "Is not a multiplier. This is literally the amount of durability damage that is taken each second.\n" +
                        "Default is 2.")
                .defineInRange("armorDamageRate", 2, 0, Integer.MAX_VALUE);

        minLightToDamagePlayer = builder
                .comment("What light level from the sky does it take to damage the player, and mobs, if applicable.\n" +
                        "This is the max potential light from the sky, not the current light level.\n" +
                        "0 is pitch black. 15 is exposed to open sky. Setting to 0 means that you will take damage in all light levels (even pitch black!)\n" +
                        "Used in context: getLightFor(SKY, player.getPosition()) < getMinLightToDamagePlayer()\n" +
                        "Default is 10.")
                .defineInRange("minLightToDamagePlayer", 10, 0, 15);

        burnTimeMultiplier = builder
                .comment("When outside during the day, how many times longer should the player burn once they are in a safe place?\n" +
                        "Multiplies existing burn time: player.setFire(burnTime * getBurnTimeMultiplier()\n" +
                        "0 is turning off burning all together.\n" +
                        "Default is 1")
                .defineInRange("burnTimeMultiplier", 1, 0, Integer.MAX_VALUE);

        damageMultiplier = builder
                .comment("Generally how much more deadly it is being outside.\n" +
                        "Multiplies existing damage taken: (solarIntensity + 1) * getDamageMultiplier()\n" +
                        "Default is 2. Anything more than 5 a quick death.")
                .defineInRange("damageMultiplier", 2, 0, Integer.MAX_VALUE);

        enchantmentProtectionMultiplier = builder
                .comment("How much more effective are enchantments at protecting armor when outside?\n" +
                        "Multiplies existing damage taken: (protectionAmount*getEnchantmentProtectionMultiplier())\n" +
                        "Default is 1. Bigger numbers means that the armor gets damaged less frequently.")
                .defineInRange("enchantmentProtectionMultiplier", 1, 0, Integer.MAX_VALUE);

        wetStopsBurn = builder
                .comment("If true, the player will not burn or take damage when they are getting rained on, or if they are in the water.\n" +
                        "Default is true")
                .define("wetStopsBurn", true);

        armorWorks = builder
                .comment("Does wearing armor block burning and damage? Only works if the player is wearing full armor.\n" +
                        "Default is true.")
                .define("armorWorks", true);

        enchantmentsWork = builder
                .comment("If true, fire resistance armor enchantments will reduce armor damage rate when outside.\n" +
                        "Default is true.")
                .define("enchantmentsWork", true);

        potionsWork = builder
                .comment("If true, fire resistance potions stop burning and damage from being outside.\n" +
                        "Default is true.")
                .define("potionsWork", true);

        debug = builder
                .comment("Enable debug mode. Will spam the console with System.out.println info about damage taken\n" +
                        "Default is false.")
                .define("debug", false);

        doPlayerDamage = builder
                .comment("Enables the main part of the mod; damaging the player when they are on the surface.\n"+
                        "Mainly for debugging purposes.\n" +
                        "Default is true.")
                .define("doPlayerDamage", true);

        doWorldDamage = builder
                .comment("Enables the secondary part of the mod; catching fire to flammable surface blocks.\n" +
                        "Default is true.")
                .define("doWorldDamage", true);

        doMobDamage = builder
                .comment("Enables the secondary part of the mod; catching fire to mobs that are exposed to the surface.\n" +
                        "Default is true.")
                .define("doMobDamage", true);

        builder.pop();
    }
}
