package org.dk;

import org.dk.model.PoeMapCombination;
import org.dk.util.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static final String INPUT_CARD = "The Union";
    public static final int INPUT_CARD_PER_MAP = 45;
    public static final int MAP_LEVEL = 83;
    public static final int CARD_PRICE_FLOOR = 6;
    public static final double MINIMUM_CARD_EV = 34.8;
    public static final String CARDS_FILE_NAME = "TOTACards.json";

    static List<String> removedMaps = JsonUtils.readListOfStringJson("Input/RemovedMaps.json");
    static List<String> atlasMaps = JsonUtils.readListOfStringJson("Input/AtlasMaps.json");
    static List<String> excludeCards = JsonUtils.readListOfStringJson("Input/ForceWorthlessCards.json");
    static List<String> includeCards = JsonUtils.readListOfStringJson("Input/ForceGoodCards.json");
    static List<String> inputMapCombination = JsonUtils.readListOfStringJson("Input/MapCombination.json");
    static List<String> inputGoodMaps = JsonUtils.readListOfStringJson("Input/GoodMaps.json");


    public static void main(String[] args) {

        DivCardService.init();
        DivCardService.overridePriceList();
        DivCardService.calculateEVsOfAllMaps(DivCardService.poeMaps, DivCardService.divinationCards);
        DivCardService.writeMapReport();

        //Test the given input Map combination
        JsonUtils.writeListToJsonFile(
                Collections.singletonList(DivCardService.generateMapCombination(inputMapCombination)),
                "Output/SingleMapComboReport.json");


        //Find maps with a card with a minimum EV
        List<String> goodMaps = DivCardService.findMapsWithMinimumEVCard(MINIMUM_CARD_EV).stream().toList();
        JsonUtils.writeListOfStringToJson(
                goodMaps,
                "Output/MapsWithMinimumCardEV" + MINIMUM_CARD_EV + ".json"
        );


        //Write out all the permutations of the "good maps"
        List<String> EVMaps = JsonUtils.readListOfStringJson("Output/MapsWithMinimumCardEV" + MINIMUM_CARD_EV + ".json");
        System.out.println("Considering # maps: " + EVMaps.size());
        if(EVMaps.size()<25){
            List<List<String>> permutationsOfGoodMaps = combine(EVMaps);
            AtomicReference<PoeMapCombination> bestCombination = new AtomicReference<>(new PoeMapCombination());
            bestCombination.get().setTotalGildedScarabEV(Integer.MIN_VALUE);
            permutationsOfGoodMaps.forEach((permString) -> {
                PoeMapCombination currentPerm = DivCardService.generateMapCombination(permString);
                if(bestCombination.get().getGildedScarabTotalEV() < currentPerm.getGildedScarabTotalEV()){
                    System.out.println("Found new better permString: " + permString);
                    bestCombination.set(currentPerm);
                }
            });
            JsonUtils.writeListToJsonFile(Collections.singletonList(bestCombination.get()), "Output/TheBestPermutation" + MINIMUM_CARD_EV + ".json");
        }
    }

    public static List<List<String>> combine(List<String> strings) {
        List<List<String>> result = new ArrayList<>();
        backtrack(strings, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<String> strings, int start, List<String> tempList, List<List<String>> result) {
        if (tempList.size() == 12) {
            result.add(new ArrayList<>(tempList));
        } else {
            for (int i = start; i < strings.size(); i++) {
                tempList.add(strings.get(i));
                backtrack(strings, i + 1, tempList, result);
                tempList.remove(tempList.size() - 1);
            }
        }
    }
}