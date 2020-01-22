/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.LocalChanges.Moves;

import TreeMapGenerator.LocalChanges.Block;
import TreeMapGenerator.LocalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.LocalChanges.OrderEquivalentMaximalSegment;
import treemap.dataStructure.Rectangle;
import static utility.Precision.eq;

/**
 *
 * @author msondag
 */
public class StretchMove extends Move {

    public StretchMove(OrderEquivalenceGraph graph) {
        super(graph);
    }

    /**
     * Stretches the smallest element at the side of the maximalsegment to the
     * other side. Note that performing this move does not automatically correct
     * the areas in contrast to the other moves as otherwise not all layouts are
     * reachable
     *
     * @param segment
     * @param leftBottom if true, take the block at the left/bottom of the
     *                   segment, otherwise take the block at the right/top of the segment
     */
    public boolean performMove(OrderEquivalentMaximalSegment segment, boolean leftBottom) {
        //Find the correct maximal segment
        OrderEquivalentMaximalSegment equivalentSegment = findEquivalentSegment(segment);

        //assert is it not a boundary segment
        assert (equivalentSegment.isBoundary() == false);
        //assert that there are at least 3 rectangles adjacent, otherwise we can't perform a stretch move
        assert (equivalentSegment.adjacentBlocks.size() > 2);

        if (equivalentSegment == null) {
            System.err.println("segment does not exists");
            return false;
        }
        boolean movePerformed;

        if (equivalentSegment.horizontal) {
            movePerformed = moveStretchSmallestHorizontal(equivalentSegment, leftBottom);
        } else {//segment vertical
            movePerformed = moveStretchSmallestVertical(equivalentSegment, leftBottom);
        }

        if (movePerformed) {
            //fixes endpoints of the segments that change due to the move
            graph.updateEndpointsRelations();
        }
        return movePerformed;
    }

    /**
     * Stretches the smallest element at the side of the maximalsegment to the
     * other side
     *
     * @pre segment is a horizontal segment
     * @param horSegment
     * @param left       if true, take the block at the left of the segment,
     *                   otherwise
     *                   take the block at the right of the segment
     * @return whether we could perform a stretch move
     */
    private boolean moveStretchSmallestHorizontal(OrderEquivalentMaximalSegment horSegment, boolean left) {
        Block bottom, top;

        if (left) {
            bottom = getLeftMostBlock(horSegment.adjacentBlockList1);
            top = getLeftMostBlock(horSegment.adjacentBlockList2);
        } else {
            bottom = getRightMostBlock(horSegment.adjacentBlockList1);
            top = getRightMostBlock(horSegment.adjacentBlockList2);
        }

        Block smallest, largest;
        if (eq(bottom.rectangle.getWidth(), top.rectangle.getWidth())) {
            //Bottom and top block have the same width in moveStretchSmallestHorizontal, can't perform a stretch move
            return false;
        }

        if (bottom.rectangle.getWidth() > top.rectangle.getWidth()) {
            largest = bottom;
            smallest = top;
        } else {
            largest = top;
            smallest = bottom;
        }

        Rectangle smallestR, largestR;
        smallestR = smallest.rectangle;
        largestR = largest.rectangle;

        double startY = Math.min(smallestR.getY(), largestR.getY());
        double endY = Math.max(smallestR.getY2(), largestR.getY2());
        //Update the rectangles
        smallest.rectangle = new Rectangle(smallestR.getX(), startY, smallestR.getWidth(), endY - startY);
        if (left) {
            largest.rectangle = new Rectangle(smallestR.getX2(), largestR.getY(), largestR.getX2() - smallestR.getX2(), largestR.getHeight());
        } else {
            largest.rectangle = new Rectangle(largestR.getX(), largestR.getY(), smallestR.getX() - largestR.getX(), largestR.getHeight());
        }

        //remove the larger block from left/right ms of the largest block
        OrderEquivalentMaximalSegment leftMsLarge = largest.getMsLeft();
        OrderEquivalentMaximalSegment rightMsLarge = largest.getMsRight();
        if (left) {
            leftMsLarge.removeFromAdjecenyList(largest);
        } else {
            rightMsLarge.removeFromAdjecenyList(largest);
        }
        OrderEquivalentMaximalSegment leftMsSmall = smallest.getMsLeft();
        OrderEquivalentMaximalSegment rightMsSmall = smallest.getMsRight();
        //Add the largest to the right/left ms of the smallest block
        if (left) {
            rightMsSmall.addToAdjacencyList(false, largest);
        } else {
            leftMsSmall.addToAdjacencyList(true, largest);
        }

        //remove the smallest block from the horizontal segment
        horSegment.removeFromAdjecenyList(smallest);

        //add the smallest segment to top/bottom ms of the largest block
        if (largest == top) {
            largest.getMsTop().addToAdjacencyList(true, smallest);
        } else {//largest == bottom
            largest.getMsBottom().addToAdjacencyList(false, smallest);
        }

        return true;
    }

    /**
     * Stretches the smallest element at the side of the maximalsegment to the
     * other side
     *
     * @pre segment is a horizontal segment
     * @param verSegment
     * @param bottom     if true, take the block at the bottom of the segment,
     *                   otherwise take the block at the top of the segment
     * @return whether we could perform a stretch move
     */
    private boolean moveStretchSmallestVertical(OrderEquivalentMaximalSegment verSegment, boolean bottom) {
        Block left, right;

        if (bottom) {
            left = getDownMostBlock(verSegment.adjacentBlockList1);
            right = getDownMostBlock(verSegment.adjacentBlockList2);
        } else {
            left = getUpMostBlock(verSegment.adjacentBlockList1);
            right = getUpMostBlock(verSegment.adjacentBlockList2);
        }
        Block smallest, largest;
        if (left == null || left.rectangle == null || right == null || right.rectangle == null) {
            return false;
        }

        if (eq(left.rectangle.getHeight(), right.rectangle.getHeight())) {
            //Bottom and top block have the same width in moveStretchSmallestHorizontal, can't perform a stretch move            
            return false;
        }

        if (left.rectangle.getHeight() > right.rectangle.getHeight()) {
            smallest = right;
            largest = left;
        } else {
            smallest = left;
            largest = right;
        }

        Rectangle smallestR, largestR;
        smallestR = smallest.rectangle;
        largestR = largest.rectangle;

        double startX = Math.min(smallestR.getX(), largestR.getX());
        double endX = Math.max(smallestR.getX2(), largestR.getX2());
        //Update the rectangles
        smallest.rectangle = new Rectangle(startX, smallestR.getY(), endX - startX, smallestR.getHeight());
        if (bottom) {
            largest.rectangle = new Rectangle(largestR.getX(), largestR.getY(), largestR.getWidth(), smallestR.getY() - largestR.getY());
        } else {//top
            largest.rectangle = new Rectangle(largestR.getX(), smallestR.getY2(), largestR.getWidth(), largestR.getY2() - smallestR.getY2());
        }

        //remove the largest block from bottom/top ms of the smallest block
        OrderEquivalentMaximalSegment bottomMsSmallest = smallest.getMsBottom();
        OrderEquivalentMaximalSegment topMsSmallest = smallest.getMsTop();
        if (bottom) {
            bottomMsSmallest.removeFromAdjecenyList(largest);
        } else {//top
            topMsSmallest.removeFromAdjecenyList(largest);
        }
        //Add the largest to the top/bottom ms of the smallest block
        if (bottom) {
            topMsSmallest.addToAdjacencyList(false, largest);
        } else { // top
            bottomMsSmallest.addToAdjacencyList(true, largest);
        }

        //remove the smallest block from the vertical segment
        verSegment.removeFromAdjecenyList(smallest);

        //add the smallest segment to right/left ms of the largest block
        if (largest == right) {
            right.getMsRight().addToAdjacencyList(true, smallest);
        } else {//largest == left
            left.getMsLeft().addToAdjacencyList(false, smallest);
        }

        return true;
    }

}
