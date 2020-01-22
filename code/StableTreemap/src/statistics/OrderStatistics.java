/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Calculates the quantile values
 *
 * @author msondag
 */
public class OrderStatistics {

    public double min;
    public double max;
    public double twentyFive;
    public double fifty;
    public double seventyFive;
    public double mean;
    public double sum;
    public double count;
    public double total;
    public double nonZero;

    public OrderStatistics(Collection<Double> values) {
        ArrayList<Double> sortedList = new ArrayList(values);
        //sorted from small to high
        Collections.sort(sortedList);

        min = sortedList.get(0);
        max = sortedList.get(sortedList.size() - 1);
        twentyFive = sortedList.get(sortedList.size() / 4);
        fifty = sortedList.get(sortedList.size() / 2);
        seventyFive = sortedList.get(3 * sortedList.size() / 4);
        mean = 0.0;
        sum = 0.0;
        total = 0;
        nonZero = 0;
        for (Double d : sortedList) {
            mean += d;
            sum += d;
            total++;
            if (d > 0) {
                nonZero++;
            }
        }
        mean /= sortedList.size();

        count = 0;
        for (Double d : sortedList) {
            if (d > mean) {
                count++;
            }
        }
    }

    String getQuantileScores() {
        return min + "," + twentyFive + "," + fifty + "," + seventyFive + "," + max + "," + mean + "," + sum + "," + count + "," + total + "," + nonZero;
    }
}
