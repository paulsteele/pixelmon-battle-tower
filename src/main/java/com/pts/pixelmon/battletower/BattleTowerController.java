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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BattleTowerController {
    HashSet<BattleTowerRun> Runs = new HashSet<>();

    public void StartRun(World world, ServerPlayerEntity serverPlayerEntity){
        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(Lists.newArrayList(PokemonFactory.create(PixelmonSpecies.get("Bidoof").get().getValueUnsafe())));
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hi";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);

        if (Runs.stream().noneMatch(battleTowerRun -> battleTowerRun.GetServerPlayerEntity() == serverPlayerEntity)){
            Runs.add(new BattleTowerRun(serverPlayerEntity));
        }

        TeamSelectionRegistry.builder()
                .closeable(false)
                .members(serverPlayerEntity, trainerNPC)
                .hideOpponentTeam()
                .start();
    }

    public void onBattleEnded(final BattleEndEvent endEvent){
        List<ServerPlayerEntity> players = endEvent.getPlayers();
        if (players.size() != 1){
            return;
        }

        ServerPlayerEntity player = players.get(0);

        List<BattleTowerRun> runs = Runs.stream()
                .filter(battleTowerRun -> battleTowerRun.GetServerPlayerEntity() == player)
                .collect(Collectors.toList());

        if (runs.size() != 1) {
           return;
        }

        BattleTowerRun run = runs.get(0);

        Optional<BattleResults> resultsOptional = endEvent.getResult(player);

        if (!resultsOptional.isPresent()){
            return;
        }

        if (resultsOptional.get() == BattleResults.VICTORY){
            run.IncrementStreak();
        }
        else {
            Runs.remove(run);
        }
    }
}
