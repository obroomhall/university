package com.example.jengar.ui.main;

import com.example.jengar.MainActivity;
import org.junit.jupiter.api.*;

import java.util.List;

class BuildViewModelTest extends MainActivity {

    private final int blocksPerRow = 3;
    private BuildViewModel vm;

    @BeforeEach
    void setUp() {
        vm = new BuildViewModel(0, blocksPerRow);
    }

    @AfterEach
    void tearDown() {
        vm = null;
    }

    @Nested
    class isRowFullOrEmptyTest {

        @Test
        void isRowFullOrEmptyTest_Empty() {
            vm.addBlockIds(getSuccessiveReals(0));
            assert (vm.isTopRowFullOrEmpty());
        }

        @Test
        void isRowFullOrEmptyTest_Full() {
            vm.addBlockIds(getSuccessiveReals(blocksPerRow));
            assert (vm.isTopRowFullOrEmpty());
        }

        @Test
        void isRowFullOrEmptyTest_Single() {
            vm.addBlockIds(getSuccessiveReals(1));
            assert !(vm.isTopRowFullOrEmpty());
        }

        @Test
        void isRowFullOrEmptyTest_HalfFull() {
            vm.addBlockIds(getSuccessiveReals(blocksPerRow/2));
            assert !(vm.isTopRowFullOrEmpty());
        }

        @Test
        void isRowFullOrEmptyTest_OverFull() {
            vm.addBlockIds(getSuccessiveReals(blocksPerRow+1));
            assert !(vm.isTopRowFullOrEmpty());
        }
    }

    @Nested
    class addBlocksTest {

        private List<Integer[]> blocks;

        @BeforeEach
        void setUp() {
            blocks = vm.getBlocks();
        }

        @Test
        void addBlocksTest_None() {
            vm.addBlockIds(getSuccessiveReals(0));
            assert (blocks.size() == 0);
        }

        @Test
        void addBlocksTest_FullRow() {
            vm.addBlockIds(getSuccessiveReals(blocksPerRow));
            assert (blocks.size() == 1 && blocks.get(blocks.size()-1).length == blocksPerRow);
        }

        @Test
        void addBlocksTest_Single() {
            vm.addBlockIds(getSuccessiveReals(1));
            assert (blocks.size() == 1 && blocks.get(blocks.size()-1).length == 1);
        }

        @Test
        void addBlocksTest_HalfRow() {
            vm.addBlockIds(getSuccessiveReals(blocksPerRow/2));
            assert (blocks.size() == 1 && blocks.get(blocks.size()-1).length == blocksPerRow/2);
        }

        @Test
        void addBlocksTest_OverfullRow() {
            vm.addBlockIds(getSuccessiveReals(blocksPerRow+1));
            assert (blocks.size() == 2 && blocks.get(blocks.size()-1).length == 1 && blocks.get(blocks.size()-2).length == blocksPerRow );
        }
    }

    private Integer[] getSuccessiveReals(int n) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i+1;
        }
        return arr;
    }
}