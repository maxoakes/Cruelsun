package com.scouter.cruelsun.biomes;

import com.scouter.cruelsun.features.FeatureHelper;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.structure.StructureFeatures;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilders;

public class BiomeScorchedForest
{
    Biome.Builder biomeBuilder = new Biome.Builder();
    BiomeGenerationSettings.Builder genBuilder = new BiomeGenerationSettings.Builder();

    public BiomeScorchedForest() {
        MobSpawnInfo.Builder mobspawninfo$builder = new MobSpawnInfo.Builder();
        DefaultBiomeFeatures.withBatsAndHostiles(mobspawninfo$builder);
        biomeBuilder.withGenerationSettings(configureGeneration(genBuilder))
                .precipitation(Biome.RainType.NONE)
                .category(Biome.Category.NONE)
                .depth(BiomeHelper.BASE_DEPTH)
                .scale(BiomeHelper.BASE_SCALE)
                .temperature(BiomeHelper.BASE_TEMP)
                .downfall(BiomeHelper.BASE_DOWNFALL)
                .setEffects((new BiomeAmbience.Builder())
                        .setWaterColor(BiomeHelper.BASE_WATER_COLOR)
                        .setWaterFogColor(BiomeHelper.BASE_WATER_FOG_COLOR)
                        .setFogColor(BiomeHelper.BASE_FOG_COLOR)
                        .withSkyColor(BiomeHelper.BASE_SKY_COLOR)
                        .withGrassColor(BiomeHelper.BASE_GRASS_COLOR)
                        .withFoliageColor(BiomeHelper.BASE_FOLIAGE_COLOR)
                        .setMoodSound(MoodSoundAmbience.DEFAULT_CAVE)
                        .build()).withMobSpawnSettings(mobspawninfo$builder.copy());
    }

    protected BiomeGenerationSettings configureGeneration(BiomeGenerationSettings.Builder builder) {

        //Gen
        builder.withSurfaceBuilder(ConfiguredSurfaceBuilders.field_244178_j);

        //Structures
        builder.withStructure(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
        builder.withStructure(StructureFeatures.STRONGHOLD);
        builder.withStructure(StructureFeatures.BURIED_TREASURE);
        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.DISK_CLAY);
        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.DISK_GRAVEL);
        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, Features.DISK_SAND);
        builder.withFeature(GenerationStage.Decoration.LAKES, Features.LAKE_LAVA);

        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, FeatureHelper.FALLEN_LOG_SMALL);//trunks of normal trees
        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, FeatureHelper.STUMP_SMALL);//log stumps or bottom half of trees
        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, FeatureHelper.STUMP_SMALL);//log stumps or bottom half of trees
        builder.withFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, FeatureHelper.FALLEN_LOG_LARGE);//trunks of large trees


        //Vegetation
        builder.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION); //sparse oak trees
        builder.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS); //sparse tall grass


        //Underground
        DefaultBiomeFeatures.withCavesAndCanyons(builder);
        DefaultBiomeFeatures.withMonsterRoom(builder);
        DefaultBiomeFeatures.withOverworldOres(builder);
        DefaultBiomeFeatures.withEmeraldOre(builder);
        DefaultBiomeFeatures.withLavaAndWaterSprings(builder);

        return builder.build();
    }

    public Biome getBiome()
    {
        return biomeBuilder.build();
    }
}