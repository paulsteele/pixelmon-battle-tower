package com.pts.pixelmon.battletower;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BattleTowerController {

    private BattleTowerSavedData savedData;

    public void StartRun(World world, ServerPlayerEntity serverPlayerEntity){
        BattleTowerSavedData data = GetOrCreateSavedData((ServerWorld) world);

        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(Lists.newArrayList(PokemonFactory.create(PixelmonSpecies.get("Bidoof").get().getValueUnsafe())));
        trainerNPC.updateDrops(new ItemStack[]{ new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:dirt"))) });
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hi";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);

        data.StartRun(serverPlayerEntity);

        TeamSelectionRegistry.builder()
                .closeable(false)
                .members(serverPlayerEntity, trainerNPC)
                .hideOpponentTeam()
                .start();
    }

    private BattleTowerSavedData GetOrCreateSavedData(ServerWorld world){
        if (savedData == null){
            savedData = world.getDataStorage().computeIfAbsent(BattleTowerSavedData::new, BattleTowerSavedData.Id);
        }

        return savedData;
    }

    public void OnBattleEnded(final BattleEndEvent endEvent){
        List<ServerPlayerEntity> players = endEvent.getPlayers();
        if (players.size() != 1){
            return;
        }

        ServerPlayerEntity player = players.get(0);

        if (!savedData.HasRun(player)){
            return;
        }

        Optional<BattleResults> resultsOptional = endEvent.getResult(player);

        if (!resultsOptional.isPresent()){
            return;
        }

        if (resultsOptional.get() == BattleResults.VICTORY){
            savedData.IncrementStreak(player);
        }
        else {
            savedData.EndRun(player);
            List<BattleParticipant> entities = endEvent.getBattleController().participants.stream().filter(battleParticipant -> battleParticipant.getEntity() != player).collect(Collectors.toList());

            if (entities.size() == 1){
                entities.get(0).getEntity().remove();
            }
        }
    }
}
