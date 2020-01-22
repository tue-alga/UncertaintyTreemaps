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
public class StripTreeMapLookAheadUncertainty extends StripTreeMap {

    @Override
    public Map<DataMap, Rectangle> generateLevel(List<DataMap> children, Rectangle treeMapRectangle) {

        Map<DataMap, Rectangle> rectangleMapping = new HashMap<>();
        //factor which we need to scale the areas for each rectangle
        double scaleFactor = getScaleFactor(children, treeMapRectangle);

        //TODO CHECK IF IT SHOULD BE SORTED OR NOT
        //holds the area we still have to fill
        Rectangle remainingRectangle = treeMapRectangle;
        //holds the datamaps that still need to be layed out
        List<DataMap> remainingDataMaps = new LinkedList();
        remainingDataMaps.addAll(children);

        //holds the previous strip
        List<DataMap> prevStrip = null;

        //layout strips untill there are no more datamaps
        while (!remainingDataMaps.isEmpty()) {
            //gets the best strip
            List<DataMap> newStrip = getBestStrip(remainingDataMaps, remainingRectangle, scaleFactor);
            //remove the datamaps in the strip from the remainingDataMaps
            remainingDataMaps.removeAll(newStrip);

            //If it was the first strip there is no need to optimize
            if (prevStrip != null) {
                //Get the optimal strip distribution
                optimizeStrips(prevStrip, newStrip, remainingRectangle, scaleFactor);
                //we now have completed work with prevStrip so we can layout it out in the treemap

                //update the rectanglemapping according to the strip
                Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStrip, scaleFactor, remainingRectangle);
                rectangleMapping.putAll(rectanglesFromStrip);
            }
            if (!newStrip.isEmpty()) {
                if (prevStrip != null) {
                    Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStrip, scaleFactor, remainingRectangle);
                    rectangleMapping.putAll(rectanglesFromStrip);

                    //update the rectangle that we still need to fill
                    //we first get one of the rectangles(strip is filled and they all have the same width/height)
                    Rectangle[] stripRectangles = rectanglesFromStrip.values().toArray(new Rectangle[0]);
                    //calculate the new remaining rectangle based on the current remaining rectangle and the rectangles just created
                    remainingRectangle = getRemainingRectangle(remainingRectangle, stripRectangles);
                }
                prevStrip = newStrip;
            }

            if (remainingDataMaps.isEmpty()) {
                //this was the last strip, so also layout the newStrip
                Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(newStrip, scaleFactor, remainingRectangle);
                rectangleMapping.putAll(rectanglesFromStrip);
            }
        }
        return rectangleMapping;
    }

    /**
     * Checks if items in the newStrip can be better put in previous strip to
     * improve the maximum aspect ratio. Makes changes towards both prevStrip
     * and nextStrip
     *
     *
     * @param prevStrip
     * @param nextStrip
     * @return whether the next strip will have a horizontal layout
     */
    public void optimizeStrips(List<DataMap> prevStrip, List<DataMap> nextStrip, Rectangle remainingRectangle, double scaleFactor) {
        List<DataMap> prevStripCopy = new LinkedList();
        prevStripCopy.addAll(prevStrip);

        List<DataMap> nextStripCopy = new LinkedList();
        nextStripCopy.addAll(nextStrip);

        //Get the rectangle as it would we after the first strip is layout
        Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(prevStripCopy, scaleFactor, remainingRectangle);
        Rectangle remainingRectangleAfter = getRemainingRectangle(remainingRectangle, rectanglesFromStrip.values().toArray(new Rectangle[0]));

        //calculate the maximum aspect ratio and store it
        double bestRatio = Math.max(getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor),
                                    getStripAspectRatio(nextStripCopy, remainingRectangleAfter, scaleFactor));
        //holds the index of the best split
        int bestIndex = -1;

        //Find the best distribution
        for (int index = 0; index < nextStrip.size(); index++) {
            DataMap dm = nextStrip.get(index);
            prevStripCopy.add(dm);
            nextStripCopy.remove(dm);
            //Get the rectangle as it would we after the first strip is layout
            rectanglesFromStrip = getRectanglesFromStrip(prevStripCopy, scaleFactor, remainingRectangle);
            remainingRectangleAfter = getRemainingRectangle(remainingRectangle, rectanglesFromStrip.values().toArray(new Rectangle[0]));

            double ratio;
            if (nextStripCopy.isEmpty()) {
                ratio = getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor);
            } else {
                ratio = Math.max(getStripAspectRatio(prevStripCopy, remainingRectangle, scaleFactor),
                                 getStripAspectRatio(nextStripCopy, remainingRectangleAfter, scaleFactor));
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

    protected Map<DataMap, Rectangle> getRectanglesFromStrip(List<DataMap> strip, double scaleFactor, Rectangle remainingRectangle) {
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        double width = remainingRectangle.getWidth();
        //holds what the starting y of the strip is
        double endY = remainingRectangle.getY2();

        double stripSize = DataMap.getTotalSize(strip);
        double stripHeight = stripSize * scaleFactor / width;

        //holds what the starting x of the next item in the strip is
        double startX = remainingRectangle.getX();
        for (DataMap dm : strip) {
            double rWidth = dm.getTargetSize() / stripSize * width;
            Rectangle r = new Rectangle(startX, endY - stripHeight, rWidth, stripHeight);
            rectangleMapping.put(dm, r);
            startX += rWidth;
        }

        return rectangleMapping;
    }

    protected Rectangle getRemainingRectangle(Rectangle inputRectangle, Rectangle[] stripRectangles) {
        double stripHeight = stripRectangles[0].getHeight();
        return new Rectangle(inputRectangle.getX(),
                             inputRectangle.getY(),
                             inputRectangle.getWidth(),
                             inputRectangle.getHeight()-stripHeight
        );
    }
    
       
    @Override
    public String getParamaterDescription() {
        return "StripLookAheadUncertainty";
    }
}
