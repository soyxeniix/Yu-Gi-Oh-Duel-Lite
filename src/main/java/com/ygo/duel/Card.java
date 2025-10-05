package com.ygo.duel;

public class Card {
    private final String name;
    private final int atk;
    private final int def;
    private final String imageUrlSmall;
    private final String imageUrl; // full

    public Card(String name, int atk, int def, String imageUrlSmall, String imageUrl) {
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public String getImageUrlSmall() { return imageUrlSmall; }
    public String getImageUrl() { return imageUrl; }

    @Override
    public String toString() {
        return name + " [ATK " + atk + " / DEF " + def + "]";
    }
}