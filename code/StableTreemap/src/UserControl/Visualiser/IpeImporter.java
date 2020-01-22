/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserControl.Visualiser;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 * Visualizes an ipe file using the treemap interface
 *
 * @author msondag
 */
public class IpeImporter {

    public static void main(String[] args) {
        String fileName = "D:\\Development\\SVN\\Max Sondag\\Papers\\StableTreemap\\Figures\\teaser\\test.ipe";
        IpeImporter ipe = new IpeImporter(fileName);
    }

    public IpeImporter(String fileName) {

        Visualiser v = new Visualiser();
        TreeMap tm = ipeToTreeMap(v, new File(fileName));
        v.setTreeMap(tm);
    }

    private TreeMap ipeToTreeMap(Visualiser v, File ipeFile) {
        //TODO, check if rectangles should be scaled
        List<TreeMap> children = new ArrayList();

        List<String> content = getContent(ipeFile);
        List<List<String>> ipeObjects = getIpeObjects(content);
        double size = 0;
        for (List<String> ipeObject : ipeObjects) {
            if (ipeObject.size() != 7) {
                continue;
            }

            Color c = getColor(ipeObject);
            Rectangle r = getRectangle(ipeObject);
            TreeMap leaf = new TreeMap(r, "", c, r.getArea(),0, null);
            children.add(leaf);
            size += leaf.getTargetSize();
        }

        Rectangle rootRectangle = TreeMap.findEnclosingRectangle(children);

        TreeMap root = new TreeMap(rootRectangle, "root", Color.yellow, size,0, children);
        root.updateRectangle(v.getTreeMapRectangle());
        //need extra parents for the root to make sure it has the proper depth
//        List<TreeMap> child = new ArrayList();
//        child.add(root);
//        TreeMap parentRoot = new TreeMap(v.getTreeMapRectangle(), "rootParent", Color.yellow, size, child);
//
//        List<TreeMap> child2 = new ArrayList();
//        child2.add(parentRoot);
//        TreeMap parentRoot2 = new TreeMap(v.getTreeMapRectangle(), "rootParent", Color.yellow, size, child2);

        return root;
    }

    private List<String> getContent(File f) {
        List<String> content = new ArrayList();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {

            String line = br.readLine();
            while (line != null) {
                content.add(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IpeImporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IpeImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }

    private List<List<String>> getIpeObjects(List<String> content) {
        List<List<String>> objects = new ArrayList();
        List<String> object = new ArrayList();
        boolean parsingObject = false;
        for (String line : content) {

            if (line.startsWith("<path layer") || (line.startsWith("<path stroke") && line.endsWith("join=\"1\">"))) {
                parsingObject = true;
            }
            if (parsingObject) {
                object.add(line);
            }
            if (line.startsWith("</path>")) {
                objects.add(object);
                parsingObject = false;
                object = new ArrayList();
            }
        }
        return objects;
    }

    private Color getColor(List<String> ipeObject) {
        String header = ipeObject.get(0);
        int indexOfFill = header.indexOf("fill=\"") + 6;
        header = header.substring(indexOfFill);
        int indexOfEndColor = header.indexOf("\"");
        String color = header.substring(0, indexOfEndColor);

        int space = header.indexOf(" ");
        double red = Double.parseDouble(color.substring(0, space));
        color = color.substring(space+1);

        space = color.indexOf(" ");
        double green = Double.parseDouble(color.substring(0, space));
        color = color.substring(space+1);

        double blue = Double.parseDouble(color);

        Color c = new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255));
        return c;
    }

    private Rectangle getRectangle(List<String> ipeObject) {
        String line = ipeObject.get(1);

        int space = line.indexOf(" ");
        double x1 = Double.parseDouble(line.substring(0, space));
        line = line.substring(space + 1);

        space = line.indexOf(" ");
        double y1 = Double.parseDouble(line.substring(0, space));

        //get x2,y2
        line = ipeObject.get(3);

        space = line.indexOf(" ");
        double x2 = Double.parseDouble(line.substring(0, space));
        line = line.substring(space + 1);

        space = line.indexOf(" ");
        double y2 = Double.parseDouble(line.substring(0, space));

        double minX = Math.min(x1, x2);
        double minY = Math.min(y1, y2);
        double maxX = Math.max(x1, x2);
        double maxY = Math.max(y1, y2);

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }
}
