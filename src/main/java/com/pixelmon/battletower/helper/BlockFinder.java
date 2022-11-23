package com.pixelmon.battletower.helper;

import net.minecraft.block.Block;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.stream.StreamSupport;

public class BlockFinder {

    private static final int yRadius = 3;
    private static final int xZRadius = 30;

    public Optional<BlockPos> FindNearestBlock(World world, BlockPos start, Block toFind){
        for( int y = start.getY() - yRadius; y < start.getY() + yRadius; y++){
            Optional<BlockPos.Mutable> posOptional = StreamSupport.stream(BlockPos.spiralAround(new BlockPos(start.getX(), y, start.getZ()), xZRadius, Direction.NORTH, Direction.EAST).spliterator(), false)
                    .filter(pos -> world.getBlockState(pos).getBlock() == toFind)
                    .findFirst();

            if (posOptional.isPresent()){
                return Optional.of(posOptional.get().immutable());
            }
        }

        return Optional.empty();
    }
}
