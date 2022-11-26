package com.pixelmon.battletower;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.export.exception.PokemonImportException;
import com.pixelmonmod.pixelmon.api.pokemon.export.impl.ShowdownConverter;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import net.minecraft.util.Tuple;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MovesetRepository {
    static Logger logger = LogManager.getLogger(BattleTowerMain.ModId);
    public final HashMap<String, ArrayList<Pokemon>> SmogonMons;
    private final List<Tuple<String, List<String>>> RawMovesets;

    public MovesetRepository(){
        SmogonMons = new HashMap<>();
        RawMovesets = new ArrayList<>();
    }

    private void AddSmogonMon(String tier, Pokemon mon){
        if (!SmogonMons.containsKey(tier)){
            SmogonMons.put(tier, new ArrayList<>());
        }

        SmogonMons.get(tier).add(mon);
    }

    public void AddRawMoveset(String tier, List<String> rawMoveset){
        RawMovesets.add(new Tuple<>(tier, rawMoveset));
    }

    public void ClearRawMovesets(){
        RawMovesets.clear();
    }

    public void OnLoad(WorldEvent.Load event){
        if (SmogonMons.isEmpty()) {
            for (Tuple<String, List<String>> moveset : RawMovesets) {
                Optional<Pokemon> smogonMon = CreateMoveSet(moveset.getB());
                smogonMon.ifPresent(pokemon -> AddSmogonMon(moveset.getA(), pokemon));
            }
        }
    }
    private Pokemon ImportAndCheck(String[] lines) throws PokemonImportException {
        ShowdownConverter converter = new ShowdownConverter();
        Pokemon p = converter.importText(lines).get(0);
        p.setDynamaxLevel(10);
        p.setGrowth(EnumGrowth.getRandomGrowth());

        if (p.getDisplayName().toLowerCase(Locale.ROOT).equals("MissingNo".toLowerCase(Locale.ROOT))){
            throw new PokemonImportException("Failed to parse", "MissingNo");
        }
        return p;
    }

    private Optional<Pokemon> CreateMoveSet(List<String> input){
        String[] lines = input.toArray(new String[0]);
        try {
            return Optional.of(ImportAndCheck(lines));
        } catch (PokemonImportException e) {
            try {
                for (int i = 0; i < lines.length; i++){
                    lines[i] = lines[i].replace("Dragon's Maw", "DragonsMaw");
                    lines[i] = lines[i].replace("Soul-Heart", "SoulHeart");
                    lines[i] = lines[i].replace("Calyrex-Ice", "Calyrex-Icerider");
                    lines[i] = lines[i].replace("Calyrex-Shadow", "Calyrex-Shadowrider");
                    lines[i] = lines[i].replace("As One (Spectrier)", "AsOne");
                    lines[i] = lines[i].replace("As One (Glastrier)", "AsOne");
                    lines[i] = lines[i].replace("As One (Spectrier)", "AsOne");
                    lines[i] = lines[i].replace("Nidoran-F", "NidoranFemale");
                    lines[i] = lines[i].replace("Nidoran-M", "NidoranMale");
                    lines[i] = lines[i].replace("-Small", "");
                    lines[i] = lines[i].replace("-Super", "");
                    lines[i] = lines[i].replace("-Large", "");
                    lines[i] = lines[i].replace("Indeedee-F", "Indeedee-Female");
                    lines[i] = lines[i].replace("Urshifu-Rapid-Strike", "Urshifu-RapidStrike");
                    lines[i] = lines[i].replace("Necrozma-Dawn Wings", "Necrozma-Dawn");
                    lines[i] = lines[i].replace("Necrozma-Dusk Mane", "Necrozma-Dusk");
                    lines[i] = lines[i].replace("-Gmax", "");
                    lines[i] = lines[i].replace("Meowstic-F", "Meowstic");
                    lines[i] = lines[i].replace("Meowstic-M", "Meowstic");
                    lines[i] = lines[i].replace("Pa'u", "Pau");
                    lines[i] = lines[i].replace("Dada", "");
                    lines[i] = lines[i].replace("10%", "ten_percent");
                }
                return Optional.of(ImportAndCheck(lines));
            }
            catch (PokemonImportException ee){
                logger.error(e.getMessage(), e);
                return Optional.empty();
            }
        }
    }
}
