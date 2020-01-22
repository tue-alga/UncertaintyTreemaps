package TreeMapGenerator.Pivot;

import java.util.List;
import treemap.dataStructure.DataMap;

/**
 *
 * @author max
 */
public class PivotBySplit extends Pivot {

    @Override
    public int findPivotNode(List<DataMap> dataMapList) {

        double totalSize = getSize(dataMapList);
        double bestDifference = Double.MAX_VALUE;

        int bestIndex = 0;
        for (int i = 0; i < dataMapList.size(); i++) {
            double leftSide = getSize(dataMapList.subList(0, i));
            double difference = totalSize/2 - leftSide;

            if (Math.abs(difference) < Math.abs(bestDifference)) {
                bestIndex = i;
                bestDifference = difference;
            }
        }
        return bestIndex;
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

}
