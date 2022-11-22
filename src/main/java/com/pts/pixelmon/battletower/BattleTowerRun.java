package com.pts.pixelmon.battletower;

import net.minecraft.entity.player.ServerPlayerEntity;

public class BattleTowerRun {
    private final ServerPlayerEntity playerEntity;

    public ServerPlayerEntity GetServerPlayerEntity() {
        return playerEntity;
    }

    int streak;

    public void IncrementStreak(){
        streak++;
    }

    public int GetStreak(){
        return streak;
    }

    public BattleTowerRun(ServerPlayerEntity playerEntity)
    {
        this.playerEntity = playerEntity;
        streak = 0;
    }
}
