package com.pixelmon.battletower.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerCoinItem extends Item {
    public BattleTowerCoinItem(DeferredRegister<Item> itemDeferredRegister) {
        super(new Properties().stacksTo(64).tab(ItemGroup.TAB_MISC));
        itemDeferredRegister.register("battle_tower_coin_item", () -> this);
    }
}
