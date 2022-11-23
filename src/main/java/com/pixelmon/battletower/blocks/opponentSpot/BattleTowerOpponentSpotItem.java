package com.pixelmon.battletower.blocks.opponentSpot;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerOpponentSpotItem extends BlockItem {
    public BattleTowerOpponentSpotItem(DeferredRegister<Item> itemDeferredRegister, BattleTowerOpponentSpotBlock b) {
        super(b, new Properties());
        itemDeferredRegister.register("battle_tower_opponent_spot_item", () -> this);
    }
}
