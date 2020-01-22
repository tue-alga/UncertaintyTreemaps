package TreeMapGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author max
 */
public class SpiralTreeMapLookAhead extends SpiralTreeMap {

    @Override
    public Map<DataMap, Rectangle> generateLevel(List<DataMap> children, Rectangle treeMapRectangle, Orientation orientation) {

        Map<DataMap, Rectangle> rectangleMapping = new HashMap<>();
        //factor which we need to scale the areas for each rectangle
        double scaleFactor = getScaleFactor(children, treeMapRectangle);

        //holds the area we still have to fill
        Rectangle remainingRectangle = treeMapRectangle;
        //holds the datamaps that still need to be layed out
        List<DataMap> remainingDataMaps = new LinkedList();
        remainingDataMaps.addAll(children);

        //holds the previous strip
        List<DataMap> prevStrip = null;
        //holds whether the previous strip had a horizontal layout
        Orientation prevOrientation = orientation;

        //layout strips untill there are no more datamaps
        while (!remainingDataMaps.isEmpty()) {
            //gets the newbest strip
            List<DataMap> newStrip = getBestStrip(remainingDataMaps, remainingRectangle, scaleFactor, orientation);
            //remove the datamaps in the strip from the remainingDataMaps
            remainingDataMaps.removeAll(newStrip);

            //If it was the first strip just store it
            if (prevStrip != null) {
                //Get the optimal strip distribution and store the orientation
                optimizeStrips(prevStrip, prevOrientation, newStrip, remainingRectangle, scaleFactor);

                if (!newStrip.isEmpty()) //we now have completed work with prevStrip so we can layout it out in the treemap
                {
                    //update the rectanglemapping according to the strip
                    Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStrip, scaleFactor, remainingRectangle, prevOrientation);
                    rectangleMapping.putAll(rectanglesFromStrip);

                    //update the rectangle that we still need to fill
                    //we first get one of the rectangles(strip is filled and they all have the same width/height)
                    Rectangle[] stripRectangles = rectanglesFromStrip.values().toArray(new Rectangle[0]);
                    //calculate the new remaining rectangle based on the current remaining rectangle and the rectangles just created
                    remainingRectangle = getRemainingRectangle(remainingRectangle, stripRectangles, prevOrientation);
                }
            }
            if (!newStrip.isEmpty()) {
                prevStrip = newStrip;
                prevOrientation = orientation;
                orientation = orientation.nextOrientation();
                if (remainingDataMaps.isEmpty()) {
                    //this was the last strip, so also layout the newStrip
                    Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(newStrip, scaleFactor, remainingRectangle, orientation);
                    rectangleMapping.putAll(rectanglesFromStrip);
                }
            } else {
                if (remainingDataMaps.isEmpty()) {
                    //this was the last strip, so also layout the newStrip
                    Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStrip, scaleFactor, remainingRectangle, prevOrientation);
                    rectangleMapping.putAll(rectanglesFromStrip);
                }
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
     */
    public void optimizeStrips(List<DataMap> prevStrip, Orientation prevOrientation, List<DataMap> nextStrip, Rectangle remainingRectangle, double scaleFactor) {
        List<DataMap> prevStripCopy = new LinkedList();
        prevStripCopy.addAll(prevStrip);

        List<DataMap> nextStripCopy = new LinkedList();
        nextStripCopy.addAll(nextStrip);

        Orientation newOrientation = prevOrientation.nextOrientation();
        //Get the rectangle as it would be after the first strip is layef out
        Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStripCopy, scaleFactor, remainingRectangle, prevOrientation);
        Rectangle remainingRectangleAfter = getRemainingRectangle(remainingRectangle, rectanglesFromStrip.values().toArray(new Rectangle[0]), newOrientation);

        //calculate the maximum aspect ratio and store it
        double bestRatio = Math.max(getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor, prevOrientation),
                getStripAspectRatio(nextStripCopy, remainingRectangleAfter, scaleFactor, newOrientation));
        //holds the index of the best split
        int bestIndex = -1;

        //Find the best distribution
        for (int index = 0; index < nextStrip.size(); index++) {
            DataMap dm = nextStrip.get(index);
            prevStripCopy.add(dm);
            nextStripCopy.remove(dm);
            //Get the rectangle as it would we after the first strip is layout
            rectanglesFromStrip = getRectanglesFromStrip(prevStripCopy, scaleFactor, remainingRectangle, prevOrientation);
            remainingRectangleAfter = getRemainingRectangle(remainingRectangle, rectanglesFromStrip.values().toArray(new Rectangle[0]), newOrientation);

            double ratio;
            if (nextStripCopy.isEmpty()) {
                ratio = getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor, prevOrientation);
            } else {
                ratio = Math.max(getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor, prevOrientation),
                        getStripAspectRatio(nextStripCopy, remainingRectangleAfter, scaleFactor, newOrientation));
            }
            if (ratio < bestRatio) {
                bestIndex = index;
                bestRatio = ratio;
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
    }

    private double getScaleFactor(List<DataMap> dataMapsInRectangle, Rectangle treeMapRectangle) {
        double totalSize = DataMap.getTotalSize(dataMapsInRectangle);
        double totalArea = treeMapRectangle.getWidth() * treeMapRectangle.getHeight();

        return totalArea / totalSize;
    }
}
