package com.pixelmon.battletower.opponentSpot;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerOpponentSpotBlock extends Block {

    private static final Properties BlockProperties = Properties
            .of(Material.STONE)
            .strength(-1);

    public BattleTowerOpponentSpotBlock(DeferredRegister<Block> blockDeferredRegister) {
        super(BlockProperties);
        blockDeferredRegister.register("battle_tower_opponent_spot_block", () -> this);
    }
}
