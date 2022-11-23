package com.pixelmon.battletower.blocks.playerSpot;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerPlayerSpotBlock extends Block {

    private static final Properties BlockProperties = Properties
            .of(Material.STONE)
            .strength(-1);

    public BattleTowerPlayerSpotBlock(DeferredRegister<Block> blockDeferredRegister) {
        super(BlockProperties);
        blockDeferredRegister.register("battle_tower_player_spot_block", () -> this);
    }
}
