package com.pts.pixelmon.battletower.BattleTowerComputer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class BattleTowerComputerBlock extends Block {
    private static final String REGISTRY_NAME = "battle_tower_block";

    private static final Properties BlockProperties = Properties
            .of(Material.STONE)
            .harvestLevel(2)
            .harvestTool(ToolType.PICKAXE)
            .strength(5f);

    public BattleTowerComputerBlock() {
        super(BlockProperties);
        setRegistryName(REGISTRY_NAME);
    }
}
