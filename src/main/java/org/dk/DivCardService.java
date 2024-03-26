package org.dk;

import org.dk.model.CardValueOverride;
import org.dk.model.DivinationCard;
import org.dk.model.PoeMap;
import org.dk.model.PoeMapCombination;
import org.dk.util.JsonUtils;

import java.util.*;
import java.util.stream.Collectors;

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
}
