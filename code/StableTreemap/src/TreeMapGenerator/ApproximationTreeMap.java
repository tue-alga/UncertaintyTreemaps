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
 * @author max
 */
public class ApproximationTreeMap implements TreeMapGenerator {

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle inputR) {
        TreeMap returnTreeMap;
        if (!dataMap.hasChildren()) {
            //base case, we do not have to recurse anymore
            returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(), dataMap.getSd(), null);
            return returnTreeMap;
        }

        List<DataMap> children = new ArrayList<>();
        children.addAll(dataMap.getChildren());
        //sort the datamaps based on their size
        Collections.sort(children, (DataMap o1, DataMap o2) -> Double.compare(o2.getTargetSize(), o1.getTargetSize()));

        //generate the rectangle positions for each child
        Map<DataMap, Rectangle> mapping = generateLevel(children, inputR);
        //recursively go through the children to generate all treemaps
        List<TreeMap> treeChildren = new ArrayList();
        for (DataMap dm : mapping.keySet()) {
            TreeMap tm = generateTreeMap(dm, mapping.get(dm));
            treeChildren.add(tm);
        }

        returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(), dataMap.getSd(), treeChildren);
        return returnTreeMap;
    }

    /**
     * DataMap is sorted on size
     *
     * @param dataMaps
     * @param inputR
     * @return
     */
    protected Map<DataMap, Rectangle> generateLevel(List<DataMap> dataMaps, Rectangle inputR) {

        double totalSize = DataMap.getTotalSize(dataMaps);
        List<DataMap> list1 = new ArrayList();
        List<DataMap> list2 = new ArrayList();

        //if any node has a weight more than 1/3 of total we split on that node
        DataMap lastElement = dataMaps.get(dataMaps.size() - 1);
        int splitPoint;

        if (lastElement.getTargetSize() >= totalSize / 3) {
            splitPoint = dataMaps.size() - 1;
        } else {
            //otherwise there must be a set of elements whose sum is more than 1/3
            //and we split on that node
            splitPoint = findSplitPoint(totalSize, dataMaps);
        }
        list1.addAll(dataMaps.subList(0, splitPoint));
        list2.addAll(dataMaps.subList(splitPoint, dataMaps.size()));
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
    private int findSplitPoint(double totalSize, List<DataMap> dataMapList) {
        double currentSize = 0;
        for (int i = 0; i < dataMapList.size(); i++) {
            if (currentSize >= totalSize / 3) {
                return i;
            }
            currentSize += dataMapList.get(i).getTargetSize();
        }
        //Should not happen
        return -1;
    }

    @Override
    public String getParamaterDescription() {
        return "approximation";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }

}
