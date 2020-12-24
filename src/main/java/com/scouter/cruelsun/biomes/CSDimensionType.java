package com.scouter.cruelsun.biomes;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.EndBiomeProvider;
import net.minecraft.world.biome.provider.NetherBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;

import java.util.OptionalLong;

public class CSDimensionType extends DimensionType
{
    protected CSDimensionType(OptionalLong p_i241972_1_, boolean p_i241972_2_, boolean p_i241972_3_, boolean p_i241972_4_, boolean p_i241972_5_, double p_i241972_6_, boolean p_i241972_8_, boolean p_i241972_9_, boolean p_i241972_10_, boolean p_i241972_11_, int p_i241972_12_, ResourceLocation p_i241972_13_, ResourceLocation p_i241972_14_, float p_i241972_15_)
    {
        super(p_i241972_1_, p_i241972_2_, p_i241972_3_, p_i241972_4_, p_i241972_5_, p_i241972_6_, p_i241972_8_, p_i241972_9_, p_i241972_10_, p_i241972_11_, p_i241972_12_, p_i241972_13_, p_i241972_14_, p_i241972_15_);
    }

    private static ChunkGenerator csEndProvider(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed)
    {
        return new NoiseChunkGenerator(new EndBiomeProvider(biomeRegistry, seed), seed, () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242737_f));
    }

    private static ChunkGenerator csNetherGenerator(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed)
    {
        BiomeProvider biomeProvider = NetherBiomeProvider.Preset.DEFAULT_NETHER_PROVIDER_PRESET.build(biomeRegistry, seed);

        return new NoiseChunkGenerator(biomeProvider, seed, () -> dimensionSettingsRegistry.getOrThrow(DimensionSettings.field_242736_e));
    }

    public static SimpleRegistry<Dimension> csDimensions(Registry<Biome> biomeRegistry, Registry<DimensionSettings> dimensionSettingsRegistry, long seed)
    {
        SimpleRegistry<Dimension> registry = new SimpleRegistry<>(Registry.DIMENSION_KEY, Lifecycle.experimental());
        registry.register(Dimension.THE_NETHER, new Dimension(() -> NETHER_TYPE, csNetherGenerator(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        registry.register(Dimension.THE_END, new Dimension(() -> END_TYPE, csEndProvider(biomeRegistry, dimensionSettingsRegistry, seed)), Lifecycle.stable());
        return registry;
    }
}