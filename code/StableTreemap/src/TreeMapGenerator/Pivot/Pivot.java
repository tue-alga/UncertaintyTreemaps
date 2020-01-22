package TreeMapGenerator.Pivot;

import TreeMapGenerator.TreeMapGenerator;
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
public abstract class Pivot implements TreeMapGenerator {

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        List<TreeMap> children = new LinkedList();
        Map<DataMap, Rectangle> pivotTreeMap = pivotTreeMap(dataMap.getChildren(), treeMapRectangle);
        for (DataMap child : dataMap.getChildren()) {
            TreeMap tm = generateTreeMap(child, pivotTreeMap.get(child));
            children.add(tm);
        }

        TreeMap tm = new TreeMap(treeMapRectangle, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), children);
        return tm;
    }

    /**
     * Finds the node on which we are pivoting
     *
     * @param dataMapList
     * @return
     */
    public abstract int findPivotNode(List<DataMap> dataMapList);

    /**
     * Calculates where the rectangle for each DataMap on this level is located
     *
     * @param dataMapList
     * @param r
     * @return
     */
    public Map<DataMap, Rectangle> pivotTreeMap(List<DataMap> dataMapList, Rectangle r) {
        HashMap<DataMap, Rectangle> rectangleMapping = new HashMap();

        if (dataMapList == null || dataMapList.isEmpty()) {
            return rectangleMapping;
        }
        if (dataMapList.size() == 1) {
            rectangleMapping.put(dataMapList.get(0), r);
            return rectangleMapping;
        }

        int pivotIndex = findPivotNode(dataMapList);

        List<DataMap> l1 = dataMapList.subList(0, pivotIndex);
        DataMap pivot = dataMapList.get(pivotIndex);
        List<DataMap> remainder = dataMapList.subList(pivotIndex + 1, dataMapList.size());

        List<DataMap> l2 = new LinkedList();
        List<DataMap> l3 = new LinkedList();

        if (!remainder.isEmpty()) {
            //+1 is included since we want to split on that item and sublist
            //is not inclusive

            int index = findBestAspectRatio(pivot, l1, remainder, r);
            if (index != 0) {
                l2.addAll(remainder.subList(0, index));
            }

            if (index < (remainder.size())) {
                l3 = remainder.subList(index, remainder.size());
            }
        }

        List<Rectangle> rectangles = calculatePivotRectangles(pivot, l1, l2, l3, r);

        Rectangle pivotRectangle = rectangles.get(0);
        Map<DataMap, Rectangle> mappingl1 = pivotTreeMap(l1, rectangles.get(1));
        Map<DataMap, Rectangle> mappingl2 = pivotTreeMap(l2, rectangles.get(2));
        Map<DataMap, Rectangle> mappingl3 = pivotTreeMap(l3, rectangles.get(3));

        rectangleMapping.put(pivot, pivotRectangle);
        rectangleMapping.putAll(mappingl1);
        rectangleMapping.putAll(mappingl2);
        rectangleMapping.putAll(mappingl3);

        return rectangleMapping;
    }

    /**
     * returns the index of the element on which the list should be split to get
     * the best aspect ratio for the pivot
     *
     * @param pivot
     * @param l1
     * @param remainder
     * @param r
     * @return
     */
    protected int findBestAspectRatio(DataMap pivot, List<DataMap> l1, List<DataMap> remainder, Rectangle r) {
        double bestRatio = Double.MAX_VALUE;
        int indexOfBest = 0;

        List<DataMap> l2 = new LinkedList();
        List<DataMap> l3 = new LinkedList();
        l3.addAll(remainder);

        for (int i = 0; i <= remainder.size(); i++) {
            if (l3.size() == 1) {
                DataMap dm = remainder.get(i);

                l3.remove(dm);
                l2.add(dm);
                continue;
            }
            Rectangle pivotRectangle = calculatePivotRectangles(pivot, l1, l2, l3, r).get(0);
            double pivotW = pivotRectangle.getWidth();
            double pivotH = pivotRectangle.getHeight();
            double ratio = Math.max(pivotW / pivotH, pivotH / pivotW);
            if (ratio < bestRatio) {
                indexOfBest = i;
                bestRatio = ratio;
            }

            if (i != remainder.size()) {
                DataMap dm = remainder.get(i);

                l3.remove(dm);
                l2.add(dm);
            }
        }
        return indexOfBest;
    }

    /**
     * Calculates where the rectangles of the pivot item and the lists l1,l2 and
     * l3 should go and returns this in the order \{pivot,l1,l2,l3\}
     *
     * @param l1
     * @param l2
     * @param l3
     * @param pivotItem
     * @param r
     * @return
     */
    protected List<Rectangle> calculatePivotRectangles(DataMap pivotItem, List<DataMap> l1, List<DataMap> l2, List<DataMap> l3, Rectangle r) {
        /**
         * See http://hcil2.cs.umd.edu/trs/2001-06/2001-06.html for layout and
         * algo
         */
        Rectangle rectL1;
        Rectangle rectL2;
        Rectangle rectL3;
        Rectangle rectPivot;
        double xLeft = r.getX();
        double xRight = r.getX() + r.getWidth();
        double yTop = r.getY();
        double yBottom = r.getY() + r.getHeight();
        double height = r.getHeight();
        double width = r.getWidth();

        double totalSize = getSize(l1) + getSize(l2) + getSize(l3) + pivotItem.getTargetSize();

        if (r.getWidth() >= r.getHeight()) {
            //l1 goes left, pivot goes topMiddle, l2 goes below pivot,l3 goes right
            double leftWidth = getSize(l1) / totalSize * width;
            rectL1 = new Rectangle(xLeft, yTop, leftWidth, height);

            double rightWidth = getSize(l3) / totalSize * width;
            double midRight = xRight - rightWidth;
            rectL3 = new Rectangle(midRight, yTop, rightWidth, height);

            double midLeft = xLeft + leftWidth;
            double midWidth = midRight - midLeft;
            double midBottomHeight = (getSize(l2) / (getSize(l2) + pivotItem.getTargetSize())) * height;
            double midSplit = yBottom - midBottomHeight;
            rectL2 = new Rectangle(midLeft, midSplit, midWidth, midBottomHeight);

            double midTopHeight = height - midBottomHeight;
            rectPivot = new Rectangle(midLeft, yTop, midWidth, midTopHeight);
        } else {
            //l3 goes top, pivot goes right middle, l2 goes left middle, l1 goes bottom,

            double topHeight = getSize(l3) / totalSize * height;
            rectL3 = new Rectangle(xLeft, yTop, width, topHeight);

            double bottomHeight = getSize(l1) / totalSize * height;
            double midBottom = yBottom - bottomHeight;
            rectL1 = new Rectangle(xLeft, midBottom, width, bottomHeight);

            double midTop = yTop + topHeight;
            double midHeight = midBottom - midTop;
            double midLeftWidth = (getSize(l2) / (getSize(l2) + pivotItem.getTargetSize())) * width;
            rectL2 = new Rectangle(xLeft, midTop, midLeftWidth, midHeight);

            double midRightWidth = width - midLeftWidth;
            double midSplit = xLeft + midLeftWidth;
            rectPivot = new Rectangle(midSplit, midTop, midRightWidth, midHeight);
        }
        List<Rectangle> returnList = new LinkedList();
        returnList.add(rectPivot);
        returnList.add(rectL1);
        returnList.add(rectL2);
        returnList.add(rectL3);
        return returnList;
    }

    protected double getSize(List<DataMap> list) {
        return DataMap.getTotalSize(list);
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
