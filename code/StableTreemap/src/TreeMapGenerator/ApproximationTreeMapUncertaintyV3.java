package TreeMapGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import statistics.UncertaintyStatistics;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import utility.Pair;

/**
 *
 * @author max
 */
public class ApproximationTreeMapUncertaintyV3 implements TreeMapGenerator {

    /**
     * Maximum guaranteed approximation ratio. Must be larger than 3.
     */
    double approximationRatio;

    /**
     * Whether to use percentage penalty or area penalty
     */
    boolean percentagePenalty;

    public ApproximationTreeMapUncertaintyV3(double approximationRatio, boolean percentagePenalty) {
        if (approximationRatio < 3) {
            throw new IllegalArgumentException("approximationRatio was " + approximationRatio + " but is not allowed to be lower than 3.");
        }
        this.approximationRatio = approximationRatio;
        this.percentagePenalty = percentagePenalty;
    }

    @Override
    public TreeMap generateTreeMap(DataMap dataMap, Rectangle inputR) {
        TreeMap returnTreeMap;
        if (!dataMap.hasChildren()) {
            //base case, we do not have to recurse anymore
            returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(), dataMap.getSd(), null);
            return returnTreeMap;
        }

        List<DataMap> children = new ArrayList<>();
        children.addAll(dataMap.getChildren());
        //sort the datamaps based on their size
        sortDataMapsDecreasing(children);

        //get maximum allowed aspect ratio
        double maximumRatio = getMaxRatio(children);
        maximumRatio = Math.max(inputR.getAspectRatio(), maximumRatio);
        maximumRatio = Math.max(maximumRatio, approximationRatio);

        //calculate uncertainty and height of standard deviation
        double uncertainty = dataMap.getSd();
        double sdY = inputR.getY2() - Math.min(1.0, uncertainty / dataMap.getTargetSize()) * inputR.getHeight();

        //generate the rectangle positions for each child on this level
        Map<DataMap, Rectangle> mapping = generateLevel(children, inputR, sdY, maximumRatio);

        //recursively go through the children to generate all treemaps
        List<TreeMap> treeChildren = new ArrayList();
        for (DataMap dm : mapping.keySet()) {
            TreeMap tm = generateTreeMap(dm, mapping.get(dm));
            treeChildren.add(tm);
        }

        returnTreeMap = new TreeMap(inputR, dataMap.getLabel(), dataMap.getColor(), dataMap.getTargetSize(), dataMap.getSd(), treeChildren);
        return returnTreeMap;
    }

    /**
     * DataMap is sorted on size
     *
     * @param dataMaps
     * @param inputR
     * @return
     */
    private Map<DataMap, Rectangle> generateLevel(List<DataMap> dataMaps, Rectangle inputR, double sdY, double maxRatio) {
        Map<DataMap, Rectangle> mapping = new HashMap();

        if (dataMaps.size() == 1) {
            mapping.put(dataMaps.get(0), inputR);
            return mapping;
        }

        if (inputR.getY2() < sdY) {
            //we are above the sd-rectangle, so can use regular approximation
            ApproximationTreeMapUncertainty atmu = new ApproximationTreeMapUncertainty();
            return atmu.generateLevel(dataMaps, inputR);
        }

        //there is still uncertainty left. We first see if a horizontal split is usefull
        //l1 and l2 will be modified to contain the nodes of the split
        List<DataMap> l1 = new ArrayList();
        List<DataMap> l2 = new ArrayList();

        boolean success = performHorSplit(dataMaps, inputR, sdY, maxRatio, l1, l2);
        if (!success) {
            //clear lists
            l1 = new ArrayList();
            l2 = new ArrayList();
            performVerSplit(dataMaps, inputR, maxRatio, l1, l2);
        }

        //l1 is put at the left/bottom
        Pair<Rectangle, Rectangle> splitRectangles = getSplitRectangles(l1, l2, inputR, success);
        Rectangle r1 = splitRectangles.x;
        Rectangle r2 = splitRectangles.y;

        if (l1.isEmpty() || l2.isEmpty()) {
            if (l1.size() + l2.size() > 1) {
                System.err.println("error");
            }
        }

        //recursively map the rectangles. If the size of the list equals 1
        //we are in the basecase and the mapping is known
        if (l1.size() == 1) {
            mapping.put(l1.get(0), r1);
        } else if (l1.size() > 1) {
            mapping.putAll(generateLevel(l1, r1, sdY, maxRatio));
        }

        if (l2.size() == 1) {
            mapping.put(l2.get(0), r2);
        } else if (l2.size() > 1) {
            mapping.putAll(generateLevel(l2, r2, sdY, maxRatio));
        }

        return mapping;

    }

    /**
     *
     * @param dataMaps
     * @param inputR
     * @param allowedRatio Maximum allowed ratio. Must be feasible.
     * @param sdY          How much uncertainty is still needed to go
     *                     down.
     * @param l1           Will be modified to hold the output. Must be
     *                     empty at start.
     * @param l2           Will be modified to hold the output. Must be
     *                     empty at start.
     * @return
     */
    protected boolean performHorSplit(List<DataMap> dataMaps, Rectangle inputR, double sdY, double allowedRatio, List<DataMap> l1, List<DataMap> l2) {

        //first try putting the largest at the bottom
        sortDataMapsDecreasing(dataMaps);
        //start by getting a valid split between l1 and l2
        boolean success = getValidRatio(dataMaps, inputR, allowedRatio, l1, l2, true);
        if (!success) {
            return false;
        }
        improvePenalty(l1, l2, inputR, sdY, allowedRatio);

        double largestBottom = getPenalty(l1, l2, inputR, sdY);

        //try putting the smallest at the bottom
        sortDataMapsIncreasing(dataMaps);
        ArrayList<DataMap> l1Temp = new ArrayList();
        ArrayList<DataMap> l2Temp = new ArrayList();
        getValidRatio(dataMaps, inputR, allowedRatio, l1Temp, l2Temp, true);//a split point always exists
        improvePenalty(l1Temp, l2Temp, inputR, sdY, allowedRatio);

        double smallestBottom = getPenalty(l1Temp, l2Temp, inputR, sdY);

        if (smallestBottom < largestBottom) {
            //better score if we put the smallest at the bottom. change l1 and l2 to use Temp.
            l1.clear();
            l1.addAll(l1Temp);
            l2.clear();
            l2.addAll(l2Temp);
        }

        return true;

    }

    /**
     *
     * @param dataMaps
     * @param inputR
     * @param allowedRatio Maximum allowed ratio. Must be feasible.
     * @param l1           Will be modified to hold the output. Must be empty at
     *                     start.
     * @param l2           Will be modified to hold the output. Must be empty at
     *                     start.
     * @return
     */
    protected void performVerSplit(List<DataMap> dataMaps, Rectangle inputR, double allowedRatio, List<DataMap> l1, List<DataMap> l2) {
        //start by getting a valid split between l1 and l2. cannot fail
        getValidRatio(dataMaps, inputR, allowedRatio, l1, l2, false);

        //add from l2 into l1 to try and equalize the sd within the bounds.
        addEqualSd(l1, l2, allowedRatio, inputR, false);

    }

    /**
     * Gets which datamaps can be safely added from l2 to l1.
     *
     * @param list1
     * @param list2
     * @param allowedRatio
     * @param inputR
     * @param horSplit
     * @return
     */
    protected List<DataMap> getValidAdditions(List<DataMap> list1, List<DataMap> list2, double allowedRatio, Rectangle inputR, boolean horSplit) {
        if (list1.isEmpty() || list2.isEmpty()) {
            return new ArrayList();
        }

        List<DataMap> l1 = new ArrayList();
        l1.addAll(list1);
        sortDataMapsDecreasing(l1);
        List<DataMap> l2 = new ArrayList();
        l2.addAll(list2);
        sortDataMapsDecreasing(l2);

        List<DataMap> candidates = new ArrayList();
        if (l2.size() == 1) {
            return candidates;
        }

        for (int i = 0; i < l2.size(); i++) {
            DataMap dmi = l2.get(i);

            l1.add(dmi);
            l2.remove(i);
            double ratio = getRatio(l1, l2, inputR, horSplit);
            if (ratio <= allowedRatio) {
                candidates.add(dmi);
            }
            l1.remove(dmi);
            l2.add(i, dmi);
        }

        return candidates;
    }

    /**
     * Returns the dataset sorted on size with the smallest first
     *
     * @param dataMaps
     */
    protected void sortDataMapsIncreasing(List<DataMap> dataMaps) {
        Collections.sort(dataMaps, (DataMap o1, DataMap o2) -> Double.compare(o1.getTargetSize(), o2.getTargetSize()));
    }

    /**
     * Returns the dataset sorted on size with the largest first
     *
     * @param dataMaps
     */
    protected void sortDataMapsDecreasing(List<DataMap> dataMaps) {
        Collections.sort(dataMaps, (DataMap o1, DataMap o2) -> Double.compare(o2.getTargetSize(), o1.getTargetSize()));

    }

    /**
     * Returns the dataset sorted on size with the largest first
     *
     * @param dataMaps
     */
    protected void sortDataMapsDecreasingUncertainty(List<DataMap> dataMaps) {
        Collections.sort(dataMaps, (DataMap o1, DataMap o2) -> Double.compare(o2.getSd(), o1.getSd()));
    }

    protected Pair<Rectangle, Rectangle> getSplitRectangles(List<DataMap> l1, List<DataMap> l2, Rectangle inputR, boolean horizontalSplit) {
        //we put l1 at the bottomLeft.
        //We now distribute them over 2 subRectangle
        Rectangle r1, r2;
        double totalSize = DataMap.getTotalSize(l1) + DataMap.getTotalSize(l2);
        double lengthPercentageR1 = DataMap.getTotalSize(l1) / totalSize;

        double x1 = inputR.getX();
        double y1 = inputR.getY();
        double height = inputR.getHeight();
        double width = inputR.getWidth();

        if (horizontalSplit) {
            r1 = new Rectangle(x1, y1 + height - lengthPercentageR1 * height, width, lengthPercentageR1 * height);
            r2 = new Rectangle(x1, y1, width, height - lengthPercentageR1 * height);
        } else {
            r1 = new Rectangle(x1, y1, lengthPercentageR1 * width, height);
            r2 = new Rectangle(x1 + lengthPercentageR1 * width, y1, width - lengthPercentageR1 * width, height);
        }

        return new Pair(r1, r2);
    }

    private boolean getValidRatio(List<DataMap> dataMaps, Rectangle inputR, double allowedRatio, List<DataMap> l1, List<DataMap> l2, boolean horizontalSplit) {
        l2.addAll(dataMaps);
        double ratio = Double.MAX_VALUE;
        while (ratio > allowedRatio) {
            if (l2.size() == 1) {
                return false;
            }

            DataMap dm = l2.get(0);
            l1.add(dm);
            l2.remove(0);

            //make sure that both the ratio of the split and the ratio's of the remaining are correct.
            ratio = getRatio(l1, l2, inputR, horizontalSplit);

        }

        //if we exit the loop, we have found a split with the desired ratio
        return true;
    }

    private double getRatio(List<DataMap> l1, List<DataMap> l2, Rectangle inputR, boolean horizontalSplit) {
        double ratioL1 = getMaxRatio(l1);
        double ratioL2 = getMaxRatio(l2);

        Pair<Rectangle, Rectangle> splitRectangles = getSplitRectangles(l1, l2, inputR, horizontalSplit);
        double ratioR1 = splitRectangles.x.getAspectRatio();
        double ratioR2 = splitRectangles.y.getAspectRatio();
        return Collections.max(Arrays.asList(ratioL1, ratioL2, ratioR1, ratioR2));
    }

    /**
     * Returns the maximum ratio between two consequitive elements. If
     * {@code list} is empty or only contains 1 elements returns 1.
     *
     * @param list
     * @return
     */
    protected double getMaxRatio(List<DataMap> list) {
        if (list.isEmpty()) {
            return Double.MAX_VALUE;
        }

        if (list.size() == 1) {
            return 1.0;
        }

        double maxRatio = Double.MIN_VALUE;
        for (int i = 0; i < (list.size() - 1); i++) {
            double sizei = list.get(i).getTargetSize();
            double sizej = list.get(i + 1).getTargetSize();

            double ratio = Math.max(sizei / sizej, sizej / sizei);
            maxRatio = Math.max(maxRatio, ratio);
        }
        return maxRatio;
    }

    private double getTotalSd(Collection<DataMap> datamaps) {
        double sd = 0;
        for (DataMap dm : datamaps) {
            sd += dm.getSd();
        }
        return sd;
    }

    @Override
    public String getParamaterDescription() {
        return "approxUncertaintyv3Ratio" + approximationRatio + "Percentage" + percentagePenalty;
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }

    protected double getL1Y1(List<DataMap> l1, List<DataMap> l2, Rectangle inputR) {
        double l1Size = DataMap.getTotalSize(l1);
        double totalSize = l1Size + DataMap.getTotalSize(l2);

        double sizePercentage = l1Size / totalSize;
        return inputR.getY2() - sizePercentage * inputR.getHeight();
    }

    private double getPenalty(List<DataMap> l1, List<DataMap> l2, Rectangle inputR, double sdY) {
        Pair<Rectangle, Rectangle> splitRectangles = getSplitRectangles(l1, l2, inputR, true);

        Rectangle r1 = splitRectangles.x;
        Rectangle sdR = new Rectangle(inputR.getX(), sdY, inputR.getWidth(), inputR.getY2() - sdY);

        double penalty = 0;
        UncertaintyStatistics us = new UncertaintyStatistics(percentagePenalty);
        for (DataMap dm : l1) {
            //assume we place them all next to each other grounded at the base. Will it improve the sd?
            double dmPercentage = dm.getSd() / dm.getTargetSize();
            double dmSdY = r1.getY2() - r1.getHeight() * dmPercentage;
            Rectangle sdDm = new Rectangle(r1.getX(), dmSdY, r1.getWidth(), r1.getY2() - dmSdY);
            penalty += us.getPenalty(r1, sdDm, sdR);
//            penalty += sdDm.getAspectRatio() - 1;
        }

        return penalty;
    }

    private void improvePenalty(List<DataMap> l1, List<DataMap> l2, Rectangle inputR, double sdY, double allowedRatio) {
        List<DataMap> candidates = getValidAdditions(l1, l2, allowedRatio, inputR, true);

        //add rectangle to improve the score
        double penalty = getPenalty(l1, l2, inputR, sdY);
        while (!candidates.isEmpty()) {
            //we still have candidates, and it is either the first iteration or we imporved the penalty
            //add one candidate
            double bestPenalty = penalty;
            DataMap bestDm = null;
            for (DataMap dm : candidates) {
                //add the dm and evaluate
                l1.add(dm);
                l2.remove(dm);
                double newPenalty = getPenalty(l1, l2, inputR, sdY);
                if (newPenalty < penalty) {
                    bestPenalty = newPenalty;
                    bestDm = dm;
                }
                //return to original state
                l1.remove(dm);
                l2.add(dm);
            }

            if (bestPenalty < penalty) {
                l1.add(bestDm);
                l2.remove(bestDm);
                penalty = bestPenalty;
            } else {
                break;
            }

            //candidates list changed
            candidates = getValidAdditions(l1, l2, allowedRatio, inputR, true);
        }

    }

    private void addEqualSd(List<DataMap> l1, List<DataMap> l2, double allowedRatio, Rectangle inputR, boolean b) {

        double l1Sd = getTotalSd(l1);
        double l2Sd = getTotalSd(l2);

        //l1 and l2 now already contain a valid split, try to balance the uncertainty.
        if (l1Sd > l2Sd) {
            //no room to balance.
            return;
        }

        double targetSd = (l1Sd + l2Sd) / 2;
        double currentOffTarget = Math.max(Math.abs(targetSd - l1Sd), Math.abs(targetSd - l2Sd));

        List<DataMap> candidates = getValidAdditions(l1, l2, allowedRatio, inputR, false);
        while (!candidates.isEmpty()) {
            sortDataMapsDecreasingUncertainty(candidates);

            boolean added = false;
            //add a single element to l1, insert the largest sd that fits
            //all candidates are safe to add for area requirements
            for (int i = 0; i < candidates.size(); i++) {
                DataMap dm = candidates.get(i);
                double sd = dm.getSd();

                double offTarget = Math.max(Math.abs(targetSd - (l1Sd + sd)), Math.abs(targetSd - (l2Sd + sd)));
                if (offTarget < currentOffTarget) {
                    currentOffTarget = offTarget;
                    l1.add(dm);
                    l2.remove(dm);
                    added = true;
                    break;//only add one element to l1 add a time. exit inner loop
                }
            }
            if (!added) {
                //if nothing was changed anymore, then candidates do not change so we can exit.
                return;
            }
            candidates = getValidAdditions(l1, l2, allowedRatio, inputR, false);
        }
    }

}
