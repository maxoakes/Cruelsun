package com.scouter.cruelsun;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configs {

    public final ForgeConfigSpec.IntValue burnTimeStart;
    public final ForgeConfigSpec.IntValue burnTimeStop;
    public final ForgeConfigSpec.IntValue lengthOfBurn;
    public final ForgeConfigSpec.BooleanValue wetStopsBurn;
    public final ForgeConfigSpec.BooleanValue fullArmorBlocksBurn;
    public final ForgeConfigSpec.BooleanValue hatsBlockBurn;
    public final ForgeConfigSpec.IntValue waitToBurnTime;
    public final ForgeConfigSpec.BooleanValue bypassFireResist;
    public final ForgeConfigSpec.IntValue bypassDamage;

    Configs(ForgeConfigSpec.Builder builder) {
        builder.push("general");

        burnTimeStart = builder
                .comment("Start time of burning.")
                .defineInRange("startTime", 1000, 0, 23999);

        burnTimeStop = builder
                .comment("End time of burning.")
                .defineInRange("startTime", 15000, 0, 23999);

        lengthOfBurn = builder
                .comment("How long the player burns after they are in a safe space.")
                .defineInRange("lengthOfBurn", 1, 0, Integer.MAX_VALUE);

        wetStopsBurn = builder
                .comment("The player being wet stops the burn. (Rain and water stops burning)")
                .define("wetStopsBurn", true);

        fullArmorBlocksBurn = builder
                .comment("Should players always burn over a certain Y level? This setting only works if playerMustSeeSky is false!")
                .define("fullArmorBlocksBurn", false);

        hatsBlockBurn = builder
                .comment("Should players always burn over a certain Y level? This setting only works if playerMustSeeSky is false!")
                .define("hatsBlockBurn", true);

        waitToBurnTime = builder
                .comment("How long to wait in ticks before players can burn after spawning into the world, This includes logging in. 20 ticks = 1 second, 1200 ticks = 1 minute, 72000 ticks = 1 hour")
                .defineInRange("waitToBurnTime", 1200, 0, Integer.MAX_VALUE);

        bypassFireResist = builder
                .comment("Changes the damage type to bypass fire resist potions/enchants.")
                .define("bypassFireResist", false);

        bypassDamage = builder
                .comment("How much damage the player takes from bypass damage.")
                .defineInRange("bypassDamage", 1, 0, Integer.MAX_VALUE);
        builder.pop();
    }
}
