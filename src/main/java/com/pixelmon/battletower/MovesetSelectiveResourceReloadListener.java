package com.pixelmon.battletower;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MovesetSelectiveResourceReloadListener implements ISelectiveResourceReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
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
        String item = "";
        String ability = "";
        int hpEV = 0;
        int atkEV = 0;
        int defEv = 0;
        int spAtkEv = 0;
        int spDefEv = 0;
        int speEv = 0;
        String nature = "";
        String move1 = "";
        String move2 = "";
        String move3 = "";
        String move4 = "";

        String[] lines = input.toArray(new String[0]);

        if (lines.length != 8){
            return;
        }

        String[] line1Split = lines[0].split("@");
        name = line1Split[0].trim();
        if (line1Split.length == 2){
            item = line1Split[1].trim();
        }

        ability = lines[1].replace("Ability:", "").trim();
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
        nature = lines[3].replace("Nature", "").trim();
        move1 = lines[4].substring(2);
        move2 = lines[5].substring(2);
        move3 = lines[6].substring(2);
        move4 = lines[7].substring(2);

        Logger.getGlobal().info("wu");
    }
}
