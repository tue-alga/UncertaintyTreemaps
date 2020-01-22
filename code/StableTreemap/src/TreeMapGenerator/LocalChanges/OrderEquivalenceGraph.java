package TreeMapGenerator.LocalChanges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import treemap.dataStructure.OrderEquivalentLineSegment;
import treemap.dataStructure.LineSegment;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
//Static important for convenience sake. Can use le(x,y) instead of Precision.le(x,y)
import static utility.Precision.*;
import treemap.dataStructure.DataMap;

/**
 *
 * Labels of all blocks should be unique. Size should be strictly positive
 *
 * @author max
 */
public class OrderEquivalenceGraph {

    /**
     * Holds all the maximal segments (for leaf nodes) in the treemap
     */
    private List<OrderEquivalentMaximalSegment> segments = new ArrayList();
    /**
     * Holds all the (leaf) blocks in the treemap
     */
    private List<Block> blocks = new ArrayList();
    /**
     * Holds the rectangle in which the treemap is contained
     */
    private Rectangle boundingBox;
    private double scaleFactor;

    /**
     * Stores the original treemap. Does not reflect any of the changes made
     */
    public TreeMap originalTreeMap;

    /**
     *
     * @param segments
     * @param blocks
     * @param boundingBox
     * @param scaleFactor
     * @param originalTreeMap
     * @param calculateAdjacenyLengths should be true on default
     */
    public OrderEquivalenceGraph(List<OrderEquivalentMaximalSegment> segments, List<Block> blocks, Rectangle boundingBox, double scaleFactor, TreeMap originalTreeMap, boolean calculateAdjacenyLengths) {
        this.segments = segments;
        this.blocks = blocks;
        this.boundingBox = boundingBox;
        this.scaleFactor = scaleFactor;
        this.originalTreeMap = originalTreeMap;
        updateEndpointsRelations();
        if (calculateAdjacenyLengths) {
            updateAdjecencyLength();
        }

        isConsistent(boundingBox, segments, blocks);
        checkSegments();

    }

    public boolean checkSegments() {
        if (getMaximalSegments().size() != (blocks.size() + 3)) {
            System.err.println("The graph does not have the correct amount of segments");
            return false;
        }
        return true;
    }

    public OrderEquivalenceGraph deepCopy() {
        //most likely an error here with instantiating new blocks or using old.
        if (checkSegments() == false) {
            return this;
        }
        Rectangle boundingBoxCopy = boundingBox.deepCopy();
        List<Block> blocksCopy = new ArrayList();
        for (Block b : blocks) {
            Block bCopy = new Block(b.rectangle.deepCopy(), b.getLabel(), b.getTargetSize());
            blocksCopy.add(bCopy);
        }

        List<String> allLabels = new ArrayList();

        allLabels.addAll(segments.get(0).getAllLabels());

        List<OrderEquivalentMaximalSegment> segmentsCopy = new ArrayList();
        for (OrderEquivalentMaximalSegment s : segments) {
            List<Block> set1Copy = new ArrayList();
            for (Block b : s.adjacentBlockList1) {
                Block bCopy = Block.getBlockWithLabel(blocksCopy, b.getLabel());
                set1Copy.add(bCopy);
            }
            List<Block> set2Copy = new ArrayList();
            for (Block b : s.adjacentBlockList2) {
                Block bCopy = Block.getBlockWithLabel(blocksCopy, b.getLabel());
                set2Copy.add(bCopy);
            }
            double[] adjecancyLengthSet = s.adjecancyLengthSet;

            OrderEquivalentMaximalSegment sCopy = new OrderEquivalentMaximalSegment(s.x1, s.x2, s.y1, s.y2, set1Copy, set2Copy, allLabels, adjecancyLengthSet);
            segmentsCopy.add(sCopy);
            //set the adjecencies for the block
            for (Block bCopy : sCopy.adjacentBlockList1) {
                if (sCopy.horizontal) {
                    bCopy.setMsTop(sCopy);
                } else {
                    bCopy.setMsRight(sCopy);
                }
            }
            for (Block bCopy : sCopy.adjacentBlockList2) {
                if (sCopy.horizontal) {
                    bCopy.setMsBottom(sCopy);
                } else {
                    bCopy.setMsLeft(sCopy);
                }
            }
        }
        //set the adjacency lengths

        //returns the new deepcopy
        return new OrderEquivalenceGraph(segmentsCopy, blocksCopy, boundingBoxCopy, scaleFactor, originalTreeMap, false);

    }

    private List<String> getAllLabels(TreeMap treeMap) {
        List<TreeMap> children = treeMap.getChildren();
        List<String> labelList = new ArrayList();
        //get all the blocks from the treemap
        for (TreeMap tm : children) {
            labelList.add(tm.getLabel());
        }

        return labelList;
    }

    public OrderEquivalenceGraph(TreeMap treeMap) {

        originalTreeMap = treeMap;
        boundingBox = treeMap.getRectangle();

        scaleFactor = treeMap.getTargetSize() / treeMap.getRectangle().getArea();

        List<TreeMap> children = treeMap.getChildren();

        for (TreeMap tm : children) {
            Block b = new Block(tm.getRectangle(), tm.getLabel(), tm.getTargetSize());
            blocks.add(b);
        }

        List<String> labelList = getAllLabels(treeMap);

        OrderEquivalentTreeMap oetm = new OrderEquivalentTreeMap(treeMap);

        //get all maximal segments from the treemap
        segments.addAll(getMaximalSegmentsFromRelations(oetm.horizontalRelations, blocks, labelList));
        segments.addAll(getMaximalSegmentsFromRelations(oetm.verticalRelations, blocks, labelList));

        /*Store which maximalSegments are adjacent to a block in the block itself
         *for performance and convenience. Every block in enclose by exactly 4 maximal segments
         */
        for (OrderEquivalentMaximalSegment ms : segments) {
            for (Block b : ms.adjacentBlockList1) {
                if (ms.horizontal) {
                    b.setMsTop(ms);
                } else {
                    b.setMsRight(ms);
                }
            }
            for (Block b : ms.adjacentBlockList2) {
                if (ms.horizontal) {
                    b.setMsBottom(ms);
                } else {
                    b.setMsLeft(ms);
                }
            }
        }

        updateEndpointsRelations();
        updateAdjecencyLength();

        isConsistent(boundingBox, segments, blocks);
        checkSegments();

    }

    /**
     * Gets all the maximal segments from a relation mapping. Uses only the keys
     *
     * @param relations
     * @param blocks
     * @param labels
     * @return
     */
    public List<OrderEquivalentMaximalSegment> getMaximalSegmentsFromRelations(Map<OrderEquivalentLineSegment, Set<OrderEquivalentLineSegment>> relations, List<Block> blocks, List<String> labels) {
        List<OrderEquivalentMaximalSegment> segments = new ArrayList();

        for (OrderEquivalentLineSegment ls : relations.keySet()) {
            List<Block> list1;
            List<Block> list2;
            if (ls.horizontal) {
                list1 = getBlocksForLabels(ls.labelsBottom, blocks);
                list2 = getBlocksForLabels(ls.labelsTop, blocks);
            } else {
                list1 = getBlocksForLabels(ls.labelsLeft, blocks);
                list2 = getBlocksForLabels(ls.labelsRight, blocks);
            }

            OrderEquivalentMaximalSegment ms = new OrderEquivalentMaximalSegment(ls.x1, ls.x2, ls.y1, ls.y2, list1, list2, labels);
            segments.add(ms);
        }
        return segments;
    }

    /**
     * returns a list of all blocks that have a label that occurs in
     * {@code labelSet}
     *
     * @param labelSet
     * @param candidates
     * @return
     */
    public List<Block> getBlocksForLabels(Set<String> labelSet, List<Block> candidates) {
        List<Block> blockList = new ArrayList();
        for (String label : labelSet) {
            blockList.add(Block.getBlockWithLabel(candidates, label));
        }
        return blockList;
    }

    /**
     * returns the block with the label equal to {@code label} or null if it is
     * not contained in blocks
     *
     * @param blockList
     * @param label
     * @return
     */
    public Block getBlockWithLabel(String label) {
        for (Block b : blocks) {
            if (b.getLabel().equals(label)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Checks whether the graph is consistent
     *
     * @return
     */
    public static boolean isConsistent(Rectangle boundingBox, List<OrderEquivalentMaximalSegment> segments, List<? extends Block> blocks) {
        //TODO Verifies if the orderEquivalenceGraph is valid. E.a. there are no
        //segments crossing each other or in the wrong order
        //(segment ss1 with module x below above a segment s2 width x above)

        //all segments should be contained in the bounding box.
        double x1 = boundingBox.getX();
        double x2 = boundingBox.getX2();
        double y1 = boundingBox.getY();
        double y2 = boundingBox.getY2();

        //Rectangles should be strictly positive
        for (Block b : blocks) {
            if (b.rectangle.getWidth() < 0 || b.rectangle.getHeight() < 0) {
                return false;
            }
        }

        //All segments should be withing the boundingbox
        for (OrderEquivalentMaximalSegment segment : segments) {
            if (le(segment.x1, x1) || ge(segment.x2, x2) || le(segment.y1, y1) || ge(segment.y2, y2)) {
                return false;
            }
        }

        //there should be 4 segments which form the bounding box exactly
        LineSegment topSegment = new LineSegment(x1, x2, y1, y1);
        LineSegment bottomSegment = new LineSegment(x1, x2, y2, y2);
        LineSegment leftSegment = new LineSegment(x1, x1, y1, y2);
        LineSegment rightSegment = new LineSegment(x2, x2, y1, y2);
        if (!(segments.contains(topSegment) && segments.contains(bottomSegment) && segments.contains(leftSegment) && segments.contains(rightSegment))) {
            return false;
        }

        //no linesegments should overlap except for endpoints
        if (checkOverLap(segments) == false) {
            return false;
        }
        //order in the segments should be maintained
        if (checkOrder(segments) == false) {
            return false;
        }

        return true;
    }

    public static boolean checkOverLap(List<OrderEquivalentMaximalSegment> segments) {
        //TODO Needs to be optimized a lot
        //TODO Need to verified precisely for all cases
        for (OrderEquivalentMaximalSegment segment1 : segments) {
            for (OrderEquivalentMaximalSegment segment2 : segments) {
                if (segment1 == segment2) {
                    continue;
                }
                //check whether the two segments overlap
                if (segment1.partiallyOverlaps(segment2)) {
                    //TODO Make sure this overlap also works for vertical+horizontal. Currently does not work

                    //they overlap. If they only overlap at the endpoints we are fine
                    //otherwise we have a graph violation
                    if (!(eq(segment1.x1, segment2.x2) || eq(segment1.x2, segment2.x2))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks whether the order of the segments is maintained
     *
     * @return
     */
    private static boolean checkOrder(List<OrderEquivalentMaximalSegment> segments) {
        //relatedsegments was not initialized yet, so do it now
        for (OrderEquivalentMaximalSegment segment1 : segments) {
            for (OrderEquivalentMaximalSegment segment2 : segment1.getRelations()) {
                //The two segments are related by a common block, thus their relative order should not be violated

                Block b = Block.getCommonBlock(segment1.adjacentBlocks, segment2.adjacentBlocks);

                //check that the order is maintained
                if (checkConditions(b, segment1, segment2) == false) {
                    return false;

                }
            }
        }
        //no violations
        return true;
    }

    private static boolean checkConditions(Block b, OrderEquivalentMaximalSegment s1, OrderEquivalentMaximalSegment s2) {

        //they are related to eachother so they must be in a certain order
        if (s1.horizontal && s2.horizontal//both horizontal
            && ((s1.adjacentBlockList1.contains(b) && geq(s1.y1, s2.y1))
                || //block b should be below s1 but it can't
                (s1.adjacentBlockList2.contains(b) && leq(s1.y1, s2.y1))) //block b should be above s1 but it can't
                ) {// it can not be below segment1
            return false;
        } else if (s1.vertical && s2.vertical//both vertical
                   && ((s1.adjacentBlockList1.contains(b) && leq(s1.x1, s2.x1))
                       || //block b should be to the left of s1 but it can't
                       (s1.adjacentBlockList2.contains(b) && geq(s1.x1, s2.x1))) //block b should be to the right of s1 but it can't
                ) {// it can not be below segment1
            return false;
        } else {
            //One horizontal and 1 vertical
            //Make sure that s1 is a horizontal sgement
            if (s1.vertical) {
                OrderEquivalentMaximalSegment sTemp = s1;
                s1 = s2;
                s2 = sTemp;
            }
            //one horizontal, one vertical. Otherwise we are done
            if (!(s1.horizontal && s2.vertical)) {
                return true;
            }

            if (s1.adjacentBlockList1.contains(b) && s2.adjacentBlockList1.contains(b) //it should be below s1 and left of s2
                && !((eq(s1.x2, s2.x1) && geq(s1.y1, s2.y1) && le(s1.y1, s2.y2)) //but it can't be
                     || (eq(s1.y2, s2.y1) && le(s1.x1, s2.x1) && geq(s1.x2, s2.x2)))) {
                return false;
            }

            if (s1.adjacentBlockList1.contains(b) && s2.adjacentBlockList2.contains(b) //it should be below s1 and right of s2
                && !((eq(s1.x1, s2.x1) && geq(s1.y1, s2.y1) && le(s1.y1, s2.y2)) //but it can't be
                     || (eq(s1.y2, s2.y1) && leq(s1.x1, s2.x1) && ge(s1.x2, s2.x2)))) {
                return false;
            }

            if (s1.adjacentBlockList2.contains(b) && s2.adjacentBlockList1.contains(b) //it should be above s1 and left of s2
                && !((eq(s1.x2, s2.x1) && ge(s1.y1, s2.y1) && leq(s1.y1, s2.y2)) //but it can't be
                     || (eq(s1.y2, s2.y2) && le(s1.x1, s2.x1) && geq(s1.x2, s2.x2)))) {
                return false;
            }

            if (s1.adjacentBlockList2.contains(b) && s2.adjacentBlockList2.contains(b) //it should be above s1 and right of s2
                && !((eq(s1.x1, s2.x1) && ge(s1.y1, s2.y1) && leq(s1.y1, s2.y2)) //but it can't be
                     || (eq(s1.y2, s2.y2) && leq(s1.x1, s2.x1) && ge(s1.x2, s2.x2)))) {
                return false;
            }
        }

        return true;
    }

    public void updateAdjecencyLength() {
        for (OrderEquivalentMaximalSegment ms : getMaximalSegments()) {
            ms.updateAdjecancyLengthSet();
        }
    }

    public void updateEndpointsRelations() {
        /*
         * Initialize the endpoint relations between maximal segments
         */
        for (OrderEquivalentMaximalSegment ms : segments) {
            ms.updateEndpointRelations();
        }
    }

    public List<OrderEquivalentMaximalSegment> getMaximalSegments() {
        return segments;
    }

    public Collection<OrderEquivalentMaximalSegment> getBoundarySegments(List<OrderEquivalentMaximalSegment> segments) {
        List<OrderEquivalentMaximalSegment> boundarySegments = new ArrayList();
        for (OrderEquivalentMaximalSegment segment : segments) {
            if (segment.adjacentBlockList1.isEmpty() || segment.adjacentBlockList2.isEmpty()) {
                boundarySegments.add(segment);
            }
        }
        return boundarySegments;
    }

    public Collection<OrderEquivalentMaximalSegment> getBoundarySegments() {
        return getBoundarySegments(segments);
    }

    public List<OrderEquivalentMaximalSegment> getInnerMaximalSegments() {
        List<OrderEquivalentMaximalSegment> innerSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment segment : segments) {
            if (segment.adjacentBlockList1.isEmpty() || segment.adjacentBlockList2.isEmpty()) {
                continue;
            }
            innerSegments.add(segment);
        }
        return innerSegments;
    }

    public List<OrderEquivalentMaximalSegment> getInnerMaximalSegments(boolean horizontal) {
        List<OrderEquivalentMaximalSegment> innerSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment segment : segments) {
            if (segment.adjacentBlockList1.isEmpty() || segment.adjacentBlockList2.isEmpty()) {
                continue;
            }
            if (segment.horizontal == horizontal) {
                innerSegments.add(segment);
            }
        }
        return innerSegments;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getMaxAspectRatio() {
        double maxAspectRatio = 0;
        for (Block b : blocks) {
            maxAspectRatio = Math.max(maxAspectRatio, b.rectangle.getAspectRatio());
        }
        return maxAspectRatio;
    }

    public double getTotalAspectRatio() {
        double totalAspectRatio = 0;
        for (Block b : blocks) {
            totalAspectRatio += b.rectangle.getAspectRatio();
        }
        return totalAspectRatio;
    }

    /**
     * Adds a segment to the graph and updates the maximal segment adjacencies
     * of the blocks adjacent to this segment
     *
     * @param ms
     */
    public void addSegment(OrderEquivalentMaximalSegment ms) {
        segments.add(ms);
        for (Block b : ms.adjacentBlockList1) {
            if (ms.horizontal) {
                b.setMsTop(ms);
            } else {
                b.setMsRight(ms);
            }
        }
        for (Block b : ms.adjacentBlockList2) {
            if (ms.horizontal) {
                b.setMsBottom(ms);
            } else {
                b.setMsLeft(ms);
            }
        }
    }

    /**
     * Removes a segment from the graph. Does not update the adjacencies from
     * the blocks adjecent to this segment
     *
     * @param ms
     */
    public void removeSegment(OrderEquivalentMaximalSegment ms) {
        segments.remove(ms);
        //We do not removeTreeMap adjacencies as this should be fixed by adding new adjacencies.
    }

    /**
     * Converts the originaltreemap into the newly formed treemap with the
     * correct areas
     *
     * @return
     */
    public TreeMap convertOriginalTreeMap() {
        updateRectangleInTm(originalTreeMap);
        return originalTreeMap;
    }

    private void updateRectangleInTm(TreeMap currentTm) {
        //We update the positions of the rectangles based on the blocks. If we encounter a changed block, we immediatly update
        //all positions of the block below, the actualSize will be changed as we go

        //get the new position of the this treemap
        Block b = Block.getBlockWithLabel(getBlocks(), currentTm.getLabel());
        if (b != null) {
            //the position of this rectangle might have changed so we update it.
            //automatically updates the rectangles of child rectangles
            currentTm.updateRectangle(b.rectangle);
        }

        //update the actualsize. Might have changed by the parent
        currentTm.setActualSize(currentTm.getRectangle().getArea() * scaleFactor);

        //recurse in the children
        for (TreeMap child : currentTm.getChildren()) {
            updateRectangleInTm(child);
        }
    }

    /**
     * Updates the target sizes of the blocks in the graph
     *
     * @param label
     * @param size
     */
    public void updateWeights(String label, double size) {
        Block block = Block.getBlockWithLabel(blocks, label);
        if (block == null) {
            System.out.println("No weight for block");
        }
        block.setTargetSize(size);
    }

    public void updateScaleFactor(double size) {
        scaleFactor = size / boundingBox.getArea();
    }

    /**
     * Removes block b from blocks.
     *
     * @param b
     */
    public void removeBlock(Block b) {
        blocks.remove(b);
        for (OrderEquivalentMaximalSegment ms : segments) {
            ms.removeLabel(b.getLabel());
        }

    }

    /**
     * Removes all adjacencies of block b.
     *
     * @param b
     */
    public void removeAdjacencies(Block b) {
        b.getMsBottom().removeFromAdjecenyList(b);
        b.getMsLeft().removeFromAdjecenyList(b);
        b.getMsRight().removeFromAdjecenyList(b);
        b.getMsTop().removeFromAdjecenyList(b);
    }

    public void addBlock(Block b) {
        blocks.add(b);
        for (OrderEquivalentMaximalSegment ms : segments) {
            ms.addLabel(b.getLabel());
        }
        //We added a block, so the total size increase
        double size = 0;
        for (Block bl : blocks) {
            size += bl.getTargetSize();
        }
        updateScaleFactor(size);
    }

    public void addToTreeMap(DataMap dm, Rectangle dmRectangle) {
        originalTreeMap.addTreeMap(new TreeMap(dmRectangle, dm.getLabel(), dm.getColor(), dm.getTargetSize(),dm.getSd(), null), originalTreeMap);
    }

    public void removeFromTreeMap(String label) {
        originalTreeMap.removeTreeMap(label);
    }

    public List<String> getAllLabels() {
        return segments.get(0).getAllLabels();
    }

    /**
     * Positions the blocks within the boundingbox to optimize the difference
     * between targetSize and actualSize while keeping the same oeg.
     */
    public void fixBlockSizes() {
        if (blocks.size() == 1) {
            //1 block simply fills up all the space.
            Block b = blocks.get(0);
            b.rectangle = new Rectangle(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight());
        } else {
            //figure out the position of all the blocks
            TreemapBlockPositioner treemapBlockPositioner = new TreemapBlockPositioner(this);
            treemapBlockPositioner.fixBlockSizes();
        }
    }

    public void fixBlockSizes(int maxIterations) {
        TreemapBlockPositioner treemapBlockPositioner = new TreemapBlockPositioner(this);
        treemapBlockPositioner.fixBlockSizes(maxIterations);
    }

}
