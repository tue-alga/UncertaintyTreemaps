package statistics.Baseline;

import com.opencsv.CSVReader;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 * Reads a treemap from an input file for the baseline generation. Input should
 * have the following format: label,x1,y1,x2,y2 Label is a string, coordinates
 * are doubles
 *
 * Current does not take the hierarchy into account
 *
 * @author msondag
 */
public class TreeMapReader {

    public static void main(String[] args) {
        TreeMapReader r = new TreeMapReader();
        TreeMap readTreeMap = r.readTreeMap(new File("D:\\Development\\TreemapStability\\incremental4-4Output\\bdb.data\\t0.rect"));
    }

    public List<TreeMap> readTreeMaps(File inputRectanglesFolder) {
        List<TreeMap> treeMaps = new ArrayList();

        File[] listFiles = inputRectanglesFolder.listFiles();
        //sort the files list based on timestep
        Arrays.sort(listFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    Integer fileId1 = Integer.parseInt(o1.getName().substring(1,o1.getName().indexOf(".")));
                    Integer fileId2 = Integer.parseInt(o2.getName().substring(1,o2.getName().indexOf(".")));
                    return fileId1.compareTo(fileId2);
                }
            });
        TreeMapReader reader = new TreeMapReader();

        for (File f : listFiles) {
            TreeMap tm = reader.readTreeMap(f);
            treeMaps.add(tm);
        }
        return treeMaps;
    }

    public TreeMap readTreeMap(File inputFile) {
        List<TreeMapData> dataList = readCSVFile(inputFile);
        TreeMap createdTreeMap = createTreeMap(dataList);
        return createdTreeMap;
    }

    private Rectangle getRectangle(List<TreeMapData> dataList) {
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;

        for (TreeMapData data : dataList) {
            minX = Math.min(minX, data.x1);
            minY = Math.min(minY, data.y1);
            maxX = Math.max(maxX, data.x1 + data.w);
            maxY = Math.max(maxY, data.y1 + data.h);
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    private TreeMap createTreeMap(List<TreeMapData> dataList) {
        Rectangle inputR = getRectangle(dataList);

        List<TreeMap> children = new ArrayList();
        for (TreeMapData data : dataList) {
            Rectangle r = new Rectangle(data.x1, data.y1, data.w, data.h);

            TreeMap tm = new TreeMap(r, data.label, Color.yellow, r.getArea(),0, null);
            children.add(tm);
        }
        TreeMap tm = new TreeMap(inputR, "root", Color.yellow, inputR.getArea(),0, children);
        return tm;
    }

    private class TreeMapData {

        String label;
        double x1, y1, w, h;

        public TreeMapData(String label, double x1, double y1, double w, double h) {
            this.label = label;
            this.x1 = x1;
            this.w = w;
            this.y1 = y1;
            this.h = h;
        }
    }

    private List<TreeMapData> readCSVFile(File f) {
        List<TreeMapData> dataList = new ArrayList();
        try {
            CSVReader reader = new CSVReader(new FileReader(f), ',');
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                String label = nextLine[0];
                double x1 = Double.parseDouble(nextLine[1]);
                double y1 = Double.parseDouble(nextLine[2]);
                double w = Double.parseDouble(nextLine[3]);
                double h = Double.parseDouble(nextLine[4]);

                TreeMapData tmd = new TreeMapData(label, x1, y1, w, h);
                dataList.add(tmd);
            }
        } catch (IOException ex) {
            Logger.getLogger(TreeMapReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataList;
    }

}
