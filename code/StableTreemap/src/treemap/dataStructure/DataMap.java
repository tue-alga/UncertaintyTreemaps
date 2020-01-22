package treemap.dataStructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import utility.Randomizer;

/**
 *
 * @author Max Sondag
 */
public class DataMap {

    /**
     * A unique identifier of this datamap
     */
    private String label;

    /**
     * The standard deviation this datamap has.
     */
    private double sd;

    /**
     * The size we want this datamap to have in a treemap
     */
    private double targetSize;
    /**
     * The children of this datamap
     */
    private List<DataMap> children;
    /**
     * The color which we want to use to display this datamap
     */
    private Color color;
    /**
     * In case of a gradient, the hue which we want to start displaying this
     * datamap on
     */
    private float startHue;

    public DataMap(String label, double size, double standardDeviation, List<DataMap> children, Color color) {
        this.label = label;
        this.targetSize = size;
        this.sd = standardDeviation;
        this.children = children;
        if (children == null) {
            this.children = new LinkedList();
        }
        this.color = color;
    }

    public DataMap(String label, double size, double standardDeviation, List<DataMap> children, Color color, float startHue) {
        this.label = label;
        this.targetSize = size;
        this.sd = standardDeviation;
        this.children = children;
        if (children == null) {
            this.children = new LinkedList();
        }
        this.color = color;
    }

    /**
     * Returns the sum of targetsizes of all datamaps in {@code list}
     *
     * @param list
     * @return
     */
    public static double getTotalSize(Collection<DataMap> list) {
        double size = 0;
        for (DataMap dm : list) {
            size += dm.getTargetSize();
        }
        return size;
    }

    /**
     * Returns the sum of targetsizes of all datamaps in {@code list}
     *
     * @param list
     * @return
     */
    public static double getTotalSizeList(Collection<List<DataMap>> list) {
        double size = 0;
        for (List<DataMap> subList : list) {
            for (DataMap dm : subList) {
                size += dm.getTargetSize();
            }
        }
        return size;
    }

    /**
     * Returns the sd given the list of children {@code datamaps}
     *
     * @param datamaps
     * @return
     */
    public static double getStandardDeviationChildren(Collection<DataMap> datamaps) {
        double sd = 0;
        for (DataMap dm : datamaps) {
            sd += dm.getSd() * dm.getSd();
        }
        return Math.sqrt(sd);
    }

    /**
     * Returns the sd given the list of values {@code datamaps}
     *
     * @param datamaps
     * @return
     */
    public static double getStandardDeviationValues(Collection<DataMap> datamaps) {
        double mean = getMean(datamaps);
        double sdSum = 0;
        for (DataMap dm : datamaps) {
            sdSum += Math.pow(dm.getTargetSize() - mean, 2);
        }
        int n = datamaps.size();
        return Math.sqrt(sdSum / n);
    }

    /**
     * Returns the mean given the list of {@code datamaps}
     *
     * @param datamaps
     * @return
     */
    public static double getMean(Collection<DataMap> datamaps) {
        double values = 0;
        for (DataMap dm : datamaps) {
            values += dm.getTargetSize();
        }
        return values / (double) datamaps.size();
    }

    /**
     * Whether this datamap has children
     *
     * @return
     */
    public boolean hasChildren() {
        if (children.isEmpty() || children == null) {
            return false;
        }
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    public float getStartHue() {
        return startHue;
    }

    /**
     * @return the targetSize
     */
    public double getTargetSize() {
        return targetSize;
    }

    /**
     * returns the sd
     *
     * @return
     */
    public double getSd() {
        return sd;
    }

    /**
     * @return the children
     */
    public List<DataMap> getChildren() {
        return children;
    }

    /**
     * Recursively finds all the child nodes
     *
     * @return all (grand)children of this dataMap
     */
    public List<DataMap> getAllChildren() {
        List<DataMap> allChildren = new LinkedList();
        allChildren.addAll(children);
        for (DataMap child : children) {
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    /**
     * Recursively finds all the child nodes
     *
     * @return all (grand)children of this dataMap
     */
    public List<DataMap> getAllLeafs() {
        List<DataMap> leafs = new LinkedList();
        if (!hasChildren()) {
            leafs.add(this);
        }

        for (DataMap child : children) {
            leafs.addAll(child.getAllLeafs());
        }
        return leafs;
    }

    public Color getColor() {
        return color;
    }
    //</editor-fold>

    /**
     * Whether {@code datamap} has the same hierarchical structure as this
     * datamap.
     *
     * @param dataMap
     * @return
     */
    public boolean hasEqualStructure(DataMap dataMap) {
        if (!dataMap.getLabel().equals(label)) {
            return false;
        }

        if (children.size() != dataMap.children.size()) {
            return false;
        }

        for (DataMap d1 : children) {
            DataMap d2 = d1.getDataMapSameLabel(dataMap.getChildren());
            if (d2 == null) {
                return false;
            }
            if (!d1.hasEqualStructure(d2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the datamap from {@code dataMaps} with the same label as this
     * datamap.
     *
     * @param dataMaps
     * @return
     */
    private DataMap getDataMapSameLabel(List<DataMap> dataMaps) {
        for (DataMap d : dataMaps) {
            if (d.getLabel().equals(label)) {
                return d;
            }
        }
        //no equal datamap
        return null;
    }

    /**
     * Returns the datamap with {@code label} from the descendantes of this
     * datamap.
     *
     * @param label
     * @return
     */
    public DataMap getDataMapWithLabel(String label) {
        List<DataMap> dataMaps = getAllChildren();
        for (DataMap dm : dataMaps) {
            if (dm.label.equals(label)) {
                return dm;
            }
        }
        return null;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!DataMap.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        DataMap other = (DataMap) obj;

        if (!label.equals(other.label)) {
            return false;
        }

        for (DataMap child1 : children) {
            boolean found = false;
            for (DataMap child2 : other.children) {
                if (child1.equals(child2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        builder.append(label);
        for (DataMap dm : children) {
            builder.append(dm.hashCode());
        }
        return builder.toString().hashCode();

    }

    /**
     * Sets the targetsize of this datamap. Does not update the tree
     *
     * @param size
     */
    public void setTargetSize(double size) {
        this.targetSize = size;
    }

    /**
     * Sets the standard deviation of this datamap. Does not update the tree
     *
     * @param sd
     */
    public void setSd(double sd) {
        this.sd = sd;
    }

    /**
     * Adds {@code addDm} to {@code parent} and updates the targetSizes and
     * standard deviations of all datamaps on the path to {@code parent} from
     * this datamap.
     *
     * @param addDm
     * @param parent
     */
    public void addDatamap(DataMap addDm, DataMap parent) {
        boolean contained = false;
        if (getAllChildren().contains(parent)) {
            for (DataMap dm : children) {
                dm.addDatamap(addDm, parent);
            }
            contained = true;
        }
        if (this == parent) {
            children.add(addDm);
            contained = true;
        }
        if (contained) {
            //either this is the parent or we are higher in the tree. In both cases
            //the targetsize and standard deviation have to be updated
            targetSize += addDm.targetSize;
            sd = getStandardDeviationChildren(children);
        }
    }

    /**
     * Adds {@code addDm} to {@code parent} and updates the targetSizes and
     * standard deviations of all datamaps on the path to {@code parent} from
     * this datamap.
     *
     * @param toAdd
     * @param parent
     */
    public void addDataMaps(List<DataMap> toAdd, DataMap parent) {
        for (DataMap dm : toAdd) {
            addDatamap(dm, parent);
        }
    }

    /**
     * Removes {@code removeDm} from their parent and updates the targetSizes
     * and standard deviations of all datamaps on the path to the parent from
     * this datamap.
     *
     * @param removeDm The datamap to remove
     */
    public void removeDataMap(DataMap removeDm) {
        boolean contained = false;

        if (getChildren().contains(removeDm)) {
            children.remove(removeDm);
            contained = true;
        } else if (getAllChildren().contains(removeDm)) {
            //not in the direct children, but it is present further down the tree.
            for (DataMap dm : children) {
                dm.removeDataMap(removeDm);
            }
            contained = true;
        }

        //if a descendant on this node is removed, we need to update the values.
        if (contained) {
            //either this is the parent of removeDm or we are higher in the tree. In both cases
            //the targetsize and standard deviation have to be updated
            targetSize -= removeDm.targetSize;
            sd = getStandardDeviationChildren(children);
        }
    }

    /**
     * Removes {@code removeDm} from their parent and updates the targetSizes of
     * all datamaps on the path to the parents from this datamap.
     *
     * @param toDelete
     */
    public void removeDataMaps(List<DataMap> toDelete) {
        while (!toDelete.isEmpty()) {
            DataMap dm = toDelete.get(0);
            removeDataMap(dm);
            toDelete.remove(dm);
        }
    }

    /**
     * Replace the datamap with the same label as {@code replaceDm} with
     * replaceDM. {@code replaceDm} must have a parent.
     *
     * @param replaceDm
     */
    public void replaceDataMap(DataMap replaceDm) {

        DataMap currentDm = getDataMapWithLabel(replaceDm.getLabel());
        DataMap parent = getParent(currentDm);
        if (parent == null) {
            System.err.println("This datamap does not have a parent");
        }
        removeDataMap(currentDm);
        addDatamap(replaceDm, parent);
    }

    /**
     * Returns the parent of {@code dm}
     *
     * @param dm
     * @return
     */
    public DataMap getParent(DataMap dm) {
        if (children.contains(dm)) {
            return this;
        }
        for (DataMap child : children) {
            DataMap parent = child.getParent(dm);
            if (parent != null) {
                return parent;
            }
        }

        return null;

    }

    /**
     * Removes datamaps with size 0
     *
     * @Pre standard deviation should be 0 as well
     */
    public void removeEmptyDataMaps() {
        Set<DataMap> toDelete = new HashSet();
        for (DataMap dm : children) {
            if (dm.targetSize == 0) {
                toDelete.add(dm);
            }
            if (dm.hasChildren()) {
                dm.removeEmptyDataMaps();
            }
        }
        children.removeAll(toDelete);
    }

    /**
     * Finds the datamap with the same label as {@code newDm}, removes all
     * children of this datamap, set the size to newDm.targetSize and updates
     * all intermediates sizes and standard deviations on the path to this
     * datamap.
     *
     * @param newDm
     * @return true if we found a node with the same label as newDm
     */
    public boolean removeChildrenAndUpdate(DataMap newDm) {

        if (label.equals(newDm.getLabel())) {
            children = new ArrayList();//remove the children
            targetSize = newDm.targetSize;
            sd = newDm.sd;
            return true;
        }
        //might be in one of the children
        for (DataMap child : children) {
            boolean contained = child.removeChildrenAndUpdate(newDm);
            //if it was contained
            if (contained) {
                targetSize = getTotalSize(children);
                sd = getStandardDeviationChildren(children);
                return true;
            }
        }

        return false;

    }

    /**
     * Returns the height of this datamap
     *
     * @return
     */
    public int getHeight() {
        int height = 1;
        if (!hasChildren()) {
            return 1;
        }
        for (DataMap dm : children) {
            height = Math.max(height, dm.getHeight() + 1);
        }
        return height;
    }

    /**
     * Converts the hierarchy to json using the bubble treemap format.
     *
     * @return
     */
    public String toJson() {
        String jsonString = "";

        jsonString += "{ \n";
        if (!children.isEmpty()) {
            jsonString += "\"children\": [ \n";
            for (int i = 0; i < children.size(); i++) {
                String recurseString = children.get(i).toJson();

                jsonString += recurseString;
                if (i == (children.size() - 1)) {
                    //last element
                    jsonString += "\n";
                } else {
                    jsonString += ",\n";
                }
            }
            jsonString += "],\n";
        }
        jsonString += "\"se\": " + sd + ",\n";
        jsonString += "\"size\": " + targetSize + ",\n";
        jsonString += "\"colorRed\": " + color.getRed() + ",\n";
        jsonString += "\"colorGreen\": " + color.getGreen() + ",\n";
        jsonString += "\"colorBlue\": " + color.getBlue() + ",\n";
        jsonString += "\"title\": \"" + label + "\"\n";
        jsonString += "}";
        return jsonString;
    }

    public void useSdAsMean() {
        if (children == null || children.isEmpty()) {
            this.targetSize = sd;
            if (targetSize == 0) {
                targetSize = 1E-3;
            }
            return;
        }
        double sumSd = 0;
        for (DataMap dm : children) {
            sumSd += dm.getSd();
            dm.useSdAsMean();
        }
        this.targetSize = sumSd;
        if (targetSize == 0) {
            targetSize = 1E-3;
        }
    }

    public void randomizeWeightsAndSd() {
        if (hasChildren()) {
            for (DataMap dm : children) {
                dm.randomizeWeightsAndSd();
            }
            sd = getStandardDeviationChildren(children);
            targetSize = getTotalSize(children);
        } else {
            //between 0 and 1
            double randomDouble = Randomizer.getRandomDouble();
            sd = sd * (0.5 + randomDouble);
            randomDouble = Randomizer.getRandomDouble();
            targetSize = targetSize * (1 + randomDouble);
        }

    }

}
