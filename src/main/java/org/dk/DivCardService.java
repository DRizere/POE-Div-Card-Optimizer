package org.dk;

import org.dk.model.*;
import org.dk.util.JsonUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DivCardService {

    public static final int GLOBAL_DROP_POOL_SIZE_NO_CARDS = 7954753;
    public static HashMap<String, DivinationCard> divinationCards = new HashMap<>();
    public static HashMap<String, PoeMap> poeMaps = new HashMap<>();
    static Set<String> poeMapsStrings = new HashSet<>();

    static TreeSet<CardEVInfo> allDivCardEVInfo = new TreeSet<>(new CardEVInfo.TotalGildedScarabEVComparator());

    public static void init(){
        poeMapsStrings.addAll(Objects.requireNonNull(JsonUtils.readListOfStringJson("Input/AllMaps.json")));
        JsonUtils.readAllDivinationCardsJson("Input/" + Main.CARDS_DATA_FILE_NAME);

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

    public static void compileAllDivCardEVInfoTS(){
        poeMaps.values().forEach((map)->{
            allDivCardEVInfo.addAll(map.getDivCardsAndSingleMapEV().values());
        });
    }

    public static void writeDivCardEVReport(){
        JsonUtils.writeObjectToJsonFile(allDivCardEVInfo, "Output/AllDivCardEVReport.json");
    }

    public static void writeMapReport(){
        List<PoeMap> allMaps = new ArrayList<>();
        poeMaps.forEach((mapName, poeMap) -> {
            allMaps.add(poeMap);
        });
        allMaps.sort(new PoeMap.TotalGildedScarabEVComparator());
        allMaps.removeIf((map) -> {
            return !Main.atlasMaps.contains(map.getMapName());
        });

        System.out.printf("MapReport has %s maps.\n" , allMaps.size());
        JsonUtils.writeObjectToJsonFile(allMaps, "Output/AllMapReport.json");
    }

    public static List<String> findMapsWithMinimumEVCard(int minEV){
        TreeSet<PoeMap> sortedMaps = new TreeSet<>(new PoeMap.TotalGildedScarabEVComparator());
        poeMapsStrings.forEach((mapName) -> {
            PoeMap poeMap = poeMaps.get(mapName);
            poeMap.getDivCardsAndSingleMapEV().forEach((cardName, cardEVInfo) -> {
                if(cardEVInfo.getGildedDivScarabEV() > minEV
                && Main.atlasMaps.contains(mapName)){
                    sortedMaps.add(poeMap);
                }
            });
        });
        return sortedMaps.stream().map(PoeMap::getMapName).collect(Collectors.toList());
    }

    public static PoeMapCombination generateMapCombination(List<String> mapNames){
        try {
            List<String> fixedMapNames = new ArrayList<>();
            mapNames.forEach((mapName) -> {
                if(mapName.startsWith("MapWorlds")){
                    fixedMapNames.add(mapName);
                } else {
                    fixedMapNames.add("MapWorlds" + mapName);
                }
            });
            PoeMapCombination poeMapCombination = new PoeMapCombination();
            poeMapCombination.setMaps(fixedMapNames);
            poeMapCombination.calculateEVs();
            return poeMapCombination;
        } catch (Exception e){
            System.out.println(mapNames);
            throw e;
        }
    }

    /**
     * Priority:
     * Overrides
     * Keep
     * Removal
     * Price Floor
     */
    public static void overridePriceList(){
        //Remove cards below the price floor
        divinationCards.values().forEach((card)->{
            if(card.getPrice()<Main.CARD_PRICE_FLOOR){
                card.setPrice(0);
            }
        });
        //Remove the included cards from the culling
        Main.excludeCards.removeAll(Main.includeCards);
        //Cull the bad (mispriced) cards
        Main.excludeCards.forEach((cardName) -> {
            divinationCards.get(cardName).setPrice(0);
        });
        //Apply the card value overrides
        List<CardValueOverride> cardValueOverrides = JsonUtils.readAllDivinationCardOverridesJson("Input/CardValueOverrides.json");
        cardValueOverrides.forEach((cvo) -> {
            divinationCards.get(cvo.getCardName()).setPrice(cvo.getCardValue());
        });
    }

    public static List<String> findOptimalMapEVWeightBestCards(){
        //Construct a TreeSet of all the maps ordered by EV/Weight
        List<String> optimalMapNames = new ArrayList<>();
        TreeSet<PoeMap> sortedPoeMaps = new TreeSet<>(new PoeMap.ValuePerWeightAndEVComparator());
        sortedPoeMaps.addAll(poeMaps.values());

        //Iterate through the sorted div cards and add the best map for the best divcard until 12 maps are added.
        //Also need to track that the card has not already been added.
        Set<DivinationCard> addedCards = new HashSet<>();
        int addedMaps = 0;
        Iterator cardIterator = allDivCardEVInfo.iterator();
        while(addedMaps<12){
            CardEVInfo currentCardEVInfo = (CardEVInfo) cardIterator.next();
            System.out.printf("Looking for map containing: %s\n", currentCardEVInfo.getCardName());
            if(!addedCards.contains(divinationCards.get(currentCardEVInfo.getCardName()))){
                //find the best map
                boolean foundBestMap = false;
                Iterator mapIterator = sortedPoeMaps.iterator();
                while(!foundBestMap) {
                    if(mapIterator.hasNext()){
                        PoeMap currentMap = (PoeMap) mapIterator.next();
                        if(Main.atlasMaps.contains(currentMap.getMapName()) && currentMap.getDivCardsAndSingleMapEV().containsKey(currentCardEVInfo.getCardName())){
                            //Found the best map
                            foundBestMap = true;
                            addedMaps++;
                            optimalMapNames.add(currentMap.getMapName());
                            currentMap.getDivCardsAndSingleMapEV().keySet().forEach((divCardName)->{
                                addedCards.add(divinationCards.get(divCardName));
                            });
                        }
                    } else {
                        System.out.printf("No map found containing: %s\n", currentCardEVInfo.getCardName());
                        foundBestMap = true;
                    }
                }
            }
        }
        JsonUtils.writeObjectToJsonFile(generateMapCombination(optimalMapNames), "Output/OptimalEVWeightMapReport.json");
        return optimalMapNames;
    }
}
