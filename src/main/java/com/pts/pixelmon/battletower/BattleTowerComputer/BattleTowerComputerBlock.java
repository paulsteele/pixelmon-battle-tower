package com.pts.pixelmon.battletower.BattleTowerComputer;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PixelmonStorageManager;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.BattleQuery;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pts.pixelmon.battletower.BattleTowerMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import java.util.UUID;

public class BattleTowerComputerBlock extends Block {
    private static final String REGISTRY_NAME = "battle_tower_block";

    private static final Properties BlockProperties = Properties
            .of(Material.STONE)
            .harvestLevel(2)
            .harvestTool(ToolType.PICKAXE)
            .strength(5f);

    public BattleTowerComputerBlock() {
        super(BlockProperties);
        setRegistryName(REGISTRY_NAME);
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult) {
        if (world.isClientSide){
            return ActionResultType.SUCCESS;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(player.getX(), player.getY(), player.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(Lists.newArrayList(PokemonFactory.create(PixelmonSpecies.get("Bidoof").get().getValueUnsafe())));
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hullo";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);

        TeamSelectionRegistry.builder()
                .closeable(false)
                .showOpponentTeam()
                .members(serverPlayer, trainerNPC)
                .start();

        return ActionResultType.SUCCESS;
    }
}
