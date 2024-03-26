package org.dk.model;

import com.google.gson.annotations.Expose;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            int decimalPlaces = 3; // Number of decimal places to keep

            // Create BigDecimal object from double
            BigDecimal bdFirst = BigDecimal.valueOf(item1.getGildedDivScarabEV());
            BigDecimal bdSecond = BigDecimal.valueOf(item2.getGildedDivScarabEV());

            // Round the number
            bdFirst = bdFirst.setScale(decimalPlaces, RoundingMode.HALF_UP);
            bdSecond = bdSecond.setScale(decimalPlaces, RoundingMode.HALF_UP);

            int firstCompare = -1*Double.compare(
                    bdFirst.doubleValue(),
                    bdSecond.doubleValue());
            if(firstCompare!=0){
                return firstCompare;
            } else {
                //tiebreak on alphabetical order
                return item1.getCardName().compareTo(item2.getCardName());
            }
        }
    }
}

