package com.eduardovernier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MetricsCollector {

    static List<HashMap<String, Rectangle>> rectangles = new ArrayList<HashMap<String, Rectangle>>();

    public static void init(int nRevisions) {
        rectangles.clear();
        for (int i = 0; i < nRevisions; ++i) {
            rectangles.add(new HashMap<String, Rectangle>());
        }
    }

    public static void saveRectangles(Package pack, int revision, String path) {

        if (pack.treemap != null) {
            saveRectangles(pack.treemap.root, revision, path);
        }

        for (Package childPackage : pack.packageList) {
            saveRectangles(childPackage, revision, path + "/" + childPackage.id);
        }
    }

    private static void saveRectangles(Container container, int revision, String path) {

        // Only leaves with weight > 0
        if (container.rectangle != null && container.kind == Kind.LEAF &&
                container.rectangle.width > 0.0 && container.rectangle.height > 0.0 &&
                !container.id.endsWith("_") && !container.id.equals("")) {
            rectangles.get(revision).put(path + "/" + container.id, new Rectangle(container.rectangle.x, container.rectangle.y, container.rectangle.width, container.rectangle.height));
        }

        if (container.central != null) {
            saveRectangles(container.central, revision, path);
        }

        if (container.right != null) {
            saveRectangles(container.right, revision, path);
        }

        if (container.bottom != null) {
            saveRectangles(container.bottom, revision, path);
        }
    }

    public static void getAspectRatioAverage(int revision) {
        double sum = 0;
        for (Map.Entry<String, Rectangle> entry : rectangles.get(revision).entrySet()) {
            sum += entry.getValue().getAspectRatio();
        }
        System.out.println(sum/rectangles.get(revision).size());
    }

    public static void getMovementAverage(int revision) {
        if (revision == 0) {
            System.out.println("0");
        } else {
            double sum = 0;
            double n = 0;
            for (Map.Entry<String, Rectangle> entry : rectangles.get(revision).entrySet()) {
                Rectangle currentRectangle = entry.getValue();
                Rectangle pastRectangle = rectangles.get(revision-1).get(entry.getKey());
                if (pastRectangle != null) {
                    double currentX = currentRectangle.x + currentRectangle.width / 2;
                    double currentY = currentRectangle.y + currentRectangle.height / 2;
                    double pastX = pastRectangle.x + pastRectangle.width / 2;
                    double pastY = pastRectangle.y + pastRectangle.height / 2;
                    double dist = Math.sqrt(Math.pow(currentX - pastX, 2) + Math.pow(currentY - pastY, 2));
                    sum += dist;
                    n++;
                }
            }
            System.out.println(sum/n);
        }

    }

    public static void writeRectanglesToDir(String outputDir) {
        new File(outputDir).mkdirs(); // In case path doesn't exist
        int i = 0;
        for (HashMap<String, Rectangle> revision : rectangles) {
            Path file = Paths.get(String.format("%s/t%d.rect", Main.outputDir, i));
            List<String> lines = new ArrayList<>();
            for (Map.Entry<String, Rectangle> entry : revision.entrySet()) {
                Rectangle rectangle = entry.getValue();
                lines.add(String.format(Locale.ROOT, "%s,%.10f,%.10f,%.10f,%.10f", entry.getKey().substring(1), rectangle.x, rectangle.y, rectangle.width, rectangle.height));
            }
            lines.sort(String.CASE_INSENSITIVE_ORDER);
            try {
                Files.write(file, lines, Charset.forName("UTF-8"));
                i++;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        System.out.println(outputDir + " done.");
    }
}
