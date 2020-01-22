package treemap.DataFaciliation.Generators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import treemap.dataStructure.DataMap;

/**
 *
 * @author Max Sondag
 */
public class RandomDataGenerator extends DataGenerator {

    protected final Random randomizer = new Random(seed);

    protected int minItemsPerLevel;
    protected int maxItemsPerLevel;
    protected int minDepth;
    protected int maxDepth;

    double minSize;
    double maxSize;
    protected List<String> labelNames;

    protected String experimentName = "none";
    private int time = 0;

    public RandomDataGenerator() {
        initialize();
        this.seed = 0;
    }

    public RandomDataGenerator(int seed) {
        initialize();
        this.seed = seed;
    }

    public RandomDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int minDepth, int maxDepth, int minSize, int maxSize) {
        initialize(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize);
        this.seed = 200;
    }

    public RandomDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int minDepth, int maxDepth, int minSize, int maxSize, int seed) {
        initialize(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize);
        this.seed = seed;
    }

    private void initialize() {
        initialize(3, 6, 2, 3, 2, 20);
    }

    private void initialize(int minItemsPerLevel, int maxItemsPerLevel, int minDepth, int maxDepth, int minSize, int maxSize) {
        this.minItemsPerLevel = minItemsPerLevel;
        this.maxItemsPerLevel = maxItemsPerLevel;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.labelNames = new LinkedList<>();
    }

    @Override
    public DataMap getData(int time) {
        this.time = time;
        return getDataMap(time);
    }

    protected DataMap getDataMap(int time) {
        this.labelNames = new LinkedList<>();

        randomizer.setSeed(seed + time);
        //first value is almost the same for the first number
        randomizer.nextDouble();

        float startHue = 226f / 360f;
        //make sure it is deterministic with regard to time
        //first dataMap contains a square containing all element
        DataMap data = generateLevels(0, startHue, 0, 1);
        return data;
    }

    private Color getColor(float startHue, int childNumber, int childCount) {
        float number = childNumber;
        float count = childCount - 1;
        float saturationValue = number / count * 0.5f + 0.5f;
        return Color.getHSBColor(startHue, saturationValue, 1f);
    }

    protected double getRandomSize() {
        return randomizer.nextDouble() * (maxSize - minSize) + minSize;
    }

    protected double getRandomSd(double maxSd) {
        return randomizer.nextDouble() * (maxSd);
    }

    protected String getLabel() {
        String label = NameGenerator.getUniqueName(labelNames, randomizer);
        labelNames.add(label);
        return label;
    }

    protected DataMap generateLevels(int depth, float startHue, int childNumber, int childCount) {

        //  startHue = (depth * 1.0f - minDepth * 1.0f) / (maxDepth * 1.0f - minDepth * 1.0f)*0.1f;
        Color color = getColor(startHue, childNumber, childCount);

        String label = getLabel();

        //uniform distribution to stop at a level between minDepth and maxDepth
        if (depth >= minDepth) {
            if (depth == maxDepth) {
                //max depth reacher
                double size = getRandomSize();
                double sd = getRandomSd(size);
                DataMap dm = new DataMap(label, size, sd, null, color, startHue);
                return dm;
            }

            //+1 added as it is not an exclusive upperbound
            int random = randomizer.nextInt(maxDepth - depth + 1) + depth;
            if (random == (maxDepth - 1)) {
                //random termination
                double size = getRandomSize();
                double sd = getRandomSd(size);
                DataMap dm = new DataMap(label, size, sd, null, color, startHue);
                return dm;
            }
        }

        //we add going to add another layer to this dataNode
        //get the amount of items in the next layer
        int items;
        if (maxItemsPerLevel == minItemsPerLevel) {
            items = maxItemsPerLevel;
        } else {
            //+1 added as it is not inclusive upper bound
            items = randomizer.nextInt(maxItemsPerLevel - minItemsPerLevel + 1) + minItemsPerLevel;
        }

        ArrayList<DataMap> children = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            DataMap child = generateLevels(depth + 1, startHue, i, items);
            children.add(child);
        }
        double size = DataMap.getTotalSize(children);
        double sd = DataMap.getStandardDeviationChildren(children);
        DataMap dm = new DataMap(label, size, sd, children, color, startHue);

        return dm;
    }

    @Override
    public String getParamaterDescription() {
        String returnString = "minItemsPerLevel=" + minItemsPerLevel
                              + ";maxItemsPerLevel=" + maxItemsPerLevel
                              + ";minDepth=" + minDepth
                              + ";maxDepth=" + maxDepth
                              + ";minSize=" + minSize
                              + ";maxSize=" + maxSize
                              + ";seed=" + seed;

        return returnString;
    }

    @Override
    public String getExperimentName() {
        return experimentName;
    }

    @Override
    public RandomDataGenerator reinitializeWithSeed(int seed) {
        return new RandomDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, (int) minSize, (int) maxSize, seed);

    }

    @Override
    public int getLastTime() {
        return time;
    }

    @Override
    public void useSdAsValue() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
