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
public class AddMove extends Move {

    public AddMove(OrderEquivalenceGraph graph) {
        super(graph);
    }

    /**
     * Adds {@code addBlock} to the graph on the position of
     * {@code originalBlock}.
     *
     *
     * @param originalBlock
     * @param addBlock
     * @param horizontal whether the relation between addBlock and originalBlock
     * is horizontal or vertical
     */
    public void addBlock(Block originalBlock, Block addBlock, boolean horizontal) {
        //Adds the block and its label to the graph
        graph.addBlock(addBlock);
        
        Rectangle originalNewRectangle;
        Rectangle addNewRectangle;
        OrderEquivalentMaximalSegment newSegment;

        ArrayList<Block> originalBlocks = new ArrayList();
        originalBlocks.add(originalBlock);
        ArrayList<Block> addBlocks = new ArrayList();
        addBlocks.add(addBlock);

        List<String> allLabels = graph.getAllLabels();
        //Label is already added in addBlock. no need to manually add it

        if (horizontal) {
            //put the original on top
            double x, y, y2, width, height;
            x = originalBlock.rectangle.getX();
            y = originalBlock.rectangle.getY();
            y2 = originalBlock.rectangle.getY2();

            width = originalBlock.rectangle.getWidth();
            height = originalBlock.rectangle.getHeight() * originalBlock.getTargetSize() / (originalBlock.getTargetSize() + addBlock.getTargetSize());

            originalNewRectangle = new Rectangle(x, y, width, height);
            addNewRectangle = new Rectangle(x, y + height, width, y2 - (y + height));

            originalBlock.rectangle = originalNewRectangle;
            addBlock.rectangle = addNewRectangle;

            //make the new segment
            newSegment = new OrderEquivalentMaximalSegment(x, x + width, y + height, y + height, addBlocks, originalBlocks, allLabels);

            //update the adjacencies for the maximal segments to include addblock
            originalBlock.getMsBottom().addToAdjacencyList(false, addBlock);
            originalBlock.getMsLeft().addToAdjacencyList(false, addBlock);
            originalBlock.getMsRight().addToAdjacencyList(true, addBlock);

            //Remove the old adjaceny
            originalBlock.getMsBottom().removeFromAdjecenyList(originalBlock);

        } else {
            //put the original to the left
            double x, x2, y, width, height;
            if (originalBlock == null) {
                System.err.println("originalBlock is null");
            }
            if (originalBlock.rectangle == null) {
                System.err.println("originalBlockRectangle is null");
            }

            x = originalBlock.rectangle.getX();
            x2 = originalBlock.rectangle.getX2();
            y = originalBlock.rectangle.getY();
            width = originalBlock.rectangle.getWidth() * originalBlock.getTargetSize() / (originalBlock.getTargetSize() + addBlock.getTargetSize());

            height = originalBlock.rectangle.getHeight();
            originalNewRectangle = new Rectangle(x, y, width, height);
            addNewRectangle = new Rectangle(x + width, y, x2 - (x + width), height);
            //make the new segment
            originalBlock.rectangle = originalNewRectangle;
            addBlock.rectangle = addNewRectangle;

            newSegment = new OrderEquivalentMaximalSegment(x + width, x + width, y, y + height, originalBlocks, addBlocks, allLabels);

            //update the adjacencies for the maximal segments to include addblock   
            originalBlock.getMsBottom().addToAdjacencyList(false, addBlock);
            originalBlock.getMsTop().addToAdjacencyList(true, addBlock);
            originalBlock.getMsRight().addToAdjacencyList(true, addBlock);

            //Remove the old adjaceny
            originalBlock.getMsRight().removeFromAdjecenyList(originalBlock);

        }
        graph.addSegment(newSegment);
        //sets the rectangles for the blocks
        //update the targetsize

        //update the graph
        fixGraph();
    }

}
