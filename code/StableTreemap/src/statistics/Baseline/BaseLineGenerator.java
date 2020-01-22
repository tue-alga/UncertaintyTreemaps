package statistics.Baseline;

import TreeMapGenerator.LocalChanges.OrderEquivalentMaximalSegment;
import TreeMapGenerator.LocalChanges.TreeMapChangeGenerator;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import statistics.StatisticalTracker;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.DataFileManagerFast;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import static utility.Precision.eq;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author msondag
 */
public class BaseLineGenerator {

    private final double DELETEWEIGHT = 0.01;

    public void generateBaseLines(File dataFile, boolean hasSd, boolean hasHeader, File inputRectanglesFolder, File outputFolder) {

        DataFacilitator df = new DataFileManagerFast(dataFile.getAbsolutePath(), hasSd,hasHeader);
        generateBaseLines(df, inputRectanglesFolder, outputFolder);
    }

    //Used for manual baseline generation of a folder
    public static void main(String[] args) {
        String dataSet = "fsharp";
        File outputFolder = new File("D:\\Development\\TreemapStability\\output-temp\\baseLineGIT" + dataSet);
        File rectangleInputFolder = new File("D:\\Development\\TreemapStability\\git-with-prefix\\" + dataSet);
        DataFacilitator fac = new DataFileManagerFast("D:\\Development\\TreemapStability\\dataset-temp\\" + dataSet + ".data", false,false);

        outputFolder.mkdir();
        BaseLineGenerator blg = new BaseLineGenerator();
        blg.generateBaseLines(fac, rectangleInputFolder, outputFolder);
    }

    public void generateBaseLines(DataFacilitator df, File inputRectanglesFolder, File outputFolder) {

        TreeMapReader tmr = new TreeMapReader();

        List<TreeMap> inputTreeMaps = tmr.readTreeMaps(inputRectanglesFolder);
        //generate a baseline for t=1 untill t=max
        for (int t = 0; t < df.getMaxTime(); t++) {
            DataMap prevDm = df.getData(t);
            DataMap newDm = df.getData(t + 1);
            TreeMap tm = inputTreeMaps.get(t);
            System.out.println("generating baseline for time t=" + t);
            TreeMap baseLineTm = generateBaseLine(tm, prevDm, newDm);

            outputTreeMap(baseLineTm, outputFolder, outputFolder + "\\baseline" + t + ".rect");
        }
        return;
    }

    private void outputTreeMap(TreeMap treeMap, File outputFolder, String fileName) {
        try {
            File f = new File(fileName);
            f.createNewFile();

            FileWriter fw = new FileWriter(f, false);

            for (TreeMap tm : treeMap.getAllLeafs()) {
                fw.append(tm.getLabel()).append(",");
                fw.append("" + tm.getRectangle().getX()).append(",");
                fw.append("" + tm.getRectangle().getY()).append(",");
                fw.append("" + tm.getRectangle().getWidth()).append(",");
                fw.append("" + tm.getRectangle().getHeight()).append("\n");

            }
            fw.close();

        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public TreeMap generateBaseLine(TreeMap root, DataMap prevDm, DataMap newDm) {
        //flatten everything. Allowed since we are not performing any moves and thus the oeg stays intact except for insertion and deletions.
        TreeMap flatTm = flattenTreeMap(root);
        DataMap prevDmFlat = flattenDataMap(prevDm);
        DataMap newDmFlat = flattenDataMap(newDm);

        TreeMap generateBaseLine = generateBaseLineFlat(flatTm, prevDmFlat, newDmFlat);

        return generateBaseLine;
    }

    /**
     * Treemaps and datamaps should be flat.
     * prevDM WILL be changed
     *
     * @param prevTm
     * @param prevDm
     * @param newDm
     * @return
     */
    private TreeMap generateBaseLineFlat(TreeMap prevTm, DataMap prevDm, DataMap newDm) {

        TreeMap newTm = prevTm.deepCopy();

        TreeMap baseLineTm = newTm;
        double insertionWeight = getInsertionWeight(prevDm, newDm);
        if (!eq(insertionWeight, 0)) {

            //only do maximal segment ballon if there were additions
            //inflate the segments
            baseLineTm = inflateMaximalSegments(newTm, insertionWeight);

            //add the inflated segments to the datamap
            for (TreeMap leaf : baseLineTm.getAllLeafs()) {
                if (leaf.getLabel().contains("InflatedSegment")) {
                    DataMap dm = new DataMap(leaf.getLabel(), leaf.getTargetSize(), 0, null, Color.yellow);
                    prevDm.addDatamap(dm, prevDm);
                }
            }
        }

        //reduce the weight of deleted rectangles and update weights in general
        DataMap newWeightsDm = changeAllWeights(prevDm, newDm);

        baseLineTm.setTargetSizes(newWeightsDm);
        //update the areas according to the weights in the treemap
        TreeMapChangeGenerator tmcg = new TreeMapChangeGenerator(baseLineTm);
        return tmcg.fixPositions();
    }

    /**
     * We are going to inflate all maximal segments in the treemap
     *
     * @param prevTm
     * @param totalInsertionWeight Holds the extra weight of all insertions
     * @return
     */
    private TreeMap inflateMaximalSegments(TreeMap newTm, double totalInsertionWeight) {
        //TODO Refactor to get rid of ugly no-deepcopy option of segments. Insert all the balloons in a single go.

        List<OrderEquivalentMaximalSegment> segments = new TreeMapChangeGenerator(newTm).getGraph().getMaximalSegments();
        double weightPerPx = totalInsertionWeight / totalSegmentLength(segments);

        int inflateNumber = 0;

        List<OrderEquivalentMaximalSegment> verticalSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment s : segments) {
            if (s.vertical) {
                verticalSegments.add(s);
            }
        }

        TreeMapChangeGenerator tmcg = new TreeMapChangeGenerator(newTm, false);

        double weightUsed = inflateVerticalSegments(newTm, verticalSegments, weightPerPx, inflateNumber);

        //Get the new segments. Do not make a deepcopy as this can messes with the segments that exists.
        tmcg = new TreeMapChangeGenerator(newTm, false);

        inflateNumber += segments.size(); //Not the correct amount, but does not matter as long as they are unique

        //Do the horizontal segments
        double remainingWeight = totalInsertionWeight - weightUsed;
        //get the horizontal segments and get the weight per px. Need to specify them specifically here as
        //new vertical segments have been added to the treemap
        segments = new TreeMapChangeGenerator(newTm).getGraph().getMaximalSegments();
        List<OrderEquivalentMaximalSegment> horizontalSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment s : segments) {
            if (s.horizontal) {
                horizontalSegments.add(s);
            }
        }

        weightPerPx = (remainingWeight) / totalSegmentLength(horizontalSegments);

        inflateHorizontalSegments(newTm, horizontalSegments, weightPerPx, inflateNumber);
        outputTreeMap(newTm, new File("D:\\Development\\TreemapStability\\errors"), "test");
        return newTm;
    }

    /**
     * Inserts a balloon segment adjacent to each vertical segment in tm.
     *
     * @param tm
     * @param verticalSegments A list of all vertical segments in the treemap
     * @param weightPerPx      How much area this segment should have per pixel
     * @param sizeDecrease     How much of the width of each segment is missing
     *                         due to other segments being inflated.
     * @param inflateNumber    A counter which holds how many segments already
     *                         have been inflated
     * @return The total weight of the inserted balloons
     */
    private double inflateVerticalSegments(TreeMap tm, List<OrderEquivalentMaximalSegment> verticalSegments, double weightPerPx, int inflateNumber) {
        //Reduce the width of all rectangles in the treemap. Insert new rectangles in the space made up.
        //Moreover, increase the x position of the rectangles in the first row

        //make sure that we can fit all our balloons
        double balloonWidth = getMinWidth(tm) / 4;

        //reduce the size of the existing rectangles in the treemap
        for (TreeMap leaf : tm.getAllLeafs()) {
            Rectangle r = leaf.getRectangle();
            double x1 = r.getX() + balloonWidth;
            double x2 = r.getX2();
            if (eq(x2, tm.getRectangle().getX2())) {
                x2 -= balloonWidth;
            }
            double width = x2 - x1;
            Rectangle newR = new Rectangle(x1, r.getY(), width, r.getHeight());
            leaf.updateRectangle(newR);
        }

        double weightUsed = 0;
        //add the balloons
        for (OrderEquivalentMaximalSegment s : verticalSegments) {
            Rectangle balloonR;
            if (s.adjacentBlockList2.isEmpty()) {
                //rightMost outer segment, special case
                balloonR = new Rectangle(s.x1 - balloonWidth, s.y1, balloonWidth, s.y2 - s.y1);
            } else {
                balloonR = new Rectangle(s.x1, s.y1, balloonWidth, s.y2 - s.y1);
            }
            double originalLength = Math.abs((s.y2 - s.y1));
            double weight = weightPerPx * originalLength;
            TreeMap balloonTm = new TreeMap(balloonR, "InflatedSegment" + inflateNumber, Color.yellow, weight, 0, null);
            tm.addTreeMap(balloonTm, tm);
            inflateNumber++;
            weightUsed += weight;
        }
        return weightUsed;
    }

    /**
     * Inserts a balloon segment adjacent to each horizontal segment in tm
     * Must be called after inflateVertical
     *
     * @param tm
     * @param segments      A list of all vertical segments in the treemap
     * @param weightPerPx   How much area this segment should have per
     *                      pixel
     *                      due to other segments being inflated.
     * @param inflateNumber A counter which holds how many segments already
     *                      have been inflated
     * @return The width of each balloon
     */
    private void inflateHorizontalSegments(TreeMap tm, List<OrderEquivalentMaximalSegment> segments, double weightPerPx, int inflateNumber) {
        //Reduce the height of all rectangles in the treemap. Insert new rectangles in the space made up.
        //Moreover, increase the y position of the rectangles in the first row

        //make sure that we can fit all our balloons
        double balloonHeight = getMinHeight(tm) / 4;

        //reduce the size of the existing rectangles in the treemap
        for (TreeMap leaf : tm.getAllLeafs()) {
            Rectangle r = leaf.getRectangle();
            double y1 = r.getY() + balloonHeight;
            double y2 = r.getY2();
            if (eq(y2, tm.getRectangle().getY2())) {
                y2 -= balloonHeight;
            }
            double height = y2 - y1;
            Rectangle newR = new Rectangle(r.getX(), y1, r.getWidth(), height);
            leaf.updateRectangle(newR);
        }
        List<OrderEquivalentMaximalSegment> horizontalSegments = new ArrayList();
        for (OrderEquivalentMaximalSegment s : segments) {
            if (s.horizontal) {
                horizontalSegments.add(s);
            }
        }

        //add the balloons
        for (OrderEquivalentMaximalSegment s : horizontalSegments) {
            double x1, y1, w, h;
            if (s.adjacentBlockList1.isEmpty()) {
                //bottommost outer segment, special case. Should stretch all the way
                x1 = s.x1;
                y1 = s.y1 - balloonHeight;
                w = s.x2 - s.x1;
                h = balloonHeight;
            } else if (s.adjacentBlockList2.isEmpty()) {
                //topmost outer segment, special case. Should stretch all the way
                x1 = s.x1;
                y1 = s.y1;
                w = s.x2 - s.x1;
                h = balloonHeight;
            } else {
                x1 = s.x1;
                y1 = s.y1;
                h = balloonHeight;
                w = s.x2 - s.x1;
            }
            Rectangle balloonR = new Rectangle(x1, y1, w, h);
            double originalLength = Math.abs((s.x2 - s.x1));
            double weight = weightPerPx * originalLength;
            TreeMap balloonTm = new TreeMap(balloonR, "InflatedSegment" + inflateNumber, Color.yellow, weight, 0, null);
            tm.addTreeMap(balloonTm, tm);
            inflateNumber++;
        }
    }

    /**
     * Gets which items were added from currentItems to newItems
     *
     * @param currentItems
     * @param newItems
     * @return
     */
    private List<DataMap> getAddedItems(List<DataMap> currentItems, List<DataMap> newItems) {
        List<DataMap> itemsToBeAdded = new ArrayList(); //fill added and unaddedItems
        for (DataMap newItem : newItems) {
            boolean found = false;
            for (DataMap currentItem : currentItems) {
                if (newItem.getLabel().equals(currentItem.getLabel())) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                itemsToBeAdded.add(newItem);
            }
        }
        return itemsToBeAdded;
    }

    /**
     * Modifies the datamap prevDm to holds the new weights for every rectangle.
     * Weight of deleted rectangles is equal toDELETEWEIGHT
     *
     * @param prevDm
     * @param newDm
     * @return
     */
    private DataMap changeAllWeights(DataMap prevDm, DataMap newDm) {
        List<DataMap> currentItems = new ArrayList(prevDm.getChildren());
        List<DataMap> newItems = new ArrayList(newDm.getChildren());

        //for deletedItems, set the weight to be near 0
        //holds all the items that were deleted from currentItems
        List<DataMap> deletedItems = getAddedItems(newItems, currentItems);

        for (DataMap dm : currentItems) {
            if (deletedItems.contains(dm)) {
                if (dm.getLabel().contains("InflatedSegment")) {
                    //not deleted, but is an inflated segment which we added
                    continue;
                }
                double oldWeight = dm.getTargetSize();
                //change weight of datamap
                prevDm.getDataMapWithLabel(dm.getLabel()).setTargetSize(DELETEWEIGHT);
                //change weight in root
                prevDm.setTargetSize(prevDm.getTargetSize() - oldWeight + DELETEWEIGHT);
            } else {
                //item was not deleted nor an inflated segment, so present in both. Update the weight
                double newWeight = newDm.getDataMapWithLabel(dm.getLabel()).getTargetSize();

                double oldWeight = dm.getTargetSize();

                //change weight of datamap
                prevDm.getDataMapWithLabel(dm.getLabel()).setTargetSize(newWeight);
                //change weight in root
                prevDm.setTargetSize(prevDm.getTargetSize() - oldWeight + newWeight);
            }
        }
        return prevDm;
    }

    /**
     * gets the total weight of all the insertions
     *
     * @return
     */
    private double getInsertionWeight(DataMap prevDm, DataMap newDm) {
        List<DataMap> currentItems = new ArrayList(prevDm.getChildren());
        List<DataMap> newItems = new ArrayList(newDm.getChildren());

        //holds all the items that were deleted from currentItems
        List<DataMap> addedItems = getAddedItems(currentItems, newItems);

        double sum = 0;
        for (DataMap dm : addedItems) {
            sum += dm.getTargetSize();
        }
        return sum;
    }

    private double totalSegmentLength(List<OrderEquivalentMaximalSegment> segments) {
        double totalLength = 0;
        for (OrderEquivalentMaximalSegment s : segments) {
            totalLength += s.getLength();
        }
        return totalLength;
    }

    private TreeMap flattenTreeMap(TreeMap tm) {
        List<TreeMap> leafs = new ArrayList();

        double size = 0;
        double sd = 0;
        for (TreeMap leaf : tm.getAllLeafs()) {
            TreeMap newLeaf = new TreeMap(leaf.getRectangle(), leaf.getLabel(), leaf.getColor(), leaf.getTargetSize(), leaf.getSd(), null);
            leafs.add(newLeaf);
            size += newLeaf.getTargetSize();
            sd += newLeaf.getSd() * newLeaf.getSd();
        }
        Rectangle r = TreeMap.findEnclosingRectangle(leafs);
        TreeMap root = new TreeMap(r, "root", Color.yellow, size, Math.sqrt(sd), leafs);
        return root;
    }

    private DataMap flattenDataMap(DataMap dm) {
        List<DataMap> leafs = new ArrayList();

        for (DataMap leaf : dm.getAllLeafs()) {
            DataMap newLeaf = new DataMap(leaf.getLabel(), leaf.getTargetSize(), leaf.getSd(), null, leaf.getColor());
            leafs.add(newLeaf);
        }

        double size = DataMap.getTotalSize(leafs);
        double sd = DataMap.getStandardDeviationChildren(leafs);
        DataMap root = new DataMap("root", size, sd, leafs, Color.yellow);
        return root;
    }

    /**
     * Returns the minimum height of a rectangle in tm
     *
     * @param tm
     * @return
     */
    private double getMinHeight(TreeMap tm) {
        double min = Double.MAX_VALUE;
        for (TreeMap leaf : tm.getAllLeafs()) {
            min = Math.min(min, leaf.getRectangle().getHeight());
        }
        return min;
    }

    /**
     * Returns the minimum width of a rectangle in tm
     *
     * @param tm
     * @return
     */
    private double getMinWidth(TreeMap tm) {
        double min = Double.MAX_VALUE;
        for (TreeMap leaf : tm.getAllLeafs()) {
            min = Math.min(min, leaf.getRectangle().getWidth());
        }
        return min;
    }

}
