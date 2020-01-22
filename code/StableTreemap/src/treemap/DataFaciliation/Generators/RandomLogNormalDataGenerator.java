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
public class RandomLogNormalDataGenerator extends DataGenerator {

    protected Random randomizer = new Random(seed);
    int minItemsPerLevel;
    int maxItemsPerLevel;
    private int time = 0;
    protected List<String> labelNames;

    protected String experimentName = "none";

    public RandomLogNormalDataGenerator() {
        initialize();
        this.seed = 0;
    }

    public RandomLogNormalDataGenerator(int seed) {
        initialize();
        this.seed = seed;
    }

    public RandomLogNormalDataGenerator(int minItemsPerLevel, int maxItemsPerLevel) {
        initialize(minItemsPerLevel, maxItemsPerLevel);
        this.seed = 200;
    }

    public RandomLogNormalDataGenerator(int minItemsPerLevel, int maxItemsPerLevel, int seed) {
        initialize(minItemsPerLevel, maxItemsPerLevel);
        this.seed = seed;
    }

    private void initialize() {
        initialize(3, 6);
    }

    private void initialize(int minItemsPerLevel, int maxItemsPerLevel) {
        this.minItemsPerLevel = minItemsPerLevel;
        this.maxItemsPerLevel = maxItemsPerLevel;
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

        //make sure it is deterministic with regard to time
        //first dataMap contains a square containing all element
        DataMap data = generateDataMap();
        return data;
    }

    private Color getColor(float startHue, int childNumber, int childCount) {
        float number = childNumber;
        float count = childCount - 1;
        float saturationValue = number / count * 0.6f + 0.2f;
        return Color.getHSBColor(startHue, saturationValue, 1f);
    }

    protected String getLabel() {
        String label = NameGenerator.getUniqueName(labelNames, randomizer);
        labelNames.add(label);
        return label;
    }

    protected DataMap generateDataMap() {
        float startHue = 226f / 360f;

        //find out how many items we have 
        int items;
        if (maxItemsPerLevel == minItemsPerLevel) {
            items = maxItemsPerLevel;
        } else {
            items = randomizer.nextInt(maxItemsPerLevel - minItemsPerLevel) + minItemsPerLevel;
        }

        ArrayList<DataMap> children = new ArrayList<>();

        double size = 0;//holds the total of the size of all children
        for (int i = 0; i < items; i++) {
            Color color = getColor(startHue, i, items);
            String label = getLabel();
            double childSize = Math.exp(randomizer.nextGaussian());
            //Scale the childsizes to make it legible
            childSize *= 100;

            size += childSize;
            double sd = Math.exp(randomizer.nextGaussian())*childSize;
            DataMap dm = new DataMap(label, childSize,sd, null, color);
            children.add(dm);
        }
        double sd = DataMap.getStandardDeviationChildren(children);
        DataMap dm = new DataMap("root", size,sd, children, Color.BLUE);
        return dm;
    }

    @Override
    public String getParamaterDescription() {
        String returnString = "minItemsPerLevel=" + minItemsPerLevel
                + ";maxItemsPerLevel=" + maxItemsPerLevel
                + ";seed=" + seed;

        return returnString;
    }

    @Override
    public String getExperimentName() {
        return experimentName;
    }

    @Override
    public RandomLogNormalDataGenerator reinitializeWithSeed(int seed) {
        return new RandomLogNormalDataGenerator(minItemsPerLevel, maxItemsPerLevel, seed);

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
