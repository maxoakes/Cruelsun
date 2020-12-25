package com.scouter.cruelsun.biomes;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CSBiomeProvider extends BiomeProvider
{
    public static final Codec<CSBiomeProvider> CODEC = RecordCodecBuilder.create((builder) ->
            builder.group
            (
                Codec.LONG.fieldOf("seed").stable().forGetter((biomeProvider) -> biomeProvider.seed),
                    RegistryLookupCodec.getLookUpCodec(Registry.BIOME_KEY).forGetter((biomeProvider) -> biomeProvider.lookupRegistry)
            ).apply(builder, builder.stable(CSBiomeProvider::new)));

    private final Registry<Biome> lookupRegistry;
    private static final List<RegistryKey<Biome>> scorchedBiomesKeys = ImmutableList.of(
            RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeRegister.BIOME_SCORCHED_FOREST.getId()),
            RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeRegister.BIOME_SCORCHED_HILLS.getId()),
            RegistryKey.getOrCreateKey(Registry.BIOME_KEY, BiomeRegister.BIOME_SCORCHED_PLAINS.getId())
    );
    private final List<Biome> scorchedBiomes;
    private final long seed;

    public CSBiomeProvider(long seed, Registry<Biome> lookupRegistry) {
        super(scorchedBiomesKeys.stream().map(lookupRegistry::getOrThrow).collect(Collectors.toList()));
        scorchedBiomes = scorchedBiomesKeys.stream().map(lookupRegistry::getOrThrow).collect(Collectors.toList());
        System.out.println("Scorched world biome list: "+(scorchedBiomesKeys.stream().map(lookupRegistry::getOrThrow).collect(Collectors.toList())));
        this.seed = seed;
        this.lookupRegistry = lookupRegistry;
    }

    @Override
    protected Codec<? extends BiomeProvider> getBiomeProviderCodec() {
        return CODEC;
    }

    @OnlyIn(Dist.CLIENT)
    public BiomeProvider getBiomeProvider(long seed) {
        return new CSBiomeProvider(seed, this.lookupRegistry);
    }

    public Biome getNoiseBiome(int x, int y, int z) {
        try
        {
            //make a biome generator, I guess...
            //otherwise, the scorched biomes are available via the single-biome gen
            return scorchedBiomes.get(0);
        }
        catch(Exception e)
        {
            return lookupRegistry.getOrThrow(Biomes.PLAINS);
        }
    }

    private static RegistryKey<Biome> key(final Biome biome) {
        return RegistryKey.getOrCreateKey(ForgeRegistries.Keys.BIOMES, Objects.requireNonNull(ForgeRegistries.BIOMES.getKey(biome), "Biome registry name was null"));
    }

}