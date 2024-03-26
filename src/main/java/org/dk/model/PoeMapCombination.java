package org.dk.model;

import com.google.gson.annotations.Expose;
import org.dk.DivCardService;

import java.util.*;

import static org.dk.Main.INPUT_CARD;
import static org.dk.Main.INPUT_CARD_PER_MAP;

public class PoeMapCombination {
    @Expose
    private List<String> poeMapNames = new ArrayList<>();

    private Set<DivinationCard> cards = new HashSet<>();

    //@Expose
    private HashMap<DivinationCard, CardEVInfo> divCardsAndSingleMapEV = new HashMap<>();
    @Expose
    private TreeSet<CardEVInfo> cardEVInfos = new TreeSet<>(new CardEVInfo.TotalGildedScarabEVComparator());
    @Expose
    private int totalWeightOfCards;
    @Expose
    private int totalWeight;
    @Expose
    private int totalRegularEV;
    @Expose
    private int totalGildedScarabEV;
    public List<String> getMaps() {
        return poeMapNames;
    }

    public void setMaps(List<String> poeMaps) {
        this.poeMapNames = poeMaps;
    }

    public int getRegularEV() {
        if(totalRegularEV== 0){
            divCardsAndSingleMapEV.forEach((dc, evInfo) -> {
                totalRegularEV += evInfo.getRegularEV();
            });
        }
        return totalRegularEV;
    }

    public void setTotalGildedScarabEV(int totalGildedScarabEV){
        this.totalGildedScarabEV = totalGildedScarabEV;
    }

    public int getGildedScarabTotalEV() {
        if(totalGildedScarabEV== 0){
            divCardsAndSingleMapEV.forEach((dc, evInfo) -> {
                totalGildedScarabEV += evInfo.getGildedDivScarabEV();
            });
        }
        return totalGildedScarabEV;
    }

    public int getTotalWeightOfCards() {
        this.getCards();
        if(totalWeightOfCards == 0){
            cards.forEach((card) -> {
                totalWeightOfCards += card.getWeight();
            });
        }
        return totalWeightOfCards;
    }

    public Set<DivinationCard> getCards() {
        if(cards.isEmpty()){
            poeMapNames.forEach((poeMapName) -> {
                //System.out.println(poeMapName);
                cards.addAll(DivCardService.poeMaps.get(poeMapName).getCards());
            });
        }
        return cards;
    }

    public HashMap<DivinationCard, CardEVInfo> getDivCardsAndSingleMapEV() {
        return divCardsAndSingleMapEV;
    }

    public void setDivCardsAndSingleMapEV(HashMap<DivinationCard, CardEVInfo> divCardsAndSingleMapEV) {
        this.divCardsAndSingleMapEV = divCardsAndSingleMapEV;
    }

    public void calculateEVs(){
        totalWeight = DivCardService.GLOBAL_DROP_POOL_SIZE_NO_CARDS + this.getTotalWeightOfCards();

        int inputCardWeight = DivCardService.divinationCards.get(INPUT_CARD).getWeight();
        //Calculate number of dropped items
        double totalDroppedItems = (double) (INPUT_CARD_PER_MAP * totalWeight) / inputCardWeight;
        cards.forEach((card) -> {
            DivinationCard thisCard = DivCardService.divinationCards.get(card.getName());
            //Calculate number of given card dropped per map
            double numberOfThisCardDropped = (double) thisCard.getWeight() / totalWeight * totalDroppedItems;
            CardEVInfo cardEVInfo = new CardEVInfo();
            cardEVInfo.setCardName(card.getName());
            cardEVInfo.setRegularEV(numberOfThisCardDropped * thisCard.getPrice());
            double thisCardGildedDivScarabValue = (thisCard.getPrice()) + (thisCard.getPrice() * (thisCard.getStack()-1)*0.2);
            cardEVInfo.setGildedDivScarabEV(thisCardGildedDivScarabValue * numberOfThisCardDropped);
            divCardsAndSingleMapEV.put(thisCard, cardEVInfo);
        });
        this.getGildedScarabTotalEV();
        this.getRegularEV();
        cardEVInfos.addAll(divCardsAndSingleMapEV.values());
    }

    public class TotalGildedScarabEVComparator implements Comparator<PoeMapCombination> {
        @Override
        public int compare(PoeMapCombination item1, PoeMapCombination item2) {
            // Compare based on totalGildedScarabEV field
            return -1*Double.compare(item1.getGildedScarabTotalEV(), item2.getGildedScarabTotalEV());
        }
    }
}
