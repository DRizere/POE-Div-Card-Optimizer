package org.dk;

import org.dk.model.PoeMapCombination;
import org.dk.util.JsonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    public static final String INPUT_CARD = "The Union";
    public static final int INPUT_CARD_PER_MAP = 45;
    public static final int MAP_LEVEL = 83;
    public static final int CARD_PRICE_FLOOR = 6;
    public static final int MINIMUM_CARD_EV = 500;
    public static final String CARDS_FILE_NAME = "TOTACardsData.json";
    static List<String> atlasMaps = JsonUtils.readListOfStringJson("Input/AtlasMaps.json");
    static List<String> excludeCards = JsonUtils.readListOfStringJson("Input/ForceWorthlessCards.json");
    static List<String> includeCards = JsonUtils.readListOfStringJson("Input/ForceGoodCards.json");
    static List<String> inputMapCombination = JsonUtils.readListOfStringJson("Input/InputMapCombination.json");
    static List<String> inputGoodMaps = JsonUtils.readListOfStringJson("Input/GoodMaps.json");

    static PoeMapCombination bestCombination = new PoeMapCombination();


    public static void main(String[] args) {
        System.out.printf("There are %s atlas maps.\n", atlasMaps.size());

        DivCardService.init();
        DivCardService.overridePriceList();
        DivCardService.calculateEVsOfAllMaps(DivCardService.poeMaps, DivCardService.divinationCards);
        DivCardService.writeMapReport();

        //Test the given input Map combination
        JsonUtils.writeObjectToJsonFile(DivCardService.generateMapCombination(inputMapCombination),
                "Output/SingleMapComboReport.json");


        //Find maps with a card with a minimum EV
        List<String> goodMaps = DivCardService.findMapsWithMinimumEVCard(MINIMUM_CARD_EV);
        JsonUtils.writeListOfStringToJson(
                goodMaps,
                "Output/MapsWithMinimumCardEV" + MINIMUM_CARD_EV + ".json"
        );

        //Write out all the permutations of the "good maps"
        System.out.println("Considering # maps: " + goodMaps.size());
        System.out.println("Will result in # combinations: " + Math.pow(2, goodMaps.size()));
        double startTime = System.currentTimeMillis();
        if(goodMaps.size()<30){
            bestCombination.setTotalGildedScarabEV(0);
            generateCombinations(goodMaps, 12);

            JsonUtils.writeListToJsonFile(Collections.singletonList(bestCombination), "Output/TheBestPermutation" + MINIMUM_CARD_EV + ".json");
        }
        double endTime = System.currentTimeMillis();
        System.out.printf("Time to process %s combinations: %s", Math.pow(2, goodMaps.size()), (endTime-startTime)/1000);
    }

    public static void processCombination(List<String> currentCombination){
        PoeMapCombination currentPerm = DivCardService.generateMapCombination(currentCombination);
        if(bestCombination.getGildedScarabTotalEV() < currentPerm.getGildedScarabTotalEV()) {
            System.out.println("Found new better permString: " + currentCombination);
            bestCombination = currentPerm;
        }
    }

    public static void generateCombinations(List<String> strings, int size) {
        generateCombinationsHelper(strings, size, 0, new ArrayList<>());
    }

    private static void generateCombinationsHelper(List<String> strings, int size, int start, List<String> current) {
        if (current.size() == size) {
            processCombination(new ArrayList<>(current));
            return;
        }
        for (int i = start; i < strings.size(); i++) {
            current.add(strings.get(i));
            generateCombinationsHelper(strings, size, i + 1, current);
            current.remove(current.size() - 1);
        }
    }
}