package com.pts.pixelmon.battletower.BattleTowerComputer;

public class BattleTowerComputer {
    public BattleTowerComputerBlock Block;

    public BattleTowerComputerItem Item;

    private static BattleTowerComputer Instance;

    public static BattleTowerComputer GetInstance()
    {
        if (Instance == null){
            Instance = new BattleTowerComputer();
            Instance.Block = new BattleTowerComputerBlock();
            Instance.Item = new BattleTowerComputerItem(Instance.Block);
        }

        return Instance;
    }
}
