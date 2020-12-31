package com.scouter.cruelsun;

import com.scouter.cruelsun.biomes.BiomeHelper;
import com.scouter.cruelsun.biomes.BiomeRegister;
import com.scouter.cruelsun.biomes.CSBiomeProvider;
import com.scouter.cruelsun.biomes.CSWorldType;
import com.scouter.cruelsun.features.FeatureRegister;
import com.scouter.cruelsun.handlers.BurnHandler;
import com.scouter.cruelsun.handlers.WorldBurnHandler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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
        modBus.addListener(this::setupBiomes);

        //Register Handlers
        forgeBus.register(new BurnHandler());
        forgeBus.register(new WorldBurnHandler());

        //Register Objects
        FeatureRegister.FEATURES.register(modBus);
        BiomeRegister.BIOMES.register(modBus);
        registerWorldType();

        forgeBus.register(this);
    }

    //private void onModConfigEvent(final ModConfig.ModConfigEvent event) {Configs.setLoaded();}

    //register the Scorched Earth world type
    public void registerWorldType()
    {
        CSWorldType csWorldType = new CSWorldType();
        System.out.println("Enqueue Provider Setup");
        csWorldType.setRegistryName(new ResourceLocation("cruelsun"));
        ForgeRegistries.WORLD_TYPES.register(csWorldType);
        Registry.register(Registry.BIOME_PROVIDER_CODEC, "cruelsun", CSBiomeProvider.CODEC);
    }

    //register the biomes
    private void setupBiomes(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (Biome b : BiomeHelper.SCORCHED_BIOMES)
            {
                BiomeDictionary.addTypes(key(b), BiomeDictionary.Type.OVERWORLD);
                BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(key(b), 0));
                //set weight to 0 because the scorched biomes should be only in the custom world type
            }
       });
    }

    private static RegistryKey<Biome> key(final Biome biome) {
        return RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, Objects.requireNonNull(ForgeRegistries.BIOMES.getKey(biome), "Biome registry name was null"));
    }

}

