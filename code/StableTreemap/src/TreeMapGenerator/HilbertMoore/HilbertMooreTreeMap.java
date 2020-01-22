package TreeMapGenerator.HilbertMoore;

import static TreeMapGenerator.HilbertMoore.HilbertMooreTreeMap.Corner.*;
import static TreeMapGenerator.HilbertMoore.HilbertMooreTreeMap.CurveOrientation.*;
import TreeMapGenerator.TreeMapGenerator;
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
public abstract class HilbertMooreTreeMap implements TreeMapGenerator {
//Enhanced spatial stability with Hilbert and Moore treemaps Susanne Tak, Andy Cockburn

    public enum Corner {

        NORTHEAST, SOUTHEAST, SOUTHWEST, NORTHWEST
    }

    public enum CurveOrientation {

        LEFTTOPCLOCK, RIGHTTOPCLOCK, LEFTBOTTOMCLOCK, RIGHTBOTTOMCLOCK,
        LEFTTOPCOUNTER, RIGHTTOPCOUNTER, LEFTBOTTOMCOUNTER, RIGHTBOTTOMCOUNTER
    }

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        List<TreeMap> children = new LinkedList();

        if (dataMap.hasChildren()) {
            List<DataMap> dmChildren = dataMap.getChildren();

            Quadrant topQuadrant = calculateQuadrants(dmChildren);
            topQuadrant.setPosition(treeMapRectangle);
            layOutQuadrants(topQuadrant, LEFTTOPCLOCK, NORTHEAST);

            layoutQuadrantItems(topQuadrant);

            Map<DataMap, Rectangle> rectangleMapping = topQuadrant.getRecursiveRectangleMapping();

            for (DataMap dm : dataMap.getChildren()) {
                TreeMap child = generateTreeMap(dm, rectangleMapping.get(dm));
                children.add(child);
            }
        }
        TreeMap tm = new TreeMap(treeMapRectangle, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(),dataMap.getSd(), children);
        return tm;
    }

    /**
     * Layout the subquadrants according to a certain pattern
     *
     * @param q the quadrant from which we are going to layout the subquadrants
     * from
     * @param orientation, the orientation of the curve above
     * @param corner the corner that this quadrant was in in the previous level
     */
    protected abstract void layOutQuadrants(Quadrant q, CurveOrientation orientation, Corner corner);

    protected Quadrant calculateQuadrants(List<DataMap> dataMaps) {
        List<List<DataMap>> quadrantItems = divideIntoEqualWeight(dataMaps);
        Quadrant q = new Quadrant(quadrantItems);

        if (q.getAllItems().size() <= 4) {
            q.setLeafQuadrant();
            return q;
        }

        Quadrant qa = calculateQuadrants(q.getItemsInQuadrantA());
        Quadrant qb = calculateQuadrants(q.getItemsInQuadrantB());
        Quadrant qc = calculateQuadrants(q.getItemsInQuadrantC());
        Quadrant qd = calculateQuadrants(q.getItemsInQuadrantD());

        List<Quadrant> quadrantList = new LinkedList();
        quadrantList.add(qa);
        quadrantList.add(qb);
        quadrantList.add(qc);
        quadrantList.add(qd);

        q.setSubQuadrants(quadrantList);
        return q;
    }

    protected List<List<DataMap>> divideIntoEqualWeight(List<DataMap> dataMaps) {
        assert (!dataMaps.isEmpty());

        double totalWeight = DataMap.getTotalSize(dataMaps);
        List<List<DataMap>> quadrants = new LinkedList();
        List<DataMap> quadrant = new LinkedList();

        //put the first item in the first datamap as it always need to have
        //an items. Cleans up the code inside the loop
        quadrant.add(dataMaps.get(0));

        for (int i = 1; i < dataMaps.size(); i++) {
            DataMap dm = dataMaps.get(i);
            //Check if adding one quadrant brings the weight of the quadrant
            //closer to a quarter
            //Furthermore we can only have 4 quadrants, so if we have 4 we should
            //always add it
            double originalWeight = DataMap.getTotalSize(quadrant);
            double targetWeight = totalWeight / 4;
            double offAmountOriginal = Math.abs(targetWeight - originalWeight);
            double copyWeight = originalWeight + dm.getTargetSize();
            double offAmountCopy = Math.abs(targetWeight - copyWeight);

            int remainingQuadrants = 4 - (quadrants.size() + 1);
            int remainingItems = dataMaps.size() - i;
            //We have to make sure that there are exactly 4 quadrants

            if (((offAmountOriginal > offAmountCopy) && (remainingItems > remainingQuadrants))) {
                //adding the dataMap comes closer to the ratio
                quadrant.add(dm);
            } else {
                //adding the dataMap brings us further from the 1/4 ratio or
                //we need to put each following in a new quadrant as we would
                //otherwise have unneccesary empty quadrants.
                quadrants.add(quadrant);
                quadrant = new LinkedList();
                if (remainingQuadrants != 1) {
                    quadrant.add(dm);
                } else {
                    //no more quadrants remaining so we can add all of the remaining
                    //items
                    quadrant.addAll(dataMaps.subList(i, dataMaps.size()));
                    break;
                }
            }
        }
        quadrants.add(quadrant);
        return quadrants;
    }

    protected void layoutQuadrantItems(Quadrant q) {

        //only the lowest level quadrants contain items that need to be layed out
        if (q.hasSubQuadrants()) {
            //recurse into the subquadrants and return
            for (Quadrant subQuadrant : q.getSubQuadrants()) {
                layoutQuadrantItems(subQuadrant);
            }
            return;
        }
        //we are at the lowest level, so we calculate how the items should
        //be positioned
        Map<DataMap, Rectangle> optimalLayout = LayoutGenerator.getOptimalLayout(q.getPosition(), q.getAllItems());
        q.setRectangleMapping(optimalLayout);

    }

    //List of methods used by both Hilbert and Moore TreeMaps to layout the items
    protected void updateByCorners(Rectangle treeMapRectangle, Quadrant leftBottom, Quadrant leftTop, Quadrant rightTop, Quadrant rightBottom) {

        double totalSize = leftBottom.getSize() + leftTop.getSize() + rightTop.getSize() + rightBottom.getSize();
        double totalArea = treeMapRectangle.getArea();
        double scaleFactor = totalArea / totalSize;

        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double height = leftTop.getSize() / (leftBottom.getSize() + leftTop.getSize()) * treeMapRectangle.getHeight();
        double width = leftTop.getSize() * scaleFactor / height;
        Rectangle r = new Rectangle(startX, startY, width, height);
        leftTop.setPosition(r);

        startY += height;
        height = treeMapRectangle.getHeight() - height;
        r = new Rectangle(startX, startY, width, height);
        leftBottom.setPosition(r);

        startX += width;
        startY = treeMapRectangle.getY();
        width = treeMapRectangle.getWidth() - width;
        height = rightTop.getSize() * scaleFactor / width;
        r = new Rectangle(startX, startY, width, height);
        rightTop.setPosition(r);

        startY += height;
        height = treeMapRectangle.getHeight() - height;
        r = new Rectangle(startX, startY, width, height);
        rightBottom.setPosition(r);
    }

    protected void layoutLeftTopClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |1 2|
         |4 3|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qd, qa, qb, qc);

        layOutQuadrants(qa, LEFTTOPCLOCK, NORTHWEST);
        layOutQuadrants(qb, LEFTTOPCLOCK, NORTHEAST);
        layOutQuadrants(qc, LEFTTOPCLOCK, SOUTHEAST);
        layOutQuadrants(qd, LEFTTOPCLOCK, SOUTHWEST);
    }

    protected void layoutRightTopClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |4 1|
         |3 2|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qc, qd, qa, qb);

        layOutQuadrants(qd, RIGHTTOPCLOCK, NORTHWEST);
        layOutQuadrants(qa, RIGHTTOPCLOCK, NORTHEAST);
        layOutQuadrants(qb, RIGHTTOPCLOCK, SOUTHEAST);
        layOutQuadrants(qc, RIGHTTOPCLOCK, SOUTHWEST);
    }

    protected void layoutRightBottomClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |3 4|
         |2 1|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qb, qc, qd, qa);
        layOutQuadrants(qc, RIGHTBOTTOMCLOCK, NORTHWEST);
        layOutQuadrants(qd, RIGHTBOTTOMCLOCK, NORTHEAST);
        layOutQuadrants(qa, RIGHTBOTTOMCLOCK, SOUTHEAST);
        layOutQuadrants(qb, RIGHTBOTTOMCLOCK, SOUTHWEST);
    }

    protected void layoutLeftBottomClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |2 3|
         |1 4|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qa, qb, qc, qd);
        layOutQuadrants(qb, RIGHTBOTTOMCLOCK, NORTHWEST);
        layOutQuadrants(qc, RIGHTBOTTOMCLOCK, NORTHEAST);
        layOutQuadrants(qd, RIGHTBOTTOMCLOCK, SOUTHEAST);
        layOutQuadrants(qa, RIGHTBOTTOMCLOCK, SOUTHWEST);
    }

    protected void layoutLeftTopCounterClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |1 4|
         |2 3|
         -----
         */

        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qb, qa, qd, qc);
        layOutQuadrants(qa, RIGHTBOTTOMCLOCK, NORTHWEST);
        layOutQuadrants(qd, RIGHTBOTTOMCLOCK, NORTHEAST);
        layOutQuadrants(qb, RIGHTBOTTOMCLOCK, SOUTHEAST);
        layOutQuadrants(qc, RIGHTBOTTOMCLOCK, SOUTHWEST);
    }

    protected void layoutRightTopCounterClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |2 1|
         |3 4|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qc, qb, qa, qd);
        layOutQuadrants(qb, RIGHTBOTTOMCLOCK, NORTHWEST);
        layOutQuadrants(qa, RIGHTBOTTOMCLOCK, NORTHEAST);
        layOutQuadrants(qd, RIGHTBOTTOMCLOCK, SOUTHEAST);
        layOutQuadrants(qc, RIGHTBOTTOMCLOCK, SOUTHWEST);
    }

    protected void layoutRightBottomCounterClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |3 2|
         |4 1|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qd, qc, qb, qa);
        layOutQuadrants(qc, RIGHTBOTTOMCLOCK, NORTHWEST);
        layOutQuadrants(qb, RIGHTBOTTOMCLOCK, NORTHEAST);
        layOutQuadrants(qd, RIGHTBOTTOMCLOCK, SOUTHEAST);
        layOutQuadrants(qa, RIGHTBOTTOMCLOCK, SOUTHWEST);
    }

    protected void layoutLeftBottomCounterClockWise(Quadrant q, Rectangle r) {
        /*
         layout in the following order
         -----
         |4 3|
         |1 2|
         -----
         */
        Quadrant qa = q.getQuadrantA();
        Quadrant qb = q.getQuadrantB();
        Quadrant qc = q.getQuadrantC();
        Quadrant qd = q.getQuadrantD();

        updateByCorners(r, qa, qd, qc, qb);
        layOutQuadrants(qd, RIGHTBOTTOMCLOCK, NORTHWEST);
        layOutQuadrants(qc, RIGHTBOTTOMCLOCK, NORTHEAST);
        layOutQuadrants(qb, RIGHTBOTTOMCLOCK, SOUTHEAST);
        layOutQuadrants(qa, RIGHTBOTTOMCLOCK, SOUTHWEST);
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
