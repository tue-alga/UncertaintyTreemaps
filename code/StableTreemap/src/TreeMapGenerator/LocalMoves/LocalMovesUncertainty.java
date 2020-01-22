package TreeMapGenerator.LocalMoves;

import TreeMapGenerator.ApproximationTreeMapUncertaintyV3;
import TreeMapGenerator.LocalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.LocalChanges.TreeMapChangeGeneratorUncertainty;
import TreeMapGenerator.TreeMapGenerator;
import java.util.ArrayList;
import java.util.List;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import treemap.dataStructure.Tuple;
import utility.Randomizer;

/**
 *
 * @author max
 */
public class LocalMovesUncertainty implements TreeMapGenerator {

    /**
     * Whether debug statement will be printed
     */
    private final boolean DEBUG = false;

    /**
     * The maximal amount of moves we will use to see if it can be improved
     */
    int maxMoveAmount = 4;//
    /**
     * The maximal amount of time we will repeat searching and performing a
     * sequence
     * of moves.
     */
    int repeatAmount = 1;//
    /**
     * The current datamap we are working on
     */
    DataMap currentDataMap = null;
    /**
     * The current treemap we are working on
     */
    TreeMap currentTreeMap = null;
    /**
     * Holds whether we perform no moves
     */
    boolean noMoves = false;

    boolean percentageUs = true;

    public LocalMovesUncertainty(boolean noMoves) {
        if (noMoves) {
            //we are not performing any moves
            maxMoveAmount = 0;
            repeatAmount = 0;
        }
        this.noMoves = noMoves;
        if (DEBUG) {
            System.out.println("maxMoveAmount: " + maxMoveAmount);
        }
    }

    public LocalMovesUncertainty(boolean noMoves, boolean percentageUs) {
        this.percentageUs = percentageUs;
        if (noMoves) {
            //we are not performing any moves
            maxMoveAmount = 0;
            repeatAmount = 0;
        }
        this.noMoves = noMoves;
        if (DEBUG) {
            System.out.println("maxMoveAmount: " + maxMoveAmount);
        }
    }

    /**
     * Perform the default amount of moves and repitition
     */
    public LocalMovesUncertainty() {
        //perform moves by default
        this(true);
    }

    public LocalMovesUncertainty(int maxMoveAmount, int repeatAmount) {
        this.maxMoveAmount = maxMoveAmount;
        this.repeatAmount = repeatAmount;
        this.noMoves = false;
    }

    @Override
    public TreeMap generateTreeMap(DataMap newDm, Rectangle inputRectangle) {

        System.out.println("generatedATreemap");
        if (currentDataMap == null) {
            currentTreeMap = generateNewTreeMap(newDm, inputRectangle);
        } else {

            //make sure the previous treemap is not changed for the animation
            currentTreeMap = currentTreeMap.deepCopy();
            if (!currentDataMap.hasEqualStructure(newDm)) {
                //the structure of the new treemap is different. handle deletions and addition first
                updateDeletionTreeMap(currentDataMap, newDm);
                updateAdditionTreeMap(currentDataMap, newDm);
            }

            //structures are the same, so we update the targetsizes
            currentTreeMap.setTargetSizes(newDm);

            //update the positions to get a correct initial treemap with the correct sizes
            if (currentTreeMap.getAllLeafs().size() > 1) {
                TreeMapChangeGeneratorUncertainty tmcg = new TreeMapChangeGeneratorUncertainty(currentTreeMap, percentageUs);
                currentTreeMap = tmcg.fixPositions();
            }
        }
        currentDataMap = newDm;
        //DataMap now has the same structure as the treemap so we can use local moves to improve it
        for (int i = 0; i < repeatAmount; i++) {
            currentTreeMap = updateCurrentTreeMap();
        }
        return currentTreeMap;
    }

    /**
     * Generates the initial treemap using the approximation algorithm
     *
     * @param dataMap
     * @param treeMapRectangle
     * @return
     */
    private TreeMap generateNewTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        ApproximationTreeMapUncertaintyV3 approximationTreeMap = new ApproximationTreeMapUncertaintyV3(3, percentageUs);
        return approximationTreeMap.generateTreeMap(dataMap, treeMapRectangle);
    }

    /**
     * Updates {@code currentTreeMap} using local moves. Performs moves on each
     * level inthe treemap
     *
     * @param dataMap
     * @return
     */
    private TreeMap updateCurrentTreeMap() {
        TreeMap updatedTreeMap = updateTreeMap(currentTreeMap, maxMoveAmount);
        return updatedTreeMap;
    }

    /**
     * Updates the treemap {@code root} using local moves. Performs moves on
     * each level in
     * the treemap
     *
     * @param root       The root of the subtreemap
     * @param moveAmount the maximal amount of moves to perform per hierarchy
     *                   level
     * @return
     */
    private TreeMap updateTreeMap(TreeMap root, int moveAmount) {
        return updateTreeMap(root, null, moveAmount);
    }

    /**
     * Updates the treemap using local moves. Performs moves on each level in
     * the treemap
     *
     * @param tm
     * @param parentTm   null if {@code tm} is the root, the parent otherwise
     * @param moveAmount
     * @return
     */
    private TreeMap updateTreeMap(TreeMap tm, TreeMap parentTm, int moveAmount) {
        if (!tm.hasChildren()) {
            //root is a leaf, can't change the layout
            return tm;
        }

        //try performing moves on this level. If moves are performed all rectangles
        //under this level also have the correct position
        TreeMapChangeGeneratorUncertainty tmCG = new TreeMapChangeGeneratorUncertainty(tm, percentageUs);
        tm = tmCG.performLookAheadMoveNoTarget(moveAmount);
        if (parentTm == null) {
            currentTreeMap = tm;
        } else {
            parentTm.replaceTreemap(tm);
        }

        //recurse in the children. 
        //Need to use the labels as identifiers instead of a single loop as treemaps are being replaced
        //and thus the order of the children changes
        List<String> childLabels = new ArrayList();
        for (TreeMap child : tm.getChildren()) {
            childLabels.add(child.getLabel());
        }

        for (String childLabel : childLabels) {
            TreeMap child = tm.getChildWithLabel(childLabel);
            updateTreeMap(child, tm, moveAmount);
        }

        return tm;
    }

    /**
     * Handles the deletion of items in the treemap. Also removes the deleted
     * items from the datamap
     *
     * @param newDm
     */
    private void updateDeletionTreeMap(DataMap currentDm, DataMap newDm) {
        //We first identify the datamaps that are in one treemap but not in the other treemap on the level
        if (!currentDm.hasChildren()) {
            return;
        }

        List<DataMap> currentItems = new ArrayList(currentDm.getChildren());
        List<DataMap> newItems = new ArrayList(newDm.getChildren());

        //holds all the items that were deleted from currentItems
        List<DataMap> deletedItems = getAddedItems(newItems, currentItems);
        List<DataMap> undeletedItems = new ArrayList(currentItems);
        undeletedItems.removeAll(deletedItems);

        if (undeletedItems.isEmpty()) {
            //all children of currentDm will be deleted,
            TreeMap parent = currentTreeMap.getTreeMapWithLabel(currentDm.getLabel());
            parent.removeChildrenAndUpdateSize(newDm, parent.getTargetSize());
            currentDm.removeChildrenAndUpdate(newDm);

            return;
        }
        for (DataMap deleteDm : deletedItems) {
            //delete the children of dm one by one

            //parent changes after every iteration, so we need to do this in the loop
            TreeMap parentTm = currentTreeMap.getTreeMapWithLabel(currentDm.getLabel());

            //remove it from the treemap
            TreeMapChangeGeneratorUncertainty tmCG = new TreeMapChangeGeneratorUncertainty(parentTm, percentageUs);
            TreeMap newParentTm = tmCG.performRemove(deleteDm);

            if (parentTm == currentTreeMap) {
                //there is no parent, replace the entire treemap
                currentTreeMap = newParentTm;
            } else {
                currentTreeMap.replaceTreemap(newParentTm);
            }
            //remove it from the datamap
            currentDataMap.removeDataMap(deleteDm);

        }
        //recurse in the undeleted items to update changes lower in the tree
        for (DataMap dmOld : undeletedItems) {
            DataMap dmNew = newDm.getDataMapWithLabel(dmOld.getLabel());
            updateDeletionTreeMap(dmOld, dmNew);
        }
    }

    /**
     * Gets which items were added from currentItems to newItems
     *
     * @param currentItems
     * @param newItems
     * @return
     */
    private List<DataMap> getAddedItems(List<DataMap> currentItems, List<DataMap> newItems) {
        List<DataMap> itemsToBeAdded = new ArrayList(); //fill added and unaddedItems
        for (DataMap newItem : newItems) {
            boolean found = false;
            for (DataMap currentItem : currentItems) {
                if (newItem.getLabel().equals(currentItem.getLabel())) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                itemsToBeAdded.add(newItem);
            }
        }
        return itemsToBeAdded;
    }

    /**
     * Handles the addition of items in the treemap. Deletion of items should be
     * done already. CurrentDm and newDm are both present in the tree, but their
     * children might not be
     *
     * @param currentDm The current datamap
     * @param currentDm The new datamap
     * @param newDm
     */
    private void updateAdditionTreeMap(DataMap currentDm, DataMap newDm) {
        //get the treemap corresponding to oldDataMap
        TreeMap parentTm = currentTreeMap.getTreeMapWithLabel(currentDm.getLabel());

        if (!currentDm.hasChildren()) {
            //handle newDm completely by generating a new treemap
            TreeMap newParentTm = generateNewTreeMap(newDm, parentTm.getRectangle());

            if (parentTm == currentTreeMap) {
                currentDataMap = newDm;//it does not have a parent in this case
                currentTreeMap = newParentTm;
            } else {
                currentDataMap.replaceDataMap(newDm);
                currentTreeMap.replaceTreemap(newParentTm);
            }

            return;
        }

        //We identify the datamaps that are in one treemap but not in the other treemap. We do this level by level
        List<DataMap> currentItems = new ArrayList(currentDm.getChildren());
        List<DataMap> newItems = new ArrayList(newDm.getChildren());

        //contains the nodes that are only present in the old dataset
        List<DataMap> itemsToBeAdded = getAddedItems(currentItems, newItems);

        //Add each item that should be added
        for (DataMap addDm : itemsToBeAdded) {
            //get the best spot for the addition for this element
            Tuple<DataMap, Boolean> bestAddition = getBestForAddition(parentTm, currentDm, addDm);
            DataMap bestDataMap = bestAddition.x;
            boolean horizontal = bestAddition.y;

            //perform the best addition
            TreeMapChangeGeneratorUncertainty tmCG = new TreeMapChangeGeneratorUncertainty(parentTm, percentageUs);
            TreeMap newParentTm = tmCG.performAdd(bestDataMap, addDm, horizontal);
            //newParentTm contains addDm
            if (addDm.hasChildren()) {
                //if the added treemap has children, we generate a new treemap for this part
                TreeMap addTm = generateNewTreeMap(addDm, newParentTm.getChildWithLabel(addDm.getLabel()).getRectangle());
                //replace addTm
                newParentTm.replaceTreemap(addTm);
            }
            //update the treemap
            if (parentTm == currentTreeMap) {
                currentTreeMap = newParentTm;
            } else {
                currentTreeMap.replaceTreemap(newParentTm);
            }
            parentTm = newParentTm;
            //Add the dataMap
            currentDataMap.addDatamap(addDm, currentDm);
        }

        for (DataMap oldDm : currentItems) {
            //Find the datamap in the new dm with the same label. must exist as deletion already occured
            DataMap newChildDm = newDm.getDataMapWithLabel(oldDm.getLabel());
            if (newChildDm.hasChildren()) {
                //if it does not have children, we do not need to recurse
                updateAdditionTreeMap(oldDm, newChildDm);
            }

        }

    }

    /**
     * Returns the best place to insert dataMap {@code addDM} and whether it
     * should inserted horizontal
     *
     * @param parentTm the treemap where we are going to insert addDm
     * @param parentDm The parent of the possible datamaps where we can insert
     *                 addDm
     * @return (Best dataMap to insert,horizontal insertion)
     */
    private Tuple<DataMap, Boolean> getBestForAddition(TreeMap parentTm, DataMap parentDm, DataMap addDm) {
        if (parentTm.getChildren().size() != parentDm.getChildren().size()) {
            System.err.println("Inconsistent sizes");
        }

        //determine what the best option is by trying them all out. Candidates are all the children of the parent
        List<DataMap> additionCandidates = getAdditionCandidates(parentDm, parentTm);

        OrderEquivalenceGraph oeg = new OrderEquivalenceGraph(parentTm);

        double bestRatio = Double.MAX_VALUE;
        DataMap bestDataMap = null;
        boolean bestHor = false;
        for (int hor = 0; hor <= 1; hor++) {
            //test both horizontal and vertical
            boolean horizontal;
            if (hor == 0) {
                horizontal = false;
            } else {
                horizontal = true;
            }
            for (DataMap dm : additionCandidates) {
                //Check if this is the best option for the addition by adding it
                TreeMap parentTmCopy = parentTm.deepCopy();

                //first update the size such that the totals add up again
                parentTmCopy.updateTargetSize(parentTmCopy.getTargetSize() + addDm.getTargetSize());

                OrderEquivalenceGraph oegCopy = oeg.deepCopy();
                oegCopy.originalTreeMap = parentTmCopy;
                //add the dataMap
                try {
                    TreeMapChangeGeneratorUncertainty tmCG = new TreeMapChangeGeneratorUncertainty(parentTmCopy, oegCopy, percentageUs);
                    parentTmCopy = tmCG.performAdd(dm, addDm, horizontal);
                    //check if it is better
                    if (parentTmCopy.getMaxLeafAspectRatio() < bestRatio) {
                        bestDataMap = dm;
                        bestRatio = parentTmCopy.getMaxLeafAspectRatio();
                        bestHor = horizontal;
                    }
                } catch (IllegalStateException ex) {
                    //catch the infinite loop exception
                    ex.printStackTrace();
                    System.out.println("Skipping this additionCandidate");
                }
            }
        }
        return new Tuple(bestDataMap, bestHor);
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return new LocalMovesUncertainty(noMoves);
    }

    /**
     * Performs a single optimal move on the current treemap
     */
    public TreeMap performMove() {
        currentTreeMap = updateTreeMap(currentTreeMap, 1);
        return currentTreeMap;
    }

    /**
     * Returns a selection of candidates to add a rectangle to.
     *
     * @param parentDm
     * @param parentTm
     * @return
     */
    private List<DataMap> getAdditionCandidates(DataMap parentDm, TreeMap parentTm) {
        //Check promising candiates. I.e., datamaps with the highest aspect ratio
        //If there are less than maxCandidates, just try all of them.
        int maxCandidates = 6;

        ArrayList<DataMap> additionCandidates = new ArrayList();

        List<TreeMap> children = new ArrayList(parentTm.getChildren());

//        //option 1: Sort in descending order of aspect ratio
//        children.sort((TreeMap o1, TreeMap o2) -> Double.compare(o2.getRectangle().getAspectRatio(), o1.getRectangle().getAspectRatio()));
//        for (int i = 0; (i < maxCandidates) && (i < children.size()); i++) {
//            TreeMap tm = children.get(i);
//            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
//            additionCandidates.add(dm);
//        }
//      //option 2:random
//        Collections.shuffle(children);
//        for (int i = 0; (i < maxCandidates) && (i < children.size()); i++) {
//            TreeMap tm = children.get(i);
//            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
//            additionCandidates.add(dm);
//        }
        //option 3: 50-50 of both
        children.sort((TreeMap o1, TreeMap o2) -> Double.compare(o2.getRectangle().getAspectRatio(), o1.getRectangle().getAspectRatio()));
        for (int i = 0; (i < maxCandidates / 2) && (i < children.size()); i++) {
            TreeMap tm = children.get(i);
            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
            additionCandidates.add(dm);
        }

        for (int i = 0; (i < maxCandidates / 2) && (i < children.size()); i++) {
            int index = (int) Math.floor(Randomizer.getRandomDouble() * children.size());
            TreeMap tm = children.get(index);
            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
            if (!additionCandidates.contains(dm)) {
                additionCandidates.add(dm);
            }
        }

        return additionCandidates;
    }

    //Debug helper functions
    /**
     * Returns whether the targetsizes of the treemap corresponds with the sizes
     * in the datamap
     *
     * @param rootDm
     * @param rootTm
     */
    private void checkTreemapSizes(DataMap rootDm, TreeMap rootTm) {
        if (!DEBUG) {
            return;
        }
        double sumTm = 0;
        double sumDm = 0;

        for (TreeMap tm : rootTm.getAllChildren()) {
            sumTm += tm.getTargetSize();
        }
        for (DataMap dm : rootDm.getAllChildren()) {
            sumDm += dm.getTargetSize();
            if (dm.getTargetSize() == 0) {
                System.err.println("Targetsize is 0, should not happen");
            }
        }
        if (Math.abs((sumDm - sumTm)) > 1) {
            System.out.println("sumDm-sumTm = " + (sumDm - sumTm));
        }

    }

    /**
     * Verifies that {@code currentDataMap} and {@code currentTreeMap} have the
     * same structure.
     */
    private void verifyStructure() {
        if (!DEBUG) {
            return;
        }
        if (currentDataMap.getChildren().size() != currentTreeMap.getChildren().size()) {
            System.err.println("Inconsistent sizes");
        }
        for (DataMap dm : currentDataMap.getAllChildren()) {
            TreeMap childWithLabel = currentTreeMap.getTreeMapWithLabel(dm.getLabel());
            if (childWithLabel == null) {
                System.err.println("no treemap for this child");
            }
            if (dm.getChildren().size() != childWithLabel.getChildren().size()) {
                System.err.println("Children size does not match up");
            }
        }

        for (TreeMap tm : currentTreeMap.getAllChildren()) {
            DataMap childWithLabel = currentDataMap.getDataMapWithLabel(tm.getLabel());
            if (childWithLabel == null) {
                System.err.println("no dataMap for this child");
            }
            if (childWithLabel.getChildren().size() != tm.getChildren().size()) {
                System.err.println("Children size does not match up");
            }
        }
    }

    /**
     * Verifies that the actualSizes of the treemaps are close to the
     * targetsizes
     *
     * @param message
     */
    private void checkWeightsAccurate(String message) {
        double sumTm = 0;
        for (TreeMap tm : currentTreeMap.getAllChildren()) {
            sumTm += Math.abs(tm.getTargetSize() - tm.getActualSize());
        }

        if (sumTm > 10) {
            System.err.println(message + "Weights are significantly off by:" + sumTm);
        }

    }

}
