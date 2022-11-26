package com.pixelmon.battletower;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MovesetSelectiveResourceReloadListener implements ISelectiveResourceReloadListener {
    static Logger logger = LogManager.getLogger(BattleTowerMain.ModId);
    private MovesetRepository repository;

    public MovesetSelectiveResourceReloadListener(MovesetRepository repository){

        this.repository = repository;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        repository.ClearRawMovesets();

        try {
             List<String> tiers = GetLinesOfResourceFile(resourceManager, new ResourceLocation("pixelmonbattletower.movesets", "tiers.txt"));

            for (String tier : tiers) {
                for (ResourceLocation resourceLocation : resourceManager.listResources(tier, s -> s.endsWith(".txt"))) {
                    repository.AddRawMoveset(tier, GetLinesOfResourceFile(resourceManager, resourceLocation));
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
}
