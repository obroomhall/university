package com.example.jengar.ui.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.example.jengar.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import static android.support.constraint.Constraints.TAG;
import static com.example.jengar.ui.main.ArrayHelper.concatenateArrays;
import static com.example.jengar.ui.main.ArrayHelper.splitArray;

public class BuildViewModel extends ViewModel implements ViewModelProvider.Factory {

    private int PADDING_INTER_BLOCK = 10;
    private int blocksPerRow;
    private int initialBlockCount;
    private List<Block[]> blocks;
    private boolean rotated = false;

    public BuildViewModel(int initialBlockCount, int blocksPerRow) {
        this.blocksPerRow = blocksPerRow;
        this.initialBlockCount = initialBlockCount;
        blocks = Collections.synchronizedList(new ArrayList<Block[]>());
        generateBlockIds();
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new BuildViewModel(initialBlockCount, blocksPerRow);
    }

    // In this class because https://stackoverflow.com/questions/50946050/should-viewmodel-class-contain-android-elements
    private void generateBlockIds() {

        int i = 0;
        while(i < initialBlockCount) {
            Block[] rowBlocks = new Block[blocksPerRow];
            for (int j = 0; j < rowBlocks.length; j++, i++) {
                if (i < initialBlockCount) {
                    rowBlocks[j] = new Block();
                } else {
                    rowBlocks[j] = null;
                }
            }
            blocks.add(rowBlocks);
        }

        Log.d(TAG, "generateBlockIds:");
        printBlocks();
    }



    public int getPaddingInterBlock() {
        return PADDING_INTER_BLOCK;
    }
    public void setPaddingInterBlock(int pad) {
        PADDING_INTER_BLOCK = pad;
    }

    public int getBlocksPerRow() {
        return blocksPerRow;
    }

    private void printBlocks() {
        Log.d(TAG, "printBlocks: --------------------------------------");
        for (Block[] row :
                blocks) {
            String blocksString = new String();
            for (Block block : row) {
                if (block == null) {
                    blocksString += "null\t";
                } else {
                    blocksString += block + "\t\t";
                }
            }
            Log.d(TAG, "printBlocks:\t\t\t" +  blocksString);
        }
        Log.d(TAG, "printBlocks: --------------------------------------");
    }

    private Pair<int[], Block> findBlockFromId(int id) {
        for (int i = blocks.size()-1; i >= 0; i--) {
            Block[] row = blocks.get(i);
            for (int j = 0; j < row.length; j++) {
                if (row[j] != null && row[j].getId() == id) {
                    int[] loc = new int[] {i,j};
                    Block block = row[j];
                    return new Pair<>(loc, block);
                }
            }
        }
        return null;
    }

    // adding more than blocksPerRow is handled
    public void moveBlock(Integer id) {

        // Find the blocks location
        Pair<int[], Block> found = findBlockFromId(id);
        int[] loc = found.first;
        Block blockToMove = found.second;

        // Get the blocks on top row
        Block[] row = blocks.get(blocks.size()-1);
        int blocksOnRow = 0;
        for (int i = 0; i < row.length; i++) {
            if (row[i] != null) {
                blocksOnRow++;
            }
        }

        // If top row full, add as new row
        if (blocksOnRow == row.length) {
            // Then add a row with new block and nulls
            Block[] newRow = new Block[row.length];
            newRow[0] = new Block(blockToMove);
            for (int i = 1; i < row.length; i++) {
                newRow[i] = null;
            }
            blocks.add(newRow);
        } else {


            // Add it to next null location
            boolean inserted = false;
            if (loc[0] == blocks.size()-1) {
                // If it was previously found on top row skip location and begin iterating at loc+1
                for (int i = 1; i < row.length; i++) {
                    if (row[i+loc[1]%row.length] == null) {
                        row[i+loc[1]%row.length] = new Block(blockToMove);
                        inserted = true;
                        break;
                    }
                }
            } else {

                // Insert into first available null location
                for (int i = 0; i < row.length; i++) {
                    if (row[i] == null) {
                        row[i] = new Block(blockToMove);
                        inserted = true;
                        break;
                    }
                }
            }
            assert (inserted); // logically can never not find a null location to insert

            blocks.set(blocks.size()-1, row);
        }

        // Remove previous instance from view
        blockToMove.toggleVisible();

        Log.d(TAG, "addBlockIds: after adding");
        printBlocks();
    }

    public List<Block[]> getBlocks() {
        return blocks;
    }

    public boolean isRotated() {
        return rotated;
    }

    public void toggleRotated() {
        this.rotated = !rotated;
    }

    public boolean isTopRowFullOrEmpty() {
        if (blocks.size() == 0) return true;
        Block[] topRow = blocks.get(blocks.size()-1);

        int blockCount = 0;
        for (Block block : topRow) {
            if (block != null && block.getId() > 0) {
                blockCount++;
            }
        }

        return (blockCount==0 || blockCount==blocksPerRow);
    }
}
