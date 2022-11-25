package com.pixelmon.battletower.persistence;

import net.minecraft.nbt.CompoundNBT;

public class BattleTowerRun {
    private int streak;
    private final RunType type;

    public void IncrementStreak(){
        streak++;
    }

    private void SetStreak(int streak){
        this.streak = streak;
    }

    public int GetStreak(){
        return streak;
    }

    public RunType GetType(){
        return type;
    }

    public BattleTowerRun(RunType type){
        this.type = type;
    }

    public CompoundNBT ToNbt(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("streak", streak);
        nbt.putString("type", type.name());

        return nbt;
    }

   public static BattleTowerRun FromNbt(CompoundNBT nbt){
        RunType type = RunType.valueOf(nbt.getString("type"));
        BattleTowerRun run = new BattleTowerRun(type);
        run.SetStreak(nbt.getInt("streak"));

        return run;
   }

   public enum RunType {
       SINGLES,
       DOUBLES,
       FULL_TEAM_SINGLES,
       FULL_TEAM_DOUBLES,
   }
}
