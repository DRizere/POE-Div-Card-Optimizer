package org.dk.model;

import com.google.gson.annotations.Expose;

import java.util.Comparator;

public class CardEVInfo {
    @Expose
    private String cardName;
    @Expose
    private double regularEV;
    @Expose
    private double gildedDivScarabEV;

    public CardEVInfo(){
    }
    public CardEVInfo(CardEVInfo old){
        this.setCardName(old.cardName);
        this.setRegularEV(old.regularEV);
        this.setGildedDivScarabEV(old.gildedDivScarabEV);
    }
    public double getRegularEV() {
        return regularEV;
    }

    public void setRegularEV(double regularEV) {
        this.regularEV = regularEV;
    }

    public double getGildedDivScarabEV() {
        return gildedDivScarabEV;
    }

    public void setGildedDivScarabEV(double gildedDivScarabEV) {
        this.gildedDivScarabEV = gildedDivScarabEV;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public static class TotalGildedScarabEVComparator implements Comparator<CardEVInfo> {
        @Override
        public int compare(CardEVInfo item1, CardEVInfo item2) {
            // Compare based on totalGildedScarabEV field
            int firstCompare = -1*Double.compare(
                    (int) (item1.getGildedDivScarabEV()),
                    (int) (item2.getGildedDivScarabEV()));
            if(firstCompare!=0){
                return firstCompare;
            } else {
                //tiebreak on alphabetical order
                return item1.getCardName().compareTo(item2.getCardName());
            }
        }
    }
}
