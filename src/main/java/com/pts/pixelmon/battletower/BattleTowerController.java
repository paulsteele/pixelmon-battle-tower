package com.pts.pixelmon.battletower;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.UUID;

public class BattleTowerController {
    public void StartRun(World world, ServerPlayerEntity serverPlayerEntity){
        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(Lists.newArrayList(PokemonFactory.create(PixelmonSpecies.get("Bidoof").get().getValueUnsafe())));
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hullo";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);

        TeamSelectionRegistry.builder()
                .closeable(false)
                .members(serverPlayerEntity, trainerNPC)
                .hideOpponentTeam()
                .start();
    }

    public void onBattleEnded(final BattleEndEvent endEvent){

    }
}
