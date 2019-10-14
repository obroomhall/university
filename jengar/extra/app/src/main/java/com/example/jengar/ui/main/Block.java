package com.example.jengar.ui.main;

import android.view.View;

import com.example.jengar.R;

public class Block {

    private static final int[] blockEndIds = {R.drawable.jenga_end_1, R.drawable.jenga_end_2, R.drawable.jenga_end_3, R.drawable.jenga_end_4,R.drawable.jenga_end_5, R.drawable.jenga_end_6, R.drawable.jenga_end_7, R.drawable.jenga_end_8, R.drawable.jenga_end_9 };
    private static final int[] blockSideIds = {R.drawable.jenga_side};

    private int id;
    private int sideDrawable;
    private int endDrawable;

    public Block() {
        id = View.generateViewId();
        sideDrawable = getRandomIntFromArray(blockSideIds);
        endDrawable = getRandomIntFromArray(blockEndIds);
    }

    public Block(Block block) {
        id = block.getId();
        sideDrawable = block.getSideDrawable();
        endDrawable = block.getEndDrawable();
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public int getId() {
        return id;
    }

    public int getSideDrawable() {
        return sideDrawable;
    }

    public int getEndDrawable() {
        return endDrawable;
    }

    public void toggleVisible() {
        id *= -1;
    }

    private int getRandomIntFromArray(int[] arr) {
        return arr[(int) (Math.random() * arr.length)];
    }
}
