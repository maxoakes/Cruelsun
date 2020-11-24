package com.scouter.cruelsun;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CruelSun.MODID)
public class CruelSun
{
    public static final String MODID = "cruelsun";

    public CruelSun() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modBus.addListener(this::setup);
        forgeBus.register(this);
        forgeBus.register(new BurnHandler());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        System.out.println("System print");
    }
}