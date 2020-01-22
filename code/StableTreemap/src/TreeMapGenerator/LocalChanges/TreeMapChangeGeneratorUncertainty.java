package TreeMapGenerator.LocalChanges;

import TreeMapGenerator.LocalChanges.Search.SearchItem;
import TreeMapGenerator.LocalChanges.Search.SearchStructure;
import TreeMapGenerator.LocalChanges.Moves.AddMove;
import TreeMapGenerator.LocalChanges.Moves.FlipMove;
import TreeMapGenerator.LocalChanges.Moves.RemoveMove;
import TreeMapGenerator.LocalChanges.Moves.StretchMove;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import statistics.UncertaintyStatistics;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import treemap.dataStructure.Tuple;

/**
 * Generates a treemap based on an orderEquivalenceGraph
 *
 * @author max
 */
public class TreeMapChangeGeneratorUncertainty {

    public enum MoveName {
        FLIPMOVE, OneSidedFalse,
        StretchTRUE, STRETCHFALSE,
        SingleElementTrue, SingleElementFalse
    }

    /**
     * The oeg of the treemap
     */
    protected OrderEquivalenceGraph graph;
    /**
     * The treemap which we are changing
     */
    protected TreeMap currentTreeMap;

    /**
     * How much weight 1 pixel of area has in comparison to the targetweight
     */
    protected double scaleFactor;

    /** How much to improve the maximum aspect ratio in percentage and absolute */
    private final double IMPROVEVALUE = 4;

    private boolean percentageUs;
    UncertaintyStatistics us;

    /**
     * Creates a deepcopy of the treemap and initialize the oeg
     *
     * @param tm
     */
    public TreeMapChangeGeneratorUncertainty(TreeMap tm, boolean percentageUs) {
        this.percentageUs = percentageUs;
        this.us = new UncertaintyStatistics(percentageUs);

        currentTreeMap = tm.deepCopy();
        graph = new OrderEquivalenceGraph(currentTreeMap);

        initialize(graph);
    }

    /**
     * Creates a (deepcopy) of the treemap and initialize the oeg
     *
     * @param tm
     * @param deepCopy
     */
    public TreeMapChangeGeneratorUncertainty(TreeMap tm, boolean deepCopy, boolean percentageUs) {
        this.percentageUs = percentageUs;
        this.us = new UncertaintyStatistics(percentageUs);

        if (deepCopy) {
            currentTreeMap = tm.deepCopy();
        } else {
            currentTreeMap = tm;
        }
        graph = new OrderEquivalenceGraph(currentTreeMap);

        initialize(graph);
    }

    /**
     * Faster method of constructing a TMCG by taking the copies as the treemap
     * and graph instead of regenerating them
     *
     * @param parentTmCopy
     * @param oegCopy
     */
    public TreeMapChangeGeneratorUncertainty(TreeMap parentTmCopy, OrderEquivalenceGraph oegCopy, boolean percentageUs) {
        this.percentageUs = percentageUs;
        this.us = new UncertaintyStatistics(percentageUs);
        currentTreeMap = parentTmCopy;
        graph = oegCopy;
        scaleFactor = graph.getScaleFactor();
    }

    /**
     * Gets the scalefactor from the graph and initializes it
     *
     * @param graph
     */
    public void initialize(OrderEquivalenceGraph graph) {
        this.graph = graph;
        scaleFactor = graph.getScaleFactor();
    }

    /**
     * Removes {@code dm} from the treemap and makes sure the resulting treemap
     * is an actual treemap.
     *
     * @param dm
     * @return
     */
    public TreeMap performRemove(DataMap dm) {
        Block removeBlock = null;
        for (Block b : graph.getBlocks()) {
            if (b.getLabel().equals(dm.getLabel())) {
                removeBlock = b;
                break;
            }
        }
        if (removeBlock == null) {
            System.out.println("going wrong");
        }
        //remove the block
        RemoveMove m = new RemoveMove(graph);
        m.removeBlock(removeBlock);
        graph.removeFromTreeMap(dm.getLabel());
        return graph.convertOriginalTreeMap();
    }

    /**
     * Performs all combinations of moves up to {@code maxDepth} moves. If t he
     * maximum aspect ratio in the graph at any time is improved by at least
     * {@code IMPROVEPERCENTAGE} procent we update the graph to the graph with
     * the best maximum aspect ratio within these combinations
     *
     *
     * @param maxDepth the maximal depth for the moves
     * @return
     */
    public TreeMap performLookAheadMoveNoTarget(int maxDepth) {
        double currentA = 500000;

        int height = currentTreeMap.getHeight();

        /**
         * We want to improve the aspect ratios in the treemap such that the
         * average aspect ratio decreases enough. We want to improve the total
         * aspect ratio by at least IMPROVEVALUE*height. improvement is based on
         * height to make sure that on higher height we are more carefull with
         * performing moves
         */
        double targetA = currentA;

        double currentPenalty = 0;
        for (TreeMap child : currentTreeMap.getChildren()) {
            currentPenalty += us.getPenalty(child.getRectangle(), child.getSdRectangle(), currentTreeMap.getSdRectangle());
        }

        //the higher up in the tree, the more the aspect ratio must improve before we make a change
        OrderEquivalenceGraph oeg = performLookAheadMove(maxDepth, targetA, currentPenalty, deepCopyGraph(graph));
        return oeg.convertOriginalTreeMap();
    }

    /**
     * Splits the block of {@code originalDm} in the treemap into two to add
     * {@code addDm} to the treemap. {@code originalDm} will be put on one side
     * of the split and {@code addDm} on the other side.
     *
     * @param originalDm The original block in the treemap that will be split
     * @param addDm
     * @param horizontal whether to split {@code originalDm} horizontally or
     *                   vertically
     * @return
     */
    public TreeMap performAdd(DataMap originalDm, DataMap addDm, boolean horizontal) {
        Block originalBlock = null;
        for (Block b : graph.getBlocks()) {
            if (b.getLabel().equals(originalDm.getLabel())) {
                originalBlock = b;
            }
        }

        Block addBlock = new Block(null, addDm.getLabel(), addDm.getTargetSize());

        //add the block to the graph using a move
        AddMove m = new AddMove(graph);
        m.addBlock(originalBlock, addBlock, horizontal);

        //Add the block to the treemap with the correct rectangle
        graph.addToTreeMap(addDm, addBlock.rectangle);

        //Make sure to fix the positions of the blocks to corresponds with the sizes
        graph.fixBlockSizes();

        return graph.convertOriginalTreeMap();
    }

    /**
     * Makes sure that each rectangle in the treemap has the correct size based
     * on the targetSize specified while keeping the same oeg
     *
     * @return
     */
    public TreeMap fixPositions() {
        //fix the position of the blocks based on the new weights
        graph.fixBlockSizes();

        //will store the updated children
        List<TreeMap> updatedChildren = new ArrayList();
        for (TreeMap childTm : currentTreeMap.getChildren()) {

            //set the rectangle position of the updated child in the correct position
            Block b = graph.getBlockWithLabel(childTm.getLabel());
            childTm.updateRectangle(b.rectangle);
            //recurse into the children to fix the positions there
            if (childTm.hasChildren()) {
                TreeMapChangeGeneratorUncertainty tmCG = new TreeMapChangeGeneratorUncertainty(childTm, percentageUs);
                childTm = tmCG.fixPositions();
            }
            updatedChildren.add(childTm);
        }
        for (TreeMap childTm : updatedChildren) {
            currentTreeMap.replaceTreemap(childTm);
        }

        return currentTreeMap;
    }

    /**
     * Returns the oeg
     *
     * @return
     */
    public OrderEquivalenceGraph getGraph() {
        return graph;
    }

    /**
     *
     * Checks the results of the move. replaces {@code bestGraph} by it if
     * it improved enough and added the updates segment to the {@code queue}.
     * Returns the
     * current bestGraph
     *
     *
     * @param graphBeforeMove the graph before the move was performed
     * @param graphAfterMove  the graph after the move was performed
     * @param bestGraph       the current best graph
     * @param movesLeft       the amount of moves left at the current iteration
     * @return the new bestGraph
     */
    protected OrderEquivalenceGraph checkMove(OrderEquivalenceGraph graphBeforeMove, OrderEquivalenceGraph graphAfterMove, OrderEquivalenceGraph bestGraph, SearchStructure searchS, double targetUs, final int movesLeft) {

        /**
         * Updates the areas of the blocks into the graph to determine the
         * aspect ratio It uses a deepcopy to make sure the graph is not
         * changed. we can only reach all possible layouts if we have both the
         * stretch move and the flip move as options and we do not update the
         * areas after a stretch move
         */
        OrderEquivalenceGraph deepCopy = graphAfterMove.deepCopy();
        deepCopy.updateEndpointsRelations();
        deepCopy.updateAdjecencyLength();
        deepCopy.fixBlockSizes(5);

//        double aspectRatioImprovement = bestGraph.getMaxLeafAspectRatio() - deepCopy.getMaxLeafAspectRatio();
        double aspectRatioImprovement = deepCopy.getMaxAspectRatio();
        if (aspectRatioImprovement < 5) {
            //There is a non trivial improvement in aspect ratio
            TreeMap tmAfter = deepCopy.convertOriginalTreeMap();

            double usAfter = 0;
            for (TreeMap child : tmAfter.getChildren()) {
                us.getPenalty(child.getRectangle(), child.getSdRectangle(), tmAfter.getSdRectangle());
            }
//            double usAfter = us.getOverlapScore(tmAfter).sum;
            if (usAfter < targetUs) {
                bestGraph = deepCopy;
            }
        }
        // add the move to the bfs with the local neighbourhoor
        List<OrderEquivalentMaximalSegment> updatedSegments = getUpdatedSegments(graphBeforeMove, graphAfterMove);

        //make sure not the change movesLeft but alter a copy
        int movesRemaining = movesLeft - 1;

        searchS.insertOption(new SearchItem(graphAfterMove, movesRemaining, updatedSegments, deepCopy.getMaxAspectRatio()));
        return bestGraph;
    }

    /**
     * Performs the series of moves that lead to an optimal improvement of the
     * maximal aspect ratio within movesLeft amount.
     *
     * @param maxDepth The maximum amount of moves we perform
     * @param targetA  The maximum aspect ratio in the graph required to
     *                 continue
     * @param oeg      The graph which we are performing the moves on
     * @return The oeg of the graph with the minimum maximal
     *         aspect ratio if it improved enough, or {@code oeg} if it did not
     */
    protected OrderEquivalenceGraph performLookAheadMove(int maxDepth, double targetA, double targetUs, OrderEquivalenceGraph oeg) {
        //Queue with tuple consisting of orderEquivalenceGraoh and the amount of moves left and the inner maximal segments to consider
        PriorityQueue<SearchItem> queue = new PriorityQueue();
        queue.add(new SearchItem(deepCopyGraph(oeg), maxDepth, oeg.getInnerMaximalSegments(), oeg.getMaxAspectRatio()));
        SearchStructure searchS = new SearchStructure(queue);
        OrderEquivalenceGraph bestGraph = oeg;
        SearchItem item;//oeg,movesleft,innermaximalsegments,maxAspectRatio
        //BFS throught the moves

        while ((item = searchS.getNext()) != null) {

            int movesLeft = item.movesLeft;
            if (movesLeft <= 0) {
                continue;
            }
            OrderEquivalenceGraph g = item.oeg;

            List<OrderEquivalentMaximalSegment> segmentsToConsider = item.innerMaximalSegments;
            OrderEquivalenceGraph copyGraph;
            //try all move we can perform
            for (OrderEquivalentMaximalSegment innerSegment : segmentsToConsider) {
                //check which move is possible

                if (innerSegment.adjacentBlockList1.size() == 1 && innerSegment.adjacentBlockList2.size() == 1) {
                    //only one segment on each side, perform flip move

                    copyGraph = deepCopyGraph(g);
                    FlipMove flipMove = new FlipMove(copyGraph);
                    flipMove.performMove(innerSegment);
                    bestGraph = checkMove(g, copyGraph, bestGraph, searchS, targetUs, movesLeft);
                } else {
                    //At least one side of the segment has more than 1 block. Perform stretch moves
                    copyGraph = deepCopyGraph(g);

                    StretchMove stretchMove = new StretchMove(copyGraph);
                    boolean movePerformed = stretchMove.performMove(innerSegment, true);
                    if (movePerformed) {
                        bestGraph = checkMove(g, copyGraph, bestGraph, searchS, targetUs, movesLeft);
                    }

                    //also try the stretch move on the other side
                    copyGraph = deepCopyGraph(g);
                    stretchMove = new StretchMove(copyGraph);
                    movePerformed = stretchMove.performMove(innerSegment, false);
                    if (movePerformed) {
                        bestGraph = checkMove(g, copyGraph, bestGraph, searchS, targetUs, movesLeft);
                    }
                }
            }
        }

        //return the best solution
        if (bestGraph.getTotalAspectRatio() < targetA) {
            //improved enough
            return bestGraph;
        } else {
            //it did not improve enough
            return oeg;
        }
    }

    /**
     * returns all maximal segments for which a block has changed the adjacency.
     * Segments are taken from the new graph
     *
     * @param oldGraph
     * @param newGraph
     * @return
     */
    protected List<OrderEquivalentMaximalSegment> getUpdatedSegments(OrderEquivalenceGraph oldGraph, OrderEquivalenceGraph newGraph) {

        /**
         * For each segment we are going to check if there is an equivalent
         * segments in the new graph(same orientation and adjacent blocks). If
         * there is no such segment then it has changed.
         *
         */
        List<OrderEquivalentMaximalSegment> updatedSegments = new ArrayList();

        for (OrderEquivalentMaximalSegment msNew : newGraph.getInnerMaximalSegments()) {
            boolean foundEquivalentSegment = false;
            for (OrderEquivalentMaximalSegment msOld : oldGraph.getInnerMaximalSegments()) {
                //check whether the segments are the same
                if (msOld.horizontal != msNew.horizontal) {
                    continue;
                }
                if (msOld.adjacentBlockList1.size() != msNew.adjacentBlockList1.size()) {
                    continue;
                }
                if (msOld.adjacentBlockList2.size() != msNew.adjacentBlockList2.size()) {
                    continue;
                }
                //check same blocks in list 1
                boolean allBlocksFound = true;
                for (Block bNew : msNew.adjacentBlockList1) {
                    boolean foundBlock = false;
                    for (Block bOld : msOld.adjacentBlockList1) {
                        if (bNew.getLabel().equals(bOld.getLabel())) {
                            foundBlock = true;
                            break;
                        }
                    }
                    if (!foundBlock) {
                        allBlocksFound = false;
                        break;
                    }
                }
                //check same blocks in list 2
                for (Block bNew : msNew.adjacentBlockList2) {
                    boolean foundBlock = false;
                    for (Block bOld : msOld.adjacentBlockList2) {
                        if (bNew.getLabel().equals(bOld.getLabel())) {
                            foundBlock = true;
                            break;
                        }
                    }
                    //check if we missed a block
                    if (!foundBlock) {
                        allBlocksFound = false;
                        break;
                    }
                }
                if (allBlocksFound) {
                    //found equivalent segment
                    foundEquivalentSegment = true;
                }
            }
            if (!foundEquivalentSegment) {
                //this segment is new
                updatedSegments.add(msNew);
            }
        }
        return updatedSegments;
    }

    /**
     * Returns a deepcopy of the current graph.
     *
     * @return
     */
    protected OrderEquivalenceGraph deepCopyGraph() {
        return deepCopyGraph(graph);
    }

    /**
     * Returns a deepcopy of graph g
     *
     * @param g
     * @return
     */
    protected OrderEquivalenceGraph deepCopyGraph(OrderEquivalenceGraph g) {
        OrderEquivalenceGraph newGraph = g.deepCopy();
        return newGraph;
    }
}
