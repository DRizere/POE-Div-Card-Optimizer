package org.dk.model;

import com.google.gson.annotations.Expose;

import java.util.List;

public class DivinationCard {
    private String art;
    private DropInfo drop;
    @Expose
    private String name;
    private String ninja;
    @Expose
    private double price;
    @Expose
    private String reward;
    @Expose
    private int stack;
    private double standardPrice;
    @Expose
    private int weight;

    public DivinationCard(){

    }

    public DivinationCard(DivinationCard source){
        this.art = source.art;
        this.drop = source.drop;
        this.name = source.name;
        this.ninja = source.ninja;
        this.price = source.price;
        this.reward = source.reward;
        this.stack = source.stack;
        this.standardPrice = source.standardPrice;
        this.weight = source.weight;
    }


    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public DropInfo getDrop() {
        return drop;
    }

    public void setDrop(DropInfo DropInfo) {
        this.drop = DropInfo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNinja() {
        return ninja;
    }

    public void setNinja(String ninja) {
        this.ninja = ninja;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
    }

    public double getStandardPrice() {
        return standardPrice;
    }

    public void setStandardPrice(double standardPrice) {
        this.standardPrice = standardPrice;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public static class DropInfo {
        private List<String> areas;
        private int min_level;
        private int max_level;
        private List<String> monsters;
        private String text;

        public List<String> getAreas() {
            return areas;
        }

        public void setAreas(List<String> areas) {
            this.areas = areas;
        }

        public int getMin_level() {
            return min_level;
        }

        public void setMin_level(int min_level) {
            this.min_level = min_level;
        }

        public List<String> getMonsters() {
            return monsters;
        }

        public void setMonsters(List<String> monsters) {
            this.monsters = monsters;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getMax_level() {
            return max_level;
        }

        public void setMax_level(int max_level) {
            this.max_level = max_level;
        }
    }
}
