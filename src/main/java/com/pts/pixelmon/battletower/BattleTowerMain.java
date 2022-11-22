package com.pts.pixelmon.battletower;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pts.pixelmon.battletower.BattleTowerComputer.BattleTowerComputer;
import com.pts.pixelmon.battletower.BattleTowerComputer.BattleTowerComputerBlock;
import com.pts.pixelmon.battletower.BattleTowerComputer.BattleTowerComputerItem;
import com.pts.pixelmon.battletower.BetterItemFinder.BetterItemFinder;
import com.pts.pixelmon.battletower.BetterItemFinder.BetterItemFinderItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("pixelmonbattletower")
public class BattleTowerMain
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public BattleTowerMain() {
        MinecraftForge.EVENT_BUS.register(this);
        Pixelmon.EVENT_BUS.register(this);
    }
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event){
        LOGGER.info("battle ended");
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            blockRegistryEvent.getRegistry().register(BattleTowerComputer.GetInstance().Block);
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegisterEvent){
            itemRegisterEvent.getRegistry().register(BattleTowerComputer.GetInstance().Item);
            itemRegisterEvent.getRegistry().register(BetterItemFinder.GetInstance().Item);
        }
    }

}
