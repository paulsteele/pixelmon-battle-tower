package com.pixelmon.battletower.blocks.playerSpot;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerPlayerSpotItem extends BlockItem {
    public BattleTowerPlayerSpotItem(DeferredRegister<Item> itemDeferredRegister, BattleTowerPlayerSpotBlock b) {
        super(b, new Properties());
        itemDeferredRegister.register("battle_tower_player_spot_item", () -> this);
    }
}
