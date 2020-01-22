package com.eduardovernier;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main {

    public static JFrame frame;
    public static String outputDir;
    private static Rectangle canvas;
    private static TreemapPanel panel;

    public static void main(String[] args) throws InterruptedException {


        if (args.length == 4) { // As specified by Max

            if (args[0].equals("-v")) {
                // Interactive mode
                int width = Integer.valueOf(args[2]);
                int height = Integer.valueOf(args[3]);
                canvas = new Rectangle(0, 0, width, height);
                List<List<Entity>> revisionList = Parser.parseCSVs(args[1]);
                TreemapManager treemapManager = new TreemapManager(canvas, revisionList);
                initFrame(treemapManager, true);
            } else {
                // Generate file with rectangles
                String inputDir = args[0];
                int width = Integer.valueOf(args[1]);
                int height = Integer.valueOf(args[2]);
                outputDir = args[3];

                canvas = new Rectangle(0, 0, width, height);
                List<List<Entity>> revisionList = Parser.parseCSVs(inputDir);
                TreemapManager treemapManager = new TreemapManager(canvas, revisionList);
                initFrame(treemapManager, false);

                while (treemapManager.getRevision() < revisionList.size() - 1) {
                    treemapManager.advanceOneRevision();
                    // System.out.println("Insertion Treemap - Revision " + treemapManager.getRevision());
                }

                MetricsCollector.writeRectanglesToDir(outputDir);
                System.exit(0);
                frame.dispose();

            }
        } else {
            argsError();
        }
    }

    private static void initFrame(TreemapManager treemapManager, boolean visible) {
        panel = new TreemapPanel(treemapManager);
        panel.setBackground(new Color(200, 200, 200, 255));
        frame = new JFrame("Insertion Treemap - Revision 0");
        frame.getContentPane().add(panel);
        frame.addKeyListener(panel);
        frame.setSize((int) canvas.width, (int) canvas.height);
        frame.setVisible(visible);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void argsError() {
        System.out.println("Usage: \njava -cp ./bin com.eduardovernier.Main input_dir width height output_dir");
        System.out.println("If you just want the rectangles, or:");
        System.out.println("Usage: java -cp ./bin com.eduardovernier.Main -v input_dir width height");
        System.out.println("If you'd like to see and interact with the treemap.");
        System.out.println("Width and Height are given in pixels (integers).");
    }
}
