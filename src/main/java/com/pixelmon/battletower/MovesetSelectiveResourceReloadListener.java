package com.pixelmon.battletower;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.moves.Moves;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.Moveset;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MovesetSelectiveResourceReloadListener implements ISelectiveResourceReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (PixelmonSpecies.getAll().size() < 1){
            Logger.getGlobal().info("Pixelmon not initialized");
            return;
        }

        try {
             List<String> tiers = GetLinesOfResourceFile(resourceManager, new ResourceLocation("pixelmonbattletower.movesets", "tiers.txt"));

            for (String tier : tiers) {
                for (ResourceLocation resourceLocation : resourceManager.listResources(tier, s -> s.endsWith(".txt"))) {
                    CreateMoveSet(GetLinesOfResourceFile(resourceManager, resourceLocation));
                }
            }
        }
        catch (Exception e) {
            Logger.getGlobal().log(Level.INFO,  e.getMessage(), e);
        }
    }

    private List<String> GetLinesOfResourceFile(IResourceManager manager, ResourceLocation location) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(manager.getResource(location).getInputStream()));
        List<String> contents = reader.lines().collect(Collectors.toList());
        reader.close();
        return contents;
    }

    private void CreateMoveSet(List<String> input){
        String name = "";
        String form = "";
        String item = "";
        String abilityName = "";
        int hpEV = 0;
        int atkEV = 0;
        int defEv = 0;
        int spAtkEv = 0;
        int spDefEv = 0;
        int speEv = 0;
        IVStore ivStore = new IVStore(31, 31, 31, 31, 31, 31);
        String natureName = "";
        List<String> moves = new ArrayList<>();

        String[] lines = input.toArray(new String[0]);

        long numMoves = Arrays.stream(lines).filter(l -> l.startsWith("-")).count();

        if (lines.length != 4 + numMoves){
            // handle level field for LC
            // handle IVS for niche builds
            String[] newLines = Arrays.stream(lines).filter(l -> !l.startsWith("Level") && !l.startsWith("IV")).toArray(String[]::new);
            if (newLines.length != 4 + numMoves){
                Logger.getGlobal().info("malformed moveset");
                return;
            }
            lines = newLines;
        }

        String[] line1Split = lines[0].split("@");
        name = line1Split[0].trim();
        if (line1Split.length == 2){
            item = line1Split[1].trim();
        }

        String[] nameSplit = name.split("-");
        if (nameSplit.length >= 2){

            name = nameSplit[0].trim();
            form = nameSplit[1].trim()
                    .replace("Alola", "alolan")
                    .replace("Galar", "galarian");

            if (nameSplit.length > 2){
                form = nameSplit[1] + nameSplit[2];
            }

            if (form.equals("F")){
                form = "female";
            }

            if (form.equals("o")){
                name = name + "-o";
                form = "";
            }

            if (form.equals("Z")){
                name = name + "-Z";
                form = "";
            }

            if (form.equals("Oh")){
                name = name + "-Oh";
                form = "";
            }

            if (form.equals("Dawn Wings")){
                form = "dawn";
            }

            if (form.equals("Dusk Mane")){
                form = "dusk";
            }

            if (form.equals("Small") || form.equals("Super") || form.equals("Large")){
                form = "";
            }
        }

        abilityName = lines[1].replace("Ability:", "").trim().replace(" ", "");
        String[] evs = lines[2].replace("EVs:", "").split("/");
        for (String ev : evs) {
            String[] split = ev.trim().split(" ");
            if (split.length != 2){
                break;
            }
            switch (split[1].trim()){
                case "HP": {
                    hpEV = Integer.parseInt(split[0]);
                    break;
                }
                case "Atk": {
                    atkEV = Integer.parseInt(split[0]);
                    break;
                }
                case "Def": {
                    defEv = Integer.parseInt(split[0]);
                    break;
                }
                case "SpA": {
                    spAtkEv = Integer.parseInt(split[0]);
                    break;
                }
                case "SpD": {
                    spDefEv = Integer.parseInt(split[0]);
                    break;
                }
                case "Spe": {
                    speEv = Integer.parseInt(split[0]);
                    break;
                }
            }
        }
        natureName = lines[3].replace("Nature", "").trim();
        for (int i = 0; i < numMoves; i++){
            String moveString = lines[4 + i].substring(2).trim();

            String[] hiddenPowerSplit = moveString.split("Hidden Power");
            if (hiddenPowerSplit.length == 2){
                moveString = "Hidden Power";
                Element ele = Element.parseType(hiddenPowerSplit[1].trim());
                ivStore = HiddenPower.getOptimalIVs(ele);
            }

            moves.add(moveString);
        }

        Optional<Species> species = PixelmonSpecies.fromName(name).getValue();
        if (!species.isPresent()){
            Logger.getGlobal().info("Could not get species from name " + name);
            return;
        }
        Optional<Ability> ability =  AbilityRegistry.getAbility(abilityName);
        if (!ability.isPresent()){
            //some abilities have mismatched spellings
            abilityName = abilityName
                    .replace("ShellArmor", "ShellArmour")
                    .replace("BattleArmor", "BattleArmour");

            ability =  AbilityRegistry.getAbility(abilityName);
            if (!ability.isPresent()){
                Logger.getGlobal().info("Could not get ability from name " + abilityName);
                return;
            }
        }
        Nature nature = Nature.natureFromString(natureName);
        if (nature == null){
            Logger.getGlobal().info("Could not get nature from name " + natureName);
            return;
        }

        ImmutableAttack[] immutableAttacks = Attack.getAttacks(moves.toArray(new String[0]));
        if (Arrays.stream(immutableAttacks).anyMatch(Objects::isNull)){
            Logger.getGlobal().info("Could not get attacks from " + moves.stream().reduce("", (a, b) -> a + " " + b));
            return;
        }
        Attack[] attacks = Arrays.stream(immutableAttacks).map(ImmutableAttack::ofMutable).toArray(Attack[]::new);

        Moveset moveset = new Moveset(attacks, ability.get());
        Pokemon p = PokemonFactory.create(species.get());

        if (!form.isEmpty()){
            if (species.get().hasForm(form)){
                p.setForm(form);
            }
            else{
                Logger.getGlobal().info("Could not set form " + form + " on " + p.getDisplayName());
            }
        }

        p.setNature(nature);
        p.getIVs().copyIVs(ivStore);
        p.getStats().recalculateStats();
    }
}
