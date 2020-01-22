package UserControl;

import TreeMapGenerator.TreeMapGenerator;
import java.util.Map;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.DataFileManager;
import treemap.ModelController;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public abstract class CommandController {

    protected ModelController modelController;

    public CommandController() {
        modelController = new ModelController(this);
    }

    protected void setDataFacilitator(DataFacilitator df) {
        modelController.setDataFacilitator(df);
    }

    protected void setDataFileManager(String fileLocation,boolean hasSd) {
        DataFileManager dfm = new DataFileManager(fileLocation,hasSd);
        modelController.setDataFacilitator(dfm);
    }

    protected void setTreeMapGenerator(TreeMapGenerator treeMapGenerator) {
        modelController.setTreeMapGenerator(treeMapGenerator);
    }

    protected abstract boolean getTreeMap(int time, boolean useStored, String commandIdentifier);

    /**
     * Updates the current treemap with new weights
     *
     * @param time
     * @return
     */
    protected abstract TreeMap updateCurrentTreeMap(int time);


    public abstract void setStability(Map<String, Double> stabilities);


    public abstract void setAspectRatioBeforeMoves(double maxAspectRatio);

    public abstract void setAspectRatioAfterMoves(double maxAspectRatio);
}
