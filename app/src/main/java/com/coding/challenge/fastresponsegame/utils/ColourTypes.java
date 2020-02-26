package com.coding.challenge.fastresponsegame.utils;

import android.content.Context;

import com.coding.challenge.fastresponsegame.R;
import com.coding.challenge.fastresponsegame.utils.data.ColourEntry;

public class ColourTypes {

    private static ColourTypes instance;
    private Context context;

    private static ColourEntry[] colours;
    private static int index = 0;

    public static int COLOUR_BLUE = 0;
    public static int COLOUR_RED = 1;
    public static int COLOUR_GREEN = 2;
    public static int COLOUR_PURPLE = 3;

    private ColourTypes() {}

    public static ColourTypes getInstance(Context context) {
        if (instance == null)
            instance = new ColourTypes(context);
        return instance;
    }

    private ColourTypes(Context ctx) {
        this.context = ctx;
        index = 0;

        colours = new ColourEntry[] {
                new ColourEntry(COLOUR_BLUE, context.getString(R.string.arrow_colour_blue), context.getColor(R.color.arrowBlue)),
                new ColourEntry(COLOUR_RED, context.getString(R.string.arrow_colour_red), context.getColor(R.color.arrowRed)),
                new ColourEntry(COLOUR_GREEN, context.getString(R.string.arrow_colour_green), context.getColor(R.color.arrowGreen)),
                new ColourEntry(COLOUR_PURPLE, context.getString(R.string.arrow_colour_purple), context.getColor(R.color.arrowPurple))
        };
    }

    public String getCurrentColourName() {
        return colours[index].getColourName();
    }

    public int getCurrentColourId() {
        return colours[index].getColourResourceId();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
