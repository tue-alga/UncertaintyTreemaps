/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.LocalChanges.Moves;

import TreeMapGenerator.LocalChanges.Block;
import TreeMapGenerator.LocalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.LocalChanges.OrderEquivalentMaximalSegment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import treemap.dataStructure.Rectangle;
import static utility.Precision.eq;
import static utility.Precision.ge;
import static utility.Precision.le;

/**
 * Contains helper methods for the moves.
 * @author msondag
 */
public abstract class Move {

    //the orderEquivalanceGraph on which the move is executed
    protected OrderEquivalenceGraph graph;

    public Move(OrderEquivalenceGraph graph) {
        this.graph = graph;
    }

    /**
     * Updates the graph such that each segment has the correct endpoints and
     * all blocks have the correct areas again
     */
    protected void fixGraph() {
        graph.updateEndpointsRelations();
        graph.updateAdjecencyLength();
        
        graph.fixBlockSizes();
    }

    protected Block getLeftMostBlock(List<Block> blockList) {
        assert (!blockList.isEmpty());
        double minX = Double.MAX_VALUE;
        Block leftMostBlock = null;
        for (Block b : blockList) {
            if (b.rectangle.getX() < minX) {
                minX = b.rectangle.getX();
                leftMostBlock = b;
            }
        }
        return leftMostBlock;
    }

    protected Block getRightMostBlock(List<Block> blockList) {
        assert (!blockList.isEmpty());
        double maxX = Double.MIN_VALUE;
        Block rightMostBlock = null;
        for (Block b : blockList) {
            if (b.rectangle.getX2() > maxX) {
                maxX = b.rectangle.getX2();
                rightMostBlock = b;
            }
        }
        return rightMostBlock;
    }

    protected Block getDownMostBlock(List<Block> blockList) {
        assert (!blockList.isEmpty());
        double maxY = Double.MIN_VALUE;
        Block downMostBlock = null;
        for (Block b : blockList) {
            if (b.rectangle.getY2() > maxY) {
                maxY = b.rectangle.getY2();
                downMostBlock = b;
            }
        }
        return downMostBlock;
    }

    protected Block getUpMostBlock(List<Block> blockList) {
        assert (!blockList.isEmpty());
        double minY = Double.MAX_VALUE;
        Block upMostBlock = null;
        for (Block b : blockList) {
            if (b.rectangle.getY() < minY) {
                minY = b.rectangle.getY();
                upMostBlock = b;
            }
        }
        return upMostBlock;
    }

    /**
     * Gets the block that is at position {@code position} when sorted on the x
     * coordinate
     *
     * @param adjacentBlockList2
     * @param position
     */
    protected Block getLeftBlock(List<Block> adjacentBlockList2, int position) {
        List<Block> sortedList = new ArrayList();
        sortedList.addAll(adjacentBlockList2);
        sortedList.sort(new Comparator<Block>() {

            @Override
            public int compare(Block o1, Block o2) {
                return Double.compare(o1.rectangle.getX(), o2.rectangle.getX());
            }
        });
        return sortedList.get(position);
    }

    /**
     * Gets the block that is at position {@code position} when inversely sorted
     * on the x2 coordinate
     *
     * @param adjacentBlockList2
     * @param position
     */
    protected Block getRightBlock(List<Block> adjacentBlockList2, int position) {
        List<Block> sortedList = new ArrayList();
        sortedList.addAll(adjacentBlockList2);
        sortedList.sort(new Comparator<Block>() {

            @Override
            public int compare(Block o1, Block o2) {
                return Double.compare(o2.rectangle.getX2(), o1.rectangle.getX2());
            }
        });
        return sortedList.get(position);
    }

    protected Block getBlockAbove(List<Block> blockList, OrderEquivalentMaximalSegment verticalSegment) {
        for (Block b : blockList) {
            Rectangle r = b.rectangle;

            if (le(r.getX(), verticalSegment.x1) && ge(r.getX2(), verticalSegment.x1)) {
                //segment ends between r.x1 and r.x2
                if (eq(r.getY2(), verticalSegment.y1)) {
                    //it ends directly on top
                    return b;
                }

            }
        }
        //couldn't find one
        return null;
    }

    protected Block getBlockBelow(List<Block> blockList, OrderEquivalentMaximalSegment verticalSegment) {
        for (Block b : blockList) {
            Rectangle r = b.rectangle;

            if (le(r.getX(), verticalSegment.x1) && ge(r.getX2(), verticalSegment.x1)) {
                //segment ends between r.x1 and r.x2
                if (eq(r.getY(), verticalSegment.y2)) {
                    //it ends directly on top
                    return b;
                }

            }
        }
        //couldn't find one
        return null;
    }

    protected Block getBlockLeft(List<Block> blockList, OrderEquivalentMaximalSegment horizontalSegment) {
        for (Block b : blockList) {
            Rectangle r = b.rectangle;

            if (le(r.getY(), horizontalSegment.y1) && ge(r.getY2(), horizontalSegment.y1)) {
                //segment ends between r.x1 and r.x2
                if (eq(r.getX2(), horizontalSegment.x1)) {
                    //it ends directly on top
                    return b;
                }

            }
        }
        //couldn't find one
        return null;
    }

    protected Block getBlockRight(List<Block> blockList, OrderEquivalentMaximalSegment horizontalSegment) {
        for (Block b : blockList) {
            Rectangle r = b.rectangle;

            if (le(r.getY(), horizontalSegment.y1) && ge(r.getY2(), horizontalSegment.y1)) {
                //segment ends between r.x1 and r.x2
                if (eq(r.getX(), horizontalSegment.x2)) {
                    //it ends directly on top
                    return b;
                }

            }
        }
        //couldn't find one
        return null;
    }

    /**
     * Finds the specified segment in this graph
     *
     * @param segment
     * @return
     */
    protected OrderEquivalentMaximalSegment findEquivalentSegment(OrderEquivalentMaximalSegment segment) {
        for (OrderEquivalentMaximalSegment ms : graph.getMaximalSegments()) {
            if (segment.equals(ms)) {
                return ms;
            }
        }

        return null;
    }

    protected void checkSegmentBoundary(OrderEquivalentMaximalSegment segment) {
        if (segment.adjacentBlockList1.isEmpty() || segment.adjacentBlockList2.isEmpty()) {
            System.err.println("boundary is empty");
        }
    }

}
