package statistics.Stability;

import java.util.ArrayList;
import java.util.List;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class StabilityLayoutDistance {

    public double getStability(TreeMap oldTm, TreeMap newTm) {

        List<TreeMap> oldRemaining = getOldRemainingLeafs(oldTm, newTm);

        //holds the stability score in the end. It is composed of the average stability
        //score of all items
        double stability = 0;

        for (TreeMap tmOld : oldRemaining) {
            TreeMap tmNew = newTm.getTreeMapWithLabel(tmOld.getLabel());
            Rectangle rOld = tmOld.getRectangle();
            Rectangle rNew = tmNew.getRectangle();
            //Holds the sum of all individual stability scores
            double distanceScore = Math.sqrt(Math.pow(rOld.getX() - rNew.getX(), 2)
                    + Math.pow(rOld.getY() - rNew.getY(), 2)
                    + Math.pow(rOld.getWidth() - rNew.getWidth(), 2)
                    + Math.pow(rOld.getHeight() - rNew.getHeight(), 2)
            );
            stability += distanceScore;
        }
        //Normalize the stability score
        stability = stability / oldRemaining.size();

        return stability;
    }

    private List<TreeMap> getOldRemainingLeafs(TreeMap oldTm, TreeMap newTm) {
        List<TreeMap> remainingLeafs = new ArrayList();
        for (TreeMap tm1 : oldTm.getAllLeafs()) {
            TreeMap tm2 = newTm.getTreeMapWithLabel(tm1.getLabel());
            if (tm2 != null) {
                //tm1 was present in both
                remainingLeafs.add(tm1);
            }
        }
        return remainingLeafs;
    }
}
