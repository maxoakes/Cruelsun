package com.scouter.cruelsun.biomes;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class BiomeHelper
{
    public static final List<Biome> SCORCHED_BIOMES = ImmutableList.of(BiomeRegister.BIOME_SCORCHED_FOREST.get(),BiomeRegister.BIOME_SCORCHED_HILLS.get(),BiomeRegister.BIOME_SCORCHED_PLAINS.get());
    public static final float BASE_DEPTH = 0.1f; //starting height of the terrain above sea level
    public static final float BASE_SCALE = 0.0f; //vertical scale of terrain. larger is more 'amplified'
    public static final float BASE_TEMP = 3.0f; //
    public static final float BASE_DOWNFALL = 0.0f; //
    public static final int BASE_WATER_COLOR = 0x304182;
    public static final int BASE_WATER_FOG_COLOR = 0x826EF5;
    public static final int BASE_FOG_COLOR = 0x804821; //ab492e
    public static final int BASE_SKY_COLOR = 0x5b4fff; //6f4fff
    public static final int BASE_GRASS_COLOR = 0xc6ab72;
    public static final int BASE_FOLIAGE_COLOR = 0xbf964e;

}
