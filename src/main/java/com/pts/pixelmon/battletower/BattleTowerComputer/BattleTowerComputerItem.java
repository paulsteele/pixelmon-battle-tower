package com.pts.pixelmon.battletower.BattleTowerComputer;

import net.minecraft.item.BlockItem;

public class BattleTowerComputerItem extends BlockItem {
    private static final String REGISTRY_NAME = "battle_tower_item_registry";

    public BattleTowerComputerItem(BattleTowerComputerBlock b) {
        super(b, new Properties());

        setRegistryName(REGISTRY_NAME);
    }
}
