package com.pixelmon.battletower.blocks.computer;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerComputerItem extends BlockItem {
    public BattleTowerComputerItem(DeferredRegister<Item> itemDeferredRegister, BattleTowerComputerBlock b) {
        super(b, new Properties().tab(ItemGroup.TAB_MISC));
        itemDeferredRegister.register("battle_tower_item", () -> this);
    }
}
