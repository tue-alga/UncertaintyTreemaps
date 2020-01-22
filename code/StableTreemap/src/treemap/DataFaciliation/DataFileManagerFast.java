package treemap.DataFaciliation;

import com.opencsv.CSVReader;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import treemap.dataStructure.DataMap;
import utility.ColorBrewer;
import utility.Randomizer;

/**
 *
 * @author Max Sondag
 */
public class DataFileManagerFast implements DataFacilitator {

    File inputFile;
    Map<Integer, DataMap> timeMap;
    private int time = 0;
    private String inputFileLocation;
    boolean hasSd;
    boolean hasHeader;

    public DataFileManagerFast(String inputFileLocation, boolean hasSd, boolean hasHeader) {
        this.inputFile = new File(inputFileLocation);
        this.hasSd = hasSd;
        this.hasHeader = hasHeader;
        timeMap = new HashMap();
//        if (inputFile.toString().endsWith(".csv"));
//        {
        readCSVFile();
//        }
        this.inputFileLocation = inputFileLocation;
    }

    
  
    /**
     * Format of csv files should be as follows: id, parentId(root if not
     * present), size at time 0, size at time 1 ..... First line should
     * contain the headers In case standard deviations are present they come
     * immediately after the size. thus:id, parentId, size at time 0, sd at time
     * 0, size at time
     * 1, sd at time 1, ...
     */
    private void readCSVFile() {

        try {
            CSVReader reader = new CSVReader(new FileReader(inputFile), ',');
            String[] nextLine;

            List<StoredData> dataList = new LinkedList();

            int hasColor = 0;//There are no colors
            //skip the first line if it is a header
            if (hasHeader) {
                nextLine = reader.readNext();
                //            //whether color values are included or not
                if (nextLine[2].equals("Color")) {
                    hasColor = 1;
                    System.out.println("hasColor");
                }
            }

            Color[] colors = new Color[0];
            int colorCount = 0;
            if (hasColor == 0) {
                ColorBrewer cb = ColorBrewer.Pastel1;
                //quickly count the amount of lines
                CSVReader reader2 = new CSVReader(new FileReader(inputFile), ',');
                List<String[]> readAll = reader2.readAll();
                int amount = readAll.size();
                colors = cb.getColorPalette(amount);
            }

            while ((nextLine = reader.readNext()) != null) {
                String id = nextLine[0];
                String parentId = nextLine[1];

                Color color = null;

                List sizes = new LinkedList();
                List sds = new LinkedList();
                boolean sdOnCurrentPlace = false;
                for (int i = (2 + hasColor); i < nextLine.length; i++) {
                    if (sdOnCurrentPlace) {
                        double sd = Double.parseDouble(nextLine[i]);
                        sds.add(sd);
                    } else {
                        double size = Double.parseDouble(nextLine[i]);
                        sizes.add(size);
                    }

                    if (hasSd) {
                        //sd is stored after the size
                        sdOnCurrentPlace = !sdOnCurrentPlace;
                    } else {
                        sds.add(0.0);//give 0 values to all sds if they are not present
                    }
                }
                if (hasColor == 0) {
                    color = colors[colorCount];
                    colorCount++;
                }
                if (hasColor == 1) {
                    color = Color.decode(nextLine[2]);//hex to color
                }
                StoredData data = new StoredData(id, parentId, sizes, sds, color);
                dataList.add(data);
            }
            if (dataList.isEmpty()) {
                return;
            }
            //Go through all the times, convert them to datamaps and add them to the mapping
            int maxTime = dataList.get(0).getDataAmount();
            for (int time = 0; time < maxTime; time++) {
                DataMap convertToDataMap = convertToDataMap(dataList, time);
                timeMap.put(time, convertToDataMap);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataFileManagerFast.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataFileManagerFast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int count = 0;

    /**
     *
     * @param id       The id of the datamap we are
     * @param dataList the complete list of data
     * @param time     the time identifier
     * @return
     */
    private DataMap convertToDataMap(List<StoredData> dataList, int time) {
        HashMap<String, DataMap> dataMapMapping = new HashMap();
        HashMap<String, List<DataMap>> childMapping = new HashMap();
        //create the datamaps
        for (StoredData sd : dataList) {
            if (sd.getSizes().get(time) == 0) {
                continue;
            }
            DataMap dm = new DataMap(sd.getId(), sd.getSizes().get(time), sd.getStandardDeviations().get(time), null, sd.getColor());
            dataMapMapping.put(sd.getId(), dm);
            if (!childMapping.containsKey(sd.getParentId())) {
                childMapping.put(sd.getParentId(), new ArrayList<>());
            }
            childMapping.get(sd.getParentId()).add(dm);
        }

        DataMap root = new DataMap("root", 0, 0, null, Color.red);
        dataMapMapping.put("root", root);

        //create the parent child relations
        for (DataMap parent : dataMapMapping.values()) {
            if (childMapping.containsKey(parent.getLabel())) {
                parent.addDataMaps(childMapping.get(parent.getLabel()), parent);
            }
        }

        //all the weights will be wrong, so we have to fix that
        //recursively update the weights
        //slight randomization of leaf weights is used to prevent degeneracies from occuring too much
        randomizeLeafWeights(root);
        initializeWeights(root);
        return root;
    }

    public StoredData getStoredDataElement(List<StoredData> dataList, String id) {
        for (StoredData sd : dataList) {
            if (sd.getId().equals(id)) {
                return sd;
            }
        }
        return null;
    }

    @Override
    public DataMap getData(int time) {
        this.time = time;
        return timeMap.get(time);
    }

    @Override
    public String getDataIdentifier() {
        return inputFile.getAbsolutePath();
    }

    @Override
    public String getParamaterDescription() {
        return "FileName=" + inputFile.getName();
    }

    @Override
    public String getExperimentName() {
        return inputFile.getName();
    }

    @Override
    public DataFacilitator reinitializeWithSeed(int seed) {
        return new DataFileManagerFast(inputFileLocation, hasSd, hasHeader);
    }

    @Override
    public boolean hasMaxTime() {
        return true;
    }

    @Override
    public int getMaxTime() {
        int maxTime = 0;
        for (int t : timeMap.keySet()) {
            maxTime = Math.max(t, maxTime);
        }
        return maxTime;
    }

    private void randomizeLeafWeights(DataMap root) {
        for (DataMap leaf : root.getAllLeafs()) {
            double increase = Randomizer.getRandomDouble() / 10000;
//            double increase = 0;
            leaf.setTargetSize(leaf.getTargetSize() + increase);
        }
    }

    private void initializeWeights(DataMap root) {

        for (DataMap dm : root.getChildren()) {
            initializeWeights(dm);
        }
        if (root.hasChildren()) {
            double size = DataMap.getTotalSize(root.getChildren());
            double sd = DataMap.getStandardDeviationChildren(root.getChildren());
            root.setTargetSize(size);
            root.setSd(sd);
        }
    }

    @Override
    public int getLastTime() {
        return time;
    }

    @Override
    public void useSdAsValue() {
        for (Integer key : timeMap.keySet()) {
            DataMap dm = timeMap.get(key);
            dm.useSdAsMean();
            timeMap.put(key, dm);
        }
    }

}
