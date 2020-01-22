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
import java.util.List;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author msondag
 */
public class FlipMove extends Move {

    public FlipMove(OrderEquivalenceGraph graph) {
        super(graph);
    }

    public void performMove(OrderEquivalentMaximalSegment segment) {
        OrderEquivalentMaximalSegment equivalentSegment = findEquivalentSegment(segment);
        if (equivalentSegment == null) {
            System.err.println("No equivalent segment could be found" + segment);
            return;
//            throw new RuntimeException();
        }
        //can't flip boundary segment
        assert (equivalentSegment.isBoundary() == false);
        //can only flip if there are exactly 2 block adjacent
        assert (equivalentSegment.adjacentBlockList1.size() == 1 && equivalentSegment.adjacentBlockList2.size() == 1);

        //flip the blocks
        if (equivalentSegment.horizontal) {
            flipHorizontal(equivalentSegment);
        } else {
            flipVertical(equivalentSegment);
        }
        graph.updateEndpointsRelations();
    }

    /**
     * Flips a horizontal segment using a counter-clockwise turn
     *
     * @param segment
     */
    private void flipHorizontal(OrderEquivalentMaximalSegment segment) {
        //segment is one sided on both sides
        assert (segment.adjacentBlockList1.size() == 1 && segment.adjacentBlockList2.size() == 1);
        //assert horizontal
        assert (segment.horizontal);

        Block topBlock = segment.getLeftTopBlock();
        Block bottomBlock = segment.getLeftBottomBlock();

        double x1 = topBlock.rectangle.getX();
        double x2 = topBlock.rectangle.getX2();
        double width = x2 - x1;
        double y1 = topBlock.rectangle.getY();
        double y2 = bottomBlock.rectangle.getY2();
        double height = y2 - y1;

        double topRatio = topBlock.getTargetSize() / (topBlock.getTargetSize() + bottomBlock.getTargetSize());
        double middleX = x1 + topRatio * width;

        Rectangle leftRectangle = new Rectangle(x1, y1, middleX - x1, height);
        Rectangle rightRectangle = new Rectangle(middleX, y1, x2 - middleX, height);

        topBlock.rectangle = leftRectangle;
        bottomBlock.rectangle = rightRectangle;

        //block rectangles are updated, we now update the segments
        OrderEquivalentMaximalSegment leftMs, rightMs, topMs, bottomMs;
        leftMs = topBlock.getMsLeft();
        rightMs = topBlock.getMsRight();
        topMs = topBlock.getMsTop();
        bottomMs = bottomBlock.getMsBottom();

        Block leftBlock = topBlock;
        Block rightBlock = bottomBlock;
        //Update the adjacencies of the other segments

        leftMs.removeFromAdjecenyList(rightBlock);
        rightMs.removeFromAdjecenyList(leftBlock);

        topMs.addToAdjacencyList(true, rightBlock);
        bottomMs.addToAdjacencyList(false, leftBlock);

        //Remove the turned segment
        graph.removeSegment(segment);
        //add the new segment
        List<Block> leftBlocks = new ArrayList();
        leftBlocks.add(leftBlock);
        List<Block> rightBlocks = new ArrayList();
        rightBlocks.add(rightBlock);

        OrderEquivalentMaximalSegment middleSegment = new OrderEquivalentMaximalSegment(middleX, middleX, y1, y2, leftBlocks, rightBlocks, segment.getAllLabels());
        graph.addSegment(middleSegment);
    }

    /**
     * Flips a vertical segment using a counter-clockwise turn
     *
     * @param segment
     */
    private void flipVertical(OrderEquivalentMaximalSegment segment) {
        //segment is one sided on both sides
        assert (segment.adjacentBlockList1.size() == 1 && segment.adjacentBlockList2.size() == 1);
        //assert horizontal
        assert (segment.vertical);

        Block leftBlock = segment.getLeftTopBlock();
        Block rightBlock = segment.getRightTopBlock();

        double x1 = leftBlock.rectangle.getX();
        double x2 = rightBlock.rectangle.getX2();
        double width = x2 - x1;
        double y1 = leftBlock.rectangle.getY();
        double y2 = leftBlock.rectangle.getY2();
        double height = y2 - y1;

        double topRatio = rightBlock.getTargetSize() / (leftBlock.getTargetSize() + rightBlock.getTargetSize());
        double middleY = y1 + topRatio * height;

        Rectangle topRectangle = new Rectangle(x1, y1, width, middleY - y1);
        Rectangle bottomRectangle = new Rectangle(x1, middleY, width, y2 - middleY);

        rightBlock.rectangle = topRectangle;
        leftBlock.rectangle = bottomRectangle;

        //block rectangles are updated, we now update the segments
        OrderEquivalentMaximalSegment leftMs, rightMs, topMs, bottomMs;

        topMs = leftBlock.getMsTop();
        bottomMs = leftBlock.getMsBottom();
        leftMs = leftBlock.getMsLeft();
        rightMs = rightBlock.getMsRight();

        Block topBlock = rightBlock;
        Block bottomBlock = leftBlock;
        //Update the adjacencies of the other segments

        topMs.removeFromAdjecenyList(bottomBlock);
        bottomMs.removeFromAdjecenyList(topBlock);

        rightMs.addToAdjacencyList(true, leftBlock);
        leftMs.addToAdjacencyList(false, rightBlock);

        //Remove the turned segment
        graph.removeSegment(segment);
        //add the new segment
        List<Block> bottomBlocks = new ArrayList();
        bottomBlocks.add(bottomBlock);
        List<Block> topBlocks = new ArrayList();
        topBlocks.add(topBlock);

        OrderEquivalentMaximalSegment middleSegment = new OrderEquivalentMaximalSegment(x1, x2, middleY, middleY, bottomBlocks, topBlocks, segment.getAllLabels());
        graph.addSegment(middleSegment);
    }

}
