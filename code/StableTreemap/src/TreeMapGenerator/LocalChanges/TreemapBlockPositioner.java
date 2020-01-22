/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.LocalChanges;

import Jama.Matrix;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import treemap.dataStructure.Rectangle;
import static utility.Precision.*;
import utility.Randomizer;

/**
 * Class that positions the blocks of a treemap correctly according the the
 * order-equivalance graph and the sizes of the blocks
 *
 * @author msondag
 */
public class TreemapBlockPositioner {

    private OrderEquivalenceGraph g;

    private List<Block> blocks;
    private List<OrderEquivalentMaximalSegment> segments;
    private List<OrderEquivalentMaximalSegment> innerSegments;

    private double scaleFactor;

    /**
     * Maximum amount of iterations to improve
     */
    private final int MAXITERATIONS = 200;
    /**
     * Initial value for epsilon
     */
    private final double EPSILONSTART = 1;
    /**
     * How much to decrease epsilon if we overshot or became inconsistent
     */
    private final double EPSILONDECREASE = 0.5;

    /**
     * How much to increase epsilon if the layout is valid but no on the target
     * yet
     */
    private final double EPSILONINCREASE = 1.1;

    /**
     * How much the sizes can differ before we consider the layout a good enough
     * approximation
     */
    private final double EPSILONTARGET = 0.001;

    public TreemapBlockPositioner(OrderEquivalenceGraph g) {
        this.g = g;
        blocks = g.getBlocks();
        segments = g.getMaximalSegments();
        innerSegments = g.getInnerMaximalSegments();
        scaleFactor = g.getScaleFactor();
    }

    /**
     * Fixes the sizes of the blocks and the location of the maximal segments in
     *
     */
    public void fixBlockSizes() {
        fixBlockSizes(MAXITERATIONS);
    }

    public void fixBlockSizes(int iterationAmount) {
        //First fix the positions of the maximal segments as best as we can using the sliceable layout
        fixSliceable(g.getInnerMaximalSegments(), g.originalTreeMap.getRectangle());
    }

    /**
     * Recursively finds a slice in the treemap and positions the maximal
     * segments according to the size. When no slice can be found it returns
     */
    private void fixSliceable(List<OrderEquivalentMaximalSegment> inputSegments, Rectangle inputRectangle) {

        for (OrderEquivalentMaximalSegment ms : inputSegments) {
            if (eq(ms.x1, inputRectangle.getX()) && eq(ms.x2, inputRectangle.getX2())) {
                //horizontalSlice
                horizontalSlice(ms, inputSegments, inputRectangle);
                //only 1 slice can exist
                return;
            }

            if (eq(ms.y1, inputRectangle.getY()) && eq(ms.y2, inputRectangle.getY2())) {
                //vertical slice 
                verticalSlice(ms, inputSegments, inputRectangle);
                //only 1 slice can exist
                return;
            }
        }

        //no sliceable segment found anaymore. Use the current segments to generate a new OEG
        //and solve the remainder of the problem using FixBlockSizes
        solveRemainder(inputSegments, inputRectangle);

    }

    private void horizontalSlice(OrderEquivalentMaximalSegment ms, List<OrderEquivalentMaximalSegment> inputSegments, Rectangle inputRectangle) {
        //get all segments that are to the top of ms
        List<OrderEquivalentMaximalSegment> segmentListAbove = ms.getTopSegments(inputSegments);
        //get all segments that are to the bottom of ms
        List<OrderEquivalentMaximalSegment> segmentListBelow = ms.getBottomSegments(inputSegments);
        //get the set of blocks to the top of ms
        Set<Block> blocksAbove = OrderEquivalentMaximalSegment.getAllBlocks(segmentListAbove);
        blocksAbove.addAll(ms.adjacentBlockList2);
        Set<Block> blocksBelow = OrderEquivalentMaximalSegment.getAllBlocks(segmentListBelow);
        blocksBelow.addAll(ms.adjacentBlockList1);

        if (segmentListAbove.size() != (blocksAbove.size() - 1)) {
            System.err.println("problem in horizontal slice above");
        }
        if (segmentListBelow.size() != (blocksBelow.size() - 1)) {
            System.err.println("problem in horizontal slice below");
        }

        //determine the new position for ms
        double topWeight = Block.getWeight(blocksAbove);
        double bottomWeight = Block.getWeight(blocksBelow);

        double percentage = topWeight / (topWeight + bottomWeight);
        double originalY = ms.y1;
        double newY = inputRectangle.getY() + (percentage * inputRectangle.getHeight());

        //update the position
        ms.updatePosition(ms.x1, ms.x2, newY, newY);
        //update the positions of the left and right segments
        moveSegmentsVertical(segmentListAbove, segmentListBelow, originalY, newY, inputRectangle.getY(), inputRectangle.getY2());

        //get the top and bottom inputrectangles
        double topHeight = newY - inputRectangle.getY();
        double bottomHeight = inputRectangle.getY2() - newY;
        //top
        Rectangle rAbove = new Rectangle(inputRectangle.getX(), inputRectangle.getY(), inputRectangle.getWidth(), topHeight);
        //bottom
        Rectangle rBelow = new Rectangle(inputRectangle.getX(), newY, inputRectangle.getWidth(), bottomHeight);
        //we had a slice, so we recurse
        if (segmentListAbove != null) {
            //update the positions of the blocks
            updateGraphPositions(innerSegments, blocks);
            //recurse in both sides
            fixSliceable(segmentListAbove, rAbove);
            fixSliceable(segmentListBelow, rBelow);

            //can only have one slice and recursion is done
            return;
        }
    }

    private void verticalSlice(OrderEquivalentMaximalSegment ms, List<OrderEquivalentMaximalSegment> inputSegments, Rectangle inputRectangle) {
        //get all segments that are left of ms
        List<OrderEquivalentMaximalSegment> segmentListLeft = ms.getLeftSegments(inputSegments);
        //get all segments that are right of ms
        List<OrderEquivalentMaximalSegment> segmentListRight = ms.getRightSegments(inputSegments);
        //get the set of blocks to the left of ms
        Set<Block> blocksLeft = OrderEquivalentMaximalSegment.getAllBlocks(segmentListLeft);
        blocksLeft.addAll(ms.adjacentBlockList1);
        //get the set of blocks to the right of ms
        Set<Block> blocksRight = OrderEquivalentMaximalSegment.getAllBlocks(segmentListRight);
        blocksLeft.addAll(ms.adjacentBlockList1);
        blocksRight.addAll(ms.adjacentBlockList2);

        if (segmentListLeft.size() != (blocksLeft.size() - 1)) {
            System.err.println("problem in vertical slice left");
        }
        if (segmentListRight.size() != (blocksRight.size() - 1)) {
            System.err.println("problem in vertical slice right");
        }

        //determine the new position for ms
        double leftWeight = Block.getWeight(blocksLeft);
        double rightWeight = Block.getWeight(blocksRight);

        double percentage = leftWeight / (leftWeight + rightWeight);
        double originalX = ms.x1;
        double newX = inputRectangle.getX() + (percentage * inputRectangle.getWidth());

        //update the position of this segment
        ms.updatePosition(newX, newX, ms.y1, ms.y2);
        //update the positions of the left and right segments
        moveSegmentsHorizontal(segmentListLeft, segmentListRight, originalX, newX, inputRectangle.getX(), inputRectangle.getX2());

        //get the left and right input rectangles
        double leftWidth = newX - inputRectangle.getX();
        double rightWidth = inputRectangle.getX2() - newX;
        Rectangle rLeft = new Rectangle(inputRectangle.getX(), inputRectangle.getY(), leftWidth, inputRectangle.getHeight());
        Rectangle rRight = new Rectangle(newX, inputRectangle.getY(), rightWidth, inputRectangle.getHeight());

        //we had a slice, so we recurse
        if (segmentListLeft != null) {
            //update the positions of the blocks
            updateGraphPositions(innerSegments, blocks);
            //recurse in both sides
            fixSliceable(segmentListLeft, rLeft);
            fixSliceable(segmentListRight, rRight);
        }
    }

    /**
     *
     * Moves all the segments in {@code leftSegments} and {@code rightSegments}
     * according to the movement of of originalX and newX.
     *
     * @param leftSegments  all segments to the left of the segment
     * @param rightSegments all segments to the right of the segment
     * @param originalX     the original position of the segment
     * @param newX          the minimum position of the segment
     * @param minX          the minimum x position of all the segments
     * @param maxX          the maximum x positions of all the segments
     */
    private void moveSegmentsHorizontal(
            List<OrderEquivalentMaximalSegment> leftSegments, List<OrderEquivalentMaximalSegment> rightSegments,
            double originalX, double newX, double minX, double maxX) {

        //left and rightsegments now contain the correct segments
        //The original length of the interval on the left
        double leftOriginalLength = originalX - minX;
        double leftNewLength = newX - minX;
        for (OrderEquivalentMaximalSegment ms : leftSegments) {
            //get at what percentage of the left boundary of the interval this segment is positioned
            double x1Percentage = (ms.x1 - minX) / leftOriginalLength;
            double x2Percentage = (ms.x2 - minX) / leftOriginalLength;

            double newX1 = minX + x1Percentage * leftNewLength;
            double newX2 = minX + x2Percentage * leftNewLength;

            ms.updatePosition(newX1, newX2, ms.y1, ms.y2);
        }

        //The original length of the interval on the left
        double rightOriginalLength = maxX - originalX;
        double rightNewLength = maxX - newX;
        for (OrderEquivalentMaximalSegment ms : rightSegments) {
            //get at what percentage of the right boundary of the interval this segment is positioned
            double x1Percentage = (maxX - ms.x1) / rightOriginalLength;
            double x2Percentage = (maxX - ms.x2) / rightOriginalLength;

            double newX1 = maxX - x1Percentage * rightNewLength;
            double newX2 = maxX - x2Percentage * rightNewLength;

            ms.updatePosition(newX1, newX2, ms.y1, ms.y2);
        }
    }

    /**
     *
     * Moves all the segments in {@code topSegments} and {@code bottomSegments}
     * according to the movement of of originalY and newY.
     *
     * @param topSegments    all segments to the left of the segment
     * @param bottomSegments all segments to the right of the segment
     * @param originalY      the original position of the segment
     * @param newY           the minimum position of the segment
     * @param minY           the minimum y position of all the segments
     * @param maxY           the maximum y positions of all the segments
     */
    private void moveSegmentsVertical(
            List<OrderEquivalentMaximalSegment> topSegments, List<OrderEquivalentMaximalSegment> bottomSegments,
            double originalY, double newY, double minY, double maxY) {

        //top and bottom segments now contain the correct segments
        //The original length of the interval on the top
        double topOriginalLength = originalY - minY;
        double topNewLength = newY - minY;
        for (OrderEquivalentMaximalSegment ms : topSegments) {
            //get at what percentage of the left boundary of the interval this segment is positioned
            double y1Percentage = (ms.y1 - minY) / topOriginalLength;
            double y2Percentage = (ms.y2 - minY) / topOriginalLength;

            double newY1 = minY + y1Percentage * topNewLength;
            double newY2 = minY + y2Percentage * topNewLength;

            ms.updatePosition(ms.x1, ms.x2, newY1, newY2);
        }

        //The original length of the interval on the bottom
        double bottomOriginalLength = maxY - originalY;
        double bottomNewLength = maxY - newY;
        for (OrderEquivalentMaximalSegment ms : bottomSegments) {
            //get at what percentage of the right boundary of the interval this segment is positioned
            double y1Percentage = (maxY - ms.y1) / bottomOriginalLength;
            double y2Percentage = (maxY - ms.y2) / bottomOriginalLength;

            double newY1 = maxY - y1Percentage * bottomNewLength;
            double newY2 = maxY - y2Percentage * bottomNewLength;

            ms.updatePosition(ms.x1, ms.x2, newY1, newY2);
        }
    }

    /**
     * Replaces all blocks with blockComparetments and compresses it as much as
     * possible. {@code segments} will be changed due to this.
     *
     * @param segments the list o
     * @return the remaining segments that could not be compressed
     */
    private List<OrderEquivalentMaximalSegment> compressToBlockComparements(List<OrderEquivalentMaximalSegment> segments) {
        //first replace all blocks with comparement
        Set<Block> originalBlocks = OrderEquivalentMaximalSegment.getAllBlocks(segments);
        for (Block b : originalBlocks) {
            new BlockCompartement(b);
        }

        //TODO: implement finding the segments which we can compress more efficiently. Should be able to do this
        List<OrderEquivalentMaximalSegment> compressedSegments = getCompressedSegments(segments);
        //update the labels as blocks have dissappeared
        //First generate a oeg using the compressedSegments
        Set<Block> compressedBlockList = OrderEquivalentMaximalSegment.getAllBlocks(compressedSegments);

        //need to copy the blocks to make sure they can accomodate new orderEquivalentLineSegments on the boundary while not changing the blocks themslves
        List<String> newLabelList = new ArrayList();
        for (Block b : compressedBlockList) {
            newLabelList.add(b.getLabel());
        }

        //update the labels of the segments to reflect the new blockCompartementes
        for (OrderEquivalentMaximalSegment ms : compressedSegments) {
            ms.updateLabels(newLabelList);
        }

        return compressedSegments;

    }

    /**
     * Iteratively removes all segments which have only 1 block on each side.
     * Updates the adjacencies
     *
     * @param innerSegments
     * @return
     */
    private List<OrderEquivalentMaximalSegment> getCompressedSegments(List<OrderEquivalentMaximalSegment> innerSegments) {
        boolean couldCompress = true;

        //go through the segment
        List<OrderEquivalentMaximalSegment> remainingSegments = new ArrayList(innerSegments);
        while (couldCompress == true) {//continue as long as we could compress at least one
            couldCompress = false;
            List<OrderEquivalentMaximalSegment> compressedSegments = new ArrayList();

            for (OrderEquivalentMaximalSegment ms : remainingSegments) {
                if (ms.adjacentBlockList1.size() == 1 && ms.adjacentBlockList2.size() == 1) {
                    //can compress a segment
                    couldCompress = true;

                    //get the involved blocks
                    Block b1, b2;
                    if (ms.horizontal) {
                        //bottom block
                        b1 = ms.adjacentBlockList1.get(0);
                        //top block
                        b2 = ms.adjacentBlockList2.get(0);

                    } else {
                        //left block
                        b1 = ms.adjacentBlockList1.get(0);
                        //right block
                        b2 = ms.adjacentBlockList2.get(0);
                    }

                    //generate the new blockCompartement
                    Rectangle compartementR = new Rectangle(b1.rectangle, b2.rectangle);

                    if (compartementR.getWidth() < 1E-5) {
                        System.out.println("problem");
                    }

                    String label = b1.getLabel() + ";" + b2.getLabel();
                    double targetSize = b1.getTargetSize() + b2.getTargetSize();
                    //constructor of BlockCompartement handles the update of the adjacencies of maximal segments a
                    BlockCompartement compartement = new BlockCompartement(compartementR, label, targetSize, ms);
                    //make sure we don't encounter this segment again
                    compressedSegments.add(ms);
                    break;
                }
            }
            //remove all segments that we have compresed
            remainingSegments.removeAll(compressedSegments);
        }
        return remainingSegments;
    }

    /**
     * Returns a list of new boundary segments and makes sure the endpoints of
     * the segments inside the graph are on the endpoint
     *
     * @param inputRectangle
     * @param labelList
     * @param blockList
     * @return
     */
    private List<OrderEquivalentMaximalSegment> addBoundarySegments(Rectangle inputRectangle, List<String> labelList, List<BlockCompartement> blockList) {
        //get the bounding rectangle coordinates
        double minX = inputRectangle.getX();
        double maxX = inputRectangle.getX2();
        double minY = inputRectangle.getY();
        double maxY = inputRectangle.getY2();
        //instantiate the maximal segments

        OrderEquivalentMaximalSegment msTop = new OrderEquivalentMaximalSegment(minX, maxX, minY, minY, null, null, labelList);
        OrderEquivalentMaximalSegment msBottom = new OrderEquivalentMaximalSegment(minX, maxX, maxY, maxY, null, null, labelList);
        OrderEquivalentMaximalSegment msLeft = new OrderEquivalentMaximalSegment(minX, minX, minY, maxY, null, null, labelList);
        OrderEquivalentMaximalSegment msRight = new OrderEquivalentMaximalSegment(maxX, maxX, minY, maxY, null, null, labelList);

        //update the block adjacencies
        for (BlockCompartement b : blockList) {
            if (eq(b.rectangle.getX(), minX)) {
                //adjacent to left boundary
                b.msLeft = msLeft;
                msLeft.addToAdjacencyList(false, b);
            }
            if (eq(b.rectangle.getX2(), maxX)) {
                //adjacent to right boundary
                b.msRight = msRight;
                msRight.addToAdjacencyList(true, b);
            }
            if (eq(b.rectangle.getY(), minY)) {
                //adjacent to top boundary
                b.msTop = msTop;
                msTop.addToAdjacencyList(true, b);
            }
            if (eq(b.rectangle.getY2(), maxY)) {
                //adjacent to bottom boundary
                b.msBottom = msBottom;
                msBottom.addToAdjacencyList(false, b);
            }
        }

        List<OrderEquivalentMaximalSegment> boundarySegments = new ArrayList();
        boundarySegments.add(msTop);
        boundarySegments.add(msBottom);
        boundarySegments.add(msLeft);
        boundarySegments.add(msRight);

        return boundarySegments;
    }

    private void solveRemainder(List<OrderEquivalentMaximalSegment> originalInnerSegments, Rectangle inputRectangle) {
        if (originalInnerSegments.isEmpty()) {
            return;
        }

        Set<Block> originalBlockSet = OrderEquivalentMaximalSegment.getAllBlocks(originalInnerSegments);

        //performs a deepcopy of the segments and and block adjacent to the segment. Block class will be replaced with BlockComponent class
        List<OrderEquivalentMaximalSegment> deepCopySegments = OrderEquivalentMaximalSegment.deepCopyCompartements(originalInnerSegments);

        //get the compressed segments. each segment that has exactly 1 rectangle on each side is iteratively removed
        List<OrderEquivalentMaximalSegment> compressedInnerSegments = compressToBlockComparements(deepCopySegments);

        if (compressedInnerSegments.size() == 0) {
            System.err.println("problem");
            //performs a deepcopy of the segments and and block adjacent to the segment. Block class will be replaced with BlockComponent class
            deepCopySegments = OrderEquivalentMaximalSegment.deepCopyCompartements(originalInnerSegments);

            //get the compressed segments. each segment that has exactly 1 rectangle on each side is iteratively removed
            compressedInnerSegments = compressToBlockComparements(deepCopySegments);
        }

        if (compressedInnerSegments.size() == 0) {
            System.out.println("");
        }
        //get the labelList
        List<String> compressedLabelList = compressedInnerSegments.get(0).getAllLabels();

        //get the blocks from the compressed segments
        List<BlockCompartement> compressedBlockList = OrderEquivalentMaximalSegment.getAllBlockCompartements(compressedInnerSegments);

        //add boundary segments
        List<OrderEquivalentMaximalSegment> newBoundarySegments = addBoundarySegments(inputRectangle, compressedLabelList, compressedBlockList);

        List<OrderEquivalentMaximalSegment> compressedSegments = new ArrayList();
        compressedSegments.addAll(compressedInnerSegments);
        compressedSegments.addAll(newBoundarySegments);

        //update endpoint relations for all segments
        for (OrderEquivalentMaximalSegment ms : compressedSegments) {
            ms.updateEndpointRelations();
            ms.updateAdjecancyLengthSet();
        }

        //solve the subproblem
        fixBlockSizes(inputRectangle, compressedLabelList, compressedBlockList, compressedSegments, compressedInnerSegments, EPSILONSTART, MAXITERATIONS);

        //expand all blockCompartements to apply the correct solution for all deepCopySegments and not only compressedSegments
        for (BlockCompartement b : compressedBlockList) {
            b.expandBlock();
        }

        //Update the positions of the original segments.
        for (OrderEquivalentMaximalSegment msNew : deepCopySegments) {
            OrderEquivalentMaximalSegment msOld = getSegmentWithSameLabeling(msNew, originalInnerSegments);
            if (msOld == null) {
                System.err.println("no old segment");
            }
            msOld.updatePosition(msNew.x1, msNew.x2, msNew.y1, msNew.y2);
        }

        List originalBlockList = new ArrayList(originalBlockSet);
        //update the positions of the blocks
        updateGraphAfterMove(originalInnerSegments, originalBlockList);
    }

    /**
     * Returns the segment in innersegments that has the blocks with the same
     * labels to the left/bottom and right/up as msNew
     *
     * @param msNew
     * @param innerSegments
     * @return
     */
    private OrderEquivalentMaximalSegment getSegmentWithSameLabeling(OrderEquivalentMaximalSegment msNew, List<OrderEquivalentMaximalSegment> innerSegments) {
        List<Block> blockList1 = msNew.adjacentBlockList1;
        List<Block> blockList2 = msNew.adjacentBlockList2;

        for (OrderEquivalentMaximalSegment ms : innerSegments) {
            if (ms.horizontal != msNew.horizontal) {
                //not the same orientation
                continue;
            }
            if (!Block.checkEqualBlockList(blockList1, ms.adjacentBlockList1)) {
                //list 1 does not match
                continue;
            }
            if (!Block.checkEqualBlockList(blockList2, ms.adjacentBlockList2)) {
                //list 2 does not match
                continue;
            }
            //segments are identical in blockslists and orientation
            return ms;
        }
        //no segment could be found
        return null;
    }

    private void fixBlockSizes(Rectangle inputRectangle, List<String> allLabels, List<BlockCompartement> blocks, List<OrderEquivalentMaximalSegment> segments, List<OrderEquivalentMaximalSegment> innerSegments, double epsilon, int maxIterations) {
        fixBlockSizes(inputRectangle, allLabels, blocks, segments, innerSegments, epsilon, maxIterations, false);
    }

    /**
     * Fixes the sizes of the blocks and the location of the maximal segments in
     * the graph
     *
     * @param epsilon       how large the step is we are going to take
     * @param maxIterations the amount of times we can repeat still repeat this
     * @param randomized:   Whether we have changed the weights slightly already
     *                      to account for things being EXACTLY horizontal/vertical due to equal
     *                      weights function
     */
    private void fixBlockSizes(Rectangle inputRectangle, List<String> allLabels, List<BlockCompartement> blocks, List<OrderEquivalentMaximalSegment> segments, List<OrderEquivalentMaximalSegment> innerSegments, double epsilon, int maxIterations, boolean randomized) {
        //Changes have been need to the graph and the relation are not updated, so update it manually.
        initializeRelatedSegments(segments);

        for (int iteration = 0; iteration < maxIterations; iteration++) {

//            for (BlockCompartement b : blocks) {
//                if (b.rectangle.getWidth() < 0.01 || b.rectangle.getHeight() < 0.01) {
//                    System.err.println("This block is doing weird stuff");
//                }
//            }
            double distanceBeforeFix = getDistanceFromTarget(blocks);
            if (distanceBeforeFix < EPSILONTARGET) {
                //no need for changes
                return;
            }

            assert (epsilon <= 1);

            Matrix shiftingMatrix = getShiftingMatrix(innerSegments, blocks);
            Matrix deltaMatrix = getDeltaMatrix(allLabels, blocks);
            //find how much we should move all maximal segments for this step
            Matrix moveMatrix = shiftingMatrix.solve(deltaMatrix);

            //make sure we don't move boundary maximal segments. 
            //Best way to do this is to just set them to be immovable.
            //multiply by epsilon as we have a quadratic system and thus this is not exact solution
            //we don't want to overshoot it repeatedly 
            moveMatrix = moveMatrix.times(epsilon);

            assert (moveMatrix.getColumnDimension() == 1);

            //moves all the maximal segments as specified by the solution
            for (int i = 0; i < moveMatrix.getRowDimension(); i++) {
                innerSegments.get(i).move(moveMatrix.get(i, 0));
            }
            //updates the graph to reflect the move
            updateGraphAfterMove(innerSegments, blocks);

            double distanceAfterFix = getDistanceFromTarget(blocks);
            //Must always go towards smaller distances
            //If it is not valid or we overshot, revert the move and try with a smaller epsilon
            if (distanceBeforeFix <= distanceAfterFix || !OrderEquivalenceGraph.isConsistent(inputRectangle, segments, blocks)) {
                //undo the move if it was not valid and retry with a smaller epsilon

                //move the segments back into their original position
                moveMatrix = moveMatrix.times(-1);
                for (int i = 0; i < moveMatrix.getRowDimension(); i++) {
                    innerSegments.get(i).move(moveMatrix.get(i, 0));
                }

                //updates the graph to reflect the inverted move
                updateGraphAfterMove(innerSegments, blocks);

                //retry with a smaller epsilon
                epsilon = epsilon * EPSILONDECREASE;
            } else if (distanceAfterFix > EPSILONTARGET) {
                //the move was valid but not in target range. Set epsilon slightly higher and continue
                epsilon = epsilon * EPSILONINCREASE;
            } else {
                //the move was valid and we are within the target range, we are done
                return;
            }

        }

        //out of iterations
        if (randomized == false) {
            System.err.println("Couldn't get the weights updated corretly. Percentage off targetsize: " + getDistanceFromTarget(blocks));
            System.err.println("Slightly randomizing the weights");
            for (Block b : blocks) {
                double increase = Randomizer.getRandomDouble() / 1000;
                increase = Math.min(increase, b.targetSize * increase);
                b.targetSize += increase;
            }
        } else {
            if (true) {
                System.err.println("Weights couldn't be corrected in the second round either.");
                System.exit(0);
                fixBlockSizes(inputRectangle, allLabels, blocks, segments, innerSegments, epsilon, maxIterations);
            }
        }

//        throw new RuntimeException("The application has crashed due to the blocks not having the correct area to prevent mistakes in the experiments.");
    }

    /**
     * For each order-equivalent maximal segment in {@code segments} we add the
     * relation from segment 1 to segment2
     */
    public void initializeRelatedSegments(List<OrderEquivalentMaximalSegment> segments) {
        for (int i = 0; i < segments.size() - 1; i++) {
            OrderEquivalentMaximalSegment segment1 = segments.get(i);
            for (int j = i + 1; j < segments.size(); j++) {
                OrderEquivalentMaximalSegment segment2 = segments.get(j);

                Block b = Block.getCommonBlock(segment1.adjacentBlocks, segment2.adjacentBlocks);
                if (b != null) {
                    //they are related
                    segment1.addRelation(segment2);
                    segment2.addRelation(segment1);
                }
            }
        }
    }

    /**
     * Updates the segment endpoint, the positions of the blocks and the segment
     * adjacency length after a move
     */
    private void updateGraphAfterMove(List<OrderEquivalentMaximalSegment> innerSegments, List<? extends Block> blocks) {
        //fix maximal segments endpoints to allign
        for (OrderEquivalentMaximalSegment ms : innerSegments) {
            ms.fixEndpoints();
        }
        //undo the scaling
        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            b.reScale(scaleFactor);
        }
        //update the adjacency lengths of the maximal segments
        for (OrderEquivalentMaximalSegment ms : innerSegments) {
            ms.updateAdjecancyLengthSet();
        }
    }

    /**
     * Updates the segment endpoints and the positions of the blocks.
     * Specifically does not update the adjacencyLengthSet.
     */
    private void updateGraphPositions(List<OrderEquivalentMaximalSegment> innerSegments, List<? extends Block> blocks) {
        //fix maximal segments endpoints to allign
        for (OrderEquivalentMaximalSegment ms : innerSegments) {
            ms.fixEndpoints();
        }
        //undo the scaling
        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            b.reScale(scaleFactor);
        }
    }

    private Matrix getShiftingMatrix(List<OrderEquivalentMaximalSegment> innerSegments, List<BlockCompartement> blocks) {
        if (blocks.isEmpty() || innerSegments.isEmpty()) {
            System.err.println("No blocks exists or no inner maximal segments exist");
        }
        double[][] shiftingVectors = getShiftingVectors(innerSegments, blocks);

        Matrix shiftingMatrix = new Matrix(shiftingVectors);
        shiftingMatrix = shiftingMatrix.transpose();
        //transpose it as going through the maximal segments gives the wrong order
        return shiftingMatrix;
    }

    private double[][] getShiftingVectors(List<OrderEquivalentMaximalSegment> innerSegments, List<BlockCompartement> blocks) {

        double[][] shiftingVectors = new double[innerSegments.size()][blocks.size()];
        for (int i = 0; i < innerSegments.size(); i++) {
            //ignore the outer 4 segments
            shiftingVectors[i] = innerSegments.get(i).getAdjecancyLengthSet();
        }
        return shiftingVectors;
    }

    /**
     * Gets the matrix denoting how much each block is off from it's target size
     * multiplied by the scaling factor(Target area-actual area). That is. How
     * far it is off from the target size in the coordinate system
     *
     * @return
     */
    private Matrix getDeltaMatrix(List<String> allLabels, List<BlockCompartement> blocks) {
        double totalOff = 0;

        double[][] deltaVector = new double[blocks.size()][1];
        for (int i = 0; i < allLabels.size(); i++) {
            BlockCompartement b = BlockCompartement.getBlockCompartmentWithLabel(blocks, allLabels.get(i));
            if (b == null) {
                System.out.println("dsa");
            }
            double sizeDiff = b.getTargetSize() - b.getActualSize(scaleFactor);
            deltaVector[i][0] = sizeDiff / scaleFactor;
            totalOff += sizeDiff / scaleFactor;
        }
        Matrix deltaMatrix = new Matrix(deltaVector);
        if (!eq(totalOff, 0)) {
            //if totalOff is not 0, then we can never solve it
            System.out.println("totalOff is not 0 but:" + totalOff);
        }
        return deltaMatrix;
    }

    /**
     * Determines the distance from how far the sizes of the blocks are of from
     * the actual size
     *
     * @param blocks
     * @return
     */
    public double getDistanceFromTarget(List<BlockCompartement> blocks) {

        //Use the sum of size difference to determine how far we are ooff
        double distance = 0;

        for (int i = 0; i < blocks.size(); i++) {
            BlockCompartement b = blocks.get(i);
            if (b.getTargetSize() > b.getActualSize(scaleFactor)) {
                distance += Math.abs(b.getTargetSize() - b.getActualSize(scaleFactor));
            }
        }
        return distance;
    }

}
