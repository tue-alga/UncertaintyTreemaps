/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.LocalChanges;

import java.util.List;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author msondag
 */
public class BlockCompartement extends Block {

    private OrderEquivalentMaximalSegment replacedSegment;

    private BlockCompartement child1;//leftBottom child
    private BlockCompartement child2;//rightTop child

    /**
     * Rectangle,targetSize are all irrelevant but needed due to inheritance and
     * time constraints. Changes the maximal segments adjacent to
     * replacementSegment
     *
     * @param rectangle
     * @param label
     * @param targetSize
     * @param replacedSegment Should have 1 block on each side. will be removed
     * and replaced by a single block
     */
    public BlockCompartement(Rectangle rectangle, String label, double targetSize, OrderEquivalentMaximalSegment replacedSegment) {
        super(rectangle, label, targetSize);

        this.replacedSegment = replacedSegment;


        Block b1 = replacedSegment.adjacentBlockList1.get(0);
        Block b2 = replacedSegment.adjacentBlockList2.get(0);

        child1 = (BlockCompartement) b1;
        child2 = (BlockCompartement) b2;

        this.rectangle = new Rectangle(b1.rectangle, b2.rectangle);
        this.label = b1.getLabel() + ";" + b2.getLabel();
        this.targetSize = b1.getTargetSize() + b2.getTargetSize();

        //update segment adjacencies for this block
        if (replacedSegment.horizontal) {
            msTop = b2.getMsTop();
            msBottom = b1.getMsBottom();
            msLeft = b1.getMsLeft();
            msRight = b1.getMsRight();
        } else {//vertical
            msTop = b1.getMsTop();
            msBottom = b1.getMsBottom();
            msLeft = b1.getMsLeft();
            msRight = b2.getMsRight();
        }
        //update child1 and child 2  ffrom adjacent segment
        if (msLeft != null) {
            msLeft.removeFromAdjecenyList(b1);
            msLeft.removeFromAdjecenyList(b2);
        }
        if (msRight != null) {
            msRight.removeFromAdjecenyList(b1);
            msRight.removeFromAdjecenyList(b2);
        }
        if (msTop != null) {
            msTop.removeFromAdjecenyList(b1);
            msTop.removeFromAdjecenyList(b2);
        }
        if (msBottom != null) {
            msBottom.removeFromAdjecenyList(b1);
            msBottom.removeFromAdjecenyList(b2);
        }
        //add this block to the adjacent segments
        if (msLeft != null) {
            msLeft.addToAdjacencyList(false, this);
        }
        if (msRight != null) {
            msRight.addToAdjacencyList(true, this);
        }
        if (msBottom != null) {
            msBottom.addToAdjacencyList(false, this);
        }
        if (msTop != null) {
            msTop.addToAdjacencyList(true, this);
        }
    }

    /**
     * Replaces this block with a blockcompartement and updates the adjacent
     * maximal segments
     *
     * @param b
     */
    public BlockCompartement(Block b) {
        super(b.rectangle, b.label, b.targetSize);
        //Set the adjacencies to maximal segments for this block
        msLeft = b.msLeft;
        msRight = b.msRight;
        msTop = b.msTop;
        msBottom = b.msBottom;
        //replace the adjacencies of the maximal segment
        if (msLeft != null) {
            msLeft.removeFromAdjecenyList(b);
            msLeft.addToAdjacencyList(false, this);
        }
        if (msRight != null) {
            msRight.removeFromAdjecenyList(b);
            msRight.addToAdjacencyList(true, this);
        }
        if (msTop != null) {
            msTop.removeFromAdjecenyList(b);
            msTop.addToAdjacencyList(true, this);
        }
        if (msBottom != null) {
            msBottom.removeFromAdjecenyList(b);
            msBottom.addToAdjacencyList(false, this);
        }

    }

    /**
     * Makes a deepcopy of this rectangle, Segments are not considered as a
     * deepCopyCompartements
     *
     * @param b
     */
    public BlockCompartement(BlockCompartement b) {
        super(new Rectangle(b.rectangle), b.label, b.targetSize);
        //Set the adjacencies to maximal segments for this block
        msLeft = b.msLeft;
        msRight = b.msRight;
        msTop = b.msTop;
        msBottom = b.msBottom;
    }

    /**
     * Simply assigns the value to this compartement
     *
     * @param rectangle
     * @param label
     * @param targetSize
     */
    BlockCompartement(Rectangle rectangle, String label, double targetSize) {
        super(new Rectangle(rectangle), label, targetSize);
    }

    /**
     * Replaces this block with child1 and child 2 and updates the adacencies of
     * the surrounding blocks
     */
    public void expandBlock() {
        if (child1 == null || child2 == null) {
            //nothing to expand
            return;
        }
        //remove this block from the adjacent segments
        msTop.removeFromAdjecenyList(this);
        msBottom.removeFromAdjecenyList(this);
        msLeft.removeFromAdjecenyList(this);
        msRight.removeFromAdjecenyList(this);

        //update the adjacencies of the children depending on the direction of the segment
        if (replacedSegment.horizontal) {
            //update the left
            msLeft.addToAdjacencyList(false, child1);
            msLeft.addToAdjacencyList(false, child2);
            //update the right
            msRight.addToAdjacencyList(true, child1);
            msRight.addToAdjacencyList(true, child2);

            msBottom.addToAdjacencyList(false, child1);
            msTop.addToAdjacencyList(true, child2);
        } else {
            //update the top
            msTop.addToAdjacencyList(true, child1);
            msTop.addToAdjacencyList(true, child2);

            //update the bottom
            msBottom.addToAdjacencyList(false, child1);
            msBottom.addToAdjacencyList(false, child2);

            //update the left
            msLeft.addToAdjacencyList(false, child1);
            //update the right
            msRight.addToAdjacencyList(true, child2);
        }

        //set the position of the replaced maximal segments correct
        double x1, x2, y1, y2;
        if (replacedSegment.horizontal) {
            //child 2 on top
            double percentage = child2.getTargetSize() / (child1.getTargetSize() + child2.getTargetSize());

            y1 = rectangle.getY() + rectangle.getHeight() * percentage;
            y2 = y1;
            x1 = rectangle.getX();
            x2 = rectangle.getX2();
            //set the rectangles of the children
            child1.rectangle = new Rectangle(x1, y2, x2 - x1, rectangle.getY2() - y1);
            child2.rectangle = new Rectangle(x1, rectangle.getY(), x2 - x1, y1 - rectangle.getY());

        } else {
            //child 1 on left
            double percentage = child1.getTargetSize() / (child1.getTargetSize() + child2.getTargetSize());

            x1 = rectangle.getX() + rectangle.getWidth() * percentage;
            x2 = x1;
            y1 = rectangle.getY();
            y2 = rectangle.getY2();
            //set the rectangles of the children
            child1.rectangle = new Rectangle(rectangle.getX(), y1, x1 - rectangle.getX(), y2 - y1);
            child2.rectangle = new Rectangle(x1, y1, rectangle.getX2() - x1, y2 - y1);
        }
        //set the position of the segments that was replaced
        replacedSegment.updatePosition(x1, x2, y1, y2);

        child1.expandBlock();
        child2.expandBlock();
    }

    public static BlockCompartement getBlockCompartmentWithLabel(List<BlockCompartement> blockList, String label) {
        for (BlockCompartement b : blockList) {
            if (b.getLabel().equals(label)) {
                return b;
            }
        }
        return null;
    }
}
