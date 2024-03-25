package org.dk;

import org.dk.model.CardValueOverride;
import org.dk.model.DivinationCard;
import org.dk.model.PoeMap;
import org.dk.model.PoeMapCombination;
import org.dk.util.JsonUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DivCardService {

    public static final int GLOBAL_DROP_POOL_SIZE_NO_CARDS = 7954753;
    public static HashMap<String, DivinationCard> divinationCards = new HashMap<>();
    public static HashMap<String, PoeMap> poeMaps = new HashMap<>();
    static Set<String> poeMapsStrings = new HashSet<>();

    public static void init(){
        poeMapsStrings.addAll(Objects.requireNonNull(JsonUtils.readListOfStringJson("Input/AllMaps.json")));
        JsonUtils.readAllDivinationCardsJson("Input/" + Main.CARDS_FILE_NAME);

        poeMapsStrings.forEach((mapName) -> {
            PoeMap poeMap = new PoeMap();
            poeMap.setMapName(mapName);
            poeMaps.put(mapName, poeMap);
        });

        divinationCards.forEach((cardName, card) -> {
            card.getDrop().getAreas().forEach((area)->{
                int maxLevel = card.getDrop().getMax_level() != 0 ? card.getDrop().getMax_level() : Integer.MAX_VALUE;
                int minLevel = card.getDrop().getMin_level() != 0 ? card.getDrop().getMax_level() : Integer.MIN_VALUE;
                if(area.contains("MapWorlds")
                        && minLevel < Main.MAP_LEVEL
                        && maxLevel > Main.MAP_LEVEL){
                    if(card.getPrice() < Main.CARD_PRICE_FLOOR && !Main.includeCards.contains(card.getName())){
                        card.setPrice(0);
                    }
                    poeMaps.get(area).getCards().add(card);
                }
            });
        });
    }

    public static void calculateEVsOfAllMaps(HashMap<String, PoeMap> poeMaps, HashMap<String, DivinationCard> divinationCards){
        poeMaps.forEach((mapName, poeMap) -> {
            poeMap.calculateEVs(divinationCards);
        });
    }

    public static void writeMapReport(){
        List<PoeMap> allMaps = new ArrayList<>();
        poeMaps.forEach((mapName, poeMap) -> {
            allMaps.add(poeMap);
        });
        allMaps.sort(new PoeMap.TotalGildedScarabEVComparator());
        allMaps.removeIf((map)-> {
            return !map.getMapName().contains("MapWorlds") || map.getMapName().contains("Unique");
        });
        JsonUtils.writeListToJsonFile(Collections.singletonList(allMaps), "Output/AllMapReport.json");
    }

    public static Set<String> findMapsWithMinimumEVCard(int minEV){
        Set<String> goodMaps = new HashSet<>();
        poeMapsStrings.forEach((mapName) -> {
            PoeMap poeMap = poeMaps.get(mapName);
            poeMap.getDivCardsAndSingleMapEV().forEach((cardName, cardEVInfo) -> {
                if(cardEVInfo.getGildedDivScarabEV() > minEV
                && !mapName.contains("Unique")){
                    goodMaps.add(mapName.replace("MapWorlds", ""));
                }
            });
        });
        return goodMaps;
    }

    public static PoeMapCombination generateMapCombination(List<String> mapNames){
        List<String> fixedMapNames = new ArrayList<>();
        mapNames.forEach((mapName) -> {
            fixedMapNames.add("MapWorlds" + mapName);
        });
        PoeMapCombination poeMapCombination = new PoeMapCombination();
        poeMapCombination.setMaps(fixedMapNames);
        poeMapCombination.calculateEVs();
        return poeMapCombination;
    }

    public static void overridePriceList(){
        //Remove the included cards from the culling
        Main.excludeCards.removeAll(Main.includeCards);
        //Cull the bad cards
        Main.excludeCards.forEach((cardName) -> {
            divinationCards.get(cardName).setPrice(0);
        });
        //Apply the card value overrides
        List<CardValueOverride> cardValueOverrides = JsonUtils.readAllDivinationCardOverridesJson("Input/CardValueOverrides.json");
        cardValueOverrides.forEach((cvo) -> {
            divinationCards.get(cvo.getCardName()).setPrice(cvo.getCardValue());
        });
    }

    public static List<String> generateOptimalMapList(){
        List<String> optimalMaps = new ArrayList<>();
        HashMap<String, DivinationCard> deepCpyDivinationCards = new HashMap<>();
        divinationCards.forEach((cardName, divinationCard)->{
            deepCpyDivinationCards.put(cardName, new DivinationCard(divinationCard));
        });
        HashMap<String, PoeMap> deepCpyPoeMaps = new HashMap<>();
        poeMaps.forEach((mapName, poeMap)->{
            deepCpyPoeMaps.put(mapName, new PoeMap(poeMap));
        });
        int mapsAdded = 0;
        while(mapsAdded < 12){
            calculateEVsOfAllMaps(deepCpyPoeMaps, deepCpyDivinationCards);
            PoeMap poppedEntry = getSortedTreeSetOfPoeMaps(deepCpyPoeMaps).pollFirst();
            updateDeepCpyZeroedMap(poppedEntry, deepCpyPoeMaps);
            printCardCountZeroPrice(deepCpyDivinationCards);
            if(!Main.removedMaps.contains(poppedEntry.getMapName()) && !poppedEntry.getMapName().contains("Unique") && !poppedEntry.getMapName().contains("Uber") && !poppedEntry.getMapName().contains("Harbinger")){
                optimalMaps.add(poppedEntry.getMapName());
                mapsAdded++;
                poppedEntry.getCards().forEach((card)->{
                    setDivinationCardValueToZero(card.getName(), deepCpyDivinationCards);
                });
            }
        }
        return optimalMaps;
    }

    public static void printCardCountZeroPrice(HashMap<String, DivinationCard> dvCards){
        AtomicInteger count = new AtomicInteger();
        dvCards.forEach((cardName, card)->{
            if(card.getPrice()==0){
                count.getAndIncrement();
            }
        });
    }

    private static void setDivinationCardValueToZero(String cardName, HashMap<String, DivinationCard> dvCards){
        DivinationCard oldEntry = dvCards.remove(cardName);
        if(DivCardService.divinationCards.get(cardName).getPrice()<0){
        } else {
            oldEntry.setPrice(0);
        }
        dvCards.put(cardName, oldEntry);
    }

    private static void updateDeepCpyZeroedMap(PoeMap zeroedMap, HashMap<String, PoeMap> poeMaps){
        poeMaps.remove(zeroedMap.getMapName());
    }

    private static TreeSet<PoeMap> getSortedTreeSetOfPoeMaps(HashMap<String, PoeMap> poeMaps){
        TreeSet<PoeMap> sortedMapsTarget = new TreeSet<PoeMap>(new PoeMap.TotalGildedScarabEVComparator());
        poeMaps.forEach((mapName, poeMap)->{
            sortedMapsTarget.add(poeMap);
        });
        return sortedMapsTarget;
    }




    /*
    ------------ATTEMPTING DYNAMIC PROGRAMMING----------------
     */

    /*
    public static List<String> generateOptimalMapListDP(){
        List<String> optimalMaps = new ArrayList<>();
        HashMap<String, DivinationCard> deepCpyDivinationCards = new HashMap<>();
        divinationCards.forEach((cardName, divinationCard)->{
            deepCpyDivinationCards.put(cardName, new DivinationCard(divinationCard));
        });
        HashMap<String, PoeMap> deepCpyPoeMaps = new HashMap<>();
        poeMaps.forEach((mapName, poeMap)->{
            deepCpyPoeMaps.put(mapName, new PoeMap(poeMap));
        });
        int mapsAdded = 0;
        while(mapsAdded < 12){
            calculateEVsOfAllMaps(deepCpyPoeMaps, deepCpyDivinationCards);
            PoeMap poppedEntry = getSortedTreeSetOfPoeMaps(deepCpyPoeMaps).pollFirst();
            updateDeepCpyZeroedMap(poppedEntry, deepCpyPoeMaps);
            printCardCountZeroPrice(deepCpyDivinationCards);
            if(!Main.removedMaps.contains(poppedEntry.getMapName()) && !poppedEntry.getMapName().contains("Unique") && !poppedEntry.getMapName().contains("Uber") && !poppedEntry.getMapName().contains("Harbinger")){
                optimalMaps.add(poppedEntry.getMapName());
                mapsAdded++;
                poppedEntry.getCards().forEach((card)->{
                    setDivinationCardValueToZero(card.getName(), deepCpyDivinationCards);
                });
            }
        }
        return optimalMaps;
    }


    static Map<String, Integer> kvMap = new HashMap<>();
    static Map<String, Integer> memoization = new HashMap<>();

    public static int optimizeForMaxValue(List<ObjectWithReferences> objectsWithReferences) {
        // Initialize kvMap
        for (ObjectWithReferences obj : objectsWithReferences) {
            for (String ref : obj.references) {
                kvMap.put(ref, 0);
            }
        }

        // Initialize memoization
        for (ObjectWithReferences obj : objectsWithReferences) {
            memoization.put(obj.toString(), -1);
        }

        return maxUtil(objectsWithReferences, 0, 0);
    }

    public static int maxUtil(List<ObjectWithReferences> objectsWithReferences, int idx, int valueSoFar) {
        if (idx == objectsWithReferences.size()) {
            return valueSoFar;
        }

        ObjectWithReferences obj = objectsWithReferences.get(idx);

        // Check if we already have the result stored in memoization
        String state = obj.toString() + ":" + valueSoFar;
        if (memoization.containsKey(state)) {
            return memoization.get(state);
        }

        // Include the current object and update the values of referenced objects
        int includeCurrent = valueSoFar + obj.value;
        for (String ref : obj.references) {
            if (kvMap.containsKey(ref)) {
                int prevValue = kvMap.get(ref);
                kvMap.put(ref, Math.max(0, prevValue)); // Ensure non-negative value
                includeCurrent += prevValue;
            }
        }

        // Skip the current object
        int skipCurrent = maxUtil(objectsWithReferences, idx + 1, valueSoFar);

        // Choose the maximum of including and skipping the current object
        int maxResult = Math.max(includeCurrent, skipCurrent);

        // Store the result in memoization
        memoization.put(state, maxResult);

        return maxResult;
    }

     */

}
