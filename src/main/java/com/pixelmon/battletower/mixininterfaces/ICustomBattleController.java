package com.pixelmon.battletower.mixininterfaces;

import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;

import java.util.HashMap;
import java.util.function.Consumer;

public interface ICustomBattleController {
    void OnBattleEnd(Consumer<HashMap<BattleParticipant, BattleResults>> action);
}
