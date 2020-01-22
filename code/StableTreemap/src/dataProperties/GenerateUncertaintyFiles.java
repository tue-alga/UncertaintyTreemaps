/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import treemap.DataFaciliation.DataFileManagerFast;
import treemap.dataStructure.DataMap;

/**
 *
 * @author msondag
 */
public class GenerateUncertaintyFiles {

    public static void main(String[] args) {
//        DataFileManagerFast df = new DataFileManagerFast(new File("").getAbsolutePath() +"/datasets/RecheckedDatasets4-9-2019/UnSchemeCoffee.csv", false, true);
//        DataFileManagerFast df = new DataFileManagerFast(new File("").getAbsolutePath() +"/datasets/RecheckedDatasets4-9-2019/uncertaintyTestCase.csv", false, true);
        DataFileManagerFast df = new DataFileManagerFast(new File("").getAbsolutePath() +"/datasets/RecheckedDatasets4-9-2019/InfantDeath25Years.csv", false, true);

        GenerateUncertaintyFiles guf = new GenerateUncertaintyFiles();
        guf.getUncertainty(df);
    }

    public String getUncertainty(DataFileManagerFast df) {
        //not using a Set as Datamap equality is on structure and not values
        HashMap<String, List<DataMap>> labelValuesMap = new HashMap();
        int maxTime = df.getMaxTime();
        for (int t = 0; t <= maxTime; t++) {
            DataMap data = df.getData(t);
            List<DataMap> allChildren = data.getAllChildren();
            //add the value to the set
            for (DataMap child : allChildren) {
                String label = child.getLabel();
                List childList = labelValuesMap.getOrDefault(label, new ArrayList());
                childList.add(child);
                labelValuesMap.put(label, childList);
            }
        }
        //values contains each node present and all their values
        HashMap<String, Double> uncertaintyScores = new HashMap();
        HashMap<String, Double> meanScores = new HashMap();

        DataMap data = df.getData(0);
        for (String label : labelValuesMap.keySet()) {
            List<DataMap> values = labelValuesMap.get(label);
            double standardDeviation = DataMap.getStandardDeviationValues(values);
            double mean = DataMap.getMean(values);
            uncertaintyScores.put(label, standardDeviation);
            meanScores.put(label, mean);
        }

        String header = "Label,Parent,Color,Mean,sd";
        String uncertaintyString = getUncertaintyString(data, "root", uncertaintyScores, meanScores);
        System.out.println("Label,Parent,Color,Mean,sd");
        System.out.println(uncertaintyString);
        return header + "\n" + uncertaintyString;
    }

    private String getUncertaintyString(DataMap data, String parentLabel, HashMap<String, Double> uncertainty, HashMap<String, Double> meanScores) {
        String returnString = "";

        if (data.getLabel() != "root") {
            //don't put root in
            double mean = meanScores.get(data.getLabel());
            double sd = uncertainty.get(data.getLabel());
            int r = data.getColor().getRed();
            int g = data.getColor().getGreen();
            int b = data.getColor().getBlue();
            //get the score of this one and add it to the returnstring
            returnString += data.getLabel() + ",";
            returnString += parentLabel + ",";
            //# take first, 0 preface with 0's when needed, x use hexidecimal.
            returnString += String.format("#%02x%02x%02x", r, g, b) + ",";
            returnString += mean + ",";
            returnString += sd + "\n";
        }
        //recurse into the children and add the values of those
        for (DataMap child : data.getChildren()) {
            returnString += getUncertaintyString(child, data.getLabel(), uncertainty, meanScores);
        }

        return returnString;
    }

}
