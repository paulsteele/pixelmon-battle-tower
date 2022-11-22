package com.pts.pixelmon.battletower;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import java.util.*;

public class BattleTowerController {
    HashMap<ServerPlayerEntity, BattleTowerRun> RunMap = new HashMap<>();

    public void StartRun(World world, ServerPlayerEntity serverPlayerEntity){
        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(Lists.newArrayList(PokemonFactory.create(PixelmonSpecies.get("Bidoof").get().getValueUnsafe())));
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hi";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);

        BattleTowerRun run = new BattleTowerRun();
        run.SetStatus(BattleTowerRun.BattleTowerRunStatus.BATTLING);
        OverwriteRun(serverPlayerEntity, run);

        TeamSelectionRegistry.builder()
                .closeable(false)
                .members(serverPlayerEntity, trainerNPC)
                .hideOpponentTeam()
                .start();
    }

    private void OverwriteRun(ServerPlayerEntity player, BattleTowerRun run){
        if (RunMap.containsKey(player)){
            RunMap.replace(player, run);
        }
        RunMap.put(player, run);
    }

    public void onBattleEnded(final BattleEndEvent endEvent){
        List<ServerPlayerEntity> players = endEvent.getPlayers();
        if (players.size() != 1){
            return;
        }

        ServerPlayerEntity player = players.get(0);

        if (!RunMap.containsKey(player)){
            return;
        }

        BattleTowerRun run = RunMap.get(player);

        Optional<BattleResults> resultsOptional = endEvent.getResult(player);

        if (!resultsOptional.isPresent()){
            return;
        }

        if (resultsOptional.get() == BattleResults.VICTORY){
            run.SetStatus(BattleTowerRun.BattleTowerRunStatus.WAITING_ON_RESULTS);
            run.IncrementStreak();
        }
        else {
            RunMap.remove(player);
        }
    }
}
