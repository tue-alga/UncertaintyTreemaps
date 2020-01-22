package statistics.Stability;

import java.util.ArrayList;
import java.util.List;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class RelativePinPointPositionStability {

    public double getStability(TreeMap oldTm, TreeMap newTm) {

        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);

        //holds the stability score in the end. It is composed of the average stability
        //score of all items
        double stability = 0;

        for (TreeMap tmOld : oldRemaining) {
            TreeMap tmNew = newTm.getChildWithLabel(tmOld.getLabel());

            for (TreeMap tmOld2 : oldRemaining) {
                if (tmOld.getLabel().equals(tmOld2.getLabel())) {
                    continue;
                }
                //get the corresponding new treemap
                TreeMap tmNew2 = newTm.getChildWithLabel(tmOld2.getLabel());

                //Get the pointpoint position
                double relativeX = getHorizontalDistance(tmOld.getRectangle(), tmOld2.getRectangle());
                double relativeY = getVerticalDistance(tmOld.getRectangle(), tmOld2.getRectangle());

                double fallOfDistance = Math.sqrt(Math.pow(relativeX, 2) + Math.pow(relativeY, 2));

                double pinPointX = tmNew.getRectangle().getCenterX() + relativeX;
                double pinPointY = tmNew.getRectangle().getCenterY() + relativeY;

                double pinPointDistance = getDistance(pinPointX, pinPointY, tmNew2.getRectangle().getCenterX(), tmNew2.getRectangle().getCenterY());

                double relativeStability = 1 - Math.min(1, pinPointDistance / fallOfDistance);
                stability += relativeStability;
            }
        }
        //Normalize the stability score
        stability = stability / (Math.pow(oldRemaining.size(), 2) - oldRemaining.size());

        return stability;
    }


    private List<TreeMap> getOldRemainingLeafs(TreeMap oldTm, TreeMap newTm) {
        List<TreeMap> remainingLeafs = new ArrayList();
        for (TreeMap tm1 : oldTm.getAllLeafs()) {
            TreeMap tm2 = newTm.getChildWithLabel(tm1.getLabel());
            if (tm2 != null) {
                //tm1 was present in both
                remainingLeafs.add(tm1);
            }
        }
        return remainingLeafs;
    }

    private List<TreeMap> getNewRemainingLeafs(TreeMap oldTm, TreeMap newTm) {
        List<TreeMap> remainingLeafs = new ArrayList();
        for (TreeMap tm1 : newTm.getAllLeafs()) {
            TreeMap tm2 = oldTm.getChildWithLabel(tm1.getLabel());
            if (tm2 != null) {
                //tm1 was present in both
                remainingLeafs.add(tm1);
            }
        }
        return remainingLeafs;
    }

    /**
     * Gets the horizontal distance between the centers of the two rectangles.
     *
     * @param r1
     * @param r2
     * @return
     */
    private Double getHorizontalDistance(Rectangle r1, Rectangle r2) {
        return r2.getCenterX() - r1.getCenterX();
    }

    /**
     * Gets the horizontal distance between the centers of the two rectangles.
     *
     * @param r1
     * @param r2
     * @return
     */
    private Double getVerticalDistance(Rectangle r1, Rectangle r2) {
        return r2.getCenterY() - r1.getCenterY();
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
