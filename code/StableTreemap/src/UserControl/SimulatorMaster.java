package UserControl;

import java.io.File;
import java.util.Map;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author max
 */
public class SimulatorMaster extends CommandController {

    Rectangle treeMapRectangle;


    @Override
    public void setStability(Map<String, Double> stabilities) {
        //Do nothing, not needed
    }

    @Override
    public void setAspectRatioBeforeMoves(double maxAspectRatio) {
        //Do nothing, not needed
    }

    @Override
    public void setAspectRatioAfterMoves(double maxAspectRatio) {
        //Do nothing, not needed
    }

    @Override
    protected TreeMap updateCurrentTreeMap(int time) {
        return modelController.updateCurrentTreeMap(time);
    }

    @Override
    protected boolean getTreeMap(int time, boolean useStored, String commandIdentifier) {
        TreeMap nextTreeMap = modelController.getTreeMap(time, false, treeMapRectangle, commandIdentifier);
        if (nextTreeMap == null) {
            return false;
        } else {
//            updateTreeMap(nextTreeMap);
            return true;
        }
    }

    public void closeStatisticsOutput() {
        modelController.closeStatisticsOutput();
    }

    public void newStatisticsOutput(File outputFile,boolean directory) {
        modelController.newStatisticsFile(outputFile,directory);
    }

}
