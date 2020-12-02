package com.scouter.cruelsun;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CruelSun.MODID)
public class CruelSun
{
    public static final String MODID = "cruelsun";

    public CruelSun() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.spec);
        modBus.addListener(this::setup);
        forgeBus.register(new BurnHandler());
        forgeBus.register(new WorldBurnHandler());
        modBus.addListener(this::onModConfigEvent);
        forgeBus.register(this);
    }

    private void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        Configs.setLoaded();
    }
    private void setup(final FMLCommonSetupEvent event)
    {
        System.out.println("Cruelsun setup");
    }
}