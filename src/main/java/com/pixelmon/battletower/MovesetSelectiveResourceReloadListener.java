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
import com.pixelmonmod.pixelmon.api.pokemon.stats.EVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.Moveset;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.CallbackI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MovesetSelectiveResourceReloadListener implements ISelectiveResourceReloadListener {
    static Logger logger = LogManager.getLogger(BattleTowerMain.ModId);
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (PixelmonSpecies.getAll().size() < 1){
            logger.info("Pixelmon not initialized");
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
            logger.error(e.getMessage(), e);
        }
    }

    private List<String> GetLinesOfResourceFile(IResourceManager manager, ResourceLocation location) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(manager.getResource(location).getInputStream()));
        List<String> contents = reader.lines().collect(Collectors.toList());
        reader.close();
        return contents;
    }

    private void CreateMoveSet(List<String> input){
        String[] lines = input.toArray(new String[0]);

        long numMoves = Arrays.stream(lines).filter(l -> l.startsWith("-")).count();

        if (lines.length != 4 + numMoves){
            // handle level field for LC
            // handle IVS for niche builds
            String[] newLines = Arrays.stream(lines).filter(l -> !l.startsWith("Level") && !l.startsWith("IV")).toArray(String[]::new);
            if (newLines.length != 4 + numMoves){
                logger.error("malformed moveset");
                return;
            }
            lines = newLines;
        }

        SmogonLineOne nameFormItem = new SmogonLineOne(lines[0]);
        SmogonLineTwo ability = new SmogonLineTwo(lines[1]);
        SmogonLineThree evs = new SmogonLineThree(lines[2]);
        SmogonLineFour nature = new SmogonLineFour(lines[3]);
        SmogonFinalLines movesAndIvs = new SmogonFinalLines(Arrays.stream(lines).skip(4).toArray(String[]::new));

        if (!nameFormItem.Species.isPresent()){
            return;
        }
        if (!ability.Ability.isPresent()){
            return;
        }
        if (!nature.NatureValue.isPresent()){
            return;
        }
        if (!movesAndIvs.Moveset.isPresent()){
            return;
        }

        Pokemon p = PokemonFactory.create(nameFormItem.Species.get());
        if (nameFormItem.Form != null && !nameFormItem.Form.isEmpty()){
            p.setForm(nameFormItem.Form);
        }
        p.setMoveset(movesAndIvs.Moveset.get());
        p.setAbility(ability.Ability);
        p.setNature(nature.NatureValue.get());
        p.getStats().setEVs(evs.evStore);
        p.getStats().setIVs(movesAndIvs.IvStore);
        p.getStats().recalculateStats();
    }

    private static class SmogonLineOne {
        Optional<Species> Species;
        public String Form = "";
        public String Item;
        public SmogonLineOne(String line){
            String[] line1Split = line.split("@");
            String name = line1Split[0].trim();
            if (line1Split.length == 2){
                Item = line1Split[1].trim();
            }

            String[] nameSplit = name.split("-");
            if (nameSplit.length >= 2){

                name = nameSplit[0].trim();
                Form = nameSplit[1].trim()
                        .replace("Alola", "alolan")
                        .replace("Galar", "galarian");

                if (nameSplit.length > 2){
                    Form = nameSplit[1] + nameSplit[2];
                }

                if (Form.equals("F")){
                    Form = "female";
                }
                if (Form.equals("M")){
                    Form = "male";
                }

                if (Form.equals("o")){
                    name = name + "-o";
                    Form = "";
                }

                if (Form.equals("Z")){
                    name = name + "-Z";
                    Form = "";
                }

                if (Form.equals("Oh")){
                    name = name + "-Oh";
                    Form = "";
                }

                if (Form.equals("Dawn Wings")){
                    Form = "dawn";
                }

                if (Form.equals("Dusk Mane")){
                    Form = "dusk";
                }

                if (Form.equals("Small") || Form.equals("Super") || Form.equals("Large")){
                    Form = "";
                }

                if (name.equals("Nidoran")){
                    name = name + Form;
                    Form = "";
                }

                if (name.equals("Calyrex")){
                    Form = Form.replace("Ice", "Icerider").replace("Shadow", "Shadowrider");
                }

                if (name.equals("Meowstic")){
                    Form = "";
                }

                if (Form.equals("Pa'u")){
                    Form = "Pau";
                }

                if (Form.equals("Gmax")){
                    Form = "";
                }

                if (Form.equals("Dada")){
                    Form = "";
                }

                if (Form.equals("10%")) {
                    Form = "ten_percent";
                }
            }

            Species = PixelmonSpecies.fromName(name).getValue();
            if (!Species.isPresent()){
                logger.error("Could not get species from name " + name);
            }
            else{
                if (!Form.isEmpty()){
                    if (!Species.get().hasForm(Form)){
                        logger.error("Could not get form " + Form + " for species " + name);
                        Species = Optional.empty();
                    }
                }
            }
        }
    }

    private static class SmogonLineTwo{
        Optional<Ability> Ability;

        public SmogonLineTwo(String line){
            String abilityName = line.replace("Ability:", "").trim().replace(" ", "");

            Ability =  AbilityRegistry.getAbility(abilityName);
            if (!Ability.isPresent()){
                //some abilities have mismatched spellings
                abilityName = abilityName
                        .replace("ShellArmor", "ShellArmour")
                        .replace("BattleArmor", "BattleArmour")
                        .replace("Dragon'sMaw", "DragonsMaw")
                        .replace("AsOne(Glastrier)", "AsOne")
                        .replace("AsOne(Spectrier)", "AsOne")
                        .replace("Soul-Heart", "SoulHeart")
                ;

                Ability =  AbilityRegistry.getAbility(abilityName);
                if (!Ability.isPresent()){
                    logger.error("Could not get ability from name " + abilityName);
                }
            }
        }
    }

    private static class SmogonLineThree{
        EVStore evStore;
        public SmogonLineThree(String line){
            int hpEV = 0;
            int atkEV = 0;
            int defEv = 0;
            int spAtkEv = 0;
            int spDefEv = 0;
            int speEv = 0;
            String[] evs = line.replace("EVs:", "").split("/");
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
            evStore = new EVStore(hpEV, atkEV, defEv, spAtkEv, spDefEv, speEv);
        }
    }

    private static class SmogonLineFour{
        Optional<Nature> NatureValue;

        public SmogonLineFour(String line){
            String nature = line.replace("Nature", "").trim();

            Nature tempNatureValue = Nature.natureFromString(nature);
            if (tempNatureValue == null){
                logger.error("Could not get nature from name " + nature);
                NatureValue = Optional.empty();
            }
            else{
                NatureValue = Optional.of(tempNatureValue);
            }
        }
    }

    private static class SmogonFinalLines{
        IVStore IvStore;
        Optional<Moveset> Moveset;

        public SmogonFinalLines(String[] lines){
            String[] moves = new String[lines.length];
            IvStore = new IVStore(31, 31, 31, 31, 31, 31);
            for (int i = 0; i < lines.length; i++){
                String moveString = lines[i].substring(2).trim();

                String[] hiddenPowerSplit = moveString.split("Hidden Power");
                if (hiddenPowerSplit.length == 2){
                    moveString = "Hidden Power";
                    Element ele = Element.parseType(hiddenPowerSplit[1].trim());
                    IvStore = HiddenPower.getOptimalIVs(ele);
                }

                moves[i] = moveString;
            }

            ImmutableAttack[] immutableAttacks = Attack.getAttacks(moves);
            if (Arrays.stream(immutableAttacks).anyMatch(Objects::isNull)){
                logger.error("Could not get attacks from " + Arrays.stream(moves).reduce("", (a, b) -> a + " " + b));
                Moveset = Optional.empty();
            }
            else {
                Attack[] attacks = Arrays.stream(immutableAttacks).map(ImmutableAttack::ofMutable).toArray(Attack[]::new);

                Moveset = Optional.of(new Moveset(attacks, null));
            }
        }
    }
}
