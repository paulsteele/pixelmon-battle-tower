package com.pixelmon.battletower.mixins;

import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import static com.pixelmon.battletower.mixinHelpers.ParticipantHelper.MoveMon;

@Mixin(TrainerParticipant.class)
public class TrainerParticipantMixin {

    @Inject(method = "releasePokemon", at = @At("TAIL"), remap = false)
    private void initialize(CallbackInfoReturnable<PixelmonEntity[]> cir){
        for (PixelmonEntity pixelmon : cir.getReturnValue()) {
            if (pixelmon.getPokemon().getOwnerTrainer() != null){
                MoveMon(pixelmon.getPokemon().getOwnerTrainer(), pixelmon);
            }
        }
    }
}
