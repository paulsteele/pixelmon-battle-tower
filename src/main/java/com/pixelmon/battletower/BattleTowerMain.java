package com.pixelmon.battletower;

import com.pixelmon.battletower.blocks.computer.BattleTowerComputerBlock;
import com.pixelmon.battletower.blocks.computer.BattleTowerComputerItem;
import com.pixelmon.battletower.blocks.opponentSpot.BattleTowerOpponentSpotBlock;
import com.pixelmon.battletower.blocks.opponentSpot.BattleTowerOpponentSpotItem;
import com.pixelmon.battletower.blocks.playerSpot.BattleTowerPlayerSpotBlock;
import com.pixelmon.battletower.blocks.playerSpot.BattleTowerPlayerSpotItem;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.pixelmon.battletower.BattleTowerMain.ModId;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModId)
public class BattleTowerMain
{
    public static final String ModId = "pixelmonbattletower";
    private static final DeferredRegister<Block> BlockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, ModId);
    private static final DeferredRegister<Item> ItemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, ModId);

    private final BattleTowerController controller;

    public BattleTowerMain() {
        MinecraftForge.EVENT_BUS.register(this);
        Pixelmon.EVENT_BUS.register(this);
        controller = new BattleTowerController();

        BattleTowerComputerBlock battleTowerComputerBlock = new BattleTowerComputerBlock(BlockRegister, controller);
        new BattleTowerComputerItem(ItemRegister, battleTowerComputerBlock);

        BattleTowerPlayerSpotBlock battleTowerPlayerSpotBlock = new BattleTowerPlayerSpotBlock(BlockRegister);
        new BattleTowerPlayerSpotItem(ItemRegister, battleTowerPlayerSpotBlock);

        BattleTowerOpponentSpotBlock battleTowerOpponentSpotBlock = new BattleTowerOpponentSpotBlock(BlockRegister);
        new BattleTowerOpponentSpotItem(ItemRegister, battleTowerOpponentSpotBlock);

        BlockRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
        ItemRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event){
        controller.OnBattleEnded(event);
    }
}
