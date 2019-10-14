package com.example.jengar.ui.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.example.jengar.R;
import com.tekle.oss.android.animation.AnimationFactory;

import java.util.List;

public class BuildFragment extends Fragment {

    private int table_padding_horizontal = 102;//100;//203;//310;
    private int table_padding_vertical = 10000/table_padding_horizontal;
    private int block_height_side = -1;
    private int block_width_side = -1;
    private int block_height_end = -1;
    private int block_width_end = -1;

    private BuildViewModel vm;
    private TableLayout table;

    public void setZoom(int pad) {
        table.setPadding(pad, 10000/pad, pad, 10000/pad);
        table.requestFocus();
    }

    public static BuildFragment newInstance() {
        BuildFragment bf = new BuildFragment();
        bf.setArguments(new Bundle());
        return bf;
    }

    public static BuildFragment newInstance(int blocks, int blocksPerRow) {
        BuildFragment bf = new BuildFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("blocks", blocks);
        bundle.putInt("blocksPerRow", blocksPerRow);
        bf.setArguments(bundle);
        return bf;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.build_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        int blockCount = args.getInt("blocks", 54);
        int blocksPerRow = args.getInt("blocksPerRow", 3);
        vm = ViewModelProviders.of(this, new BuildViewModel(blockCount, blocksPerRow)).get(BuildViewModel.class);
    }

    @Override
    public void onViewCreated(@Nullable final View view, @Nullable Bundle savedInstanceState) {
        table = view.findViewById(R.id.table);
        table.setPadding(table_padding_horizontal, table_padding_vertical, table_padding_horizontal, table_padding_vertical);
        // Create the observer which updates the UI.
        final Observer<String> towerObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String newName) {
                drawTower();
            }
        };

        //vm.getCurrentName().observe(this, towerObserver);

        // Must run after view created because accessing elements in viewmodel
        view.post(new Runnable() {
            @Override
            public void run() {
                drawTower();
            }
        });
    }

    private void drawTower() {
        TableRow[] rows = getTowerRows(vm.getBlocks());
        for (int i = rows.length-1; i >= 0; i--) {
            table.addView(rows[i]);
        }
    }

    private boolean isRowOfTypeEnd(int row) {
        return (row % 2 == (vm.isRotated() ? 1 : 0));
    }

    private TableRow[] getTowerRows(List<Block[]> blocks) {

        TableRow[] rows = new TableRow[blocks.size()];

        for (int i = 0; i < rows.length; i++) {
            rows[i] = getTowerRow(i, blocks.get(i));
        }

        return rows;
    }

    private TableRow getTowerRow(int i, Block[] blocks) {
        TableRow row = new TableRow(getContext());

        if (isRowOfTypeEnd(i)) {
            addBlocksToRow(row, blocks);
        } else {
            FrameLayout fLayout = new FrameLayout(row.getContext());
            TableRow.LayoutParams trLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            trLayout.span = vm.getBlocksPerRow();
            trLayout.weight = 1;
            fLayout.setLayoutParams(trLayout);
            row.addView(fLayout);
            addBlocksToRow(fLayout, blocks);
        }

        return row;
    }



    private void addBlocksToRow(ViewGroup vg, Block[] blocks) {

        assert (vg.getChildCount() + blocks.length < vm.getBlocksPerRow()+1);
        assert (vg instanceof FrameLayout || vg instanceof TableRow);

        boolean isSideBlock = (vg instanceof FrameLayout);
        int padding = vm.getPaddingInterBlock();
        int nullCounter = 0;

        // Add blocks to the table row
        for (Block block : blocks) {

            ImageView iv = new ImageView(vg.getContext());
            iv.setAdjustViewBounds(true);
            iv.setPaddingRelative(padding, padding, padding, padding);

            if (block != null && block.getId() > 0)  {
                iv.setId(block.getId());
                iv.setImageResource((isSideBlock) ? block.getSideDrawable() : block.getEndDrawable());
                iv.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Toast toast = Toast.makeText(v.getContext(), "You clicked button " + v.getId(), Toast.LENGTH_SHORT);
                        toast.show();
                        removeBlock((ImageView) v);
                    }
                });
            } else {

                if (block_height_side > 0) {
                    iv.setImageResource(android.R.color.transparent);
                    if (isSideBlock) {
                        iv.setMaxHeight(block_height_side);
                        iv.setMaxWidth(block_width_side);
                    } else {
                        iv.setMaxHeight(block_height_end);
                        iv.setMaxWidth(block_width_end);
                    }
                }
                nullCounter++;
            }

            vg.addView(iv);
        }

        // Don't display empty rows
        if (nullCounter == vm.getBlocksPerRow()) {
            vg.removeAllViews();
        }
    }

    public boolean rotateTower() {
        vm.toggleRotated();
        table.removeAllViews();
        drawTower();

        AnimationFactory.FlipDirection dir;
        if (vm.isRotated()) {
            dir = AnimationFactory.FlipDirection.LEFT_RIGHT;
        } else {
            dir = AnimationFactory.FlipDirection.RIGHT_LEFT;
        }

        ViewFlipper vf = getView().findViewById(R.id.viewFlipper);
        TableLayout transitionTower = new TableLayout(getContext());
        AnimationFactory.flipTransition(vf, transitionTower, dir, 100);
        return vm.isRotated();
    }

    public void removeBlock(ImageView iv) {

        if (block_height_end < 0) {
            int rows = table.getChildCount();

            TableRow tr = (TableRow) table.getChildAt(rows-1);
            ImageView endBlock = (ImageView) tr.getChildAt(0);
            block_height_end = endBlock.getHeight();
            block_width_end = endBlock.getWidth();

            TableRow tr2 = (TableRow) table.getChildAt(rows-2);
            FrameLayout fl = (FrameLayout) tr2.getChildAt(0);
            ImageView sideBlock = (ImageView) fl.getChildAt(0);
            block_height_side = sideBlock.getHeight();
            block_width_side = sideBlock.getWidth();
        }

        vm.moveBlock(iv.getId());
        table.removeAllViews();
        drawTower();
    }
}
