package com.pixelmon.battletower.blocks.playerSpot;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerPlayerSpotItem extends BlockItem {
    public BattleTowerPlayerSpotItem(DeferredRegister<Item> itemDeferredRegister, BattleTowerPlayerSpotBlock b) {
        super(b, new Properties().tab(ItemGroup.TAB_MISC));
        itemDeferredRegister.register("battle_tower_player_spot_item", () -> this);
    }
}
