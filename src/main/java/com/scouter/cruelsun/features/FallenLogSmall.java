package com.scouter.cruelsun.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class FallenLogSmall extends Feature<NoFeatureConfig> {

    public FallenLogSmall(Codec<NoFeatureConfig> configIn) {
        super(configIn);
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        // determine direction
        boolean goingX = rand.nextBoolean();

        int logLength = rand.nextInt(3) + 2;
        int stumpLength = rand.nextInt(3) + 1;
        int fallDistance = rand.nextInt(2) + 2;


        // check area clear
        if (!FeatureHelper.isOnGrass(world,pos)) return false;
        if (goingX) {
            if (!FeatureHelper.isAreaSuitable(world, rand, pos, logLength+fallDistance, 3, 2)) {
                return false;
            }
        } else {
            if (!FeatureHelper.isAreaSuitable(world, rand, pos, 3, logLength+fallDistance, 2)) {
                return false;
            }
        }

        // determine wood type
        BlockState logState;
        BlockState branchState;

        switch (rand.nextInt(10)) {
            case 0:
            default:
                logState = Blocks.OAK_LOG.getDefaultState();
                break;
            case 7:
                logState = Blocks.BIRCH_LOG.getDefaultState();
                break;
            case 8:
                logState = Blocks.SPRUCE_LOG.getDefaultState();
                break;
            case 9:
                logState = Blocks.JUNGLE_LOG.getDefaultState();
                break;
        }

        //make stump
        for (int ly = 0; ly < stumpLength; ly++) {
            world.setBlockState(pos.add(0, ly, 0), logState, 3);
        }
        // make log
        if (goingX) {
            logState = logState.with(RotatedPillarBlock.AXIS, Direction.Axis.X);
            branchState = logState.with(RotatedPillarBlock.AXIS, Direction.Axis.Z);

            for (int lx = 0; lx < logLength; lx++) {
                world.setBlockState(FeatureHelper.posOnGround(world,pos.add(lx+fallDistance, 0, 0)), logState, 3);
            }
        } else {
            logState = logState.with(RotatedPillarBlock.AXIS, Direction.Axis.Z);
            branchState = logState.with(RotatedPillarBlock.AXIS, Direction.Axis.X);

            for (int lz = 0; lz < logLength; lz++) {
                world.setBlockState(FeatureHelper.posOnGround(world,pos.add(0, 0, lz+fallDistance)), logState, 3);
            }
        }
        // possibly make branch
        if (rand.nextInt(3) > 0) {
            if (goingX) {
                int bx = rand.nextInt(logLength)+fallDistance;
                int bz = rand.nextBoolean() ? 1 : -1;

                world.setBlockState(pos.add(bx, 0, bz), branchState, 3);

            } else {
                int bx = rand.nextBoolean() ? 1 : -1;
                int bz = rand.nextInt(logLength)+fallDistance;

                world.setBlockState(pos.add(bx, 0, bz), branchState, 3);

            }
        }
        return true;
    }

}