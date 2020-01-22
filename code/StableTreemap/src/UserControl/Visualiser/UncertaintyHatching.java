/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UserControl.Visualiser;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import utility.ColorBrewer;

/**
 *
 * @author msondag
 */
public class UncertaintyHatching {

    public void drawCheckerBoardPattern(Graphics g, UncertaintyParams params, TreeMap root, TreeMap tm, int depth, int maxDepth) {

        double tmHeight = (maxDepth - depth);
        double sideLength = params.baseWidth * Math.pow(2, tmHeight);
        Color color = getColor(params, (int) tmHeight, maxDepth, tm);

        Rectangle rootR = root.getRectangle();

        //get how many squares we can fit in the width and height.
        int widthSquares = (int) Math.ceil(rootR.getWidth() / sideLength);
        int heightSquares = (int) Math.ceil(rootR.getHeight() / sideLength);

        //get the area we need to color with the checkerboard.
        Rectangle tmR = tm.getRectangle();
        //do not go over the maximum height
        double sdPercentage = Math.min(tm.getSd() / tm.getTargetSize(), 1);
        double barHeight = tmR.getHeight() * sdPercentage;

        double startY = tmR.getY2() - barHeight;

        Rectangle uncertaintyAreaR = new Rectangle(tmR.getX(), startY, tmR.getWidth(), barHeight);

        /**
         * Only need the even squares to make checkerboard _______________
         * |XX|13|XX|33|XX| |02|XX|22|XX|42| |XX|11|XX|31|XX| |00|XX|20|XX|40|
         * _______________
         */
        //draw a checkerboard pattern
        for (int i = 0; i < widthSquares; i++) {
            for (int j = 0; j < heightSquares; j++) {
                if ((i + j) % 2 == 0)//if it is an even square summing up both, it is on the checkerboard.
                {
                    //area covered by the pattern. Need to make sure it starts at leftbottom

                    Rectangle patternR = new Rectangle(i * sideLength, root.getRectangle().getHeight() - (j + 1) * sideLength, sideLength, sideLength);

//                    Rectangle patternR = new Rectangle(i * sideLength, j * sideLength, sideLength, sideLength);
                    //get the area that is overlapping
                    Rectangle overlap = patternR.intersection(uncertaintyAreaR);
                    if (overlap.getWidth() > 0 && overlap.getHeight() > 0) {
                        drawRectangle(g, overlap, color);
                    }
                }
            }
        }
    }

    public void drawBarsPattern(Graphics g, UncertaintyParams params, TreeMap root, TreeMap tm, int depth, int maxDepth) {

        double tmHeight = (maxDepth - depth);
        double width = params.baseWidth * Math.pow(2, tmHeight);
        Color color = getColor(params, (int) tmHeight, maxDepth, tm);

        Rectangle rootR = root.getRectangle();

        //get how many bar we can fit in the width
        int barAmount = (int) Math.ceil(rootR.getWidth() / width) + 2;
        int skipAmount = (int) params.baseSep;
        //get the area we need to color with the bar pattern
        Rectangle tmR = tm.getRectangle();
        //do not go over the maximum height
        double sdPercentage = Math.min(tm.getSd() / tm.getTargetSize(), 1);
        double barHeight = tmR.getHeight() * sdPercentage;

        double startY = tmR.getY2() - barHeight;
        Rectangle uncertaintyAreaR = new Rectangle(tmR.getX(), startY, tmR.getWidth(), barHeight);

        for (double i = 0; i < barAmount; i++) {
            if ((i) % skipAmount == 0)//only do the even bars. Odd bars are empty space
            {
                //area covered by the pattern. Add 0.5 to make sure all bars are centered
                Rectangle patternR = new Rectangle((i - 0.5) * width, 0, width, rootR.getHeight());
                //get the area that is overlapping
                Rectangle overlap = patternR.intersection(uncertaintyAreaR);
                if (overlap.getWidth() > 0 && overlap.getHeight() > 0) {
                    drawRectangle(g, overlap, color);
                }
            }
        }
    }

    public void drawDiagonalPattern(Graphics g, UncertaintyParams params, TreeMap root, TreeMap tm, int tmHeight, int maxDepth) {
        int diagonalWidth = (int) Math.round(params.baseWidth * Math.pow(2, tmHeight));
        Color color = getColor(params, (int) tmHeight, maxDepth, tm);

        Rectangle rootR = root.getRectangle();

        //get how many bar we can fit in the width
        int diagonalAmount = (int) Math.ceil(rootR.getWidth() / diagonalWidth) + 2;

        int skipAmount = (int) params.baseSep;

        //get the area we need to color with the bar pattern
        Rectangle tmR = tm.getRectangle();
        //do not go over the maximum height
        double sdPercentage = Math.min(tm.getSd() / tm.getTargetSize(), 1);
        double barHeight = tmR.getHeight() * sdPercentage;
        double startY = tmR.getY2() - barHeight;

//        tmR = tmR.shrink(2);
        Rectangle2D.Double uncertaintyAreaR = new Rectangle2D.Double(tmR.getX(), startY, tmR.getWidth(), barHeight);
        Set<Polygon> leftDiagonals = getLeftDiagonals(rootR, maxDepth - tmHeight, diagonalAmount, diagonalWidth, skipAmount);

        for (Polygon p : leftDiagonals) {
            drawDiagonals(g, p, uncertaintyAreaR, color);
        }
        //only do right hatch if sd > 1
        if (sdPercentage < 1) {
            return;
        }
        //calculate how high the right diagonals are
        sdPercentage = Math.min(tm.getSd() / tm.getTargetSize(), 2) - 1;
        barHeight = tmR.getHeight() * sdPercentage;
        startY = tmR.getY2() - barHeight;
        uncertaintyAreaR = new Rectangle2D.Double(tmR.getX(), startY, tmR.getWidth(), barHeight);

        Set<Polygon> rightDiagonals = getRightDiagonals(rootR, tmHeight, diagonalAmount, diagonalWidth, skipAmount);

        for (Polygon p : rightDiagonals) {
            drawDiagonals(g, p, uncertaintyAreaR, color);
        }
    }

    private void drawDiagonals(Graphics g, Polygon p, Rectangle2D uncertaintyAreaR, Color color) {
        Area a = new Area(p);
        a.intersect(new Area(uncertaintyAreaR));

        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(color);
        g2.fill(a);
    }

    public void drawDiagonalPatternOutline(Graphics g, UncertaintyParams params, TreeMap root, TreeMap tm, int tmHeight, int maxDepth) {
        int diagonalWidth = (int) Math.round(params.baseWidth * Math.pow(2, tmHeight));
        Color color = getColor(params, (int) tmHeight, maxDepth, tm);

        Rectangle rootR = root.getRectangle();

        //get how many bar we can fit in the width
        int diagonalAmount = (int) Math.ceil(rootR.getWidth() / diagonalWidth) + 2;

        int skipAmount = (int) params.baseSep;

        //get the area we need to color with the bar pattern
        Rectangle tmR = tm.getRectangle();
        Rectangle2D.Double fullNode = new Rectangle2D.Double(tmR.getX(), tmR.getY(), tmR.getWidth(), tmR.getHeight());

        //do not go over the maximum height
        double sdPercentage = Math.min(tm.getSd() / tm.getTargetSize(), 1);
        Rectangle2D.Double uncertaintyAreaR = null;
        if (sdPercentage < 1) {
            double shrink = Math.sqrt(1 - sdPercentage);
            double subWidth = tmR.getWidth() * shrink;
            double subHeight = tmR.getHeight() * shrink;
            uncertaintyAreaR = new Rectangle2D.Double(tmR.getCenterX() - subWidth / 2.0, tmR.getCenterY() - subHeight / 2.0, subWidth, subHeight);
        }

        Set<Polygon> leftDiagonals = getLeftDiagonals(rootR, maxDepth - tmHeight, diagonalAmount, diagonalWidth, skipAmount);

        for (Polygon p : leftDiagonals) {
            drawDiagonalsOutline(g, p, fullNode, uncertaintyAreaR, color);
        }
        //only do right hatch if sd > 1
        if (sdPercentage < 1) {
            return;
        }
        //calculate how high the right diagonals are
        sdPercentage = Math.min(tm.getSd() / tm.getTargetSize(), 2) - 1;
        uncertaintyAreaR = null;
        if (sdPercentage < 1) {
            double shrink = Math.sqrt(1 - sdPercentage);
            double subWidth = tmR.getWidth() * shrink;
            double subHeight = tmR.getHeight() * shrink;
            uncertaintyAreaR = new Rectangle2D.Double(tmR.getCenterX() - subWidth / 2.0, tmR.getCenterY() - subHeight / 2.0, subWidth, subHeight);
        }

        Set<Polygon> rightDiagonals = getRightDiagonals(rootR, tmHeight, diagonalAmount, diagonalWidth, skipAmount);

        for (Polygon p : rightDiagonals) {
            drawDiagonalsOutline(g, p, fullNode, uncertaintyAreaR, color);
        }
    }

    private void drawDiagonalsOutline(Graphics g, Polygon p, Rectangle2D fullArea, Rectangle2D subtractArea, Color color) {
        Area a = new Area(p);
        a.intersect(new Area(fullArea));
        if (subtractArea != null) {
            a.subtract(new Area(subtractArea));
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(color);
        g2.fill(a);
    }

    private Set<Polygon> getLeftDiagonals(Rectangle rootRectangle, int depth, int diagonalAmount, int diagonalWidth, int skipAmount) {
        Set<Polygon> diagonals = new HashSet();
        for (double i = -diagonalAmount; i < diagonalAmount; i++) {
            if (i % skipAmount == 0)//only do the even diagonals. Odd bars are empty diagonals
            {
                //offset to make sure they are all centered
                //(2^{depth+1}-1)/2*width = offset
                int offset = (int) Math.round(((0.5 * (Math.pow(2, depth + 1) - 1)) * diagonalWidth));
                //define the polygon
                int startpX = (int) (Math.round(i) * diagonalWidth) + offset;
                int startpY = 0;
                int endpX = (int) Math.round(rootRectangle.getHeight() * 2 + startpX);//45 degree angle
                int endpY = (int) Math.round(rootRectangle.getHeight() * 2);//45 degree angle

                int[] xPoints = new int[]{startpX, endpX, endpX + diagonalWidth, startpX + diagonalWidth};
                int[] yPoints = new int[]{startpY, endpY, endpY, startpY};
                Polygon p = new Polygon(xPoints, yPoints, 4);
                diagonals.add(p);
            }
        }
        return diagonals;
    }

    private Set<Polygon> getRightDiagonals(Rectangle rootRectangle, int depth, int diagonalAmount, int diagonalWidth, int skipAmount) {
        Set<Polygon> diagonals = new HashSet();
        for (double i = 0; i < 2 * diagonalAmount; i++) {
            if (i % skipAmount == 0)//only do the even diagonals. Odd bars are empty diagonals
            {
                //offset to make sure they are all centered
                //(2^{depth+1}-1)/2*width = offset
                int offset = (int) Math.round(((0.5 * (Math.pow(2, depth + 1) - 1)) * diagonalWidth));
                //define the polygon
                int startpX = (int) (Math.round(i) * diagonalWidth) + offset;
                int startpY = 0;
                int endpX = (int) Math.round(-rootRectangle.getHeight() * 2 + startpX);//135 degree angle
                int endpY = (int) Math.round(rootRectangle.getHeight() * 2);//135 degree angle

                int[] xPoints = new int[]{startpX, endpX, endpX + diagonalWidth, startpX + diagonalWidth};
                int[] yPoints = new int[]{startpY, endpY, endpY, startpY};
                Polygon p = new Polygon(xPoints, yPoints, 4);
                diagonals.add(p);
            }
        }
        return diagonals;
    }

    private Color blendColor(Color grayColor, TreeMap tm, double blendFactor) {
        double gray = grayColor.getRed();
        Color tmColor = tm.getColor();
        int red = (int) Math.min(255, tmColor.getRed() * (1 - blendFactor) + gray * blendFactor);
        int green = (int) Math.min(255, tmColor.getGreen() * (1 - blendFactor) + gray * blendFactor);
        int blue = (int) Math.min(255, tmColor.getBlue() * (1 - blendFactor) + gray * blendFactor);
        Color newColor = new Color(red, green, blue);
        return newColor;
    }

    private Color getColor(UncertaintyParams params, int height, int maxHeight, TreeMap tm) {
        Color[] colorPalette = ColorBrewer.Greys.getColorPalette(maxHeight + 1);
        //higher alpha values
        colorPalette[0] = blendColor(colorPalette[0], tm, 0.5);
//        colorPalette[1] = blendColor(colorPalette[1], tm, 0.8);

        return colorPalette[height];

//        float lum = (float) (params.baseLum + height * params.addLum) / 100;
//        Color color = Color.getHSBColor(0, 0, lum);
//        return color;
    }

    private void drawRectangle(Graphics g, Rectangle r, Color color) {
        Graphics2D g2 = (Graphics2D) g;

        int x = (int) Math.round(r.getX());
        int y = (int) Math.round(r.getY());
        int x2 = (int) Math.round(r.getX2());
        int y2 = (int) Math.round(r.getY2());

        g2.setPaint(color);
        Polygon p = new Polygon();

        p.addPoint(x, y);
        p.addPoint(x, y2);
        p.addPoint(x2, y2);
        p.addPoint(x2, y);

        g2.fillPolygon(p);
    }

}
