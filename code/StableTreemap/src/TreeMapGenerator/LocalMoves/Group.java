package TreeMapGenerator.LocalMoves;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import treemap.dataStructure.DataMap;

/**
 *
 * @author max
 */
public class Group {

    private Set<DataMap> groupItems;
    private List<Group> children;
    private String label;
    private Group parent;

    public Group(DataMap dataMap) {
        groupItems = new HashSet();
        groupItems.add(dataMap);
        initializeLabel();

    }

    public Group(Set<DataMap> dataMaps) {
        groupItems = dataMaps;
        initializeLabel();
    }

    public Group(List<Group> selectedGroups) {
        groupItems = new HashSet();
        children = new ArrayList();
        for (Group g : selectedGroups) {
            groupItems.addAll(g.groupItems);
            children.add(g);
            g.setParent(this);
        }

        initializeLabel();
        sortChildren();
    }

    public void initializeLabel() {
        label = "";
        //label is lexiographically as we otherwise run into problem with sorting.
        List<String> labels = new ArrayList();
        for (DataMap dm : groupItems) {
            labels.add(dm.getLabel());
        }
        labels.sort(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }

        });
        for (String s : labels) {
            label += s;
        }
    }

    public void sortChildren() {
        if (isLeaf()) {
            return;
        }
        children.sort((Group o1, Group o2) -> Double.compare(o2.getSize(), o1.getSize()));
    }

    /**
     * Transforms the group into a dataMap structure that only has 2 levels.
     *
     * @return
     */
    public DataMap toShallowDataMap() {
        List<DataMap> dataMapChildren = new ArrayList();
        for (Group g : children) {
            DataMap dm = new DataMap(g.getLabel(), g.getSize(), g.getSD(), null, Color.yellow);
            dataMapChildren.add(dm);
        }

        DataMap parentDm = new DataMap(getLabel(), getSize(), getSD(), dataMapChildren, Color.GREEN);
        return parentDm;
    }

    /**
     * Adds a relation to each Group in {@code groups} if they share an item in
     * {@code groupItems}
     *
     * @param groups
     */
    public void setParentChildRelations(List<Group> groups) {
        children = new ArrayList();
        for (Group g : groups) {
            if (containsSharedItem(g.groupItems) && !g.equals(this)) {
                children.add(g);
                g.setParent(this);
            }
        }
    }

    /**
     * Returns true if {@code items} contain an item in {@code groupItems}
     *
     * @param items
     * @return
     */
    public boolean containsSharedItem(Set<DataMap> items) {
        //copy of set as it modifies the content
        Set<DataMap> duplicate = new HashSet<DataMap>(groupItems);
        //intersection
        duplicate.retainAll(items);
        //If it is not empty it must share an item
        return !duplicate.isEmpty();
    }

    /**
     * If the group only contains one item it can be represented by a dataMap
     * directly
     *
     * @return
     */
    public DataMap getDataMap() {
        assert (groupItems.size() == 1);
        return (DataMap) groupItems.toArray()[0];
    }

    public List<Group> getChildren() {
        if (children == null) {
            return new ArrayList();
        }
        return children;
    }

    public List<Group> getAllChildren() {
        List<Group> allChildren = new ArrayList();
        if (children == null) {
            return new ArrayList();
        }

        allChildren.addAll(children);
        for (Group g : children) {
            if (g == this) {
                continue;
            }
            allChildren.addAll(g.getAllChildren());
        }
        return allChildren;
    }

    public List<Group> getAllLeafGroups() {
        List<Group> allChildren = getAllChildren();
        List<Group> leafs = new ArrayList();

        for (Group g : allChildren) {
            if (g.isLeaf()) {
                leafs.add(g);
            }
        }
        return leafs;
    }

    public Set<DataMap> getGroupItems() {
        return groupItems;
    }

    /**
     * Deletes the dataMap {@code dm} from the group and all child groups
     *
     * @param dm
     */
    public void deleteDataMap(DataMap dm) {
        if (groupItems.contains(dm)) {
            //remove the datamap from this group
            groupItems.remove(dm);

            if (groupItems.isEmpty()) {
                //no more items in this group, so we removeDataMap the group from the structure

                parent.removeGroup(this);
            }
            if (!isLeaf()) {
                //Find the child which contains dm
                Group child = null;
                //if it is not a leaf then one of its children contained dm,
                //recurse in the children
                for (Group g : children) {
                    if (g.getGroupItems().contains(dm)) {
                        child = g;
                        break;
                    }
                }
                if (child == null) {
                    System.err.println("A non-leaf node did not have a child contain a item which itself contained");
                }
                child.deleteDataMap(dm);
            }
        }
        initializeLabel();
    }

    /**
     * Removes group g from the current group. If this group no longer has any
     * children we will removeDataMap this group from it's parent group. Does
     *
     * @param g
     */
    public void removeGroup(Group g) {
        groupItems.removeAll(g.groupItems);

        Set<Group> removeList = new HashSet();
        if (children != null) {
            for (Group child : children) {
                if (child.getLabel().equals(g.getLabel())) {
                    removeList.add(child);
                }
            }
            for (Group child : removeList) {
                children.remove(child);
            }
        }

        if (parent != null) {
            parent.removeGroup(g);
        }
        if (groupItems.isEmpty()) {
            parent.removeGroup(this);
        }
        initializeLabel();
    }

    /**
     * Adds a child group to the current group and updates the structure for
     * it's parents
     *
     * @param g
     */
    public void addChildGroup(Group g) {
        g.parent = this;
        if (children == null) {
            children = new ArrayList();
        }
        children.add(g);
        addGroupItems(g.groupItems);
    }

    private void addGroupItems(Collection<DataMap> items) {
        groupItems.addAll(items);
        if (parent != null) {
            parent.addGroupItems(items);
        }
        initializeLabel();
    }

    public double getSize() {
        return DataMap.getTotalSize(groupItems);
    }

    public double getSD() {
        return DataMap.getStandardDeviationChildren(groupItems);
    }

    public boolean isLeaf() {

        return (children == null || children.isEmpty());
        //   return (groupItems.size() == 1);
    }

    public String getLabel() {
        return label;
    }

    private void setParent(Group g) {
        this.parent = g;
    }

    public Group getParent() {
        return parent;
    }

    public void updateDataMap(DataMap rootDataMap) {
        Set<DataMap> newList = new HashSet();
        List<DataMap> allChildren = rootDataMap.getAllChildren();
        allChildren.add(rootDataMap);
        for (DataMap d1 : allChildren) {
            for (DataMap d2 : groupItems) {
                if (d1.getLabel().equals(d2.getLabel())) {
                    newList.add(d1);
                }
            }
        }
        groupItems = newList;
    }

    /**
     * Gets the maximum quotient when the children are in sorted order
     *
     * @return
     */
    public double getMaxQuotient() {
        //Sort should not be needed but left in for security. Not that big of a performance hit
        sortChildren();
        double maxQuotient = 1;
        for (int i = 0; i < children.size() - 1; i++) {
            Group child1 = children.get(i);
            Group child2 = children.get(i + 1);
            double qoutient = child1.getSize() / child2.getSize();
            assert (qoutient >= 1);
            maxQuotient = Math.max(maxQuotient, qoutient);
        }
        return maxQuotient;
    }

    /**
     * Adds a child to this group which contains only the groupitem contained in
     * this group. Deepens the group hierarchy by 1 level
     *
     * @pre this group must be a leaf node
     */
    public void addLevel() {
        assert isLeaf();
        HashSet<DataMap> items = new HashSet();
        items.addAll(groupItems);
        Group g = new Group(items);
        addChildGroup(g);
    }

    /**
     * Gets the leaf group in which dm is contained
     *
     * @param dm
     */
    public Group getLeafGroup(DataMap dm) {
        if (isLeaf() && groupItems.contains(dm)) {
            return this;
        }

        for (Group g : children) {

            if (g.groupItems.contains(dm)) {
                return g.getLeafGroup(dm);
            }
        }
        return null;
    }

    public Group deepCopy() {
        if (children == null || children.isEmpty()) {
            Group g = new Group(groupItems);
            return g;
        }
        List<Group> newChildren = new ArrayList();
        for (Group g : children) {
            Group childG = g.deepCopy();
            newChildren.add(g);
        }
        Group g = new Group(newChildren);
        return g;
    }

}
