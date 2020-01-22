/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataSetClassifier;

import UserControl.Simulator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import treemap.DataFaciliation.DataFacilitator;
import treemap.dataStructure.DataMap;

/**
 * This class classifies each dataset according to its attributes
 *
 * @author msondag
 */
public class DataSetClassifier {

    List<DataFacilitator> datasets;

    public static void main(String args[]) {
//        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\TreemapStability\\dataSetEdu"));
        System.out.println("start reading datasets");
        //processed
//        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\MovieDatabase\\data\\processed"));
        //procesed-copy
//        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\MovieDatabase\\data\\processed - Copy"));
        //dataset-temp
        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\TreeMapUncertainty\\allDatasets"),false);
        //final-table
//        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\TreemapStability\\dataSetsInTable"));
        //renamedfinal-table
//        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\TreemapStability\\dataSetsInTableRenamed"));
        //temp
//List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\TreemapStability\\dataset-temp"));

        System.out.println("datasets.size() = " + datasets.size());
        System.out.println("got the datasets");
        DataSetClassifier dsc = new DataSetClassifier(datasets);
        System.out.println("start classifying");
        dsc.classifyDataSets();
    }

    public DataSetClassifier(List<DataFacilitator> datasets) {
        this.datasets = datasets;
    }

    public void classifyDataSets() {
        StringBuilder sb = new StringBuilder();
        sb.append("Verify that all properties are correct \r\n");
        sb.append("title,"
                  //                  + "uniqueNodes,"
                  //                  + "maxNodes,"
                  //                  + "maxHeight,"
                  //                  + "numberOfTimeSteps,"
                  //                  + "maxValueChangeRate,"
                  //                  + "medianValueChangeRate,"
                  //                  + "maxChangeRate,"
                  //                  + "medianChangeRate,"
                  //                  + "VariationCofficientOfSizes\r\n");
                  //                    + "uniqueNodes,"
                  + "maxNodes,"
                  + "timesteps,"
                  + "sizeSpike,"
                  + "sizeSmall,"
                  + "sizeRegular,"
                  + "dataSpike,"
                  + "dataSmall,"
                  + "dataRegular,"
                  + "deep,"
                  + "shallow,"
                  + "hasHighVariance,"
                  + "hasLowVariance\r\n");

        for (DataFacilitator dataset : datasets) {
            classifyDataSet(sb, dataset);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\Development\\TreeMapUncertainty\\classifications\\datasetsClassified.csv"))) {
            //header row

            bw.append(sb);//Internally it does aSB.toString();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void classifyDataSet(StringBuilder sb, DataFacilitator dataset) {
        //stringBuilder will store the classification
        int uniqueNodes = getUniqueNodeAmount(dataset); //get the total amount of nodes in the tree
        int maxNodes = getMaxNodes(dataset); //get the maximum amount of nodes in the tree at a point in time
        int maxHeight = getHeight(dataset); //gets the maximum height of the tree        
        int numberOfTimeSteps = getTimeSteps(dataset);//get the number of time steps in the dataset

        //calculate the changes in sizes
        List<Double> sizeChangeValues = getSumChangeValues(dataset); //returns how much the data changed per timestep.

        //calculate the insertion/deletion amount
        List<Double> deletionAmount = getDeletionAmount(dataset); //returns how many leafs were deleted at t+1 normalized on the amount of leafs at time t for all t
        List<Double> additionAmount = getAdditionAmount(dataset); //returns how many leafs were added at t+1 normalized on the amount of leafs at time t for all t
        List<Double> dataChangeValues = new ArrayList();
        for (int i = 0; i < deletionAmount.size(); i++) {
            //add the additions to the deletion number
            double delAmount = deletionAmount.get(i);
            double addAmount = additionAmount.get(i);
            dataChangeValues.add(addAmount + delAmount);
        }

        //calculate the variation
        List<Double> nodeSizes = getLeafSizes(dataset); //get the distribution of leaf sizes(aggregated over all time)
        double sizeCoefficient = getCoefficentOfVariation(nodeSizes);
        if (Double.isNaN(sizeCoefficient)) {
            System.out.println("nan");
        }
        System.out.println("dataset = " + dataset.getDataIdentifier());
//
        System.out.print("size,");
        for (double d : sizeChangeValues) {
            System.out.print("," + d);
        }

        System.out.println("");
        boolean sizeSpike = isSpikeClassification(sizeChangeValues);
        boolean sizeSmall = isSmallClassification(sizeChangeValues);
        boolean sizeRegular = isRegularClassification(sizeChangeValues);

        System.out.print("data,");
        boolean dataSpike = isSpikeClassification(dataChangeValues);
        boolean dataSmall = isSmallClassification(dataChangeValues);
        boolean dataRegular = isRegularClassification(dataChangeValues);
        for (double d : dataChangeValues) {
            System.out.print("," + d);
        }
        System.out.println("");
        boolean isDeep = isDeepClassification(maxHeight);
        boolean isShallow = isShallowClassification(maxHeight);

        boolean hasHighVariance = sizeCoefficient > 1;
        boolean hasLowVariance = sizeCoefficient < 1;
//        System.out.println("sizeCoefficient = " + sizeCoefficient);
        writeClassification(sb, dataset.getDataIdentifier(), maxNodes, numberOfTimeSteps, sizeSpike, sizeSmall, sizeRegular, dataSpike, dataSmall, dataRegular, isDeep, isShallow, hasHighVariance, hasLowVariance);
    }

    private boolean isRegularClassification(List<Double> changeValues) {
        for (double d : changeValues) {

            if (d >= 0.2 || d <= 0.04) {
                return false;
            }
        }
        return true;
    }

    private boolean isSmallClassification(List<Double> changeValues) {
        for (double d : changeValues) {
            if (d >= 0.04) {
                return false;
            }
        }
        return true;
    }

    private boolean isSpikeClassification(List<Double> changeValues) {
        for (double d : changeValues) {
            if (d >= 0.2) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeepClassification(int maxHeight) {
        System.out.println("maxHeight = " + maxHeight);
        if (maxHeight >= 5) {
            return true;
        }
        return false;
    }

    private boolean isShallowClassification(int maxHeight) {
        if (maxHeight <= 2) {
            return true;
        }
        return false;
    }

    private int getUniqueNodeAmount(DataFacilitator dataSet) {
        Set<String> nodeNames = new HashSet();
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allLeafs = data.getAllLeafs();
            for (DataMap dm : allLeafs) {
                nodeNames.add(dm.getLabel());
            }
        }
        return nodeNames.size();
    }

    private int getMaxNodes(DataFacilitator dataSet) {
        int maxNodes = 0;
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allLeafs = data.getAllLeafs();
            maxNodes = Math.max(allLeafs.size(), maxNodes);
        }
        return maxNodes;
    }

    private int getHeight(DataFacilitator dataSet) {
        int maxHeight = 0;
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            int height = getHeight(data);

            maxHeight = Math.max(height, maxHeight);
        }
        return maxHeight;
    }

    private int getHeight(DataMap data) {
        int maxHeight = 0;
        for (DataMap dm : data.getChildren()) {
            maxHeight = Math.max(getHeight(dm) + 1, maxHeight);
        }
        return maxHeight;
    }

    /**
     * Returns a list of relative sizes. For each leaf node we store the
     * relative size compared to the root
     *
     * @param dataSet
     * @return
     */
    private List<Double> getLeafSizes(DataFacilitator dataSet) {
        List<Double> sizes = new ArrayList();
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            double sizeT = data.getTargetSize();

            List<DataMap> allLeafs = data.getAllLeafs();
            for (DataMap dm : allLeafs) {
                if (dm.getTargetSize() == 0) {
                    continue;
                }
                double relSize = dm.getTargetSize() / sizeT;
                if (!Double.isNaN(relSize)) {
                    sizes.add(relSize);
                }
            }
        }
        return sizes;
    }

    private int getTimeSteps(DataFacilitator dataset) {
        return dataset.getMaxTime()+2;
    }

    private List<Double> getSumChangeValues(DataFacilitator dataset) {
        List<Double> sumDataChange = new ArrayList();
        for (int t1 = 0; t1 < (dataset.getMaxTime()); t1++) {
            //get the data change for a single timestep
            double sum = 0;
            DataMap dataT1 = dataset.getData(t1);
            List<DataMap> allLeafsT1 = dataT1.getAllLeafs();

            DataMap dataT2 = dataset.getData(t1 + 1);
            List<DataMap> allLeafsT2 = dataT2.getAllLeafs();

            double tw1 = dataT1.getTargetSize();
            double tw2 = dataT2.getTargetSize();

            for (DataMap dm1 : allLeafsT1) {
                for (DataMap dm2 : allLeafsT2) {
                    if (dm1.getLabel().equals(dm2.getLabel())) {
                        //dm1 is present in dm2
                        double w1 = dm1.getTargetSize() / tw1;
                        double w2 = dm2.getTargetSize() / tw2;

                        double absoluteDiff = Math.abs(w2 - w1);
                        sum += absoluteDiff;
                    }
                }
            }
            if (!Double.isNaN(sum)) {
                sumDataChange.add(sum);
            }
        }
        return sumDataChange;
    }

    private List<Double> getDeletionAmount(DataFacilitator dataset) {
        List<Double> deletionAmounts = new ArrayList();
        for (int t1 = 0; t1 < (dataset.getMaxTime()); t1++) {
            int amountDeleted = 0;

            DataMap dataT1 = dataset.getData(t1);
            List<DataMap> allLeafsT1 = dataT1.getAllLeafs();

            DataMap dataT2 = dataset.getData(t1 + 1);
            List<DataMap> allLeafsT2 = dataT2.getAllLeafs();

            for (DataMap dm1 : allLeafsT1) {
                boolean found = false;
                for (DataMap dm2 : allLeafsT2) {
                    if (dm1.getLabel().equals(dm2.getLabel())) {
                        found = true;
                        break;//no need to look further for dm2
                    }
                }
                if (!found) {
                    amountDeleted++;
                }
            }

            deletionAmounts.add((double) amountDeleted / (double) allLeafsT1.size());
        }
        return deletionAmounts;
    }

    private List<Double> getAdditionAmount(DataFacilitator dataset) {
        List<Double> addedAmounts = new ArrayList();
        for (int t1 = 0; t1 < (dataset.getMaxTime()); t1++) {
            int amountAdded = 0;

            DataMap dataT1 = dataset.getData(t1);
            List<DataMap> allLeafsT1 = dataT1.getAllLeafs();

            DataMap dataT2 = dataset.getData(t1 + 1);
            List<DataMap> allLeafssT2 = dataT2.getAllLeafs();

            for (DataMap dm2 : allLeafssT2) {
                boolean found = false;
                for (DataMap dm1 : allLeafsT1) {
                    if (dm1.getLabel().equals(dm2.getLabel())) {
                        found = true;
                        break;//no need to look further for dm2
                    }
                }
                if (!found) {
                    amountAdded++;
                }
            }
            addedAmounts.add((double) amountAdded / (double) allLeafsT1.size());
        }
        return addedAmounts;
    }

    private double getMean(List<Double> doubleList) {

        double sum = 0;
        for (double d : doubleList) {
            if (Double.isNaN(d)) {
                continue;
            }
            sum += d;
        }
        return sum / (double) doubleList.size();
    }

    private double getMedian(List<Double> doubleList) {
        Collections.sort(doubleList);
        return doubleList.get(doubleList.size() / 2);
    }

    private double getMax(List<Double> doubleList) {
        double max = 0;
        for (double d : doubleList) {
            max = Math.max(max, d);
        }
        return max;
    }

    private double getCoefficentOfVariation(List<Double> doubleList) {
        double mean = getMean(doubleList);
        double variance = 0;
        for (Double d : doubleList) {
            if (Double.isNaN(d)) {
                continue;
            }
            variance += Math.pow((d - mean), 2);
        }
        double returnVal = Math.sqrt(variance / (doubleList.size() - 1)) / mean;
        return returnVal;
    }

//    private void writeClassification(StringBuilder sb, String dataIdentifier,
//                                     int uniqueNodes, int maxNodes, int maxHeight,
//                                     int numberOfTimeSteps, double maxDataChangeRate,
//                                     double medianDataChangeRate, double maxChangeRate,
//                                     double medianChangeRate, double sizeCoefficient) {
//
//        String dataSetName = dataIdentifier.substring(dataIdentifier.lastIndexOf("\\") + 1, dataIdentifier.lastIndexOf("."));
//        sb.append(dataSetName);
//        sb.append("," + uniqueNodes);
//        sb.append("," + maxNodes);
//        sb.append("," + maxHeight);
//        sb.append("," + numberOfTimeSteps);
//        sb.append("," + maxDataChangeRate);
//        sb.append("," + medianDataChangeRate);
//        sb.append("," + maxChangeRate);
//        sb.append("," + medianChangeRate);
//        sb.append("," + sizeCoefficient + "\r\n");
//    }
    private void writeClassification(
            StringBuilder sb, String dataIdentifier, int maxNodes, int numberOfTimeSteps,
            boolean sizeSpike, boolean sizeSmall, boolean sizeRegular,
            boolean dataSpike, boolean dataSmall, boolean dataRegular,
            boolean deep, boolean shallow,
            boolean hasHighVariance, boolean hasLowVariance) {
        String dataSetName = dataIdentifier.substring(dataIdentifier.lastIndexOf("\\") + 1, dataIdentifier.lastIndexOf("."));
        sb.append(dataSetName);
        sb.append("," + maxNodes);
        sb.append("," + numberOfTimeSteps);
        sb.append("," + sizeSpike);
        sb.append("," + sizeSmall);
        sb.append("," + sizeRegular);
        sb.append("," + dataSpike);
        sb.append("," + dataSmall);
        sb.append("," + dataRegular);
        sb.append("," + deep);
        sb.append("," + shallow);
        sb.append("," + hasHighVariance);
        sb.append("," + hasLowVariance + "\r\n");
    }

}
