package com.scouter.cruelsun.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class StumpSmall extends Feature<NoFeatureConfig> {

    public StumpSmall(Codec<NoFeatureConfig> configIn) {
        super(configIn);
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        // determine direction
        boolean goingX = rand.nextBoolean();

        int stumpLength = rand.nextInt(2) + 1;

        // check area clear
        if (!FeatureInit.isAreaSuitable(world, rand, pos, 3, 3, 2)) return false;

        // determine wood type
        BlockState logState;

        switch (rand.nextInt(10)) {
            case 0:
            default:
                logState = Blocks.OAK_LOG.getDefaultState();
                break;
            case 7:
                logState = Blocks.BIRCH_LOG.getDefaultState();
                break;
            case 9:
                logState = Blocks.SPRUCE_LOG.getDefaultState();
                break;
            case 10:
                logState = Blocks.JUNGLE_LOG.getDefaultState();
                break;
        }

        //make stump
        for (int ly = 0; ly < stumpLength; ly++) {world.setBlockState(pos.add(0, ly, 0), logState, 3);}

        return true;
    }

}