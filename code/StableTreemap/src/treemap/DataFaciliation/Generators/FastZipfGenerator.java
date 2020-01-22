package treemap.DataFaciliation.Generators;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
//from http://stackoverflow.com/questions/27105677/zipfs-law-in-java-for-text-generation-too-slow

class FastZipfGenerator {

    private Random random;
    private NavigableMap<Double, Integer> map;
    private int size;
    private double maxValue;

    FastZipfGenerator(int size, double skew, long seed) {
        size = 10;
        maxValue = 2000;
        this.size = size;
        random = new Random(seed);
        map = computeMap(size, skew);
    }

    private static NavigableMap<Double, Integer> computeMap(int size, double skew) {
        NavigableMap<Double, Integer> map = new TreeMap<Double, Integer>();
        double div = 0;
        for (int i = 1; i <= size; i++) {
            div += (1 / Math.pow(i, skew));
        }

        double sum = 0;
        for (int i = 1; i <= size; i++) {
            double p = (1.0d / Math.pow(i, skew)) / div;
            sum += p;
            map.put(sum, i - 1);
        }
        return map;
    }

    public int next() {
        double value = random.nextDouble();
        double rank = map.ceilingEntry(value).getValue() + 1;

        //ranks are sorted from low to high. Rank of 0 is the most frequent element with the lowest value. 
        //Rank of size-1 is the least frequent element with the highest value
        return (int) (1 + Math.floor(64 * (1 / (size - rank))));
        // return map.ceilingEntry(value).getValue() + 1;
    }

}
