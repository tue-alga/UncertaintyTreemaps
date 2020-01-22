package TreeMapGenerator;

import java.util.LinkedList;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class SliceAndDice implements TreeMapGenerator {

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        return generateLevelHorizontal(treeMapRectangle, dataMap);
    }

    public TreeMap generateLevelHorizontal(Rectangle treeMapRectangle, DataMap dataMap) {
        Rectangle areaLeft = treeMapRectangle;
        double height = treeMapRectangle.getHeight();
        double y = treeMapRectangle.getY();

        double totalSize = dataMap.getTargetSize();

        LinkedList<TreeMap> children = new LinkedList();
        if (dataMap.hasChildren()) {
            for (DataMap child : dataMap.getChildren()) {
                double size = child.getTargetSize();
                double width = (size / totalSize) * treeMapRectangle.getWidth();
                double x = areaLeft.getX();

                Rectangle childArea = new Rectangle(x, y, width, height);

                TreeMap childTreeMap = generateLevelVertical(childArea, child);
                children.add(childTreeMap);

                areaLeft = new Rectangle(x + width, y, areaLeft.getWidth() - width, height);
            }
        }

        TreeMap tm = new TreeMap(treeMapRectangle, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), children);
        return tm;
    }

    public TreeMap generateLevelVertical(Rectangle treeMapRectangle, DataMap dataMap) {
        Rectangle areaLeft = treeMapRectangle;
        double width = treeMapRectangle.getWidth();
        double x = treeMapRectangle.getX();

        double totalSize = dataMap.getTargetSize();

        LinkedList<TreeMap> children = new LinkedList();
        if (dataMap.hasChildren()) {
            for (DataMap child : dataMap.getChildren()) {

                double size = child.getTargetSize();
                double height = (size / totalSize) * treeMapRectangle.getHeight();
                double y = areaLeft.getY();

                Rectangle childArea = new Rectangle(x, y, width, height);

                TreeMap childTreeMap = generateLevelHorizontal(childArea, child);
                children.add(childTreeMap);

                areaLeft = new Rectangle(x, y + height, width, areaLeft.getHeight() - height);

            }
        }

        TreeMap tm = new TreeMap(treeMapRectangle, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), children);
        return tm;
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
