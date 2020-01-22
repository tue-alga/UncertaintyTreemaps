/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.util.ArrayList;
import java.util.List;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author msondag
 */
public class UncertaintyStatistics {

    boolean percentagePenalty;

    public UncertaintyStatistics(boolean percentagePenalty) {
        this.percentagePenalty = percentagePenalty;
    }

    public OrderStatistics getOverlapScore(TreeMap root) {
        List<Double> overlapScores = getOverlapScores(root, null);
        OrderStatistics q = new OrderStatistics(overlapScores);
        return q;
    }

    public OrderStatistics getAllOverlapScore(TreeMap root) {
        List<Double> overlapScores = getAllOverlapScores(root, new ArrayList());
        OrderStatistics q = new OrderStatistics(overlapScores);
        return q;
    }

    /**
     * Returns the percentage of overlap
     *
     * @param tm
     * @return
     */
    protected List<Double> getAllOverlapScores(TreeMap tm, ArrayList<TreeMap> ancestors) {

        //get all scores with the relation to this tm.
        List<Double> scores = new ArrayList();

        double sumScore = 0;
        for (TreeMap parent : ancestors) {
            //if parent is null we are at the root
            Rectangle childR = tm.getRectangle();
            Rectangle childSdR = tm.getSdRectangle();
            Rectangle tmSdR = parent.getSdRectangle();

            double score = getPenalty(childR, childSdR, tmSdR);
            sumScore += score;

        }
        scores.add(sumScore);
        ancestors.add(tm);
        //recurse into the children.
        for (TreeMap child : tm.getChildren()) {
            List<Double> childScores = getAllOverlapScores(child, ancestors);
            scores.addAll(childScores);
        }
        return scores;
    }

    /**
     * Returns the percentage of overlap
     *
     * @param tm
     * @return
     */
    protected List<Double> getOverlapScores(TreeMap child, TreeMap parent) {
        //Contains a list of all child scores
        List<Double> scores = new ArrayList();
        if (parent != null) {
            //if parent is null we are at the root
            Rectangle childR = child.getRectangle();
            Rectangle childSdR = child.getSdRectangle();
            Rectangle parentSdR = parent.getSdRectangle();

            double score = getPenalty(childR, childSdR, parentSdR);
            if (score > 0.01) {
                double displayScore = Math.ceil(score * 1000) / 1000;
//                System.out.println(displayScore + " = score from " + child.getLabel() + " to " + parent.getLabel());
            }

            scores.add(score);
        }

        //recurse into the children.
        for (TreeMap tm : child.getChildren()) {
            List<Double> childScores = getOverlapScores(tm, child);
            scores.addAll(childScores);
        }
        return scores;
    }

    /**
     * Returns the penalty score of the child
     *
     * @param childR
     * @param childSd
     * @param parentSdR
     * @return
     */
    public double getPenalty(Rectangle childR, Rectangle childSd, Rectangle parentSdR) {
        double overlap = childR.intersection(parentSdR).getArea();

        //penalty(x) = max\left(\frac{A(R(x)\cap R_{sd}(p))-A(R_{sd}(x))}{A(R(x))},0\right)
        double penalty = (overlap - childSd.getArea());
        if (percentagePenalty) {
            penalty = penalty / childR.getArea();
        }
        penalty = Math.max(penalty, 0);

        return penalty;

    }

}
