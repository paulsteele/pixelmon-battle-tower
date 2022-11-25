package com.pixelmon.battletower;

import com.google.common.collect.Lists;
import com.pixelmon.battletower.blocks.opponentSpot.BattleTowerOpponentSpotBlock;
import com.pixelmon.battletower.blocks.playerSpot.BattleTowerPlayerSpotBlock;
import com.pixelmon.battletower.helper.BlockFinder;
import com.pixelmon.battletower.persistence.BattleTowerRun;
import com.pixelmon.battletower.persistence.BattleTowerSavedData;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BattleTowerController {

    private BattleTowerSavedData savedData;
    private BlockFinder finder;
    private final BattleTowerPlayerSpotBlock playerSpotBlock;
    private final BattleTowerOpponentSpotBlock opponentSpotBlock;

    private HashMap<String, ArrayList<Pokemon>> SmogonMons;

    public BattleTowerController(
        BlockFinder finder,
        BattleTowerPlayerSpotBlock playerSpotBlock,
        BattleTowerOpponentSpotBlock opponentSpotBlock
    ){
        this.finder = finder;

        this.playerSpotBlock = playerSpotBlock;
        this.opponentSpotBlock = opponentSpotBlock;
        SmogonMons = new HashMap<>();
    }

    public void AddSmogonMon(String tier, Pokemon mon){
        if (!SmogonMons.containsKey(tier)){
            SmogonMons.put(tier, new ArrayList<>());
        }

        SmogonMons.get(tier).add(mon);
    }

    public void ClearSmogonMons(){
        SmogonMons.clear();
    }

    public void PresentChoices(
            World world,
            ServerPlayerEntity serverPlayerEntity,
            BlockPos computerBlockPos
    ){
        BattleTowerSavedData data = GetOrCreateSavedData((ServerWorld) world);

        if (data.HasRun(serverPlayerEntity)){
            PresentContinueRun(world, serverPlayerEntity, computerBlockPos);
        }
        else {
            PresentStartRun(world, serverPlayerEntity, computerBlockPos);
        }
    }

    private void PresentContinueRun(World world, ServerPlayerEntity player, BlockPos computerBlockPos){
        Dialogue.DialogueBuilder builder = new Dialogue.DialogueBuilder();
        Choice.ChoiceBuilder continueChoice = new Choice.ChoiceBuilder();

        continueChoice.setText("Continue");
        continueChoice.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, computerBlockPos, savedData.GetType(player));
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

    private void PresentStartRun(World world, ServerPlayerEntity player, BlockPos computerBlockPos){
        Dialogue.DialogueBuilder builder = new Dialogue.DialogueBuilder();
        Choice.ChoiceBuilder singles = new Choice.ChoiceBuilder();

        singles.setText("Singles");
        singles.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, computerBlockPos, BattleTowerRun.RunType.SINGLES);
        });

        Choice.ChoiceBuilder doubles = new Choice.ChoiceBuilder();
        doubles.setText("Doubles");
        doubles.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, computerBlockPos, BattleTowerRun.RunType.DOUBLES);
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

    private void StartRun(World world, ServerPlayerEntity serverPlayerEntity, BlockPos computerBlockPos, BattleTowerRun.RunType type){
        Optional<BlockPos> playerBlock = GetPlayerBlock(world, computerBlockPos);
        Optional<BlockPos> opponentBlock = GetOpponentBlock(world, computerBlockPos);

        if (!playerBlock.isPresent()){
            serverPlayerEntity.displayClientMessage(new StringTextComponent("Could not find player pedestal nearby"), false);
            return;
        }
        if (!opponentBlock.isPresent()){
            serverPlayerEntity.displayClientMessage(new StringTextComponent("Could not find opponent pedestal nearby"), false);
            return;
        }
        BlockPos playerPos = playerBlockPos.get();
        BlockPos opponentPos = opponentBlockPos.get();

        String tier = "pu";
        ArrayList<Pokemon> trainerTeam = GenerateTeam(tier);
        if (trainerTeam.isEmpty()){
            serverPlayerEntity.displayClientMessage(new StringTextComponent("Could not generate team for tier " + tier), false);
            return;
        }

        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.loadPokemon(trainerTeam);
        trainerNPC.canEngage = true;
        trainerNPC.greeting = "hi";
        trainerNPC.init("Cool Guy");

        world.addFreshEntity(trainerNPC);
        trainerNPC.teleportTo(opponentPos.getX() + .5, opponentPos.getY() + 1, opponentPos.getZ() + .5);
        serverPlayerEntity.teleportTo(playerPos.getX() + .5, playerPos.getY() + 1, playerPos.getZ() + .5);

        savedData.StartRun(serverPlayerEntity, type);

        TeamSelectionRegistry.builder()
                .closeable(false)
                .members(serverPlayerEntity, trainerNPC)
                .showOpponentTeam()
                .start();
    }

    public ArrayList<Pokemon> GenerateTeam(String tier){
        ArrayList<Pokemon> list = new ArrayList<>();
        if (!SmogonMons.containsKey(tier)){
            return list;
        }

        Random r = new Random();
        List<Integer> options = IntStream.range(0, SmogonMons.get(tier).size()).boxed().collect(Collectors.toList());
        for (int i = 0; i < 6; i++){
            int index = options.remove(r.nextInt(options.size()));
            list.add(SmogonMons.get(tier).get(index));
        }

        return list;
    }

    private Optional<BlockPos> playerBlockPos;
    private Optional<BlockPos> opponentBlockPos;
    private Optional<BlockPos> GetPlayerBlock(World world, BlockPos computerBlockPos){
        if (playerBlockPos == null || !playerBlockPos.isPresent() || world.getBlockState(playerBlockPos.get()).getBlock() != playerSpotBlock){
            playerBlockPos = finder.FindNearestBlock(world, computerBlockPos, playerSpotBlock);
        }

        return playerBlockPos;
    }

    private Optional<BlockPos> GetOpponentBlock(World world, BlockPos computerBlockPos){
        if (opponentBlockPos == null || !opponentBlockPos.isPresent() || world.getBlockState(opponentBlockPos.get()).getBlock() != opponentSpotBlock){
            opponentBlockPos = finder.FindNearestBlock(world, computerBlockPos, opponentSpotBlock);
        }

        return opponentBlockPos;
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
