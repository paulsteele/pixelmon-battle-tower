package com.pixelmon.battletower;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.export.exception.PokemonImportException;
import com.pixelmonmod.pixelmon.api.pokemon.export.impl.ShowdownConverter;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.stats.EVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.Moveset;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.items.HeldItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MovesetSelectiveResourceReloadListener implements ISelectiveResourceReloadListener {
    static Logger logger = LogManager.getLogger(BattleTowerMain.ModId);
    private BattleTowerController controller;

    public MovesetSelectiveResourceReloadListener(BattleTowerController controller){

        this.controller = controller;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if (PixelmonSpecies.getAll().size() < 1){
            logger.info("Pixelmon not initialized");
            return;
        }
        controller.ClearSmogonMons();

        try {
             List<String> tiers = GetLinesOfResourceFile(resourceManager, new ResourceLocation("pixelmonbattletower.movesets", "tiers.txt"));

            for (String tier : tiers) {
                for (ResourceLocation resourceLocation : resourceManager.listResources(tier, s -> s.endsWith(".txt"))) {
                    Optional<Pokemon> smogonMon = CreateMoveSet(GetLinesOfResourceFile(resourceManager, resourceLocation));
                    smogonMon.ifPresent(pokemon -> controller.AddSmogonMon(tier, pokemon));
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

    private Pokemon ImportAndCheck(String[] lines) throws PokemonImportException {
        ShowdownConverter converter = new ShowdownConverter();
        Pokemon p = converter.importText(lines).get(0);
        p.setDynamaxLevel(10);
        p.setGrowth(EnumGrowth.getRandomGrowth());

        if (p.getDisplayName().toLowerCase(Locale.ROOT).equals("MissingNo".toLowerCase(Locale.ROOT))){
            throw new PokemonImportException("Failed to parse", "MissingNo");
        }
        return p;
    }

    private Optional<Pokemon> CreateMoveSet(List<String> input){
        String[] lines = input.toArray(new String[0]);
        try {
            return Optional.of(ImportAndCheck(lines));
        } catch (PokemonImportException e) {
            try {
                for (int i = 0; i < lines.length; i++){
                    lines[i] = lines[i].replace("Dragon's Maw", "DragonsMaw");
                    lines[i] = lines[i].replace("Soul-Heart", "SoulHeart");
                    lines[i] = lines[i].replace("Calyrex-Ice", "Calyrex-Icerider");
                    lines[i] = lines[i].replace("Calyrex-Shadow", "Calyrex-Shadowrider");
                    lines[i] = lines[i].replace("As One (Spectrier)", "AsOne");
                    lines[i] = lines[i].replace("As One (Glastrier)", "AsOne");
                    lines[i] = lines[i].replace("As One (Spectrier)", "AsOne");
                    lines[i] = lines[i].replace("Nidoran-F", "NidoranFemale");
                    lines[i] = lines[i].replace("Nidoran-M", "NidoranMale");
                    lines[i] = lines[i].replace("-Small", "");
                    lines[i] = lines[i].replace("-Super", "");
                    lines[i] = lines[i].replace("-Large", "");
                    lines[i] = lines[i].replace("Indeedee-F", "Indeedee-Female");
                    lines[i] = lines[i].replace("Urshifu-Rapid-Strike", "Urshifu-RapidStrike");
                    lines[i] = lines[i].replace("Necrozma-Dawn Wings", "Necrozma-Dawn");
                    lines[i] = lines[i].replace("Necrozma-Dusk Mane", "Necrozma-Dusk");
                    lines[i] = lines[i].replace("-Gmax", "");
                    lines[i] = lines[i].replace("Meowstic-F", "Meowstic");
                    lines[i] = lines[i].replace("Meowstic-M", "Meowstic");
                    lines[i] = lines[i].replace("Pa'u", "Pau");
                    lines[i] = lines[i].replace("Dada", "");
                    lines[i] = lines[i].replace("10%", "ten_percent");
                }
                return Optional.of(ImportAndCheck(lines));
            }
            catch (PokemonImportException ee){
                logger.error(e.getMessage(), e);
                return Optional.empty();
            }
        }
    }
}
