package treemap.DataFaciliation.Generators;

import java.awt.Color;
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
public class RandomLogNormalSequentialDataGenerator extends RandomLogNormalDataGenerator {

    HashMap<Integer, DataMap> generatedDataMaps;
    double addRemoveChange = 0;
    double sd = 0.05;

    public RandomLogNormalSequentialDataGenerator() {
        super();
        initializeDataMapGeneration(0);
    }

    public RandomLogNormalSequentialDataGenerator(int seed) {
        super(seed);
        initializeDataMapGeneration(0);
    }

    public RandomLogNormalSequentialDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int time) {
        super(minItemsPerLevel, maxItemsPerLevel);
        initializeDataMapGeneration(time);
    }

    public RandomLogNormalSequentialDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int time, int addRemoveChange, String experimentName) {
        this(minItemsPerLevel, maxItemsPerLevel, time);
        this.addRemoveChange = addRemoveChange / (100.0);
        this.experimentName = experimentName;
    }

    public RandomLogNormalSequentialDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int time, int addRemoveChange, String experimentName, int seed) {
        this(minItemsPerLevel, maxItemsPerLevel, time);
        this.addRemoveChange = addRemoveChange / (100.0);
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

            //add removeDataMap random dataMaps
            if (addRemoveChange != 0) {
                dm = randomAddRemove(dm);
            }
            dm = randomizeDataMap(dm);
            key++;
            generatedDataMaps.put(key, dm);
        }

        return dm;
    }

    private DataMap randomizeDataMap(DataMap dm) {
        double size = dm.getTargetSize();
        double leafSD = dm.getSd();
        if (!dm.hasChildren()) {
            //it is a leaf node, so we are going to change the size
            size = size * Math.exp(randomizer.nextGaussian() * sd);
            leafSD = leafSD * Math.exp(randomizer.nextGaussian() * sd);
            DataMap newDm = new DataMap(dm.getLabel(), size, leafSD, null, dm.getColor());
            return newDm;
        }
        //not a leaf node, recurse
        LinkedList<DataMap> children = new LinkedList<>();

        for (DataMap child : dm.getChildren()) {
            DataMap newChild = randomizeDataMap(child);
            children.add(newChild);
        }
        double newSize = DataMap.getTotalSize(children);
        double newSd = DataMap.getStandardDeviationChildren(children);
        DataMap newDm = new DataMap(dm.getLabel(), newSize, newSd, children, dm.getColor());
        return newDm;
    }

    @Override
    public String getParamaterDescription() {
        String returnString = "minItemsPerLevel=" + minItemsPerLevel
                              + ";maxItemsPerLevel=" + maxItemsPerLevel
                              + ";seed=" + seed
                              + ";addRemoveChange=" + addRemoveChange;

        return returnString;
    }

    private DataMap randomAddRemove(DataMap dm) {
        double minSize = Double.MAX_VALUE, maxSize = Double.MIN_VALUE;
        //figure out the datamaps to removeDataMap
        List<DataMap> toRemove = new ArrayList();
        for (DataMap child : dm.getChildren()) {
            if (randomizer.nextDouble() < addRemoveChange) {
                toRemove.add(child);
            } else {
                minSize = Math.min(minSize, child.getTargetSize());
                maxSize = Math.max(maxSize, child.getTargetSize());
            }
        }
        //figure out how much new dataMaps we need to add
        //and what the new size will become
        int amount = 0;
        List<DataMap> toAdd = new ArrayList();
        for (int childNumber = dm.getChildren().size(); childNumber < maxItemsPerLevel; childNumber++) {
            if (randomizer.nextDouble() < addRemoveChange) {
                amount++;
            }
        }
        for (int i = 0; i < amount; i++) {
            double newSize = randomizer.nextGaussian() * (maxSize - minSize) + minSize;
            double newSd = randomizer.nextGaussian() * newSize;
            DataMap dmNew = new DataMap(getLabel(), newSize, newSd, null, Color.GREEN);
            toAdd.add(dmNew);
        }

        //only keep the children that do not need to be removed
        List<DataMap> children = new ArrayList();
        for (DataMap child : dm.getChildren()) {
            if (!toRemove.contains(child)) {
                DataMap newDm = new DataMap(child.getLabel(), child.getTargetSize(), child.getSd(), child.getChildren(), child.getColor());
                children.add(newDm);
            }
        }
        //add the children we need to add
        children.addAll(toAdd);
        double size = DataMap.getTotalSize(children);
        double sd = DataMap.getStandardDeviationChildren(children);
        DataMap rootDm = new DataMap(dm.getLabel(), size, sd, children, dm.getColor());
        return rootDm;
    }

    @Override
    public String getExperimentName() {
        return experimentName;
    }

    @Override
    public RandomLogNormalSequentialDataGenerator reinitializeWithSeed(int seed) {
        return new RandomLogNormalSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, 0, (int) addRemoveChange * 100, experimentName, seed);
    }
}
