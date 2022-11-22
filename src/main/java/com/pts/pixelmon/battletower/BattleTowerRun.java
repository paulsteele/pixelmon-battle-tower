package com.pts.pixelmon.battletower;

public class BattleTowerRun {
    private int streak;

    public void IncrementStreak(){
        streak++;
    }

    public int GetStreak(){
        return streak;
    }

    private BattleTowerRunStatus status;

    public void SetStatus(BattleTowerRunStatus status){
        this.status = status;
    }

    public BattleTowerRunStatus GetStatus(){
        return status;
    }

    public BattleTowerRun()
    {
        streak = 0;
        status = BattleTowerRunStatus.WAITING_ON_CHOICES;
    }

    public enum BattleTowerRunStatus {
        WAITING_ON_CHOICES,
        BATTLING,
        WAITING_ON_RESULTS,
    }
}
