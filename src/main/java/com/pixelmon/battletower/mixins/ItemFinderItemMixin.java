package com.pixelmon.battletower.mixins;

import com.pixelmonmod.pixelmon.api.util.helpers.BlockHelper;
import com.pixelmonmod.pixelmon.blocks.tileentity.PokeChestTileEntity;
import com.pixelmonmod.pixelmon.items.ItemFinderItem;
import com.pixelmonmod.pixelmon.items.PixelmonItem;
import com.pixelmonmod.pixelmon.items.group.PixelmonItemGroups;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemFinderItem.class)
public abstract class ItemFinderItemMixin extends PixelmonItem{
    private static final double RADIUS = 100.0;
    public ItemFinderItemMixin() {
        super((new Item.Properties()).stacksTo(1).tab(PixelmonItemGroups.TAB_POKE_LOOT));
    }
    @Overwrite
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, 20);
        if (!world.isClientSide) {
            PokeChestTileEntity chest = BlockHelper.findClosestTileEntity(PokeChestTileEntity.class, player, RADIUS, (p) -> true);
            if (chest != null) {
                Direction direction = this.getDirection(player, chest.getBlockPos());
                player.displayClientMessage(new StringTextComponent(direction.toString()), false);

                world.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.LEVER_CLICK, SoundCategory.PLAYERS, 0.5F, 1.0F);
            }
            else{
                player.displayClientMessage(new StringTextComponent("No loot found..."), false);
            }
        }

        return new ActionResult<>(ActionResultType.SUCCESS, itemStack);
    }

    private Direction getDirection(PlayerEntity player, BlockPos pos) {
        int x = (int)(player.getX() - (double)pos.getX());
        int z = (int)(player.getZ() - (double)pos.getZ());
        Direction direction;
        if (Math.abs(x) > Math.abs(z)) {
            if (x > 0) {
                direction = Direction.WEST;
            } else {
                direction = Direction.EAST;
            }
        } else if (z > 0) {
            direction = Direction.NORTH;
        } else {
            direction = Direction.SOUTH;
        }

        return direction;
    }
}
