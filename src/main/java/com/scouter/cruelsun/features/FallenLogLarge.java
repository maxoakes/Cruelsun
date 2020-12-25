package com.scouter.cruelsun.features;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.Random;

public class FallenLogLarge extends Feature<NoFeatureConfig> {

    final private BlockState oakLogWithZAxis = Blocks.OAK_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z);
    final private BlockState oakLogWithXAxis = Blocks.OAK_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.X);
    final private BlockState oakLeaves = Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true);
    final private BlockState dirt = Blocks.DIRT.getDefaultState();

    public FallenLogLarge(Codec<NoFeatureConfig> configIn) {
        super(configIn);
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        return rand.nextBoolean() ? makeLog4Z(world, rand, pos) : makeLog4X(world, rand, pos);
    }

    //borrowed from the Twilight Forest team for making the large fallen log feature
    private boolean makeLog4Z(IWorld world, Random rand, BlockPos pos) {
        // +Z 4x4 log
        if (!FeatureHelper.isAreaSuitable(world, rand, pos, 9, 3, 4)) {
            return false;
        }

        // jaggy parts
        makeNegativeZJaggy(world, pos, rand.nextInt(3), 0, 0);
        makeNegativeZJaggy(world, pos, rand.nextInt(3), 3, 0);
        makeNegativeZJaggy(world, pos, rand.nextInt(3), 0, 1);
        makeNegativeZJaggy(world, pos, rand.nextInt(3), 3, 1);
        makeNegativeZJaggy(world, pos, rand.nextInt(3), 1, 2);
        makeNegativeZJaggy(world, pos, rand.nextInt(3), 2, 2);

        makePositiveZJaggy(world, pos, rand.nextInt(3), 0, 0);
        makePositiveZJaggy(world, pos, rand.nextInt(3), 3, 0);
        makePositiveZJaggy(world, pos, rand.nextInt(3), 0, 1);
        makePositiveZJaggy(world, pos, rand.nextInt(3), 3, 1);
        makePositiveZJaggy(world, pos, rand.nextInt(3), 1, 2);
        makePositiveZJaggy(world, pos, rand.nextInt(3), 2, 2);

        // center
        for (int dz = 0; dz < 4; dz++) {
            // floor
            if (rand.nextBoolean()) {
                world.setBlockState(pos.add(1, -1, dz + 3), oakLogWithZAxis, 3);
            } else {
                world.setBlockState(pos.add(1, -1, dz + 3), dirt, 3);
            }
            if (rand.nextBoolean()) {
                world.setBlockState(pos.add(2, -1, dz + 3), oakLogWithZAxis, 3);
            } else {
                world.setBlockState(pos.add(2, -1, dz + 3), dirt, 3);
            }

            // log part
            world.setBlockState(pos.add(0, 0, dz + 3), oakLogWithZAxis, 3);
            world.setBlockState(pos.add(3, 0, dz + 3), oakLogWithZAxis, 3);
            world.setBlockState(pos.add(0, 1, dz + 3), oakLogWithZAxis, 3);
            world.setBlockState(pos.add(3, 1, dz + 3), oakLogWithZAxis, 3);
            world.setBlockState(pos.add(1, 2, dz + 3), oakLogWithZAxis, 3);
            world.setBlockState(pos.add(2, 2, dz + 3), oakLogWithZAxis, 3);
        }

        // a few leaves?
        int offZ = rand.nextInt(3) + 2;
        boolean plusX = rand.nextBoolean();
        for (int dz = 0; dz < 3; dz++) {
            if (rand.nextBoolean()) {
                world.setBlockState(pos.add(plusX ? 3 : 0, 2, dz + offZ), oakLeaves, 3);
                if (rand.nextBoolean()) {
                    world.setBlockState(pos.add(plusX ? 3 : 0, 3, dz + offZ), oakLeaves, 3);
                }
                if (rand.nextBoolean()) {
                    world.setBlockState(pos.add(plusX ? 4 : -1, 2, dz + offZ), oakLeaves, 3);
                }
            }
        }

        return true;
    }

    private void makeNegativeZJaggy(IWorld world, BlockPos pos, int length, int dx, int dy) {
        for (int dz = -length; dz < 0; dz++) {
            world.setBlockState(pos.add(dx, dy, dz + 3), oakLogWithZAxis, 3);
        }
    }

    private void makePositiveZJaggy(IWorld world, BlockPos pos, int length, int dx, int dy) {
        for (int dz = 0; dz < length; dz++) {
            world.setBlockState(pos.add(dx, dy, dz + 7), oakLogWithZAxis, 3);
        }
    }

    /**
     * Make a 4x4 log in the +X direction
     */
    private boolean makeLog4X(IWorld world, Random rand, BlockPos pos) {
        // +Z 4x4 log
        if (!FeatureHelper.isAreaSuitable(world, rand, pos, 4, 3, 9)) {
            return false;
        }

        // jaggy parts
        makeNegativeXJaggy(world, pos, rand.nextInt(3), 0, 0);
        makeNegativeXJaggy(world, pos, rand.nextInt(3), 3, 0);
        makeNegativeXJaggy(world, pos, rand.nextInt(3), 0, 1);
        makeNegativeXJaggy(world, pos, rand.nextInt(3), 3, 1);
        makeNegativeXJaggy(world, pos, rand.nextInt(3), 1, 2);
        makeNegativeXJaggy(world, pos, rand.nextInt(3), 2, 2);

        makePositiveXJaggy(world, pos, rand.nextInt(3), 0, 0);
        makePositiveXJaggy(world, pos, rand.nextInt(3), 3, 0);
        makePositiveXJaggy(world, pos, rand.nextInt(3), 0, 1);
        makePositiveXJaggy(world, pos, rand.nextInt(3), 3, 1);
        makePositiveXJaggy(world, pos, rand.nextInt(3), 1, 2);
        makePositiveXJaggy(world, pos, rand.nextInt(3), 2, 2);

        // center
        for (int dx = 0; dx < 4; dx++) {
            // floor
            if (rand.nextBoolean()) {
                world.setBlockState(pos.add(dx + 3, -1, 1), oakLogWithXAxis, 3);
            } else {
                world.setBlockState(pos.add(dx + 3, -1, 1), dirt, 3);
            }
            if (rand.nextBoolean()) {
                world.setBlockState(pos.add(dx + 3, -1, 2), oakLogWithXAxis, 3);
            } else {
                world.setBlockState(pos.add(dx + 3, -1, 2), dirt, 3);
            }

            // log part
            world.setBlockState(pos.add(dx + 3, 0, 0), oakLogWithXAxis, 3);
            world.setBlockState(pos.add(dx + 3, 0, 3), oakLogWithXAxis, 3);
            world.setBlockState(pos.add(dx + 3, 1, 0), oakLogWithXAxis, 3);
            world.setBlockState(pos.add(dx + 3, 1, 3), oakLogWithXAxis, 3);
            world.setBlockState(pos.add(dx + 3, 2, 1), oakLogWithXAxis, 3);
            world.setBlockState(pos.add(dx + 3, 2, 2), oakLogWithXAxis, 3);
        }

        // a few leaves?
        int offX = rand.nextInt(3) + 2;
        boolean plusZ = rand.nextBoolean();
        for (int dx = 0; dx < 3; dx++) {
            if (rand.nextBoolean()) {

                world.setBlockState(pos.add(dx + offX, 2, plusZ ? 3 : 0), oakLeaves, 3);
                if (rand.nextBoolean()) {
                    world.setBlockState(pos.add(dx + offX, 3, plusZ ? 3 : 0), oakLeaves, 3);
                }
                if (rand.nextBoolean()) {
                    world.setBlockState(pos.add(dx + offX, 2, plusZ ? 4 : -1), oakLeaves, 3);
                }
            }
        }
        return true;
    }

    private void makeNegativeXJaggy(IWorld world, BlockPos pos, int length, int dz, int dy) {
        for (int dx = -length; dx < 0; dx++) {
            world.setBlockState(pos.add(dx + 3, dy, dz), oakLogWithXAxis, 3);
        }
    }

    private void makePositiveXJaggy(IWorld world, BlockPos pos, int length, int dz, int dy) {
        for (int dx = 0; dx < length; dx++) {
            world.setBlockState(pos.add(dx + 7, dy, dz), oakLogWithXAxis, 3);
        }
    }
}