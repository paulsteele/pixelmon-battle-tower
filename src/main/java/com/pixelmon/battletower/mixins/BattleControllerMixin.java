package com.pixelmon.battletower.mixins;

import com.pixelmon.battletower.mixinHelpers.ICustomBattleController;
import com.pixelmonmod.pixelmon.api.battles.BattleEndCause;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.function.Consumer;

@Mixin(BattleController.class)
//@Implements(@Interface(iface = ICustomBattleController.class, prefix = "custom$"))
public abstract class BattleControllerMixin implements ICustomBattleController {
    private Consumer<HashMap<BattleParticipant, BattleResults>> onBattleEndRunnable = null;

    @Override
    public void OnBattleEnd(Consumer<HashMap<BattleParticipant, BattleResults>> action) {
        onBattleEndRunnable = action;
    }


    @Inject(method = "endBattle(Lcom/pixelmonmod/pixelmon/api/battles/BattleEndCause;Ljava/util/HashMap;)Ljava/util/HashMap;", at = @At("TAIL"), remap = false)
    protected void endBattle(BattleEndCause cause, HashMap<BattleParticipant, BattleResults> results, CallbackInfoReturnable<HashMap<BattleParticipant, BattleResults>> cir){
        if (onBattleEndRunnable != null){
            onBattleEndRunnable.accept(results);
        }
    }
}
