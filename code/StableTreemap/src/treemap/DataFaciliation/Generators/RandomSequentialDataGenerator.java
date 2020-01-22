package treemap.DataFaciliation.Generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import treemap.dataStructure.DataMap;

/**
 *
 * @author max
 */
public class RandomSequentialDataGenerator extends RandomDataGenerator {

    HashMap<Integer, DataMap> generatedDataMaps;

    double changeChance = 0.4;
    double minChangeValue = -5;
    double maxChangeValue = 5;

    double addRemoveChange = 0.3;

    boolean exponential = false;

    public RandomSequentialDataGenerator() {
        super();
        initializeDataMapGeneration(0);
    }

    public RandomSequentialDataGenerator(int seed) {
        super(seed);
        initializeDataMapGeneration(0);
    }

    public RandomSequentialDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int minDepth, int maxDepth, int minSize, int maxSize, double changeValue, double changeChance, int time) {
        super(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize);
        this.minChangeValue = -changeValue;
        this.maxChangeValue = changeValue;
        this.changeChance = changeChance / (100.0);
        initializeDataMapGeneration(time);
    }

    public RandomSequentialDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int minDepth, int maxDepth, int minSize, int maxSize, double changeValue, double changeChance, int time, int addRemoveChange, boolean exponential, String experimentName) {
        this(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time);
        this.addRemoveChange = addRemoveChange / (100.0);
        this.exponential = exponential;
        this.experimentName = experimentName;
    }

    public RandomSequentialDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int minDepth, int maxDepth, int minSize, int maxSize, double changeValue, double changeChance, int time, int addRemoveChange, boolean exponential, String experimentName, int seed) {
        this(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time);
        this.addRemoveChange = addRemoveChange / (100.0);
        this.exponential = exponential;
        this.experimentName = experimentName;
        this.seed = seed;
    }

    private void initializeDataMapGeneration(int time) {

        generatedDataMaps = new HashMap<>();

        //generate initial dataset
        DataMap dataMap = getDataMap(time);

        generatedDataMaps.put(time, dataMap);
    }

    private int findClosestDataSet(int time) {
        Set<Integer> keySet = generatedDataMaps.keySet();
        int closestBelow = -1;
        for (int key : keySet) {
            if (key > closestBelow && key <= time) {
                closestBelow = key;
            }
        }
        return closestBelow;
    }

    @Override
    public DataMap getData(int time) {
        assert (time >= 0);
        int key = findClosestDataSet(time);
        if (key == -1) {
            initializeDataMapGeneration(0);
            key = 0;
        }

        DataMap dm = generatedDataMaps.get(key);

        while (key < time) {

            randomizer.setSeed(seed + (key));
            //first value is almost the same for the first number
            randomizer.nextDouble();
            //first values is almost the same with the same key

            //add removeDataMap random dataMaps
            if (addRemoveChange != 0) {
                dm = randomAddRemove(dm, 0);
            }

            if (exponential) {
                dm = randomizeDataMapExponential(dm, 0);
            } else {
                dm = randomizeDataMap(dm, 0);
            }
            key++;
            generatedDataMaps.put(key, dm);
        }

        return dm;
    }

    private DataMap randomizeDataMap(DataMap dm, int depth) {
        if (!dm.hasChildren()) {
            //it is a leaf node, so we are going to change the size
            double size = dm.getTargetSize();

            if (randomizer.nextDouble() > (1 - changeChance)) {
                double changeAmount = randomizer.nextDouble() * (maxChangeValue - minChangeValue) + minChangeValue;
                size += changeAmount;
            }

            size = Math.min(Math.max(minSize, size), maxSize);
            double sd = randomizer.nextDouble()*size;
            DataMap newDm = new DataMap(dm.getLabel(), size,sd, null, dm.getColor());
            return newDm;
        }

        LinkedList<DataMap> children = new LinkedList<>();

        for (DataMap child : dm.getChildren()) {
            DataMap newChild = randomizeDataMap(child, depth + 1);
            children.add(newChild);
        }
        double size = DataMap.getTotalSize(children);
        double sd = DataMap.getStandardDeviationChildren(children);
        DataMap newDm = new DataMap(dm.getLabel(), size,sd, children, dm.getColor());
        return newDm;
    }

    private DataMap randomizeDataMapExponential(DataMap dm, int depth) {
        if (!dm.hasChildren()) {
            //it is a leaf node, so we are going to change the size
            double size = dm.getTargetSize();

            if (randomizer.nextDouble() > (1 - changeChance)) {
                double changeExponent = randomizer.nextDouble() * (1.05 - 0.95) + 0.95;
                size *= changeExponent;
            }
            size = Math.min(Math.max(minSize, size), Double.MAX_VALUE / 100);
            double sd = randomizer.nextDouble()*size;
            DataMap newDm = new DataMap(dm.getLabel(), size,sd, null, dm.getColor());
            return newDm;
        }

        LinkedList<DataMap> children = new LinkedList<>();

        for (DataMap child : dm.getChildren()) {

            DataMap newChild = randomizeDataMapExponential(child, depth + 1);
            children.add(newChild);
        }
        double size = DataMap.getTotalSize(children);
        double sd = DataMap.getStandardDeviationChildren(children);
        DataMap newDm = new DataMap(dm.getLabel(), size,sd, children, dm.getColor());
        return newDm;
    }

    @Override
    public String getParamaterDescription() {
        String returnString = "minItemsPerLevel=" + minItemsPerLevel
                + ";maxItemsPerLevel=" + maxItemsPerLevel
                + ";minDepth=" + minDepth
                + ";maxDepth=" + maxDepth
                + ";minSize=" + minSize
                + ";maxSize=" + maxSize
                + ";seed=" + seed
                + ";changeChance=" + changeChance
                + ";minChangeValue=" + minChangeValue
                + ";maxChangeValue=" + maxChangeValue
                + ";addRemoveChange=" + addRemoveChange
                + ";exponential=" + exponential;

        return returnString;
    }

    /**
     * Randomly adds and removes from the children of dm
     *
     * @param dm
     * @return
     */
    private DataMap randomAddRemove(DataMap dm, int depth) {
        if (dm.hasChildren() == false) {
            return dm;
        }
        //figure out the datamaps to removeDataMap
        List<DataMap> toRemove = new ArrayList();
        if (dm.getChildren().size() > minItemsPerLevel) {
            //if we have enough children we are allowed to removeDataMap on child
            for (DataMap child : dm.getChildren()) {
                if (randomizer.nextDouble() < addRemoveChange) {
                        toRemove.add(child);
                }
            }
        }
        //make sure we keep at least minItemsPerLevel by keeping the last one

        while ((dm.getChildren().size() - toRemove.size()) < minItemsPerLevel) {
            toRemove.remove(randomizer.nextInt(toRemove.size()));
        }

        //figure out how much new dataMaps we need to add
        //and what the new size will become

        int childrenToAdd = 0;
        for (int childNumber = dm.getChildren().size(); childNumber < maxItemsPerLevel; childNumber++) {
            // For every child that is not yet in here, there is a chance that it will be added
            if (randomizer.nextDouble() < addRemoveChange) {
                childrenToAdd++;
            }
        }

        //figure out all the children of this dataset
        List<DataMap> children = new ArrayList();

        for (int i = 0; i < childrenToAdd; i++) {
            int childCount = dm.getChildren().size();
            //TODO Fix Color
            DataMap child = generateLevels(depth + 1, dm.getStartHue(), childCount + i, childCount + childrenToAdd);
            children.add(child);
        }

        for (DataMap child : dm.getChildren()) {
            if (!toRemove.contains(child)) {
                child = randomAddRemove(child, depth + 1);
                children.add(child);
            }
        }
        
        double size = DataMap.getTotalSize(children);
        double sd = DataMap.getStandardDeviationChildren(children);
        DataMap rootDm = new DataMap(dm.getLabel(), size,sd, children, dm.getColor(), dm.getStartHue());
        return rootDm;
    }

    @Override
    public String getExperimentName() {
        return experimentName;
    }

    @Override
    public RandomSequentialDataGenerator reinitializeWithSeed(int seed) {

        return new RandomSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, (int) minSize, (int) maxSize, maxChangeValue, changeChance, 0, (int) addRemoveChange * 100, exponential, experimentName, seed);
    }
}
