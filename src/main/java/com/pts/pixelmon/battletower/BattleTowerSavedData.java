package com.pts.pixelmon.battletower;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.UUID;

public class BattleTowerSavedData extends WorldSavedData {

    public static final String Id = "BattleTowerSavedData";
    public BattleTowerSavedData() {
        super(Id);
        RunMap = new HashMap<>();
    }

    private final HashMap<UUID, BattleTowerRun> RunMap;

    @Override
    public void load(CompoundNBT nbt) {
        RunMap.clear();
        nbt.getAllKeys().forEach(s ->{
            UUID uuid = UUID.fromString(s);
            RunMap.put(uuid, BattleTowerRun.FromNbt(nbt.getCompound(s)));
        });

    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        RunMap.forEach((uuid, battleTowerRun) -> {
            nbt.put(uuid.toString(), battleTowerRun.ToNbt());
        });
        return nbt;
    }

    public boolean HasRun(ServerPlayerEntity player){
        return RunMap.containsKey(player.getUUID());
    }

    public void StartRun(ServerPlayerEntity player, BattleTowerRun.RunType type){
        if (HasRun(player)){
            return;
        }

        RunMap.put(player.getUUID(), new BattleTowerRun(type));
        setDirty();
    }

    public void EndRun(ServerPlayerEntity player){
        if (!HasRun(player)){
            return;
        }

        RunMap.remove(player.getUUID());
        setDirty();
    }

    public int GetStreak(ServerPlayerEntity player){
        if (!HasRun(player)){
            return -1;
        }

        return RunMap.get(player.getUUID()).GetStreak();
    }

    public BattleTowerRun.RunType GetType(ServerPlayerEntity player){
        if (!HasRun(player)){
            return BattleTowerRun.RunType.SINGLES;
        }

        return RunMap.get(player.getUUID()).GetType();
    }

    public void IncrementStreak(ServerPlayerEntity player){
        if (!HasRun(player)){
            return;
        }

        RunMap.get(player.getUUID()).IncrementStreak();
        setDirty();
    }
}
