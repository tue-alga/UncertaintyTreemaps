/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import treemap.DataFaciliation.DataFileManagerFast;
import treemap.dataStructure.DataMap;
import utility.Pair;

/**
 * Returns properties of the data given.
 *
 * @author msondag
 */
public class DataProperties {

    File dataFolder;

    public static void main(String[] args) {

        String folder = "D:\\Development\\TreeMapUncertainty\\allDatasets";
        DataProperties dp = new DataProperties(folder);

        dp.printProperties();
    }

    public DataProperties(String folder) {
        dataFolder = new File(folder);
        if (!dataFolder.isDirectory()) {
            throw new IllegalStateException(folder + " is not a folder");
        }
    }

    private void printProperties() {
        PropertyHolder property = new PropertyHolder();

        System.out.println(property.getHeaders());
        for (File f : dataFolder.listFiles()) {
            if (f.isDirectory()) {
                throw new IllegalStateException(f + " is a folder contained in the dataFolder instead of a file");
            }
            try {
                property = getFileProperties(f);
            } catch (Exception e) {
                e.printStackTrace();

                throw new IllegalStateException("Exception occured in file " + f);
            }

            System.out.println(f.getName() + ";" + property.getContent());
        }
    }

    /**
     * returns the root sd
     *
     * @param df
     * @param startTime
     * @param endTime
     * @return
     */
    private double getRootSdPercentage(DataFileManagerFast df, int startTime, int endTime) {
        //init list
        HashMap<String, List<Double>> areaValues = new HashMap();
        HashMap<String, Double> leafSd = new HashMap();
        HashMap<String, Double> leafMean = new HashMap();
        for (String s : getAllNames(df.getData(startTime).getAllLeafs())) {
            areaValues.put(s, new ArrayList());
        }
        //get all the values in
        for (int time = startTime; time <= endTime; time++) {
            for (DataMap dm : df.getData(time).getAllLeafs()) {
                if (areaValues.get(dm.getLabel()) == null) {
                    System.out.println("");
                }
                areaValues.get(dm.getLabel()).add(dm.getTargetSize());
            }
        }
        //calculate mean for each leaf as well as sd
        for (String s : areaValues.keySet()) {
            double mean = 0;
            List<Double> areas = areaValues.get(s);
            for (Double a : areas) {
                mean += a;
            }
            mean = mean / areas.size();
            leafMean.put(s, mean);

            //calculate sd
            double sdSum = 0;
            for (Double a : areaValues.get(s)) {
                sdSum += (a - mean) * (a - mean);
            }
            double sd = Math.sqrt(sdSum / areaValues.get(s).size());
            leafSd.put(s, sd);
        }

        //calculate sd of root
        double rootSdSum = 0;
        for (double sd : leafSd.values()) {
            rootSdSum += sd * sd;
        }
        double rootSd = Math.sqrt(rootSdSum);
        double rootMean = 0;
        for (double mean : leafMean.values()) {
            rootMean += mean;
        }

        return rootSd / rootMean;
    }

    /**
     * returns the root sd
     *
     * @param df
     * @param startTime
     * @param endTime
     * @return
     */
    private double getMaxSdPercentage(DataFileManagerFast df, int startTime, int endTime) {
        //init list
        HashMap<String, List<Double>> areaValues = new HashMap();
        HashMap<String, Double> leafSd = new HashMap();
        HashMap<String, Double> leafMean = new HashMap();
        for (String s : getAllNames(df.getData(startTime).getAllLeafs())) {
            areaValues.put(s, new ArrayList());
        }
        //get all the values in
        for (int time = startTime; time <= endTime; time++) {
            for (DataMap dm : df.getData(time).getAllLeafs()) {
                areaValues.get(dm.getLabel()).add(dm.getTargetSize());
            }
        }
        double maxSdPercentage = 0;
        //calculate mean each leaf
        for (String s : areaValues.keySet()) {
            double mean = 0;
            List<Double> areas = areaValues.get(s);
            for (Double a : areas) {
                mean += a;
            }
            mean = mean / areas.size();
            leafMean.put(s, mean);

            //calculate sd
            double sdSum = 0;
            for (Double a : areaValues.get(s)) {
                sdSum += (a - mean) * (a - mean);
            }
            double sd = Math.sqrt(sdSum / areaValues.get(s).size());
            leafSd.put(s, sd);
            maxSdPercentage = Math.max(maxSdPercentage, sd / mean);
        }
        return maxSdPercentage;
    }

    private class PropertyHolder {

        private int height;
        private int totalTime;
        private int startTime;
        private int endTime;
        private double rootSdPercentage;
        private int leafAmount;
        private double maxSdPercentage;

        private String getHeaders() {
            return "Dataset;Height;TotalTime;StartTime;EndTime;RootSdPercentage;maxSdPercentage;LeafAmount";
        }

        private String getContent() {
            String returnString = "";
            returnString += height + ";";
            returnString += totalTime + ";";
            returnString += startTime + ";";
            returnString += endTime + ";";
            returnString += rootSdPercentage + ";";
            returnString += maxSdPercentage + ";";
            returnString += leafAmount;
            return returnString;
        }

    }

    private PropertyHolder getFileProperties(File f) {
        DataFileManagerFast df = new DataFileManagerFast(f.getAbsolutePath(), false, false);
        PropertyHolder property = new PropertyHolder();

        Pair<Integer, Integer> time = getLargestUnchangedPeriod(df);
        int startTime = time.x;
        int endTime = time.y;

        if (startTime == endTime) {
            //nothing interesting in this case.
            return new PropertyHolder();
        }
        property.totalTime = df.getMaxTime();
        property.startTime = startTime;
        property.endTime = endTime;

        property.height = df.getData(startTime).getHeight();

        double sd = getRootSdPercentage(df, startTime, endTime);
        property.rootSdPercentage = sd;

        double maxSd = getMaxSdPercentage(df, startTime, endTime);
        property.maxSdPercentage = maxSd;

        int leafAmount = df.getData(startTime).getAllLeafs().size();
        property.leafAmount = leafAmount;
        return property;
    }

    /**
     * gets the largest period in time where no additions of deletions have been
     * done. [startTime,endtime], both are inclusive
     *
     * @param df
     * @return
     */
    private Pair<Integer, Integer> getLargestUnchangedPeriod(DataFileManagerFast df) {

        int startTime = 0;
        int startMaxTime = 0;
        int endMaxTime = 0;

        List<String> currentNames = getAllNames(df.getData(0).getAllLeafs());
        for (int time = 0; time <= df.getMaxTime(); time++) {
            List<String> newNames = getAllNames(df.getData(time).getAllLeafs());
            if (isSameList(newNames, currentNames)) {
                continue;
            }
            //no longer the same list. check if it is longer
            if (time - startTime > endMaxTime - startMaxTime) {
                //it is longer
                startMaxTime = startTime;
                endMaxTime = time-1;
            }
            currentNames = newNames;
            startTime = time+1;
        }
        if ((df.getMaxTime() - 1) - startTime > endMaxTime - startMaxTime) {
            //it is longer
            startMaxTime = startTime;
            endMaxTime = df.getMaxTime();
        }

        return new Pair(startMaxTime, endMaxTime);
    }

    private <T> boolean isSameList(List<T> l1, List<T> l2) {
        return (l1.containsAll(l2) && l1.size() == l2.size());
    }

    private List<String> getAllNames(List<DataMap> nodes) {
        List<String> names = new ArrayList();
        nodes.forEach((dm) -> {
            if (dm.getTargetSize() > 0) { //only add those that have a value
                names.add(dm.getLabel());
            }
        });
        return names;
    }

}
