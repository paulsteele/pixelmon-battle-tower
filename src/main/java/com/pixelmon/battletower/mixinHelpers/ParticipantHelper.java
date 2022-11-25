package com.pixelmon.battletower.mixinHelpers;

import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.Entity;

public class ParticipantHelper {
    public static void MoveMon(Entity owner, PixelmonEntity pixelmon){
        pixelmon.moveTo(owner.getLookAngle().normalize().multiply(2, 0, 2).add(owner.position()));
    }
}
