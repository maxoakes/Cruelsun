package com.scouter.cruelsun.features;

import com.scouter.cruelsun.CruelSun;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Random;

public class FeatureInit {

    /*
        Feature initialization
    */
   public static final ConfiguredFeature<?, ?> FALLEN_LOG_SMALL = register("fallen_log_small", FeatureRegister.FALLEN_LOG_SMALL
            .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
            .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT.chance(0))
            .withChance(1f).feature.get());
    public static final ConfiguredFeature<?, ?> FALLEN_LOG_LARGE = register("fallen_log_large", FeatureRegister.FALLEN_LOG_LARGE
            .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
            .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT.chance(33))
            .withChance(1f).feature.get());
    public static final ConfiguredFeature<?, ?> STUMP_SMALL = register("stump_small", FeatureRegister.STUMP_SMALL
            .withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG)
            .withPlacement(Features.Placements.HEIGHTMAP_PLACEMENT.chance(0))
            .withChance(1f).feature.get());

    private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String key, ConfiguredFeature<FC, ?> feature)
    {
        return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(CruelSun.MODID, key), feature);
    }

    /*
     *      Helper Methods for features
     */
    public static BlockPos posOnGround(IWorld world, BlockPos pos)
    {
        while (world.isAirBlock(pos)) pos = pos.down();
        pos = pos.up();
        return pos;
    }
    public static boolean isAreaSuitable(IWorld world, Random rand, BlockPos pos, int width, int height, int depth) {
        boolean flag = true;

        // check if there's anything within the diameter
        for (int cx = 0; cx < width; cx++) {
            for (int cz = 0; cz < depth; cz++) {
                BlockPos pos_ = pos.add(cx, 0, cz);
                // check if the blocks even exist?
                if (world.isAreaLoaded(pos_, 8)) {
                    // is there grass, dirt or stone below?
                    Material m = world.getBlockState(pos_.down()).getMaterial();
                    if (m != Material.EARTH && m != Material.ORGANIC && m != Material.ROCK) {
                        flag = false;
                    }

                    for (int cy = 0; cy < height; cy++) {
                        // blank space above?
                        if (!world.isAirBlock(pos_.up(cy))) {
                            flag = false;
                        }
                    }
                } else {
                    flag = false;
                }
            }
        }
        return flag;
    }
}
