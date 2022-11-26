package com.pixelmon.battletower;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class MovesetSelectiveResourceReloadListener extends ReloadListener {
    static Logger logger = LogManager.getLogger(BattleTowerMain.ModId);
    private MovesetRepository repository;

    public MovesetSelectiveResourceReloadListener(MovesetRepository repository){

        this.repository = repository;
    }

    private List<String> GetLinesOfResourceFile(IResourceManager manager, ResourceLocation location) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(manager.getResource(location).getInputStream()));
        List<String> contents = reader.lines().collect(Collectors.toList());
        reader.close();
        return contents;
    }

    @Override
    protected Object prepare(IResourceManager resourceManager, IProfiler profiler) {
        return null;
    }

    @Override
    protected void apply(Object obj, IResourceManager resourceManager, IProfiler profiler) {
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
}
