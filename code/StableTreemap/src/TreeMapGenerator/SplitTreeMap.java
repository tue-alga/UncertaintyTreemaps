/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author msondag
 */
public class SplitTreeMap implements TreeMapGenerator {

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle inputR) {
        TreeMap returnTreeMap;
        if (!dataMap.hasChildren()) {
            //base case, we do not have to recurse anymore
            returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), null);
            return returnTreeMap;
        }

        //generate the rectangle positions for each child
        Map<DataMap, Rectangle> mapping = generateLevel(dataMap.getChildren(), inputR);
        //recursively go through the children to generate all treemaps
        List<TreeMap> treeChildren = new ArrayList();
        for (DataMap dm : mapping.keySet()) {
            TreeMap tm = generateTreeMap(dm, mapping.get(dm));
            treeChildren.add(tm);
        }

        returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), treeChildren);
        return returnTreeMap;
    }

    /**
     *
     * Generate the current level. Recursively split the datamap into two
     * roughly equal sized partition while preservering the order. Cuts the
     * inputR over the longest side
     *
     * @param dataMaps
     * @param inputR
     * @return
     */
    private Map<DataMap, Rectangle> generateLevel(List<DataMap> dataMaps, Rectangle inputR) {

        double totalSize = DataMap.getTotalSize(dataMaps);
        int splitPoint = findBestSplitPoint(dataMaps);

        List<DataMap> list1 = new ArrayList(dataMaps.subList(0, splitPoint));
        List<DataMap> list2 = new ArrayList(dataMaps.subList(splitPoint, dataMaps.size()));
        
        //We now have the two lists of elements we are spliting in. 
        //We now distribute them over 2 subRectangle
        Rectangle r1, r2;
        double lengthPercentageR1 = DataMap.getTotalSize(list1) / totalSize;

        double x1 = inputR.getX();
        double y1 = inputR.getY();
        double height = inputR.getHeight();
        double width = inputR.getWidth();

        if (inputR.getHeight() >= inputR.getWidth()) {
            r1 = new Rectangle(x1, y1, width, lengthPercentageR1 * height);
            r2 = new Rectangle(x1, y1 + lengthPercentageR1 * height, width, height - lengthPercentageR1 * height);
        } else {
            r1 = new Rectangle(x1, y1, lengthPercentageR1 * width, height);
            r2 = new Rectangle(x1 + lengthPercentageR1 * width, y1, width - lengthPercentageR1 * width, height);
        }

        //recursively map the rectangles. If the size of the list equals 1
        //we are in the basecase and the mapping is known
        Map<DataMap, Rectangle> mapping = new HashMap();
        if (list1.size() == 1) {
            mapping.put(list1.get(0), r1);
        } else if (list1.size() > 1) {
            mapping.putAll(generateLevel(list1, r1));
        }

        if (list2.size() == 1) {
            mapping.put(list2.get(0), r2);
        } else if (list2.size() > 1) {
            mapping.putAll(generateLevel(list2, r2));
        }

        return mapping;
    }

    /**
     * find the index which splits the list into two parts of roughly equal size
     *
     * @param dataMapList
     * @return
     */
    private int findBestSplitPoint(List<DataMap> dataMaps) {
        int splitPoint = 0;
        double lastRatio = Double.MAX_VALUE;
        //split dataMaps into two roughty equal size
        for (int i = 1; i <= dataMaps.size(); i++) {
            double sizeLeft = DataMap.getTotalSize(dataMaps.subList(0, i));
            double sizeRight = DataMap.getTotalSize(dataMaps.subList(i, dataMaps.size()));
            double ratio = Math.max(sizeLeft / sizeRight, sizeRight / sizeLeft);
            //ratio will always improve untill we get to the splitpoint
            if (ratio >= lastRatio) {
                splitPoint = i-1;
                break;
            }
            lastRatio = ratio;
        }
        return splitPoint;
    }

    @Override
    public String getParamaterDescription() {
        return "Split";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }

}
