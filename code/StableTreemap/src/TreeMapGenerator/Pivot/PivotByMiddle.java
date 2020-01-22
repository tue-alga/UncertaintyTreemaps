package TreeMapGenerator.Pivot;

import java.util.List;
import treemap.dataStructure.DataMap;

/**
 *
 * @author max
 */
public class PivotByMiddle extends Pivot {

    @Override
    public int findPivotNode(List<DataMap> dataMapList) {
        return (int) Math.ceil(dataMapList.size() / 2);
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

}
