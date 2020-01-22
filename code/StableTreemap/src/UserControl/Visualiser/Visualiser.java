package UserControl.Visualiser;

import TreeMapGenerator.LocalMoves.LocalMoves;
import TreeMapGenerator.TreeMapGenerator;
import UserControl.CommandController;
import java.util.Map;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.DataFaciliation.DataFacilitator;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public class Visualiser extends CommandController {

    private GUI gui;
    private Rectangle treeMapRectangle;

    Visualiser() {
        super();
        gui = new GUI(this);
    }

    public static void main(String args[]) {
        new Visualiser();
    }

    @Override
    protected boolean getTreeMap(int time, boolean useStored, String commandIdentifier) {
        TreeMap nextTreeMap = modelController.getTreeMap(time, useStored, treeMapRectangle, commandIdentifier);
        if (nextTreeMap == null) {
            return false;
        } else {
            gui.updateTreeMap(nextTreeMap);
            return true;
        }
    }

    public void setTreeMap(TreeMap treeMap) {
        if (treeMap != null) {
            gui.updateTreeMap(treeMap);
        }
    }

    public void setTreeMapRectangle(Rectangle treeMapRectangle) {
        this.treeMapRectangle = treeMapRectangle;
    }

    @Override
    public void setTreeMapGenerator(TreeMapGenerator treeMapGenerator) {
        modelController.setTreeMapGenerator(treeMapGenerator);
    }

    public void setDataFacilitator(DataFacilitator dataFacilitator) {
        modelController.setDataFacilitator(dataFacilitator);
    }

    boolean isInitialized() {
        if (gui != null) {
            return true;
        }
        return false;
    }

    @Override
    public void setStability(Map<String, Double> stabilities) {
        gui.setStability(stabilities);
    }

    @Override
    public void setAspectRatioBeforeMoves(double maxAspectRatio) {
        gui.setAspectRatioBeforeMoves(maxAspectRatio);
    }

    @Override
    public void setAspectRatioAfterMoves(double maxAspectRatio) {
        gui.setAspectRatioAfterMoves(maxAspectRatio);
    }

    @Override
    protected TreeMap updateCurrentTreeMap(int time) {
        return modelController.updateCurrentTreeMap(time);
    }

    public TreeMap getCurrentTreeMap() {
        return modelController.getCurrentTreeMap();
    }

    public void takeSnapShot(String identifier) {
        modelController.takeSnapShot(identifier);
    }

    public TreeMap getTreeMapWithoutMoves(int currentTime, boolean useStored) {
        return modelController.getMovesTreeMap(currentTime, useStored, 0, treeMapRectangle, "");

    }

    public TreeMap getTreeMapWithMoves(int currentTime, int moveAmount, boolean useStored) {
        return modelController.getMovesTreeMap(currentTime, useStored, moveAmount, treeMapRectangle, "");
    }

    public TreeMap getNewGeneratedTreeMap(int currentTime) {
        return modelController.getNewGeneratedTreeMap(currentTime, false, treeMapRectangle, "");
    }


    void toIpe() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DataFacilitator getDataFacilitator() {
        return modelController.getDataFacilitator();
    }

    Rectangle getTreeMapRectangle() {
        return treeMapRectangle;
    }

    public void performMove() {
        TreeMap updatedTreeMap = modelController.performMove();
        gui.updateTreeMap(updatedTreeMap);

    }

 

}
