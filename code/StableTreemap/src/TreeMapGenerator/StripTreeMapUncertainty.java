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
public class StripTreeMapUncertainty implements TreeMapGenerator {

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

    public Map<DataMap, Rectangle> generateLevel(List<DataMap> children, Rectangle treeMapRectangle) {
        //TODO IMPLEMENT LOOKAHEAD STRIPS
        double totalSize = DataMap.getTotalSize(children);
        double totalArea = treeMapRectangle.getWidth() * treeMapRectangle.getHeight();

        //factor which we need to scale the areas for each rectangle
        double scaleFactor = totalArea / totalSize;

        //holds all the strips
        List<List<DataMap>> stripList = new LinkedList();

        List<DataMap> remainingDataMaps = new LinkedList();
        remainingDataMaps.addAll(children);
        while (!remainingDataMaps.isEmpty()) {
            List<DataMap> bestStrip = getBestStrip(remainingDataMaps, treeMapRectangle, scaleFactor);
            stripList.add(bestStrip);
            remainingDataMaps.removeAll(bestStrip);
        }
        //generate the strips and return them
        Map<DataMap, Rectangle> rectangleMapping = generateStrip(stripList, treeMapRectangle, scaleFactor);
        return rectangleMapping;
    }

    protected List<DataMap> getBestStrip(List<DataMap> remainingDataMaps, Rectangle remainingRectangle, double scaleFactor) {
        //holds the new strip
        List<DataMap> strip = new LinkedList();

        for (DataMap dm : remainingDataMaps) {
            //check if adding the next rectangle would degenerate the aspect ratio
            List<DataMap> newStrip = new LinkedList();
            newStrip.addAll(strip);
            newStrip.add(dm);
            double currentAspectRatio = getStripAspectRatio(strip, remainingRectangle, scaleFactor);
            double newAspectRatio = getStripAspectRatio(newStrip, remainingRectangle, scaleFactor);

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

    protected Map<DataMap, Rectangle> generateStrip(List<List<DataMap>> stripList, Rectangle treeMapRectangle, double scaleFactor) {
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        double width = treeMapRectangle.getWidth();
        
        //start at the bottom so the larger nodes are at the bottom.
        double endY = treeMapRectangle.getY2();
        for (List<DataMap> strip : stripList) {
            double stripSize = DataMap.getTotalSize(strip);
            double stripHeight = stripSize * scaleFactor / width;

            //holds what the starting x of the next item in the strip is
            double startX = treeMapRectangle.getX();
            for (DataMap dm : strip) {
                double rWidth = dm.getTargetSize() / stripSize * width;
                Rectangle r = new Rectangle(startX, endY-stripHeight, rWidth, stripHeight);
                rectangleMapping.put(dm, r);
                startX += rWidth;
            }
            endY -= stripHeight;
        }
        return rectangleMapping;
    }

    protected double getStripAspectRatio(List<DataMap> strip, Rectangle treeMapRectangle, double scaleFactor) {
        if (strip.size() == 0) {
            return Double.MAX_VALUE;
        }

        double totalSize = DataMap.getTotalSize(strip);
        double height = totalSize * scaleFactor / treeMapRectangle.getWidth();

        double sumAspectRatio = 0;
        for (DataMap dm : strip) {
            double width = dm.getTargetSize() * scaleFactor / height;
            double aspectRatio = Math.max(width / height, height / width);
            sumAspectRatio += aspectRatio;
        }
        return sumAspectRatio / strip.size();
    }

    public double getScaleFactor(List<DataMap> dataMapsInRectangle, Rectangle treeMapRectangle) {
        double totalSize = DataMap.getTotalSize(dataMapsInRectangle);
        double totalArea = treeMapRectangle.getWidth() * treeMapRectangle.getHeight();

        return totalArea / totalSize;
    }

    @Override
    public String getParamaterDescription() {
        return "StripUncertainty";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
