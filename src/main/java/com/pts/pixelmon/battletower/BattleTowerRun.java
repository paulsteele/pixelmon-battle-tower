package com.pts.pixelmon.battletower;

import net.minecraft.nbt.CompoundNBT;

public class BattleTowerRun {
    private int streak;

    public void IncrementStreak(){
        streak++;
    }

    private void SetStreak(int streak){
        this.streak = streak;
    }

    public int GetStreak(){
        return streak;
    }

    public CompoundNBT ToNbt(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("streak", streak);

        return nbt;
    }

   public static BattleTowerRun FromNbt(CompoundNBT nbt){
        BattleTowerRun run = new BattleTowerRun();
        run.SetStreak(nbt.getInt("streak"));

        return run;
   }
}
