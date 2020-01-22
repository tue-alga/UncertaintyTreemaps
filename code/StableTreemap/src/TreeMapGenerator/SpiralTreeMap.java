package TreeMapGenerator;

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
public class SpiralTreeMap implements TreeMapGenerator {

    @Override
    public String getParamaterDescription() {
        return "";
    }

    public enum Orientation {

        TOP, RIGHT, BOTTOM, LEFT;

        public Orientation nextOrientation() {
            if (this == Orientation.TOP) {
                return Orientation.RIGHT;
            }
            if (this == Orientation.RIGHT) {
                return Orientation.BOTTOM;
            }
            if (this == Orientation.BOTTOM) {
                return Orientation.LEFT;
            }
            if (this == Orientation.LEFT) {
                return Orientation.TOP;
            }
            //never happens
            return null;
        }
    };

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        List<TreeMap> children = new LinkedList();
        //If the dataMap has children we will recurse into them to generate
        //those treeMaps first
        if (dataMap.hasChildren()) {
            //get the rectangles for all the children according to the strip layout
            Map<DataMap, Rectangle> rectangleMapping = generateLevel(dataMap.getChildren(), treeMapRectangle, Orientation.TOP);

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

    public Map<DataMap, Rectangle> generateLevel(List<DataMap> children, Rectangle treeMapRectangle, Orientation orientation) {
        Map<DataMap, Rectangle> rectangleMapping = new HashMap<>();

        double totalSize = DataMap.getTotalSize(children);
        double totalArea = treeMapRectangle.getWidth() * treeMapRectangle.getHeight();

        //factor which we need to scale the areas for each rectangle
        double scaleFactor = totalArea / totalSize;

        Rectangle remainingRectangle = treeMapRectangle;

        List<DataMap> remainingDataMaps = new LinkedList();
        remainingDataMaps.addAll(children);

        while (!remainingDataMaps.isEmpty()) {
            //generate a new strip
            List<DataMap> bestStrip = getBestStrip(remainingDataMaps, treeMapRectangle, scaleFactor, orientation);

            //figure out the psotitioning of the stirps
            Map<DataMap, Rectangle> rectanglesFromStrip = getRectanglesFromStrip(bestStrip, scaleFactor, remainingRectangle, orientation);
            rectangleMapping.putAll(rectanglesFromStrip);

            //update the remaining rectangle
            Rectangle[] stripRectangles = rectanglesFromStrip.values().toArray(new Rectangle[0]);
            remainingRectangle = getRemainingRectangle(remainingRectangle, stripRectangles, orientation);

            //prepare for the next strip
            remainingDataMaps.removeAll(bestStrip);
            orientation = orientation.nextOrientation();
        }
        //generate the strips and return them
        return rectangleMapping;
    }

    protected List<DataMap> getBestStrip(List<DataMap> remainingDataMaps, Rectangle remainingRectangle, double scaleFactor, Orientation orientation) {
        //holds the new strip
        List<DataMap> strip = new LinkedList();

        for (DataMap dm : remainingDataMaps) {
            //check if adding the next rectangle would degenerate the aspect ratio
            List<DataMap> newStrip = new LinkedList();
            newStrip.addAll(strip);
            newStrip.add(dm);
            double currentAspectRatio = getStripAspectRatio(strip, remainingRectangle, scaleFactor, orientation);
            double newAspectRatio = getStripAspectRatio(newStrip, remainingRectangle, scaleFactor, orientation);

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

    protected Rectangle getRemainingRectangle(Rectangle initialRectangle, Rectangle[] stripRectangles, Orientation orientation) {

        double x = initialRectangle.getX();
        double y = initialRectangle.getY();
        double width = initialRectangle.getWidth();
        double height = initialRectangle.getHeight();

        //if it was filled horizontally we only need to change in the y direction
        //if not we only need to change in the x direction
        if (orientation == Orientation.TOP) {
            //all items in the list have the same height
            double fillHeight = stripRectangles[0].getHeight();
            y += fillHeight;
            height -= fillHeight;
        }
        if (orientation == Orientation.LEFT) {
            //all items in the list have the same width
            double fillWidth = stripRectangles[0].getWidth();
            x += fillWidth;
            width -= fillWidth;
        }
        if (orientation == Orientation.RIGHT) {
            //all items in the list have the same width
            double fillWidth = stripRectangles[0].getWidth();
            width -= fillWidth;
        }
        if (orientation == Orientation.BOTTOM) {
            //all items in the list have the same width
            double fillHeight = stripRectangles[0].getHeight();
            height -= fillHeight;
        }
        return new Rectangle(x, y, width, height);
    }

    protected Map<DataMap, Rectangle> getRectanglesFromStrip(List<DataMap> dmList, double scaleFactor, Rectangle treeMapRectangle, Orientation orientation) {
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();
        //holds the side that is of a fixed side for the entire strip
        double side;
        double totalArea = DataMap.getTotalSize(dmList);
        if (orientation == Orientation.BOTTOM || orientation == Orientation.TOP) {
            //calculate the height
            side = totalArea * scaleFactor / treeMapRectangle.getWidth();
        } else {
            //calculate the width
            side = totalArea * scaleFactor / treeMapRectangle.getHeight();
        }

        double x;
        double y;
        if (orientation == Orientation.TOP) {
            x = treeMapRectangle.getX();
            y = treeMapRectangle.getY();
        } else if (orientation == Orientation.BOTTOM) {
            x = treeMapRectangle.getX() + treeMapRectangle.getWidth();
            y = treeMapRectangle.getY() + treeMapRectangle.getHeight() - side;
        } else if (orientation == Orientation.RIGHT) {
            x = treeMapRectangle.getX() + treeMapRectangle.getWidth() - side;
            y = treeMapRectangle.getY();
        } else {//orientation == Orientation.LEFT
            x = treeMapRectangle.getX();
            y = treeMapRectangle.getY() + treeMapRectangle.getHeight();
        }

        for (DataMap dm : dmList) {
            //calculates how much of the strip this dataMap takes up
            double otherSide = dm.getTargetSize() * scaleFactor / side;
            Rectangle r;
            //creates a rectangle and moves the startposition further to
            //accomodate the next rectangle
            if (orientation == Orientation.TOP) {
                r = new Rectangle(x, y, otherSide, side);
                x += otherSide;
            } else if (orientation == Orientation.BOTTOM) {
                r = new Rectangle(x - otherSide, y, otherSide, side);
                x -= otherSide;
            } else if (orientation == Orientation.RIGHT) {
                r = new Rectangle(x, y, side, otherSide);
                y += otherSide;
            } else {//(orientation == Orientation.LEFT) 
                r = new Rectangle(x, y - otherSide, side, otherSide);
                y -= otherSide;
            }
            rectangleMapping.put(dm, r);
        }
        return rectangleMapping;
    }

    protected double getStripAspectRatio(List<DataMap> strip, Rectangle treeMapRectangle, double scaleFactor, Orientation orientation) {

        if (strip.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double totalSize = DataMap.getTotalSize(strip);

        //holds how long the fixed side is
        double side;

        if (orientation == Orientation.BOTTOM || orientation == Orientation.TOP) {
            side = totalSize * scaleFactor / treeMapRectangle.getWidth();
        } else {
            side = totalSize * scaleFactor / treeMapRectangle.getHeight();
        }

        double maxAspectRatio = 0;
        for (DataMap dm : strip) {
            double otherSide = dm.getTargetSize() * scaleFactor / side;
            double aspectRatio = Math.max(side / otherSide, otherSide / side);
            maxAspectRatio = Math.max(aspectRatio, maxAspectRatio);
        }
        return maxAspectRatio;
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
