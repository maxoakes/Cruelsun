package com.scouter.cruelsun;

import com.scouter.cruelsun.biomes.BiomeRegister;
import com.scouter.cruelsun.features.FeatureRegister;
import com.scouter.cruelsun.handlers.BurnHandler;
import com.scouter.cruelsun.handlers.WorldBurnHandler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

@Mod(CruelSun.MODID)
public class CruelSun
{
    public static final String MODID = "cruelsun";

    public CruelSun()
    {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.spec);

        //inits
        //modBus.addListener(this::setupBiomes);
        //modBus.addListener(this::onModConfigEvent);

        //Register Handlers
        forgeBus.register(new BurnHandler());
        forgeBus.register(new WorldBurnHandler());

        //Register Objects
        FeatureRegister.FEATURES.register(modBus);
        BiomeRegister.BIOMES.register(modBus);

        forgeBus.register(this);
    }

    private void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        Configs.setLoaded();
    }

    @SubscribeEvent
    public void setupBiomes(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            setupBiome(BiomeRegister.BIOME_SCORCHED.get(), BiomeManager.BiomeType.WARM, 1000, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.OVERWORLD);
        });
    }

    private void setupBiome(final Biome biome, final BiomeManager.BiomeType biomeType, final int weight, final BiomeDictionary.Type... types) {
        BiomeDictionary.addTypes(key(biome), types);
        BiomeManager.addBiome(biomeType, new BiomeManager.BiomeEntry(key(biome), weight));
    }

    private RegistryKey<Biome> key(final Biome biome) {
        return RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, Objects.requireNonNull(ForgeRegistries.BIOMES.getKey(biome), "Biome registry name was null"));
    }
}

