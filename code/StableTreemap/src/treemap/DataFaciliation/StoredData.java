package treemap.DataFaciliation;

import java.awt.Color;
import java.util.List;

/**
 *
 * @author max
 */
public class StoredData {

    private String id;
    private String parentId;
    private List<Double> sizes;
    private List<Double> standardDeviations;
    private Color color;
    
    public StoredData(String id, String parentId, List<Double> sizes,List<Double> standardDeviations,Color color) {
        this.id = id;
        this.parentId = parentId;
        this.sizes = sizes;
        this.standardDeviations = standardDeviations;
        this.color = color;
    }

    /**
     * @return the timeStamp
     */
    public int getDataAmount() {
        return sizes.size();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @return the size
     */
    public List<Double> getSizes() {
        return sizes;
    }
    
      public List<Double> getStandardDeviations() {
        return standardDeviations;
    }

    Color getColor() {
        return color;
    }
}
