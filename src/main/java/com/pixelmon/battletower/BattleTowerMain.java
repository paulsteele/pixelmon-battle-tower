package com.pixelmon.battletower;

import com.pixelmon.battletower.blocks.computer.BattleTowerComputerBlock;
import com.pixelmon.battletower.blocks.computer.BattleTowerComputerItem;
import com.pixelmon.battletower.blocks.opponentSpot.BattleTowerOpponentSpotBlock;
import com.pixelmon.battletower.blocks.opponentSpot.BattleTowerOpponentSpotItem;
import com.pixelmon.battletower.blocks.playerSpot.BattleTowerPlayerSpotBlock;
import com.pixelmon.battletower.blocks.playerSpot.BattleTowerPlayerSpotItem;
import com.pixelmon.battletower.helper.BlockFinder;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
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
        MinecraftForge.EVENT_BUS.addListener(this::addSelectiveReloadListeners);
        Pixelmon.EVENT_BUS.register(this);

        BattleTowerPlayerSpotBlock battleTowerPlayerSpotBlock = new BattleTowerPlayerSpotBlock(BlockRegister);
        new BattleTowerPlayerSpotItem(ItemRegister, battleTowerPlayerSpotBlock);

        BattleTowerOpponentSpotBlock battleTowerOpponentSpotBlock = new BattleTowerOpponentSpotBlock(BlockRegister);
        new BattleTowerOpponentSpotItem(ItemRegister, battleTowerOpponentSpotBlock);

        controller = new BattleTowerController(new BlockFinder(), battleTowerPlayerSpotBlock, battleTowerOpponentSpotBlock);

        BattleTowerComputerBlock battleTowerComputerBlock = new BattleTowerComputerBlock(BlockRegister, controller);
        new BattleTowerComputerItem(ItemRegister, battleTowerComputerBlock);

        BlockRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
        ItemRegister.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event){
        controller.OnBattleEnded(event);
    }

    private void addSelectiveReloadListeners(AddReloadListenerEvent event){
        event.addListener(new MovesetSelectiveResourceReloadListener());
    }
}
