package com.pts.pixelmon.battletower.BetterItemFinder;

import com.pts.pixelmon.battletower.BattleTowerComputer.BattleTowerComputerBlock;
import com.pts.pixelmon.battletower.BattleTowerComputer.BattleTowerComputerItem;

public class BetterItemFinder {
    public BetterItemFinderItem Item;

    private static BetterItemFinder Instance;

    public static BetterItemFinder GetInstance()
    {
        if (Instance == null){
            Instance = new BetterItemFinder();
            Instance.Item = new BetterItemFinderItem();
        }

        return Instance;
    }
}
