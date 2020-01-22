/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.LocalChanges.Search;

import java.util.PriorityQueue;

/**
 *
 * @author msondag
 */
public class SearchStructure {

    public int totalConsidered = 1;

    private final int itemsPerLevel = 4;

    //oeg,movesleft,innermaximalsegments,maxAspectRatio
    private PriorityQueue<SearchItem> currentOptions;
    private PriorityQueue<SearchItem> newOptions;

    public SearchStructure(PriorityQueue<SearchItem> initialQueue) {
        currentOptions = initialQueue;
        newOptions = new PriorityQueue<>();
    }

    public SearchItem getNext() {
        if (currentOptions.isEmpty()) {
            updateOptions();
        }

        return currentOptions.poll();

    }

    public void insertOption(SearchItem option) {
        newOptions.add(option);
        totalConsidered++;
    }

    private void updateOptions() {
        //Only keep the best options

        //put the best {itemsPerLevel} options in the new queue
        currentOptions = new PriorityQueue<>();
        for (int i = 1; i <= itemsPerLevel; i++) {
            if (!newOptions.isEmpty()) {
                currentOptions.add(newOptions.poll());
            }
        }

        //reset the new options for the next level
        newOptions = new PriorityQueue<>();
    }
}
