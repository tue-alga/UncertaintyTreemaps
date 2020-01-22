package TreeMapGenerator.LocalChanges;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import treemap.dataStructure.LineSegment;
import treemap.dataStructure.Rectangle;
import static utility.Precision.geq;
import static utility.Precision.leq;

/**
 * Represents a maximalSegment in the orderEquivalence graph.
 *
 * @author max
 */
public class OrderEquivalentMaximalSegment extends LineSegment {

    /**
     * Contains all blacks that should be adjecent to this segment(top/bottom or
     * left/right)
     */
    public List<Block> adjacentBlocks;
    /**
     * Set1 contains all the blocks that should be to the left/bottom of this
     * segment
     */
    public List<Block> adjacentBlockList1 = new ArrayList();

    /**
     * Set1 contains all the blocks that should be to the right/top of this
     * segment
     */
    public List<Block> adjacentBlockList2 = new ArrayList();

    /**
     * Holds a list with all the labels
     */
    private List<String> allLabels;

    /**
     * A vector where position i holds how long the interval alongside the
     * maximal segment is of a block in {@code adjacentBlockList1} or
     * {@code adjacentBlockList2} which has label {@code allLabels.get(i)}. 0 if
     * block is not in adjacentBlockList1 or adjacentBlockList2
     */
    public double[] adjecancyLengthSet;

    /**
     * Contains the maximal segment to the left/bottom of this segment
     */
    public OrderEquivalentMaximalSegment endPoint1;
    /**
     * Contains the maximal segment to the right/top of this segment
     */
    public OrderEquivalentMaximalSegment endPoint2;

    /**
     *
     * @param isHorizontal whether the maximal segment is horizontal
     * @param x1 the x1-coordinate of the maximal segment
     * @param x2 the x2-coordinate of the maximal segment
     * @param y1 the y1-coordinate of the maximal segment
     * @param y2 the y2-coordinate of the maximal segment
     * @param set1 the blocks that should be to the left/bottom of the maximal
     * segment
     * @param set2 the blocks that should be to the right/top of the maximal
     * segment
     * @param allLabels a list containing all the labels occuring in the treemap
     */
    public OrderEquivalentMaximalSegment(double x1, double x2, double y1, double y2, List<Block> set1, List<Block> set2, List<String> allLabels) {
        super(x1, x2, y1, y2);
        //can only be vertical or horizontal segments
        assert (x2 == x1 || y2 == y1);
        //we must have at least 1 label, otherwise it is not a treemap
        assert (allLabels != null && allLabels.size() > 0);

        if (set1 == null) {
            set1 = new ArrayList();
        }
        if (set2 == null) {
            set2 = new ArrayList();
        }

        this.adjacentBlockList1 = set1;
        this.adjacentBlockList2 = set2;

        adjacentBlocks = new ArrayList();
        adjacentBlocks.addAll(set1);
        adjacentBlocks.addAll(set2);

        this.allLabels = allLabels;
        //initialize the vectorSets

        updateAdjecancyLengthSet();

    }

    OrderEquivalentMaximalSegment(double x1, double x2, double y1, double y2, List<Block> set1, List<Block> set2, List<String> allLabels, double[] adjecancyLengthSet) {
        super(x1, x2, y1, y2);
        //can only be vertical or horizontal segments
        assert (x2 == x1 || y2 == y1);
        //we must have at least 1 label, otherwise it is not a treemap
        assert (allLabels != null && allLabels.size() > 0);

        if (set1 == null) {
            set1 = new ArrayList();
        }
        if (set2 == null) {
            set2 = new ArrayList();
        }

        this.adjacentBlockList1 = set1;
        this.adjacentBlockList2 = set2;

        adjacentBlocks = new ArrayList();
        adjacentBlocks.addAll(set1);
        adjacentBlocks.addAll(set2);

        this.allLabels = allLabels;
        //initialize the vectorSets
    }

    /**
     * @Pre Requires that all blocks have the correct maximalsegments
     * adjacencies
     */
    public void updateEndpointRelations() {
        if (horizontal) {
            /*Due to t-junction property the maximal segment must end in one of
             these constructions:
             */

            // |        |
            // |---  OR |-- OR  |--
            // |                |
            /*In all these cases it holds that the maximal segment that is to
             the left of the leftmost block is the endpoint of this maximals segment.
             */
            double minX = Double.MAX_VALUE;
            Block leftBlock = null;
            double maxX = Double.MIN_VALUE;
            Block rightBlock = null;
            //find the leftmost and rightmost block
            for (Block b : adjacentBlocks) {
                if (b.rectangle.getX() < minX) {
                    minX = b.rectangle.getX();
                    leftBlock = b;
                }
                if (b.rectangle.getX2() > maxX) {
                    maxX = b.rectangle.getX2();
                    rightBlock = b;
                }
            }

            endPoint1 = leftBlock.getMsLeft();
            endPoint2 = rightBlock.getMsRight();
            if (endPoint1 == null || endPoint2 == null) {
                System.err.println("the segment only has one endpoint");
                return;
            }
        } else {//vertical

            /*Due to t-junction property the maximal segment must end in one of
             these constructions:
             */
            // ---    --     --
            //  |  OR  | OR  |
            /*In all these cases it holds that the maximal segment that is to
             the top of the uppermost block is the endpoint of this maximals segment.
             */
            double minY = Double.MAX_VALUE;
            Block topBlock = null;
            double maxY = Double.MIN_VALUE;
            Block bottomBlock = null;
            //find the leftmost and rightmost block
            for (Block b : adjacentBlocks) {
                if (b.rectangle.getY() < minY) {
                    minY = b.rectangle.getY();
                    topBlock = b;
                }
                if (b.rectangle.getY2() > maxY) {
                    maxY = b.rectangle.getY2();
                    bottomBlock = b;
                }
            }
            endPoint1 = bottomBlock.getMsBottom();
            endPoint2 = topBlock.getMsTop();
            if (endPoint1 == null || endPoint2 == null) {
                System.err.println("the segment only has one endpoint");
                return;
            }
        }

        //Update the position of the ends
        fixEndpoints();
    }

    /**
     * Initializes the adjacency vectors
     *
     * @param allLabels The labels occuring in the treemap
     * @param set1 The blocks that are occur in adjacentBlockList1
     * @param set2 the blocks that occur in adjacentBlockList2
     * @return
     */
    public void updateAdjecancyLengthSet() {

        HashMap<String, Block> blockMap = new HashMap();
        for (Block b : adjacentBlocks) {
            blockMap.put(b.getLabel(), b);
        }

        adjecancyLengthSet = new double[allLabels.size()];
        for (int i = 0; i < allLabels.size(); i++) {
            String label = allLabels.get(i);
            Block b = blockMap.get(label);
            if (b == null) {//no block with the given label in the list
                adjecancyLengthSet[i] = 0;
                continue;
            }
            //there is a block with the given label in the list

            //Check of long the overlap is, Block must have 1 side completly
            //overlapping with this maximal segment so can use width/height.
            double length;
            if (horizontal) {
                length = b.rectangle.getWidth();
                if (adjacentBlockList1.contains(b)) {
                    //it is below the segment. Moving it positive decrease the length
                    length = -length;
                }
            } else {//vertical
                length = b.rectangle.getHeight();
                if (adjacentBlockList2.contains(b)) {
                    //it is to the right of the segment. Moving it positive decrease the length
                    length = -length;
                }
            }

            adjecancyLengthSet[i] = length;
        }
    }

    /**
     * returns the block with the label equal to {@code label} or null if no
     * block in blocklist has this label
     *
     * @param blockList
     * @param label
     * @return
     */
    private Block getBlockWithLabel(List<Block> blockList, String label) {
        for (Block b : blockList) {
            if (b.getLabel().equals(label)) {
                return b;
            }
        }
        return null;
    }

    public void move(double amount) {
        if (horizontal) {
            y1 += amount;
            y2 += amount;
        } else {
            x1 += amount;
            x2 += amount;
        }
    }

    public void updatePosition(double newX1, double newX2, double newY1, double newY2) {
        x1 = newX1;
        x2 = newX2;
        y1 = newY1;
        y2 = newY2;
    }

    public List<String> getAllLabels() {
        return allLabels;
    }

    public double[] getAdjecancyLengthSet() {
        return adjecancyLengthSet;
    }

    /**
     * Sets the endpoints of this segment on the maximal segments it is enclosed
     * by
     */
    public void fixEndpoints() {
        //We are only moving segments without changing the length. This needs
        //to be updated afterwards so that all endpoints are connected again.
        if (horizontal) {
            //endPoint1 = left
            //endpoint2 = right
            x1 = endPoint1.x1;
            x2 = endPoint2.x2;
        } else {//vertical
            //endPoint1 = bottom
            //endpoint2 = top
            y1 = endPoint2.y1;
            y2 = endPoint1.y2;
        }
    }

    /**
     * Updates the adjacencyList and the adjacencies from the blocks to this
     * segment
     *
     * @param addToList1
     * @param newBlockList
     */
    void updateAdjacencyList(boolean addToList1, List<Block> newBlockList) {
        adjacentBlocks = new ArrayList();
        if (addToList1) {
            adjacentBlockList1 = newBlockList;
            for (Block b : newBlockList) {
                if (horizontal) {
                    b.setMsTop(this);
                } else {
                    b.setMsRight(this);
                }
            }
        } else {
            adjacentBlockList2 = newBlockList;
            for (Block b : newBlockList) {
                if (horizontal) {
                    b.setMsBottom(this);
                } else {
                    b.setMsLeft(this);
                }
            }
        }
        adjacentBlocks.addAll(adjacentBlockList1);
        adjacentBlocks.addAll(adjacentBlockList2);
    }

    /**
     * set1 the blocks that should be to the left/bottom of the maximal segment
     * set2 the blocks that should be to the right/top of the maximal segment
     *
     * @param addToList1
     * @param blockToAdd
     */
    public void addToAdjacencyList(boolean addToList1, Block blockToAdd) {
        List<Block> blockList = new ArrayList();
        blockList.add(blockToAdd);
        addToAdjacencyList(addToList1, blockList);
    }

    public void addToAdjacencyList(boolean addToList1, List<Block> blockToAdd) {
        adjacentBlocks = new ArrayList();
        //TODO Check if list contains before adding. Might trigger duplicates with addAll
        if (addToList1) {
            for (Block b : blockToAdd) {
                if (getBlockWithLabel(adjacentBlockList1, b.getLabel()) == null) {
                    //was not present yet
                    adjacentBlockList1.add(b);
                }
                if (horizontal) {
                    b.setMsTop(this);
                } else {
                    b.setMsRight(this);
                }
            }
        } else {
            for (Block b : blockToAdd) {
                if (getBlockWithLabel(adjacentBlockList2, b.getLabel()) == null) {
                    //was not present yet
                    adjacentBlockList2.add(b);
                }
                if (horizontal) {
                    b.setMsBottom(this);
                } else {
                    b.setMsLeft(this);
                }
            }
        }
        adjacentBlocks.addAll(adjacentBlockList1);
        adjacentBlocks.addAll(adjacentBlockList2);
    }

    public void removeFromAdjecenyList(Block block) {
        adjacentBlockList1.remove(block);
        adjacentBlockList2.remove(block);
        adjacentBlocks.remove(block);
    }

    /**
     * Returns a copy of the sorted adjacentBlockList1. Sorted based on smallest
     * x and smallest y
     *
     * @return
     */
    public List<Block> getSortedAdjacentList1() {
        ArrayList<Block> sortedBlockList = new ArrayList();
        sortedBlockList.addAll(adjacentBlockList1);
        sortedBlockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (horizontal) {
                    return Double.compare(o1.rectangle.getX(), o2.rectangle.getX());
                } else {
                    return Double.compare(o1.rectangle.getY(), o2.rectangle.getY());
                }
            }
        });
        return sortedBlockList;
    }

    /**
     * Returns a copy of the sorted adjacentBlockList2. Sorted based on smallest
     * x and smallest y
     *
     * @return
     */
    public List<Block> getSortedAdjacentList2() {
        ArrayList<Block> sortedBlockList = new ArrayList();
        sortedBlockList.addAll(adjacentBlockList2);
        sortedBlockList.sort(new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (horizontal) {
                    return Double.compare(o1.rectangle.getX(), o2.rectangle.getX());
                } else {
                    return Double.compare(o1.rectangle.getY(), o2.rectangle.getY());
                }
            }
        });
        return sortedBlockList;
    }

    public boolean isOneSidedWithBlock(Block b) {
        if (adjacentBlockList1.contains(b)) {
            if (adjacentBlockList1.size() == 1) {
                return true;
            }
        }
        if (adjacentBlockList2.contains(b)) {
            if (adjacentBlockList2.size() == 1) {
                return true;
            }
        }
        return false;
    }

    public Block getLeftTopBlock() {
        if (horizontal) {
            return getSortedAdjacentList2().get(0);
        } else {
            return getSortedAdjacentList1().get(0);
        }
    }

    public Block getRightTopBlock() {
        if (horizontal) {
            return getSortedAdjacentList2().get(getSortedAdjacentList2().size() - 1);
        } else {
            return getSortedAdjacentList2().get(0);
        }
    }

    public Block getLeftBottomBlock() {
        if (horizontal) {
            return getSortedAdjacentList1().get(0);
        } else {
            return getSortedAdjacentList1().get(getSortedAdjacentList1().size() - 1);
        }
    }

    public Block getRightBottomBlock() {
        if (horizontal) {
            return getSortedAdjacentList1().get(getSortedAdjacentList1().size() - 1);
        } else {
            return getSortedAdjacentList2().get(getSortedAdjacentList2().size() - 1);
        }
    }

    public static Set<Block> getAllBlocks(List<OrderEquivalentMaximalSegment> segments) {
        Set<Block> blockList = new HashSet();
        for (OrderEquivalentMaximalSegment ms : segments) {
            blockList.addAll(ms.adjacentBlocks);
        }
        return blockList;
    }

    static List<BlockCompartement> getAllBlockCompartements(List<OrderEquivalentMaximalSegment> compressedSegments) {
        List<BlockCompartement> blockList = new ArrayList();
        for (OrderEquivalentMaximalSegment ms : compressedSegments) {
            for (Block b : ms.adjacentBlocks) {
                BlockCompartement bc = (BlockCompartement) b;
                if (!blockList.contains(bc)) {
                    blockList.add(bc);
                }
            }
        }
        return blockList;
    }

    /**
     * Get all segments that end on or to the left of this segment from
     * {@code segments}.
     *
     * @param segments
     * @return
     */
    public List<OrderEquivalentMaximalSegment> getLeftSegments(List<OrderEquivalentMaximalSegment> segments) {
        List<OrderEquivalentMaximalSegment> leftSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment ms : segments) {
            if (ms.equals(this)) {
                continue;
            }
            if (leq(ms.x2, x1)) {
                //segment ends to the left or on movedSegment
                leftSegments.add(ms);
            }
        }
        return leftSegments;
    }

    /**
     * Get all segments that start on or to the right this segment from
     * {@code segments}
     *
     * @param segments
     * @return
     */
    public List<OrderEquivalentMaximalSegment> getRightSegments(List<OrderEquivalentMaximalSegment> segments) {
        List<OrderEquivalentMaximalSegment> rightSegment = new ArrayList();
        for (OrderEquivalentMaximalSegment ms : segments) {
            if (ms.equals(this)) {
                continue;
            }
            if (geq(ms.x1, this.x1)) {
                //segment start to the right or on movedSegment
                rightSegment.add(ms);
            }
        }
        return rightSegment;
    }

    /**
     * Get all segments that end on or above this segment from {@code segments}
     *
     * @param segments
     * @return
     */
    public List<OrderEquivalentMaximalSegment> getTopSegments(List<OrderEquivalentMaximalSegment> segments) {
        List<OrderEquivalentMaximalSegment> topSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment ms : segments) {
            if (ms.equals(this)) {
                continue;
            }
            if (leq(ms.y2, y1)) {
                //segment ends above or on movedSegment
                topSegments.add(ms);
            }
        }
        return topSegments;
    }

    /**
     * Get all segments that start on or below this segment from
     * {@code segments}
     *
     * @param segments
     * @return
     */
    public List<OrderEquivalentMaximalSegment> getBottomSegments(List<OrderEquivalentMaximalSegment> segments) {
        List<OrderEquivalentMaximalSegment> bottomSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment ms : segments) {
            if (ms.equals(this)) {
                continue;
            }
            if (geq(ms.y1, y1)) {
                //segment ends below or on movedSegment
                bottomSegments.add(ms);
            }
        }
        return bottomSegments;
    }

    /**
     * Holds whether this segment is a boundary segment
     *
     * @return
     */
    public boolean isBoundary() {
        if (adjacentBlockList1.isEmpty() || adjacentBlockList2.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Removes the label from allLabels
     *
     * @param label
     */
    public void removeLabel(String label) {
        if (allLabels.contains(label)) {
            allLabels.remove(label);
        }
    }

    public void addLabel(String label) {
        if (!allLabels.contains(label)) {
            allLabels.add(label);
        }
    }

    /**
     * Holds all segments that this segment is related though via a block. Only
     * used and updated for consistency checking
     */
    public List<OrderEquivalentMaximalSegment> relatedSegments = null;

    public Iterable<OrderEquivalentMaximalSegment> getRelations() {
        if (relatedSegments == null) {
            relatedSegments = new ArrayList();
        }
        return relatedSegments;
    }

    public void addRelation(OrderEquivalentMaximalSegment segment2) {
        if (relatedSegments == null) {
            relatedSegments = new ArrayList();
        }
        if (!relatedSegments.contains(segment2)) {
            relatedSegments.add(segment2);
        }
    }

    /**
     * Returns a deepcopy of the list of innersegments including the blocks
     *
     * @param innerSegments
     */
    public static List<OrderEquivalentMaximalSegment> deepCopyCompartements(List<OrderEquivalentMaximalSegment> innerSegments) {
        List<OrderEquivalentMaximalSegment> deepCopyList = new ArrayList();
        if (innerSegments.isEmpty()) {
            return deepCopyList;
        }

        List<Block> allBlocks = new ArrayList();
        for (OrderEquivalentMaximalSegment ms : innerSegments) {
            List<Block> blocks = ms.adjacentBlocks;
            for (Block b : blocks) {
                if (!allBlocks.contains(b)) {
                    allBlocks.add(b);
                }
            }
        }
        //allBlocks now contains 1 copy of each block that is adjacent to a segment in innerSegments

        List<String> labels = innerSegments.get(0).allLabels;

        //first make a copy of the blocks
        List<Block> copyBlockList = new ArrayList();
        for (Block b : allBlocks) {
            BlockCompartement copyB = new BlockCompartement(new Rectangle(b.rectangle), b.label, b.targetSize);
            copyBlockList.add(copyB);
        }
        //Copy the maximal segments
        for (OrderEquivalentMaximalSegment ms : innerSegments) {
            //use the copied block for adjacentBlockList1
            List<Block> blockList1 = new ArrayList();
            for (Block b : ms.adjacentBlockList1) {
                Block copyBlock = Block.getBlockWithLabel(copyBlockList, b.getLabel());
                blockList1.add(copyBlock);

            }
            //use the copied block for adjacentBlockList2
            List<Block> blockList2 = new ArrayList();
            for (Block b : ms.adjacentBlockList2) {
                Block copyBlock = Block.getBlockWithLabel(copyBlockList, b.getLabel());
                blockList2.add(copyBlock);
            }

            OrderEquivalentMaximalSegment copySegment = new OrderEquivalentMaximalSegment(ms.x1, ms.x2, ms.y1, ms.y2, blockList1, blockList2, labels);
            deepCopyList.add(copySegment);
            //set block adjacencies
            for (Block b : copySegment.adjacentBlockList1) {
                if (ms.vertical) {
                    b.msRight = copySegment;
                } else {
                    b.msTop = copySegment;
                }
            }
            for (Block b : copySegment.adjacentBlockList2) {
                if (ms.vertical) {
                    b.msLeft = copySegment;
                } else {
                    b.msBottom = copySegment;
                }
            }

        }

        return deepCopyList;
    }

    public void updateLabels(List<String> newLabelList) {
        allLabels = newLabelList;
    }

}
