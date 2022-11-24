package com.pixelmon.battletower;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
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
        input.forEach(s -> {
        });
    }
}
