package com.pixelmon.battletower;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
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

    public void PresentChoices(World world, ServerPlayerEntity serverPlayerEntity){
        BattleTowerSavedData data = GetOrCreateSavedData((ServerWorld) world);

        if (data.HasRun(serverPlayerEntity)){
            PresentContinueRun(world, serverPlayerEntity);
        }
        else {
            PresentStartRun(world, serverPlayerEntity);
        }
    }

    private void PresentContinueRun(World world, ServerPlayerEntity player){
        Dialogue.DialogueBuilder builder = new Dialogue.DialogueBuilder();
        Choice.ChoiceBuilder continueChoice = new Choice.ChoiceBuilder();

        continueChoice.setText("Continue");
        continueChoice.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, savedData.GetType(player));
        });

        Choice.ChoiceBuilder quit = new Choice.ChoiceBuilder();
        quit.setText("Quit");
        quit.setHandle(dialogueChoiceEvent -> {
            savedData.EndRun(player);
        });

        Choice.ChoiceBuilder cancel = new Choice.ChoiceBuilder();
        cancel.setText("Cancel");
        cancel.setHandle(dialogueChoiceEvent -> {});

        builder
                .setName("Battle Tower")
                .setText("You have a streak of " + savedData.GetStreak(player) + " in format " + savedData.GetType(player))
                .addChoice(continueChoice.build(1))
                .addChoice(quit.build(2))
                .addChoice(cancel.build(3))
                .open(player);
    }

    private void PresentStartRun(World world, ServerPlayerEntity player){
        Dialogue.DialogueBuilder builder = new Dialogue.DialogueBuilder();
        Choice.ChoiceBuilder singles = new Choice.ChoiceBuilder();

        singles.setText("Singles");
        singles.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, BattleTowerRun.RunType.SINGLES);
        });

        Choice.ChoiceBuilder doubles = new Choice.ChoiceBuilder();
        doubles.setText("Doubles");
        doubles.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, BattleTowerRun.RunType.DOUBLES);
        });

        Choice.ChoiceBuilder cancel = new Choice.ChoiceBuilder();
        cancel.setText("Cancel");
        cancel.setHandle(dialogueChoiceEvent -> {});

        builder
                .setName("Battle Tower")
                .setText("Select Format")
                .addChoice(singles.build(1))
                .addChoice(doubles.build(2))
                .addChoice(cancel.build(3))
                .open(player);
    }

    private void StartRun(World world, ServerPlayerEntity serverPlayerEntity, BattleTowerRun.RunType type){

        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(Lists.newArrayList(PokemonFactory.create(PixelmonSpecies.get("Bidoof").get().getValueUnsafe())));
        trainerNPC.updateDrops(new ItemStack[]{ new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:dirt"))) });
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hi";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);

        savedData.StartRun(serverPlayerEntity, type);

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