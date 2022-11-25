package com.pixelmon.battletower;

import com.pixelmon.battletower.blocks.opponentSpot.BattleTowerOpponentSpotBlock;
import com.pixelmon.battletower.blocks.playerSpot.BattleTowerPlayerSpotBlock;
import com.pixelmon.battletower.helper.BlockFinder;
import com.pixelmon.battletower.mixinHelpers.ICustomBattleController;
import com.pixelmon.battletower.persistence.BattleTowerRun;
import com.pixelmon.battletower.persistence.BattleTowerSavedData;
import com.pixelmonmod.pixelmon.api.battles.BattleAIMode;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.enums.EnumMegaItemsUnlocked;
import com.pixelmonmod.pixelmon.init.registry.ItemRegistration;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
        quit.setText("Reset");
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

        Choice.ChoiceBuilder smogonSingles = new Choice.ChoiceBuilder();
        smogonSingles.setText("Smogon Singles");
        smogonSingles.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, computerBlockPos, BattleTowerRun.RunType.FULL_TEAM_SINGLES);
        });

        Choice.ChoiceBuilder smogonDoubles = new Choice.ChoiceBuilder();
        smogonDoubles.setText("Smogon Doubles");
        smogonDoubles.setHandle(dialogueChoiceEvent -> {
            StartRun(world, player, computerBlockPos, BattleTowerRun.RunType.FULL_TEAM_DOUBLES);
        });

        Choice.ChoiceBuilder cancel = new Choice.ChoiceBuilder();
        cancel.setText("Cancel");
        cancel.setHandle(dialogueChoiceEvent -> {});

        builder
                .setName("Battle Tower")
                .setText("Select Format")
                .addChoice(singles.build(1))
                .addChoice(doubles.build(2))
                .addChoice(smogonSingles.build(3))
                .addChoice(smogonDoubles.build(4))
                .addChoice(cancel.build(5))
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

        String[] tiers = getTiersBasedOnStreak(savedData.GetStreak(serverPlayerEntity));
        ArrayList<Pokemon> trainerTeam = GenerateTeam(tiers);
        if (trainerTeam.isEmpty()){
            serverPlayerEntity.displayClientMessage(new StringTextComponent("Could not generate team for tier " + Arrays.stream(tiers).reduce((s, s2) -> s + ", " + s2)), false);
            return;
        }

        NPCTrainer trainerNPC = new NPCTrainer(world);
        trainerNPC.setPos(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ());
        trainerNPC.setUUID(UUID.randomUUID());
        trainerNPC.setMegaItem(EnumMegaItemsUnlocked.Both);
        trainerNPC.loadPokemon(trainerTeam);
        trainerNPC.updateDrops(new ItemStack[]{getRewards(savedData.GetStreak(serverPlayerEntity) + 1)});
        trainerNPC.getPokemonStorage().getTeam().forEach(p -> p.setLevel(50));
        trainerNPC.setBattleAIMode(BattleAIMode.ADVANCED);
        trainerNPC.init("Trainer");

        world.addFreshEntity(trainerNPC);
        trainerNPC.teleportTo(opponentPos.getX() + .5, opponentPos.getY() + 1, opponentPos.getZ() + .5);
        serverPlayerEntity.teleportTo(playerPos.getX() + .5, playerPos.getY() + 1, playerPos.getZ() + .5);

        savedData.StartRun(serverPlayerEntity, type);

        TeamSelectionRegistry.builder()
                .closeable(true)
                .members(serverPlayerEntity, trainerNPC)
                .battleRules(getBattleRulesFromRunType(savedData.GetType(serverPlayerEntity)))
                .battleStartConsumer(battleController -> {
                    serverPlayerEntity.lookAt(EntityAnchorArgument.Type.FEET, trainerNPC, EntityAnchorArgument.Type.FEET);
                    trainerNPC.lookAt(EntityAnchorArgument.Type.FEET,EntityAnchorArgument.Type.FEET.apply(serverPlayerEntity));

                    ((ICustomBattleController) battleController).OnBattleEnd( battleParticipantBattleResultsHashMap -> {
                        BattleParticipant playerParticipant = battleController.getParticipantForEntity(serverPlayerEntity);

                        if (!battleParticipantBattleResultsHashMap.containsKey(playerParticipant)){
                            return;
                        }

                        BattleResults results = battleParticipantBattleResultsHashMap.get(playerParticipant);
                        if (results == BattleResults.VICTORY){
                            savedData.IncrementStreak(serverPlayerEntity);
                        }
                        else {
                            savedData.EndRun(serverPlayerEntity);
                            trainerNPC.remove();
                        }
                    });
                })
                .start();
    }

    public ArrayList<Pokemon> GenerateTeam(String[] tiers){
        ArrayList<Pokemon> list = new ArrayList<>();

        int totalSize = 0;
        for (String tier : tiers) {
            if (!SmogonMons.containsKey(tier)) {
                return list;
            }
            totalSize += SmogonMons.get(tier).size();
        }

        Random r = new Random();
        List<Integer> options = IntStream.range(0, totalSize).boxed().collect(Collectors.toList());
        for (int i = 0; i < 6; i++){
            int index = options.remove(r.nextInt(options.size()));
            for (String tier : tiers){
                if (index < SmogonMons.get(tier).size()){
                    list.add(SmogonMons.get(tier).get(index));
                    break;
                }
                index -= SmogonMons.get(tier).size();
            }
        }

        return list;
    }

    private BattleRules getBattleRulesFromRunType(BattleTowerRun.RunType runType){
        BattleRules br = new BattleRules();
        br.set(BattleRuleRegistry.BATTLE_TYPE, runType == BattleTowerRun.RunType.DOUBLES || runType == BattleTowerRun.RunType.FULL_TEAM_DOUBLES ? BattleType.DOUBLE : BattleType.SINGLE);
        switch (runType){
            case SINGLES:{
                br.set(BattleRuleRegistry.NUM_POKEMON, 3);
                break;
            }
            case DOUBLES:{
                br.set(BattleRuleRegistry.NUM_POKEMON, 4);
                break;
            }
            case FULL_TEAM_SINGLES:
            case FULL_TEAM_DOUBLES:{
                br.set(BattleRuleRegistry.NUM_POKEMON, 6);
                break;
            }
        }
        br.set(BattleRuleRegistry.TEAM_PREVIEW, true);
        br.set(BattleRuleRegistry.FULL_HEAL, true);
        br.set(BattleRuleRegistry.LEVEL_CAP, 75);

        return br;
    }

    private String[] getTiersBasedOnStreak(int streak){
        if (streak < 1) {
            return new String[] { "untiered" };
        }
        if (streak < 2) {
            return new String[] { "pu" };
        }
        if (streak < 3) {
            return new String [] { "pu", "publ" };
        }
        if (streak < 4) {
            return new String[] { "publ" , "nu"};
        }
        if (streak < 5) {
            return new String[] { "nu" , "nubl"};
        }
        if (streak < 6) {
            return new String[] { "nubl" , "ru"};
        }
        if (streak < 7) {
            return new String[] { "ru" , "rubl"};
        }
        if (streak < 8) {
            return new String[] { "rubl" , "uu"};
        }
        if (streak < 9) {
            return new String[] { "uu" , "uubl"};
        }
        if (streak < 10) {
            return new String[] { "uubl" , "ou"};
        }
        if (streak < 11) {
            return new String[] { "ou" , "oubl"};
        }
        if (streak < 12) {
            return new String[] { "ou" };
        }
        if (streak < 13) {
            return new String[] { "ou", "national_dex" };
        }
        if (streak < 14) {
            return new String[] { "ou", "national_dex" };
        }
        if (streak < 15) {
            return new String[] { "ou", "national_dex", "ubers" };
        }

       return new String[] { "ou", "national_dex", "ubers", "ag" };
    }

    private ItemStack getRewards(int streak){
        int cappedStreak = Math.min(20, streak);
        int reward = cappedStreak;

        if (streak != 0 && cappedStreak % 5 == 0){
           reward += 10;
        }

        return new ItemStack(ItemRegistration.getItemFromName("item.pixelmon.gold_ability_symbol"), reward);
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
}
