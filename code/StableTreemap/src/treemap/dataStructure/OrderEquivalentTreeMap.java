package treemap.dataStructure;

import TreeMapGenerator.LocalChanges.Block;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static utility.Precision.*;

/**
 *
 * @author max
 */
public class OrderEquivalentTreeMap {

    /**
     * Relations between horizontal segments
     */
    public Map<OrderEquivalentLineSegment, Set<OrderEquivalentLineSegment>> horizontalRelations;
    /**
     * Relations between vertical segments
     */
    public Map<OrderEquivalentLineSegment, Set<OrderEquivalentLineSegment>> verticalRelations;

    /**
     * Generates the orderequivalence relations between the line segments in the
     * treemap {@code tm}
     *
     * @param tm
     */
    public OrderEquivalentTreeMap(TreeMap tm) {
        List<TreeMap> children = new ArrayList();
        Set<OrderEquivalentLineSegment> maximalHorizontalSegments = new HashSet();
        Set<OrderEquivalentLineSegment> maximalVerticalSegments = new HashSet();

        int tries = 0;//used to prevent infinite loops from hanging the program

        boolean done = false;
        while (done == false) {
            children = tm.getChildren();

            //Find all vertical segments
            List<OrderEquivalentLineSegment> allVerticalSegments = getAllVerticalSegments(children);

            //construct the maximal vertical segments
            maximalVerticalSegments = findMaximalVerticalSegments(allVerticalSegments);
            //construct the relation graph
            findRelationsBetweenVertical(maximalVerticalSegments, children);

            //Find all horizontal segments
            List<OrderEquivalentLineSegment> allHorizontalSegments = getAllHorizontalSegments(children);
            //construct the maximal horizontal segments
            maximalHorizontalSegments = findMaximalHorizontalSegments(allHorizontalSegments);

            if (handleDegenerateCases(tm, maximalVerticalSegments, maximalHorizontalSegments) == false) {
                done = true;
            }
            tries++;

            if (tries > 5000) {
                //never need this many tries, that would mean there are more than 5000 degeneracies
                throw new IllegalStateException("infinite loop occured in degeneracy handling");
            }
        }

        findRelationsBetweenHorizontal(maximalHorizontalSegments, children);
    }

    public Block getBlockWithLabel(String label, List<Block> blocks) {
        for (Block b : blocks) {
            if (b.getLabel().equals(label)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Resolves cases of linesegment ending in each other by slightly offsetting
     * one.
     *
     * @param treeMap
     * @param maximalVerticalSegments
     * @param maximalHorizontalSegments
     * @return whether the degeneracy solving was successfull
     */
    private boolean handleDegenerateCases(TreeMap treeMap, Set<OrderEquivalentLineSegment> maximalVerticalSegments, Set<OrderEquivalentLineSegment> maximalHorizontalSegments) {

        for (OrderEquivalentLineSegment verticalMs : maximalVerticalSegments) {
            for (OrderEquivalentLineSegment horizontalMs : maximalHorizontalSegments) {
                if (verticalMs.intersects(horizontalMs)) {
                    //intersection occured, are going to split the horizontal segment
                    //We move everything above slightly up, and everything below slightly down.
                    //we will also update the rectangles of the blocks

                    double offset = 0.001;

                    for (String label : horizontalMs.labelsTop) {
                        TreeMap tm = treeMap.getChildWithLabel(label);
                        Rectangle tmR = tm.getRectangle();
                        if (leq(tmR.getX2(), verticalMs.x1)) {
                            //it is to the left
                            Rectangle r = new Rectangle(tmR.getX(), tmR.getY(), tmR.getWidth(), tmR.getHeight() - offset);
                            tm.updateRectangle(r);
                        }
                    }

                    for (String label : horizontalMs.labelsBottom) {
                        TreeMap tm = treeMap.getChildWithLabel(label);
                        Rectangle tmR = tm.getRectangle();

                        if (leq(tmR.getX2(), verticalMs.x1)) {
                            //it is to the right
                            Rectangle r = new Rectangle(tmR.getX(), tmR.getY() - offset, tmR.getWidth(), tmR.getHeight() + offset);
                            tm.updateRectangle(r);
                        }
                    }
                    return true;
                }
            }
        }

        if ((treeMap.getChildren().size() + 3) != (maximalHorizontalSegments.size() + maximalVerticalSegments.size())) {
            System.err.println("The amount of children does not match the amount of segments. There are "
                               + treeMap.getChildren().size() + " children and "
                               + (maximalHorizontalSegments.size() + maximalVerticalSegments.size()) + "segments"
            );
        }
        return false;
    }

    /**
     * Returns a list of all vertical segments in the treemap
     *
     * @param treeMaps
     * @return
     */
    private List<OrderEquivalentLineSegment> getAllVerticalSegments(List<TreeMap> treeMaps) {
        List<OrderEquivalentLineSegment> verticalSegments = new LinkedList();
        for (TreeMap tm : treeMaps) {
            Rectangle r = tm.getRectangle();

            LineSegment left = LineSegment.getLeftSegment(r);
            LineSegment right = LineSegment.getRightSegment(r);

            OrderEquivalentLineSegment orderedLeft = new OrderEquivalentLineSegment(left, null, tm.getLabel(), null, null);
            OrderEquivalentLineSegment orderedRight = new OrderEquivalentLineSegment(right, tm.getLabel(), null, null, null);
            verticalSegments.add(orderedLeft);
            verticalSegments.add(orderedRight);
        }
        return verticalSegments;
    }

    /**
     * Returns all maximal vertical segments
     *
     * @param verticalSegments
     * @return
     */
    private Set<OrderEquivalentLineSegment> findMaximalVerticalSegments(List<OrderEquivalentLineSegment> verticalSegments) {
        if (verticalSegments.isEmpty()) {
            return null;
        }
        Collections.sort(verticalSegments, new Comparator<OrderEquivalentLineSegment>() {
                     @Override
                     public int compare(OrderEquivalentLineSegment o1, OrderEquivalentLineSegment o2) {
                         //sort first on x1, then on y1 and then on y2.
                         //Meaning we sort on the x-coordinate, the first y-coordinate and
                         //then on the shortets segment
                         int result = comparePrecision(o1.x1, o2.x1);
                         if (result == 0) {
                             //same x-coordinate so start sort on y1
                             result = comparePrecision(o1.y1, o2.y1);
                             if (result == 0) {
                                 //same y1-coordinate so start sort on y2
                                 result = comparePrecision(o1.y2, o2.y2);
                             }
                         }
                         return result;
                     }
                 });
        //We are now going to find the maximal segment by going through
        //the segment from left to right and top to bottom
        Set<OrderEquivalentLineSegment> maximalSegments = new HashSet();
        //start with an initial segemnt
        OrderEquivalentLineSegment ls0 = verticalSegments.get(0);
        double startX = ls0.x1;
        double startY = ls0.y1;
        double endY = ls0.y2;
        //keep track of the labels that are to either side of the lineSegment
        Set<String> labelsLeft = new HashSet();
        labelsLeft.addAll(ls0.labelsLeft);
        Set<String> labelsRight = new HashSet();
        labelsRight.addAll(ls0.labelsRight);
        for (OrderEquivalentLineSegment ls : verticalSegments) {
            //it does not have the same x-coordinate so it cannot be an extension
            //we have thus concluded the maximal segment
            if (!eq(ls.x1, startX)) {
                OrderEquivalentLineSegment maximalSegment = new OrderEquivalentLineSegment(startX, startX, startY, endY, labelsLeft, labelsRight, null, null);
                maximalSegments.add(maximalSegment);
                startX = ls.x1;
                startY = ls.y1;
                endY = ls.y2;
                labelsLeft = new HashSet();
                labelsLeft.addAll(ls.labelsLeft);
                labelsRight = new HashSet();
                labelsRight.addAll(ls.labelsRight);
                continue;
            }
            //Same x-coordinate so it can be an extension. Still need to check
            //whether it is actually an extension or a new segment
            if (geq(ls.y1, startY) && leq(ls.y1, endY)) {
                //new segment starts in the oldSegment so it is an extension
                //Maximum as it can still be shorter and we want to find the maximal
                //segment
                endY = Math.max(endY, ls.y2);
                //add the labels
                labelsLeft.addAll(ls.labelsLeft);
                labelsRight.addAll(ls.labelsRight);
                continue;
            }
            //it is a new segment
            if (ge(ls.y1, endY)) {
                OrderEquivalentLineSegment maximalSegment = new OrderEquivalentLineSegment(startX, startX, startY, endY, labelsLeft, labelsRight, null, null);
                maximalSegments.add(maximalSegment);
                startX = ls.x1;
                startY = ls.y1;
                endY = ls.y2;
                labelsLeft = new HashSet();
                labelsLeft.addAll(ls.labelsLeft);
                labelsRight = new HashSet();
                labelsRight.addAll(ls.labelsRight);
                continue;
            }
        }//end of for loop

        //add the last segment. Can't be completed yet
        OrderEquivalentLineSegment maximalSegment = new OrderEquivalentLineSegment(startX, startX, startY, endY, labelsLeft, labelsRight, null, null);
        maximalSegments.add(maximalSegment);
        return maximalSegments;

    }

    /**
     * Two vertical maximal segments are related if they are on the left/right
     * side of a rectangle in the treemap.
     *
     * @param maximalVerticalSegments
     * @param allLeafs
     */
    private void findRelationsBetweenVertical(Set<OrderEquivalentLineSegment> maximalVerticalSegments, List<TreeMap> allLeafs) {

        //initialize vertical relations
        verticalRelations = new HashMap();
        for (OrderEquivalentLineSegment leftSegment : maximalVerticalSegments) {
            Set<OrderEquivalentLineSegment> relations = new HashSet();

            for (OrderEquivalentLineSegment rightSegment : maximalVerticalSegments) {
                //no self relations
                if (leq(rightSegment.x1, leftSegment.x1)) {
                    continue;
                }
                //if there is a rectangle whoms left edge overlaps leftSegment
                //and right edge overlaps the rightSegment then there is a 
                //relation leftSegment -> rightSegment

                for (String label : leftSegment.labelsRight) {
                    if (rightSegment.labelsLeft.contains(label)) {
                        relations.add(rightSegment);
                        break;
                    }
                }

            }
            verticalRelations.put(leftSegment, relations);
        }
    }

    /**
     * Finds all horizontal segments in the treemap
     *
     * @param treeMaps
     * @return
     */
    private List<OrderEquivalentLineSegment> getAllHorizontalSegments(List<TreeMap> treeMaps) {
        List<OrderEquivalentLineSegment> horizontalSegment = new LinkedList();
        for (TreeMap tm : treeMaps) {
            Rectangle r = tm.getRectangle();

            LineSegment top = LineSegment.getTopSegment(r);
            LineSegment bottom = LineSegment.getBottomSegment(r);

            OrderEquivalentLineSegment orderedTop = new OrderEquivalentLineSegment(top, null, null, null, tm.getLabel());
            OrderEquivalentLineSegment orderedBottom = new OrderEquivalentLineSegment(bottom, null, null, tm.getLabel(), null);

            horizontalSegment.add(orderedTop);
            horizontalSegment.add(orderedBottom);
        }
        return horizontalSegment;
    }

    /**
     * Finds all maximal horizontal segments in the treemap
     *
     * @param horizontalSegments
     * @return
     */
    private Set<OrderEquivalentLineSegment> findMaximalHorizontalSegments(List<OrderEquivalentLineSegment> horizontalSegments) {
        if (horizontalSegments.isEmpty()) {
            return null;
        }
        Collections.sort(horizontalSegments, new Comparator<OrderEquivalentLineSegment>() {
                     @Override
                     public int compare(OrderEquivalentLineSegment o1, OrderEquivalentLineSegment o2) {
                         //sort first on y1, then on x1 and then on x2.
                         //Meaning we sort on the y-coordinate, the first x-coordinate and
                         //then on the shortest segment
                         int result = comparePrecision(o1.y1, o2.y1);
                         if (result == 0) {
                             //same y1-coordinate so start sort on x1
                             result = comparePrecision(o1.x1, o2.x1);
                             if (result == 0) {
                                 //same x1-coordinate so start sort on x2
                                 result = comparePrecision(o1.x2, o2.x2);
                             }
                         }
                         return result;
                     }
                 });
        //We are now going to find the maximal segment by going through
        //the segment from top to bottom and left to right
        Set<OrderEquivalentLineSegment> maximalSegments = new HashSet();
        //start with an initial segemnt
        OrderEquivalentLineSegment ls0 = horizontalSegments.get(0);
        double startY = ls0.y1;
        double startX = ls0.x1;
        double endX = ls0.x2;
        Set<String> topLabels = new HashSet();
        topLabels.addAll(ls0.labelsTop);
        Set<String> bottomLabels = new HashSet();
        bottomLabels.addAll(ls0.labelsBottom);

        for (OrderEquivalentLineSegment ls : horizontalSegments) {
            //it does not have the same x-coordinate so it cannot be an extension
            //we have thus concluded the maximal segment
            if (!eq(ls.y1, startY)) {
                OrderEquivalentLineSegment maximalSegment = new OrderEquivalentLineSegment(startX, endX, startY, startY, null, null, topLabels, bottomLabels);
                maximalSegments.add(maximalSegment);
                startY = ls.y1;
                startX = ls.x1;
                endX = ls.x2;
                topLabels = new HashSet();
                topLabels.addAll(ls.labelsTop);
                bottomLabels = new HashSet();
                bottomLabels.addAll(ls.labelsBottom);
                continue;
            }
            //Same y-coordinate so it can be an extension. Still need to check
            //whether it is actually an extension or a new segment
            if (geq(ls.x1, startX) && leq(ls.x1, endX)) {
                //new segment starts in the oldSegment so it is an extension
                //Maximum as it can still be shorter and we want to find the maximal
                //segment
                endX = Math.max(endX, ls.x2);
                topLabels.addAll(ls.labelsTop);
                bottomLabels.addAll(ls.labelsBottom);
                continue;
            }
            //it is a new segment
            if (ge(ls.x1, endX)) {
                OrderEquivalentLineSegment maximalSegment = new OrderEquivalentLineSegment(startX, endX, startY, startY, null, null, topLabels, bottomLabels);
                maximalSegments.add(maximalSegment);
                startY = ls.y1;
                startX = ls.x1;
                endX = ls.x2;
                topLabels = new HashSet();
                topLabels.addAll(ls.labelsTop);
                bottomLabels = new HashSet();
                bottomLabels.addAll(ls.labelsBottom);
                continue;
            }
        }//end of for loop

        //add the last segment. Can't be completed yet
        OrderEquivalentLineSegment maximalSegment = new OrderEquivalentLineSegment(startX, endX, startY, startY, null, null, topLabels, bottomLabels);
        maximalSegments.add(maximalSegment);
        return maximalSegments;

    }

    /**
     * Horizontal maximal Segments are related if they are to the top/bottom of
     * a leaf in the treemap.
     *
     * @param maximalHorizontalSegments
     * @param allLeafs
     */
    private void findRelationsBetweenHorizontal(Set<OrderEquivalentLineSegment> maximalHorizontalSegments, List<TreeMap> allLeafs) {

        //initialize vertical relations
        horizontalRelations = new HashMap();
        for (OrderEquivalentLineSegment bottomSegment : maximalHorizontalSegments) {
            Set<OrderEquivalentLineSegment> relations = new HashSet();

            for (OrderEquivalentLineSegment topSegment : maximalHorizontalSegments) {
                //no self relations and we can't have a relation to anything
                //above it
                if (geq(topSegment.y1, bottomSegment.y1)) {
                    continue;
                }
                //if there is a rectangle whoms top edge overlaps topSegment
                //and bottom edge overlaps the bottomSegment then there is a 
                //relation topSegment -> bottomSegment

                for (String label : topSegment.labelsBottom) {
                    if (bottomSegment.labelsTop.contains(label)) {
                        relations.add(bottomSegment);
                        break;
                    }
                }
            }
            horizontalRelations.put(bottomSegment, relations);
        }
    }

    public Map<OrderEquivalentLineSegment, Set<OrderEquivalentLineSegment>> getVerticalRelations() {
        return verticalRelations;
    }

    public Map<OrderEquivalentLineSegment, Set<OrderEquivalentLineSegment>> getHorizontalRelations() {
        return horizontalRelations;
    }
}
