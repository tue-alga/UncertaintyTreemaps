package TreeMapGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class SquarifiedTreeMapUncertainty implements TreeMapGenerator {

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        List<TreeMap> children = new LinkedList();

        //If the dataMap has children we will recurse into them to generate
        //those treeMaps first
        if (dataMap.hasChildren()) {
            //get the rectangles for all the children according to the strip layout
            Map<DataMap, Rectangle> rectangleMapping = generateLevel(dataMap.getChildren(), treeMapRectangle);

            //recurse into the children and generate the rectangles for them
            //Add the children treemaps to the list
            for (DataMap dm : dataMap.getChildren()) {
                Rectangle r = rectangleMapping.get(dm);
                TreeMap child = generateTreeMap(dm, r);
                children.add(child);
            }
        }
        TreeMap tm = new TreeMap(treeMapRectangle, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), children);
        return tm;
    }

    protected boolean isHorizontalLonger(Rectangle r) {
        return r.getWidth() > r.getHeight();
    }

    public double getScaleFactor(List<DataMap> dataMapsInRectangle, Rectangle treeMapRectangle) {
        double totalSize = DataMap.getTotalSize(dataMapsInRectangle);
        double totalArea = treeMapRectangle.getWidth() * treeMapRectangle.getHeight();

        return totalArea / totalSize;
    }

    public Map<DataMap, Rectangle> generateLevel(List<DataMap> children, Rectangle treeMapRectangle) {

        Map<DataMap, Rectangle> rectangleMapping = new HashMap<>();
        //factor which we need to scale the areas for each rectangle
        double scaleFactor = getScaleFactor(children, treeMapRectangle);

        //order the dataMap in decreasing order
        Collections.sort(children, (DataMap o1, DataMap o2) -> Double.compare(o2.getTargetSize(), o1.getTargetSize()));

        //holds the area we still have to fill
        Rectangle remainingRectangle = treeMapRectangle;
        //holds the datamaps that still need to be layed out
        List<DataMap> remainingDataMaps = new LinkedList();
        remainingDataMaps.addAll(children);

        while (!remainingDataMaps.isEmpty()) {
            //holds whether the strip is horizontal or vertical
            boolean layoutHorizontal = isHorizontalLonger(remainingRectangle);
            //gets the best strip
            List<DataMap> strip = getBestStrip(remainingDataMaps, remainingRectangle, scaleFactor, layoutHorizontal);
            //update the rectanglemapping according to the strip
            Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(strip, scaleFactor, remainingRectangle, layoutHorizontal);
            rectangleMapping.putAll(rectanglesFromStrip);
            //update the rectangle that we still need to fill

            //we first get one of the rectangles(strip is filled and they all have the same width/height)
            Rectangle[] stripRectangles = rectanglesFromStrip.values().toArray(new Rectangle[0]);
            //calculate the new remaining rectangle based on the current remaining rectangle and the rectangles just created
            remainingRectangle = getRemainingRectangle(remainingRectangle, stripRectangles, layoutHorizontal);

            //remove the datamaps in the strip from the remainingDataMaps
            remainingDataMaps.removeAll(strip);
        }
        //generate the strips and return them
        return rectangleMapping;
    }

    /**
     * @pre RemainingDataMaps is sorted from high to low
     * @param remainingDataMaps
     * @param remainingRectangle
     * @param scaleFactor
     * @return
     */
    protected List<DataMap> getBestStrip(List<DataMap> remainingDataMaps, Rectangle remainingRectangle, double scaleFactor, boolean layoutHorizontal) {

        //if the rectangle has more space horizontally we use a horizontal layout
        //otherwise we use a vertical layout
        //holds the new strip
        List<DataMap> strip = new LinkedList();

        for (DataMap dm : remainingDataMaps) {
            //check if adding the next rectangle would degenerate the aspect ratio
            List<DataMap> newStrip = new LinkedList();
            newStrip.addAll(strip);
            newStrip.add(dm);
            double currentAspectRatio = getStripAspectRatio(strip, remainingRectangle, scaleFactor, layoutHorizontal);
            double newAspectRatio = getStripAspectRatio(newStrip, remainingRectangle, scaleFactor, layoutHorizontal);

            if (newAspectRatio <= currentAspectRatio) {
                //aspect ratio improved so we add it
                strip.add(dm);
            } else {
                //adding does not improve it anymore so we return
                break;
            }
        }
        return strip;
    }

    protected Rectangle getRemainingRectangle(Rectangle initialRectangle, Rectangle[] stripRectangles, boolean filledHorizontal) {

        double x = initialRectangle.getX();
        double y = initialRectangle.getY();
        double width = initialRectangle.getWidth();
        double height = initialRectangle.getHeight();

        //if it was filled horizontally we only need to change in the y direction
        //if not we only need to change in the x direction
        if (filledHorizontal) {
            //all items in the list have the same height
            double fillHeight = stripRectangles[0].getHeight();
//            y += fillHeight;
            height -= fillHeight;
        } else {
            //all items in the list have the same width
            double fillWidth = stripRectangles[0].getWidth();
            x += fillWidth;
            width -= fillWidth;
        }
        return new Rectangle(x, y, width, height);
    }

    protected Map<DataMap, Rectangle> getRectanglesFromStrip(List<DataMap> dmList, double scaleFactor, Rectangle treeMapRectangle, boolean layoutHorizontal) {
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        //holds the side that is of a fixed side for the entire strip
        double side;
        double totalArea = DataMap.getTotalSize(dmList);
        if (layoutHorizontal) {
            //calculate the height
            side = totalArea * scaleFactor / treeMapRectangle.getWidth();
        } else {
            //calculate the width
            side = totalArea * scaleFactor / treeMapRectangle.getHeight();
        }

        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double endY = treeMapRectangle.getY2();
        for (DataMap dm : dmList) {
            //calculates how much of the strip this dataMap takes up
            double otherSide = dm.getTargetSize() * scaleFactor / side;
            Rectangle r;
            //creates a rectangle and moves the startposition further to
            //accomodate the next rectangle
            if (layoutHorizontal) {
                r = new Rectangle(startX, endY-side, otherSide, side);
                startX += otherSide;
            } else {
                r = new Rectangle(startX, startY, side, otherSide);
                startY += otherSide;
            }
            rectangleMapping.put(dm, r);
        }
        return rectangleMapping;
    }

    protected double getStripAspectRatio(List<DataMap> strip, Rectangle treeMapRectangle, double scaleFactor, boolean isHorizontal) {
        if (strip.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double totalSize = DataMap.getTotalSize(strip);

        //holds how long the fixed side is
        double side;

        if (isHorizontal) {
            side = totalSize * scaleFactor / treeMapRectangle.getWidth();
        } else {
            side = totalSize * scaleFactor / treeMapRectangle.getHeight();
        }

        double sumAspectRatio = 0;
        for (DataMap dm : strip) {
            double otherSide = dm.getTargetSize() * scaleFactor / side;
            double aspectRatio = Math.max(side / otherSide, otherSide / side);
            sumAspectRatio += aspectRatio;
        }
        return sumAspectRatio / strip.size();
    }

    @Override
    public String getParamaterDescription() {
        return "squarifiedUncertainty";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
