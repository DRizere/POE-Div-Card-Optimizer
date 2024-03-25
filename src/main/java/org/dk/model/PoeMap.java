package org.dk.model;

import com.google.gson.annotations.Expose;
import org.dk.DivCardService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.dk.Main.INPUT_CARD;
import static org.dk.Main.INPUT_CARD_PER_MAP;

public class PoeMap {
    @Expose
    private String mapName;

    private List<DivinationCard> cards = new ArrayList<>();

    private HashMap<String, CardEVInfo> divCardsAndSingleMapEV = new HashMap<>();

    //@Expose
    private List<CardEVInfo> divCardsAndSingleMapEVList = new ArrayList<>();
    @Expose
    private int totalRegularSingleMapEV;
    @Expose
    private int totalGildedScarabSingleMapEV;
    @Expose
    private int totalSingleMapWeight;
    @Expose
    private int totalWeightOfCards;

    public PoeMap(){

    }

    public PoeMap(PoeMap oldPoeMap){
        this.mapName = oldPoeMap.mapName;
        this.cards = oldPoeMap.cards;
        this.divCardsAndSingleMapEV = oldPoeMap.divCardsAndSingleMapEV;
        this.divCardsAndSingleMapEVList = oldPoeMap.divCardsAndSingleMapEVList;
        this.totalRegularSingleMapEV = oldPoeMap.totalRegularSingleMapEV;
        this.totalGildedScarabSingleMapEV = oldPoeMap.totalGildedScarabSingleMapEV;
        this.totalSingleMapWeight = oldPoeMap.totalSingleMapWeight;
        this.totalWeightOfCards = oldPoeMap.totalWeightOfCards;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public void setDivCardsAndSingleMapEV(HashMap<String, CardEVInfo> divCardsAndSingleMapEV) {
        this.divCardsAndSingleMapEV = divCardsAndSingleMapEV;
    }
    public HashMap<String, CardEVInfo> getDivCardsAndSingleMapEV() {
        return divCardsAndSingleMapEV;
    }

    public double getRegularTotalSingleMapEV() {
        AtomicReference<Double> totalRegularSingleMapEV = new AtomicReference<>((double) 0);
        divCardsAndSingleMapEV.forEach((dc, evInfo) -> {
            totalRegularSingleMapEV.updateAndGet(v -> v + evInfo.getRegularEV());
        });
        return totalRegularSingleMapEV.get();
    }


    public double getGildedScarabTotalSingleMapEV() {
        AtomicReference<Double> totalGildedScarabSingleMapEV = new AtomicReference<>((double) 0);
        divCardsAndSingleMapEV.forEach((dc, evInfo) -> {
            totalGildedScarabSingleMapEV.updateAndGet(v -> v + evInfo.getGildedDivScarabEV());
        });
        return totalGildedScarabSingleMapEV.get();
    }

    public void setGildedScarabTotalSingleMapEV(int gildedScarabTotalSingleMapEV) {
        this.totalGildedScarabSingleMapEV = gildedScarabTotalSingleMapEV;
    }

    public int getTotalSingleMapWeight() {
        return totalSingleMapWeight;
    }

    public List<DivinationCard> getCards() {
        return cards;
    }

    public void setCards(List<DivinationCard> cards) {
        this.cards = cards;
    }

    public int getTotalWeightOfCards() {
        if(totalWeightOfCards == 0){
            cards.forEach((card) -> {
                totalWeightOfCards += card.getWeight();
            });
        }
        return totalWeightOfCards;
    }

    public void calculateEVs(HashMap<String, DivinationCard> divinationCardHashMap){
        totalSingleMapWeight = DivCardService.GLOBAL_DROP_POOL_SIZE_NO_CARDS + this.getTotalWeightOfCards();

        int inputCardWeight = divinationCardHashMap.get(INPUT_CARD).getWeight();
        //Calculate number of dropped items
        double totalDroppedItems = (double) (INPUT_CARD_PER_MAP * totalSingleMapWeight) / inputCardWeight;
        cards.forEach((card) -> {
            DivinationCard thisCard = divinationCardHashMap.get(card.getName());
            //Calculate number of given card dropped per map
            double numberOfThisCardDropped = (double) thisCard.getWeight() / totalSingleMapWeight * totalDroppedItems;
            CardEVInfo cardEVInfo = new CardEVInfo();
            cardEVInfo.setCardName(card.getName());
            cardEVInfo.setRegularEV(numberOfThisCardDropped * thisCard.getPrice());
            cardEVInfo.setGildedDivScarabEV((thisCard.getPrice() * numberOfThisCardDropped) + (thisCard.getPrice() * (thisCard.getStack()-1)*0.2));
            divCardsAndSingleMapEV.put(thisCard.getName(), cardEVInfo);
        });
        this.getGildedScarabTotalSingleMapEV();
        this.getRegularTotalSingleMapEV();
        divCardsAndSingleMapEV.forEach((cardName, cardEvInfo) -> {
            divCardsAndSingleMapEVList.add(cardEvInfo);
        });
    }

    public static class TotalGildedScarabEVComparator implements Comparator<PoeMap> {
        @Override
        public int compare(PoeMap item1, PoeMap item2) {
            // Compare based on totalGildedScarabEV field
            return -1*Double.compare(item1.getGildedScarabTotalSingleMapEV(), item2.getGildedScarabTotalSingleMapEV());
        }
    }

    public static class ValuePerWeightComparator implements Comparator<PoeMap> {
        @Override
        public int compare(PoeMap item1, PoeMap item2) {
            // Compare based on totalGildedScarabEV field
            return -1*Double.compare(item1.getGildedScarabTotalSingleMapEV()*item1.cards.size()/item1.getTotalWeightOfCards(), item2.getGildedScarabTotalSingleMapEV()*item2.cards.size()/item2.getTotalWeightOfCards());
        }
    }
}
