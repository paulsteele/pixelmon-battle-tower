package com.pts.pixelmon.battletower.BattleTowerComputer;

import com.pts.pixelmon.battletower.BattleTowerController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.DeferredRegister;

public class BattleTowerComputerBlock extends Block {

    private static final Properties BlockProperties = Properties
            .of(Material.STONE)
            .harvestLevel(2)
            .harvestTool(ToolType.PICKAXE)
            .strength(5f);
    private final BattleTowerController controller;

    public BattleTowerComputerBlock(
            DeferredRegister<Block> blockDeferredRegister,
            BattleTowerController controller
    ) {
        super(BlockProperties);
        this.controller = controller;
        blockDeferredRegister.register("battle_tower_block", () -> this);
    }

    @SuppressWarnings({"deprecation", "NullableProblems"})
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult traceResult) {
        if (world.isClientSide){
            return ActionResultType.SUCCESS;
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        controller.StartRun(world, serverPlayer);

        return ActionResultType.SUCCESS;
    }
}
