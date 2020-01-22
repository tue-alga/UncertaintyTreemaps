package TreeMapGenerator.Pivot;

import java.util.List;
import treemap.dataStructure.DataMap;

/**
 *
 * @author max
 */
public class PivotBySize extends Pivot {

    @Override
    public int findPivotNode(List<DataMap> dataMapList) {
        DataMap largestItem = dataMapList.get(0);
        double largest = largestItem.getTargetSize();
        for (DataMap dm : dataMapList) {
            if (dm.getTargetSize() > largest) {
                largest = dm.getTargetSize();
                largestItem = dm;
            }
        }

        return dataMapList.indexOf(largestItem);
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

}
