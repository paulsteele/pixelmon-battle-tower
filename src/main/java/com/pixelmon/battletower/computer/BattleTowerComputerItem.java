package com.pixelmon.battletower.computer;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerComputerItem extends BlockItem {
    public BattleTowerComputerItem(DeferredRegister<Item> itemDeferredRegister, BattleTowerComputerBlock b) {
        super(b, new Properties());
        itemDeferredRegister.register("battle_tower_item", () -> this);
    }
}
