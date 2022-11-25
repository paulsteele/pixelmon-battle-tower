package com.pixelmon.battletower.mixins;

import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.pixelmon.battletower.mixinHelpers.ParticipantHelper.MoveMon;

@Mixin(PlayerParticipant.class)
public class PlayerParticipantMixin {

    @Inject(method = "switchPokemon", at = @At("TAIL"), remap = false)
    public void switchPokemon(PixelmonWrapper pw, UUID newPixelmonUUID, CallbackInfoReturnable<PixelmonWrapper> cir) {
        try {
            MoveMon(cir.getReturnValue().pokemon.getOwnerPlayer(), cir.getReturnValue().entity);
        }
        catch (Exception ignored){

        }
    }

    @Inject(method = "initialize", at = @At("TAIL"), remap = false)
    private void initialize(ServerPlayerEntity p, PixelmonEntity[] startingPixelmon, CallbackInfo ci){
        for (PixelmonEntity pixelmon : startingPixelmon) {
            MoveMon(p, pixelmon);
        }
    }
}
