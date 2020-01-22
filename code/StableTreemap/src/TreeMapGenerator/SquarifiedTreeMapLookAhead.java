package TreeMapGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;

/**
 * Uses a lookahead for the
 *
 * @author max
 */
public class SquarifiedTreeMapLookAhead extends SquarifiedTreeMap {

    @Override
    public Map<DataMap, Rectangle> generateLevel(List<DataMap> childrenOriginal, Rectangle treeMapRectangle) {

        Map<DataMap, Rectangle> rectangleMapping = new HashMap<>();
        //factor which we need to scale the areas for each rectangle
        double scaleFactor = getScaleFactor(childrenOriginal, treeMapRectangle);

        List<DataMap> children = new ArrayList<>();
        children.addAll(childrenOriginal);

        //order the dataMap in decreasing order
        Collections.sort(children, (DataMap o1, DataMap o2) -> Double.compare(o2.getTargetSize(), o1.getTargetSize()));

        //holds the area we still have to fill
        Rectangle remainingRectangle = treeMapRectangle;
        //holds the datamaps that still need to be layed out
        List<DataMap> remainingDataMaps = new LinkedList();
        remainingDataMaps.addAll(children);

        //holds the previous strip
        List<DataMap> prevStrip = null;
        //holds whether the previous strip had a horizontal layout
        boolean prevHorizontal = false;

        //layout strips untill there are no more datamaps
        while (!remainingDataMaps.isEmpty()) {
            //holds whether the strip is horizontal or vertical
            boolean newHorizontal = isHorizontalLonger(remainingRectangle);
            //gets the best strip
            List<DataMap> newStrip = getBestStrip(remainingDataMaps, remainingRectangle, scaleFactor, newHorizontal);
            //remove the datamaps in the strip from the remainingDataMaps
            remainingDataMaps.removeAll(newStrip);

            //If it was the first strip just store it
            if (prevStrip != null) {
                //Get the optimal strip distribution and store the orientation
                newHorizontal = optimizeStrips(prevStrip, prevHorizontal, newStrip, remainingRectangle, scaleFactor);
                //we now have completed work with prevStrip so we can layout it out in the treemap
                //update the rectanglemapping according to the strip
                Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStrip, scaleFactor, remainingRectangle, prevHorizontal);
                rectangleMapping.putAll(rectanglesFromStrip);
                if (!newStrip.isEmpty()) {

                    //update the rectangle that we still need to fill
                    //we first get one of the rectangles(strip is filled and they all have the same width/height)
                    Rectangle[] stripRectangles = rectanglesFromStrip.values().toArray(new Rectangle[0]);
                    //calculate the new remaining rectangle based on the current remaining rectangle and the rectangles just created
                    remainingRectangle = getRemainingRectangle(remainingRectangle, stripRectangles, prevHorizontal);
                }
            }
            if (!newStrip.isEmpty()) {
                prevStrip = newStrip;
                prevHorizontal = newHorizontal;
            }

            if (remainingDataMaps.isEmpty()) {
                //this was the last strip, so also layout the newStrip
                Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(newStrip, scaleFactor, remainingRectangle, newHorizontal);
                rectangleMapping.putAll(rectanglesFromStrip);
            }
        }
        //layout the last strip 
        //generate the strips and re
        return rectangleMapping;
    }

    /**
     * Checks if items in the newStrip can be better put in previous strip to
     * improve the maximum aspect ratio. Makes changes towards both prevStrip
     * and nextStrip
     *
     * Assumes that the treemapRectangle is from the previous one
     *
     * @param prevStrip
     * @param nextStrip
     * @return whether the next strip will have a horizontal layout
     */
    public boolean optimizeStrips(List<DataMap> prevStrip, boolean prevHorizontal, List<DataMap> nextStrip, Rectangle remainingRectangle, double scaleFactor) {
        List<DataMap> prevStripCopy = new LinkedList();
        prevStripCopy.addAll(prevStrip);

        List<DataMap> nextStripCopy = new LinkedList();
        nextStripCopy.addAll(nextStrip);

        //Get the rectangle as it would we after the first strip is layout
        Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStripCopy, scaleFactor, remainingRectangle, prevHorizontal);
        Rectangle remainingRectangleAfter = getRemainingRectangle(remainingRectangle, rectanglesFromStrip.values().toArray(new Rectangle[0]), prevHorizontal);

        //calculate the maximum aspect ratio and store it
        //holds the best orientation for the best split
        boolean bestOrientation = isHorizontalLonger(remainingRectangleAfter);
        double bestRatio = Math.max(getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor, prevHorizontal),
                                    getStripAspectRatio(nextStripCopy, remainingRectangleAfter, scaleFactor, bestOrientation));
        //holds the index of the best split
        int bestIndex = -1;

        //Find the best distribution
        for (int index = 0; index < nextStrip.size(); index++) {
            DataMap dm = nextStrip.get(index);
            prevStripCopy.add(dm);
            nextStripCopy.remove(dm);
            //Get the rectangle as it would we after the first strip is layout
            rectanglesFromStrip = getRectanglesFromStrip(prevStripCopy, scaleFactor, remainingRectangle, prevHorizontal);
            remainingRectangleAfter = getRemainingRectangle(remainingRectangle, rectanglesFromStrip.values().toArray(new Rectangle[0]), prevHorizontal);
            boolean nextHorizontal = isHorizontalLonger(remainingRectangleAfter);

            double ratio;
            if (nextStripCopy.isEmpty()) {
                ratio = getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor, prevHorizontal);
            } else {
                ratio = Math.max(getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor, prevHorizontal),
                                 getStripAspectRatio(nextStripCopy, remainingRectangleAfter, scaleFactor, nextHorizontal));
            }
            if (ratio < bestRatio) {
                bestIndex = index;
                bestRatio = ratio;
                bestOrientation = nextHorizontal;
            }
        }
        //adhere to the distribution
        if (bestIndex != -1) {
            for (int index = 0; index < bestIndex + 1; index++) {
                DataMap dm = nextStrip.get(0);
                prevStrip.add(dm);
                nextStrip.remove(dm);
            }

        }
        //return the orientation
        return bestOrientation;
    }

    @Override
    public String getParamaterDescription() {
        return "squarifiedLookAhead";
    }
}
