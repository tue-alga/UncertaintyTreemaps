package treemap.dataStructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Max Sondag
 */
public class TreeMap {

    /**
     * Find the smallest enclosing rectangle of all treemaps in the list
     *
     * @param tmList
     * @return
     */
    public static Rectangle findEnclosingRectangle(List<TreeMap> tmList) {
        //Find the enclosing rectangle of all the treeMap
        double x1 = Double.MAX_VALUE;
        double y1 = Double.MAX_VALUE;
        double x2 = 0;
        double y2 = 0;

        for (TreeMap tm : tmList) {
            Rectangle r = tm.getRectangle();
            x1 = Math.min(x1, r.getX());
            y1 = Math.min(y1, r.getY());
            x2 = Math.max(x2, r.getX2());
            y2 = Math.max(y2, r.getY2());
        }

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * The position of this treemap
     */
    private Rectangle rectangle;
    /**
     * The color that the rectangle should have
     */
    private Color color;
    /**
     * The unique label of this treemapF
     */
    private String label;
    /**
     * The size we want this rectangle to have
     */
    private double targetSize;
    /**
     * The size this rectangle currently has.
     */
    private double actualSize;
    /**
     * The children of this treemap
     */
    private List<TreeMap> children;

    /**
     * The parent of this treemap. null if it does not exist
     */
    private TreeMap parent;

    /**
     * The standard deviation of this treemap.
     */
    private double sd;

    public TreeMap(Rectangle rectangle, String label, Color color, double targetSize, double sd, List<TreeMap> children) {
        this.rectangle = rectangle;
        this.label = label;
        this.color = color;
        this.children = children;
        this.targetSize = targetSize;
        this.actualSize = targetSize;
        this.sd = sd;
        if (children == null) {
            this.children = new LinkedList();
        } else {
            for (TreeMap tm : children) {
                tm.setParent(this);
            }
        }
        this.parent = null;
    }

    public TreeMap(Rectangle rectangle, String label, Color color, double targetSize, double actualSize, double sd, List<TreeMap> children) {
        this.rectangle = rectangle;
        this.label = label;
        this.color = color;
        this.children = children;
        this.targetSize = targetSize;
        this.actualSize = actualSize;
        this.sd = sd;
        if (children == null) {
            this.children = new LinkedList();
        } else {
            for (TreeMap tm : children) {
                tm.setParent(this);
            }
        }
        this.parent = null;
    }

    /**
     * Returns whether this treemap has children
     *
     * @return
     */
    public boolean hasChildren() {
        if (children == null || children.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Returns all children of this treemap . Does not include itself
     *
     * @return
     */
    public List<TreeMap> getAllChildren() {
        if (!hasChildren()) {
            return new LinkedList();
        }
        List<TreeMap> childList = new LinkedList<>();
        for (TreeMap tm : children) {
            childList.addAll(tm.getAllChildren());
            childList.add(tm);
        }

        return childList;
    }

    /**
     * Returns all children of this treemap . Does not include itself
     *
     * @return
     */
    public List<TreeMap> getAllNodes() {
        if (!hasChildren()) {
            return new LinkedList();
        }
        List<TreeMap> nodesList = new LinkedList<>();
        for (TreeMap tm : children) {
            nodesList.addAll(tm.getAllChildren());
            nodesList.add(tm);
        }
        //add yourself for completeness
        nodesList.add(this);

        return nodesList;
    }

    /**
     * Returns the child with the given label. Does not search recursively
     *
     * @param label
     * @return
     */
    public TreeMap getChildWithLabel(String label) {
        if (!hasChildren()) {
            return null;
        }
        for (TreeMap tm : children) {
            if (tm.getLabel().equals(label)) {
                return tm;
            }
        }
        return null;
    }

    /**
     * Returns the treemap with the given label
     *
     * @param label
     * @return
     */
    public TreeMap getTreeMapWithLabel(String label) {
        if (this.label.equals(label)) {
            return this;
        }
        for (TreeMap tm : children) {
            //recurse
            TreeMap recurseTm = tm.getTreeMapWithLabel(label);
            if (recurseTm != null) {
                return recurseTm;
            }
        }
        return null;
    }

    public List<TreeMap> getAllLeafs() {
        List<TreeMap> childList = new LinkedList<>();
        if (!hasChildren()) {
            childList.add(this);
            return childList;
        }
        for (TreeMap tm : children) {
            childList.addAll(tm.getAllLeafs());
        }
        return childList;
    }

    /**
     * Returns the maximum aspect ratio of a leaf node in the treeMap
     *
     * @return
     */
    public double getMaxLeafAspectRatio() {
        List<TreeMap> allLeafs = getAllLeafs();
        double maxAspectRatio = Double.MIN_VALUE;
        for (TreeMap tm : allLeafs) {
            maxAspectRatio = Math.max(maxAspectRatio, tm.getRectangle().getAspectRatio());
        }
        return maxAspectRatio;
    }

    /**
     * Returns whether this treemap has the same structure as another treemap.
     *
     * @param tm
     * @return
     */
    public boolean equalStructure(TreeMap tm) {
        if (tm == this) {
            return true;
        }
        if (!tm.getColor().equals(getColor())) {
            return false;
        }
        if (!tm.getLabel().equals(getLabel())) {
            return false;
        }
        //treemaps themselves are fine, comparing their child structure now
        if (tm.hasChildren() != hasChildren()) {
            return true;
        }
        //they both either have or don't have children
        if (!hasChildren()) {
            return true;
        }
        //they have children

        //verify that they have the same amount of children
        if (tm.getAllChildren().size() != getAllChildren().size()) {
            return false;
        }

        //if we can find a matching child in tm for every child this treemap has
        //then the two treemap share the same structure as they must then have the 
        //same children
        for (TreeMap child : children) {
            boolean found = false;

            for (TreeMap childrenTm : tm.getChildren()) {
                if (childrenTm.equalStructure(child)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }

        //we could find a matching treemap in tm.getChildren for every child in children
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="getters">
    /**
     * @return the rectangle
     */
    public Rectangle getRectangle() {
        return rectangle;
    }

    /**
     * Returns the sd of this rectangles
     *
     * @return
     */
    public double getSd() {
        return sd;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    public double getTargetSize() {
        return targetSize;
    }

    public double getActualSize() {
        return actualSize;
    }

    /**
     * @return the children
     */
    public List<TreeMap> getChildren() {
        return children;
    }

    /**
     * Returns the average aspect ratio of all(including non-leaf) nodes
     *
     * @return
     */
    public double getAverageAspectRatio() {
        double sumAspectRatio = 0;
        List<TreeMap> allChildren = getAllChildren();
        for (TreeMap tm : allChildren) {
            sumAspectRatio += tm.rectangle.getAspectRatio();
        }

        return sumAspectRatio / (allChildren.size() + 1);
    }

    /**
     * Returns the average aspect ratio of all(including non-leaf) nodes
     *
     * @return
     */
    public double getAverageLeafAspectRatio() {
        double sumAspectRatio = 0;
        List<TreeMap> leafs = getAllLeafs();
        for (TreeMap tm : leafs) {
            sumAspectRatio += tm.rectangle.getAspectRatio();
        }

        return sumAspectRatio / (leafs.size());
    }

    /**
     * Returns the median aspect ratio of all (including non-leaf) nodes,
     *
     * @return
     */
    public double getMedianAspectRatio() {

        ArrayList<Double> aspectRatios = new ArrayList();

        List<TreeMap> allChildren = getAllChildren();
        for (TreeMap tm : allChildren) {
            aspectRatios.add(tm.rectangle.getAspectRatio());
        }

        aspectRatios.sort((Double o1, Double o2) -> Double.compare(o1, o2));

        int middleElement = (int) aspectRatios.size() / 2;
        return aspectRatios.get(middleElement);
    }

    /**
     * @return
     */
    public TreeMap getParent() {
        return parent;
    }

    /**
     * Returns the height of this node
     *
     * @return
     */
    public int getHeight() {
        int height = 0;
        for (TreeMap tm : getChildren()) {
            height = Math.max(tm.getHeight() + 1, height);
        }
        return height;
    }

    //</editor-fold>
    /**
     * Updates the targetsize of this treemap
     *
     * @param targetSize
     */
    public void updateTargetSize(double targetSize) {
        this.targetSize = targetSize;
    }

    /**
     * Updates the rectangle of this treemap and makes sure that all its
     * children are still in the rectangle of this treemap in the same
     * proportions.
     * Does NOT update the actualsize
     *
     * @param newR
     */
    public void updateRectangle(Rectangle newR) {

        double newX = newR.getX();
        double newY = newR.getY();
        double newW = newR.getWidth();
        double newH = newR.getHeight();

        if (rectangle != null) {
            //if it already had a rectangle
            double oldX = rectangle.getX();
            double oldW = rectangle.getWidth();

            double oldY = rectangle.getY();
            double oldH = rectangle.getHeight();

            double scaleWidth = newW / oldW;
            double scaleHeight = newH / oldH;

            for (TreeMap child : children) {
                //X percentage indicates at what percentages of the width/height 
                //the child used to be.
                double oldChildX = child.getRectangle().getX();
                double xPercent = (oldChildX - oldX) / (oldW);

                double oldChildY = child.getRectangle().getY();
                double yPercent = (oldChildY - oldY) / (oldH);

                double oldChildW = child.getRectangle().getWidth();
                double oldChildH = child.getRectangle().getHeight();

                //update the rectangle of the child
                double newChildX = newX + newW * xPercent;
                double newChildY = newY + newH * yPercent;
                double newChildW = oldChildW * scaleWidth;
                double newChildH = oldChildH * scaleHeight;
                Rectangle newRectangle = new Rectangle(newChildX, newChildY, newChildW, newChildH);
                //recurse in the child
                child.updateRectangle(newRectangle);
            }
        }
        //update this rectangle
        this.rectangle = new Rectangle(newR);
    }

    /**
     * Removes the treemaps from the structure and updates the weights and sd
     *
     * @param treemaps
     */
    public void removeTreeMaps(List<TreeMap> treemaps) {
        while (!treemaps.isEmpty()) {
            TreeMap tm = treemaps.get(0);
            removeTreeMap(tm.getLabel());
            treemaps.remove(tm);
        }
    }

    /**
     * Removes the treemap with the given label and updates the weights
     *
     * @param label
     */
    public void removeTreeMap(String label) {
        TreeMap childToRemove = getTreeMapWithLabel(label);
//        this.label = this.label.replace(label, "");

        if (getAllChildren().contains(childToRemove)) {
            targetSize -= childToRemove.targetSize;

            //it is a child of this treemap remove it
            if (children.contains(childToRemove)) {
                children.remove(childToRemove);
            } else {
                //recurse to find it
                for (TreeMap child : children) {
                    child.removeTreeMap(label);
                }
            }
            //update sd
            this.sd = getSd(children);
        }

    }

    /**
     * Adds child to parent and updates all the targetsizes of the treemaps
     *
     * @param addChild
     * @param parent
     */
    public void addTreeMap(TreeMap addChild, TreeMap parent) {

        boolean contained = false;//holds whether parent is a descendent
        //
        if (getAllChildren().contains(parent)) {
            contained = true;
            //make sure descendants are properly updated
            for (TreeMap child : children) {
                child.addTreeMap(addChild, parent);
            }
        }
        if (this == parent) {//add the child
            children.add(addChild);
            addChild.setParent(this);
            contained = true;
        }
        //change weight and sd
        if (contained) {
            //either this is the parent or we are higher in the tree. In both cases
            //the targetsize and standard deviation have to be updated
            targetSize += addChild.targetSize;
            sd = getSd(children);
        }
    }

    /**
     * Adds a list of treemaps to a parent and updates the targetsizes and sd
     *
     * @param updatedChildList
     * @param parent
     */
    public void addTreeMaps(List<TreeMap> updatedChildList, TreeMap parent) {
        for (TreeMap child : updatedChildList) {
            addTreeMap(child, parent);
        }
    }

    /**
     * Sets the parent of this treemap
     *
     * @param parent
     */
    public void setParent(TreeMap parent) {
        this.parent = parent;
    }

    /**
     * Makes a deepcopy of this treemap
     *
     * @return
     */
    public TreeMap deepCopy() {
        List<TreeMap> copyChildren = new ArrayList();
        for (TreeMap tm : children) {
            copyChildren.add(tm.deepCopy());
        }

        TreeMap copyTm = new TreeMap(rectangle.deepCopy(), label, color, targetSize, sd, copyChildren);
        return copyTm;
    }

    /**
     * Returns the first parent that has more than 1 child
     *
     * @return
     */
    public TreeMap getFirstLevelParent() {

        TreeMap parent = getParent();
        if (parent == null) {
            //tm is the root
            return null;
        }

        while (parent.getChildren().size() == 1) {
            parent = parent.getParent();
            if (parent == null) {
                //first level parent is the root
                return null;
            }
        }
        //parent has more than 1 child
        return parent;
    }

    /**
     * Replaces the treemap with the same label as {@code replaceTm} with
     * {@code replaceTm}.
     *
     * @param replaceTm
     */
    public void replaceTreemap(TreeMap replaceTm) {
        TreeMap originalTm = getTreeMapWithLabel(replaceTm.getLabel());
        removeTreeMap(replaceTm.getLabel());
        addTreeMap(replaceTm, originalTm.parent);
    }

    /**
     * Removes all children of the treemap with label newDm and updates the
     * sizes
     *
     * @param newDm
     * @param oldSize
     * @return
     */
    public boolean removeChildrenAndUpdateSize(DataMap newDm, double oldSize) {
        if (label.equals(newDm.getLabel())) {
            children = new ArrayList();
            targetSize = newDm.getTargetSize();
            sd = newDm.getSd();
            return true;
        } else {
            for (TreeMap child : children) {//recurse into the children
                boolean contained = child.removeChildrenAndUpdateSize(newDm, oldSize);
                //something changed
                if (contained) {
                    this.targetSize += (newDm.getTargetSize() - oldSize);
                    this.sd = getSd(children);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * sets the actualSize of the treemap
     *
     * @param actualSize
     */
    public void setActualSize(double actualSize) {
        this.actualSize = actualSize;
    }

    /**
     * Updates all the target sizes for the treemap. newDm and this treemap
     * should have the exact same structure
     *
     * @param newDm
     */
    public void setTargetSizes(DataMap newDm) {
        targetSize = newDm.getTargetSize();

        for (TreeMap childTm : children) {
            DataMap childDm = newDm.getDataMapWithLabel(childTm.getLabel());
            childTm.setTargetSizes(childDm);
        }
    }

    private double getSd(List<TreeMap> children) {
        double sdSum = 0;
        for (TreeMap tm : children) {
            sdSum += tm.getSd() * tm.getSd();
        }
        return Math.sqrt(sdSum);
    }

    /**
     * Each node that has depth d from the root of the treemap will be added to
     * the same set.
     *
     * @return
     */
    public HashMap<Integer, Set<TreeMap>> getDepthMap(int depth) {
        HashMap<Integer, Set<TreeMap>> depthMap = new HashMap();
        //add the node itself
        HashSet<TreeMap> selfSet = new HashSet();
        selfSet.add(this);
        depthMap.put(depth, selfSet);

        if (children == null || children.isEmpty()) {
            return depthMap;//if there are no children this is a leaf and we can go back up
        }

        //go through all children and recurse. Merge the depthmaps
        for (TreeMap childTm : children) {
            HashMap<Integer, Set<TreeMap>> depthMapChild = childTm.getDepthMap(depth + 1);
            for (Integer depthKey : depthMapChild.keySet()) {
                //make sure the hashset for this depth exists in the depthmap
                if (!depthMap.containsKey(depthKey)) {
                    depthMap.put(depthKey, new HashSet());
                }
                //Merge the new hashSet for this depth with the original.
                Set<TreeMap> originalSet = depthMap.get(depthKey);
                originalSet.addAll(depthMapChild.get(depthKey));

                depthMap.put(depthKey, originalSet);
            }
        }
        return depthMap;
    }

    /**
     * Each node that has height h from the root of the treemap will be added to
     * the same set.
     *
     * @return
     */
    public HashMap<Integer, Set<TreeMap>> getHeightMap() {
        HashMap<Integer, Set<TreeMap>> heightMap = new HashMap();

        //go through all children and recurse. Merge the heightMap
        for (TreeMap childTm : children) {
            HashMap<Integer, Set<TreeMap>> heightMapChild = childTm.getHeightMap();
            for (Integer heightKey : heightMapChild.keySet()) {
                //make sure the hashset for this depth exists in the depthmap
                if (!heightMap.containsKey(heightKey)) {
                    heightMap.put(heightKey, new HashSet());
                }
                //Merge the new hashSet for this depth with the original.
                Set<TreeMap> originalSet = heightMap.get(heightKey);
                originalSet.addAll(heightMapChild.get(heightKey));

                heightMap.put(heightKey, originalSet);
            }
        }
        int maxHeight = -1;
        for (Integer h : heightMap.keySet()) {
            maxHeight = Math.max(h, maxHeight);
        }
        int selfHeight = maxHeight + 1;

        //add the node itself
        HashSet<TreeMap> selfSet = new HashSet();
        selfSet.add(this);
        heightMap.put(selfHeight, selfSet);

        return heightMap;
    }

    public Rectangle getSdRectangle() {
        double x1 = rectangle.getX();
        double width = rectangle.getWidth();

        double sdPercentage = Math.min(sd / targetSize, 1);
        double sdHeight = rectangle.getHeight() * sdPercentage;

        double y1 = rectangle.getY2() - sdHeight;

        return new Rectangle(x1, y1, width, sdHeight);
    }
//
//    /**
//     * Returns a new treemap without any hierarchy information.
//     *
//     * @return
//     */
//    public TreeMap flatten() {
//        List<TreeMap> leafs = new ArrayList();
//        double size = 0;
//
//        for (TreeMap tm : getAllLeafs()) {
//            TreeMap newTm = new TreeMap(tm.getRectangle(), tm.getLabel(), tm.color, tm.targetSize, tm.sd, null);
//            leafs.add(newTm);
//            size += tm.targetSize;
//        }
//        double newSd = getSd(leafs);
//        TreeMap newRoot = new TreeMap(rectangle, "root", color, size, newSd, null);
//        return newRoot;
//    }

    //replaces all targetsizes by the standard deviation
    public void setSdAsSize() {
        if (children == null || children.isEmpty()) {
            if (sd != 0) {
                targetSize = sd;
            } else {
                //small value to make sure it stays an equivalent layout.
                targetSize = 1E-3;
            }
            sd = 0;
            return;
        }
        //has children, aggregate sd to get correct size, not meaningfull.
        double sumSd = 0;

        for (TreeMap tm : children) {
            sumSd += tm.sd;
            tm.setSdAsSize();;
        }
        targetSize = sumSd;
        sd = 0;
    }

}
