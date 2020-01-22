package statistics.Stability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class RelativeQuadrantStability {

    public double getStability(TreeMap oldTm, TreeMap newTm) {

        //TODO: Punishment score for additions and removals?
        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);
        List<TreeMap> newRemaining = getNewRemainingLeafs(oldTm, newTm);

        System.out.println("oldTm.getAllLeafs().size() = " + oldTm.getAllLeafs().size());
        System.out.println("newTm.getAllLeafs().size() = " + newTm.getAllLeafs().size());
        //holds the stability score in the end. It is composed of the average stability
        //score of all items
        double stability = 0;

        HashMap<String, TreeMap> newMapping = new HashMap();
        for (TreeMap tmNew : newTm.getAllChildren()) {
            newMapping.put(tmNew.getLabel(), tmNew);
        }
        System.out.println("stab");
        for (TreeMap tmOld1 : oldRemaining) {
            TreeMap tmNew1 = newMapping.get(tmOld1.getLabel());
            double tmStab = 0;
            for (TreeMap tmOld2 : oldRemaining) {
                if (tmOld1.getLabel().equals(tmOld2.getLabel())) {
                    continue;
                }
                //get the corresponding new treemap
                TreeMap tmNew2 = newMapping.get(tmOld2.getLabel());

                Double[] oldPercentages = getQuadrantPercentages(tmOld1.getRectangle(), tmOld2.getRectangle());
                Double[] newPercentages = getQuadrantPercentages(tmNew1.getRectangle(), tmNew2.getRectangle());

                double itemStability = getQuadrantStability(oldPercentages, newPercentages);
                stability += itemStability;
                tmStab += itemStability;

            }
//            System.out.println("itemStability = " + tmStab/(oldRemaining.size()-1) + tmOld1.getLabel());
        }
        //Normalize the stability score
        stability = stability / (Math.pow(oldRemaining.size(), 2) - oldRemaining.size());

        return stability;
    }

    private List<TreeMap> getOldRemainingLeafs(TreeMap oldTm, TreeMap newTm) {
        List<TreeMap> remainingLeafs = new ArrayList();
        for (TreeMap tm1 : oldTm.getAllLeafs()) {
            TreeMap tm2 = newTm.getTreeMapWithLabel(tm1.getLabel());
            if (tm2 != null) {
                //DEBUG ONLY
                if (tm2.getTargetSize() != 1.00001) {
                    //tm1 was present in both
                    remainingLeafs.add(tm1);
                }
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
     * Returns an array of size 8 containing the percentage percentage of r2
     * that lies in the respective quadrant. Quadrants are encoded as follows:
     * 0:east,1norhteast,2north,3northwest,4west,5southwest,6south,7southeast
     *
     * @param tmOld
     * @param tmOld2
     * @return
     */
    private Double[] getQuadrantPercentages(Rectangle r1, Rectangle r2) {
        //check in which quardrants it lies
        double E = 0, NE = 0, N = 0, NW = 0, W = 0, SW = 0, S = 0, SE = 0;
        if (r2.getX() >= r1.getX2()) {
            //Strictly east
            if (r2.getY() < r1.getY()) {
                //at least partially in NE

                //get the percentage that r2 is in NE
                NE = (r1.getY() - r2.getY()) / r2.getHeight();
                NE = Math.min(1, NE);
            }
            if (r2.getY2() > r1.getY2()) {
                //at least partiall in SE
                SE = (r2.getY2() - r1.getY2()) / r2.getHeight();
                SE = Math.min(1, SE);
            }
            //remainder is in east
            E = 1 - NE - SE;

        } else if (r2.getX2() <= r1.getX()) {
            //strictly west
            if (r2.getY() < r1.getY()) {
                //at least partially in NW

                //get the percentage that r2 is in NW
                NW = (r1.getY() - r2.getY()) / r2.getHeight();
                NW = Math.min(1, NW);
            }
            if (r2.getY2() > r1.getY2()) {
                //at least partiall in SW
                SW = (r2.getY2() - r1.getY2()) / r2.getHeight();
                SW = Math.min(1, SW);
            }
            //remainder is in west
            W = 1 - NW - SW;
        } else if (r2.getY2() <= r1.getY()) {
            //strictly North
            if (r2.getX() < r1.getX()) {
                //at least partially in NW

                //get the percentage that r2 is in NW
                NW = (r1.getX() - r2.getX()) / r2.getWidth();
                NW = Math.min(1, NW);
            }
            if (r2.getX2() > r1.getX2()) {
                //at least partiall in SW
                NE = (r2.getX2() - r1.getX2()) / r2.getWidth();
                NE = Math.min(1, NE);
            }
            //remainder is in west
            N = 1 - NW - NE;
        } else {
            //strictly south
            if (r2.getX() < r1.getX()) {
                //at least partially in SW

                //get the percentage that r2 is in NW
                SW = (r1.getX() - r2.getX()) / r2.getWidth();
                SW = Math.min(1, SW);
            }
            if (r2.getX2() > r1.getX2()) {
                //at least partiall in SE
                SE = (r2.getX2() - r1.getX2()) / r2.getWidth();
                SE = Math.min(1, SE);
            }
            //remainder is in west
            S = 1 - SW - SE;
        }

        Double[] quadrant = new Double[8];
        quadrant[0] = E;
        quadrant[1] = NE;
        quadrant[2] = N;
        quadrant[3] = NW;
        quadrant[4] = W;
        quadrant[5] = SW;
        quadrant[6] = S;
        quadrant[7] = SE;
        return quadrant;
    }

    /**
     * returns the stability of the movement between quadrants
     *
     * @param oldPercentages
     * @param newPercentages
     * @return
     */
    private double getQuadrantStability(Double[] oldPercentages, Double[] newPercentages) {
        double stability = 1;
        for (int i = 0; i < oldPercentages.length; i++) {
            double oldPercentage = oldPercentages[i];
            double newPercentage = newPercentages[i];

            stability -= Math.abs(oldPercentage - newPercentage) / 2;
        }
        //TODO
        return stability;
    }
}
