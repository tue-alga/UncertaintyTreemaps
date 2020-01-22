package TreeMapGenerator;

import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public interface TreeMapGenerator {
    
 
    public abstract TreeMap generateTreeMap(DataMap dataMap, Rectangle treeMapRectangle);

    public String getParamaterDescription();

    public TreeMapGenerator reinitialize();
}
