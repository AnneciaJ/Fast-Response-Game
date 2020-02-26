package com.coding.challenge.fastresponsegame.utils.data;

public class ColourEntry {

    private int id;
    private String colourName;
    private int colourResourceId;

    public ColourEntry(int id, String colourName, int colourResourceId) {
        this.id = id;
        this.colourName = colourName;
        this.colourResourceId = colourResourceId;
    }

    public int getId() {
        return id;
    }

    public String getColourName() {
        return colourName;
    }

    public int getColourResourceId() {
        return colourResourceId;
    }
}
