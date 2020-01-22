package TreeMapGenerator.HilbertMoore;

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
public class Quadrant {

    private final List<List<DataMap>> itemsInQuadrants;
    private List<Quadrant> subQuadrants;
    private boolean isLeafQuadrant;
    private Map<DataMap, Rectangle> rectangleMapping;

    private DataMap dm;

    private Rectangle position;

    public Quadrant(List<List<DataMap>> itemsInQuadrant) {
        this.itemsInQuadrants = itemsInQuadrant;
        subQuadrants = null;
        position = null;
    }

    public DataMap getDm() {
        return dm;
    }

    public double getSize() {
        return DataMap.getTotalSize(getAllItems());
    }

    public Map<DataMap, Rectangle> getRecursiveRectangleMapping() {
        if (isLeafQuadrant) {
            return rectangleMapping;
        } else {
            Map<DataMap, Rectangle> recursiveMapping = new HashMap();
            for (Quadrant subQuadrant : subQuadrants) {
                recursiveMapping.putAll(subQuadrant.getRecursiveRectangleMapping());
            }
            return recursiveMapping;
        }
    }

    public Map<DataMap, Rectangle> getRectangleMapping() {
        return rectangleMapping;
    }

    public void setRectangleMapping(Map<DataMap, Rectangle> rectangleMapping) {
        assert isLeafQuadrant;
        this.rectangleMapping = rectangleMapping;
    }

    public Rectangle getPosition() {
        return position;
    }

    public void setPosition(Rectangle position) {
        this.position = position;
    }

    public void setLeafQuadrant() {
        isLeafQuadrant = true;
    }

    public boolean isLeafQuadrant() {
        return isLeafQuadrant;
    }

    public boolean hasSubQuadrants() {
        if (subQuadrants == null || subQuadrants.isEmpty()) {
            return false;
        }
        return true;
    }

    public void verifyHasQuadrants() {
        if (!hasSubQuadrants()) {
            throw new IllegalStateException("There are no subquadrants");
        }
    }

    public void setSubQuadrants(List<Quadrant> subQuadrants) {
        this.subQuadrants = subQuadrants;
    }

    public List<Quadrant> getSubQuadrants() {
        return subQuadrants;
    }

    public Quadrant getQuadrantA() {
        verifyHasQuadrants();
        return subQuadrants.get(0);
    }

    public Quadrant getQuadrantB() {
        verifyHasQuadrants();
        return subQuadrants.get(1);
    }

    public Quadrant getQuadrantC() {
        verifyHasQuadrants();
        return subQuadrants.get(2);
    }

    public Quadrant getQuadrantD() {
        verifyHasQuadrants();
        return subQuadrants.get(3);
    }

    public List<DataMap> getAllItems() {
        List<DataMap> items = new LinkedList();
        for (List l : itemsInQuadrants) {
            items.addAll(l);
        }
        return items;
    }

    public List<List<DataMap>> getItemsInQuadrants() {
        return itemsInQuadrants;
    }

    public List<DataMap> getItemsInQuadrantA() {
        return itemsInQuadrants.get(0);
    }

    public List<DataMap> getItemsInQuadrantB() {
        return itemsInQuadrants.get(1);
    }

    public List<DataMap> getItemsInQuadrantC() {
        return itemsInQuadrants.get(2);
    }

    public List<DataMap> getItemsInQuadrantD() {
        return itemsInQuadrants.get(3);
    }

}
