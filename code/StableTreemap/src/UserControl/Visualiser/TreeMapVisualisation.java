package UserControl.Visualiser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import statistics.StatisticalTracker;
import statistics.UncertaintyStatistics;
import treemap.dataStructure.OrderEquivalentLineSegment;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import utility.ColorBrewer;

/**
 *
 * @author Max Sondag
 */
public class TreeMapVisualisation extends JPanel {

    Timer timer = new Timer();

    private Rectangle treeMapRectangle;
    private TreeMap newTreeMap;
    private TreeMap oldTreeMap;

    private boolean animationEnabled = false;
    private final int animationMinSteps = 5;
    private final int animationMaxSteps = 40;
    private int animationSteps = 20;
    private final int timeBetweenSteps = 20;//time in milliseconds between steps
    private int animationProgress = 0;

    public boolean drawWeightsEnabled = false;
    public boolean drawLabelsEnabled = true;

    public boolean drawUncertainty = true;

    //minimum font size before we show the label
    private int minFontSize = 10;

    public UncertaintyParams params = new UncertaintyParams(4.0, 3.0, 2.0, 0, 25, "Diagonal");

    //Whether the new treeMap was the result of a move
    private boolean movePerformed = false;
    /**
     * whether we have finished painting the treemap. Happens after
     * interpolation
     */
    public volatile boolean treeMapRepaint = false;

    public TreeMapVisualisation() {
        newTreeMap = null;
        oldTreeMap = null;
    }

    public void setAnimationSpeed(double percentage) {
        this.animationSteps = (int) (animationMinSteps + (animationMaxSteps - animationMinSteps) * ((100 - percentage) / 100));
    }

    public Rectangle getTreemapRectangle() {
        updateTreeMapRectangle();
        return treeMapRectangle;
    }

    public boolean isShowingTreeMap() {
        if (newTreeMap == null) {
            return false;
        }
        return true;
    }

    public void updateTreeMapRectangle() {
        treeMapRectangle = new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public void updateTreeMap(TreeMap treeMap) {
        movePerformed = false;
        oldTreeMap = newTreeMap;
        newTreeMap = treeMap;
        treeMapRepaint = false;

        repaint();
    }

    public void movePerformed(TreeMap treeMap) {
        updateTreeMap(treeMap);
        //we can set it afterwards as repaint is asynchronous
        movePerformed = true;

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (oldTreeMap != null && oldTreeMap != newTreeMap && animationEnabled == true) {

            drawInterpolationTreeMap(g, oldTreeMap, newTreeMap, 0);

            animationProgress++;
            if (animationProgress <= animationSteps) {
                delayedRepaint(timeBetweenSteps);
            } else {
                animationProgress = 0;
                //used to draw the outlines

                drawTreeMap(g, newTreeMap, 0);
                drawUncertaintyAndOutline(g, newTreeMap);
                drawLabels(g, newTreeMap);
                //indicate that we have finished drawing
                treeMapRepaint = true;
            }

        } else if (newTreeMap != null) {

            drawTreeMap(g, newTreeMap, 0);
            drawUncertaintyAndOutline(g, newTreeMap);
            drawLabels(g, newTreeMap);
            //indicate that we have finished drawing
            treeMapRepaint = true;
        }

    }

    long lastTime = System.currentTimeMillis();

    private void requestRepaint() {
        long time = System.currentTimeMillis();
        lastTime = time;
        repaint();
    }

    private void delayedRepaint(int time) {
        //time is in second    
        timer.cancel();
        timer = new Timer();
        TimerTask action = new TimerTask() {
            public void run() {
                requestRepaint();
            }

        };

        timer.schedule(action, time); //this starts the task
    }

    protected Rectangle getInterpolatedRectangle(TreeMap oldTm, TreeMap newTm, double progressPercentage) {
        Rectangle oldR = null;
        if (oldTm != null) {
            oldR = oldTm.getRectangle();
        }
        Rectangle newR = null;
        if (newTm != null) {
            newR = newTm.getRectangle();
        }

        double oldX = 0, oldY = 0, oldW = 0, oldH = 0, newX = 0, newY = 0, newW = 0, newH = 0;

        if (oldR == null && newR == null) {
            System.out.println("Both rectangles are null, cannot interpolate");
            return null;
        }

        if (oldR != null) {
            oldX = oldR.getX();
            oldY = oldR.getY();
            oldW = oldR.getWidth();
            oldH = oldR.getHeight();
        }
        if (newR != null) {
            newX = newR.getX();
            newY = newR.getY();
            newW = newR.getWidth();
            newH = newR.getHeight();
        }
        if (oldR == null) {
            //oldR does not exists. put in in the middle of newR as a 1x1 rectangle
            oldX = newX + newW / 2;
            oldY = newY + newH / 2;
            oldW = 1;
            oldH = 1;
        }
        if (newR == null) {
            //newR does not exists. put in in the middle of oldR as a 1x1 rectangle
            newX = oldX + oldW / 2;
            newY = oldY + oldH / 2;
            newW = 1;
            newH = 1;
        }

        double xDifference = newX - oldX;
        double yDifference = newY - oldY;
        double widthDifference = newW - oldW;
        double heightDifference = newH - oldH;

        double interpolatedX = oldX + xDifference * progressPercentage;
        double interpolatedY = oldY + yDifference * progressPercentage;
        double interpolatedWidth = oldW + widthDifference * progressPercentage;
        double interpolatedHeight = oldH + heightDifference * progressPercentage;

        Rectangle interpolated = new Rectangle(interpolatedX, interpolatedY, interpolatedWidth, interpolatedHeight);
        return interpolated;
    }

    private void drawInterpolationTreeMap(Graphics g, TreeMap oldTm, TreeMap newTm, int depth) {
        double progressPercentage = (double) animationProgress / (double) animationSteps;

        Rectangle interpolated = getInterpolatedRectangle(oldTm, newTm, progressPercentage);
        //topleft corner
        double x = interpolated.getX();
        double y = interpolated.getY();
        if (!newTm.hasChildren()) {
            //if new has no children neither does old
            drawGradientRectangle(g, interpolated, newTm.getColor());
            drawWeights(g, newTm.getTargetSize(), x, y);
        }

        for (TreeMap childNew : newTm.getChildren()) {
            //recurse in the same child
            TreeMap childOld;
            if (oldTm == null) {
                childOld = null;
            } else {
                childOld = findTreeMapWithSameLabel(oldTm.getChildren(), childNew.getLabel());
            }
            drawInterpolationTreeMap(g, childOld, childNew, depth + 1);
        }

        //draw the outline again to make sure it is drawn over it
        drawRectOutline(g, interpolated, newTm.getHeight(), newTm);

        if (!newTm.hasChildren()) {
            drawLabel(g, interpolated, newTm.getLabel());
        }

    }

    public TreeMap findTreeMapWithSameLabel(List<TreeMap> treeMaps, String label) {
        for (TreeMap tm : treeMaps) {
            if (tm.getLabel().equals(label)) {
                return tm;
            }
        }
        return null;
    }

    private void drawTreeMap(Graphics g, TreeMap tm, int depth) {

        if (!tm.hasChildren()) {
            drawGradientRectangle(g, tm.getRectangle(), tm.getColor());
            drawWeights(g, tm.getTargetSize(), tm.getRectangle().getX(), tm.getRectangle().getY());
        }

        for (TreeMap child : tm.getChildren()) {
            drawTreeMap(g, child, depth + 1);
        }
    }

    /**
     * Draw the labels and the outline
     *
     * @param g
     * @param tm
     */
    private void drawLabels(Graphics g, TreeMap tm) {

        for (TreeMap child : tm.getChildren()) {
            drawLabels(g, child);

            UncertaintyStatistics un = new UncertaintyStatistics(false);
            double penalty = un.getPenalty(child.getRectangle(), child.getSdRectangle(), tm.getSdRectangle());
            if (penalty > 1) {
                penalty = Math.floor(penalty);
//                drawLabel(g, child.getRectangle(), "" + penalty, 15);
            }
        }
        if (tm.getParent() == null) {
            System.out.println("root");
        }
        int shrinkSize = getShrinkSize(newTreeMap.getHeight() - tm.getHeight());
        //get outline
        Rectangle innerR = getRectWithinOutline(tm.getRectangle(), shrinkSize);
        //draw label within outline
        if (!tm.hasChildren()) {
            drawLabel(g, innerR, tm.getLabel());
        }

    }

    private void drawUncertaintyAndOutline(Graphics g, TreeMap root) {
//
        HashMap<Integer, Set<TreeMap>> depthMap = root.getDepthMap(0);
        //to properly render this we need to first render all nodes and outlines of the highest depth, then the second highest and so on.
        int maxDepth = depthMap.size() - 1;
        UncertaintyHatching uh = new UncertaintyHatching();
        for (int i = (maxDepth); i >= 0; i--) {
            Set<TreeMap> treemapSet = depthMap.get(i);
            //draw all uncertainties on this level
            if (drawUncertainty) {
                for (TreeMap treeMap : treemapSet) {
                    if ("CheckerBoard".equals(params.uncertaintyType)) {
                        uh.drawCheckerBoardPattern(g, params, root, treeMap, i, maxDepth);
                    } else if ("Bars".equals(params.uncertaintyType)) {
                        uh.drawBarsPattern(g, params, root, treeMap, i, maxDepth);
                    } else if ("Diagonal".equals(params.uncertaintyType)) {
                        int height = treeMap.getHeight();
                        uh.drawDiagonalPattern(g, params, root, treeMap, height, maxDepth);
                    } else if ("DiagonalOutline".equals(params.uncertaintyType)) {
                        int height = treeMap.getHeight();
                        uh.drawDiagonalPatternOutline(g, params, root, treeMap, height, maxDepth);
                    }
                }
            }
            //draw all outlines on this level
            for (TreeMap treeMap : treemapSet) {
                int height = treeMap.getHeight();
                drawRectOutline(g, treeMap.getRectangle(), height, treeMap);
            }
        }
    }

    private int getShrinkSize(int height) {
        int rootHeight = newTreeMap.getHeight();
        return rootHeight - height + 1;
    }

    public void drawLabel(Graphics g, Rectangle r, String label) {

        int maxFontSize = getFontSize(label, r, g);
        g.setFont(new Font("Times", Font.PLAIN, 20));

        if (maxFontSize < minFontSize || maxFontSize < 40) {
            //needs to be a font size that is visible enough
            return;
        }
//        int fontSize = 40;
        drawLabel(g, r, label, maxFontSize);
    }

    public void drawLabel(Graphics g, Rectangle r, String label, float fontSize) {

        if (!drawLabelsEnabled) {
            return;
        }

        Font strokeFont = g.getFont().deriveFont(0, fontSize);
        g.setFont(strokeFont);

        Graphics2D g2 = (Graphics2D) g;
        // activate anti aliasing for text rendering (if you want it to look nice)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        int width = g2.getFontMetrics(strokeFont).stringWidth(label);
        int height = g2.getFontMetrics(strokeFont).getHeight();

//        if (!labelFits(g2, r, label)) {
//            return;
//        }
        int x = (int) ((r.getWidth() - width) / 2 + r.getX());
        int y = (int) ((r.getHeight() + height / 2) / 2 + r.getY());

//        //draw the label itself
        g2.setFont(strokeFont);

        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        TextLayout tl = new TextLayout(label, strokeFont, g2.getFontRenderContext());
        Shape outline = tl.getOutline(AffineTransform.getTranslateInstance(x, y));

        //draw outline
        g2.setColor(Color.black);
        g2.draw(outline);
        //draw inside
        g2.setColor(Color.white);
        g2.fill(outline);
//        tl.draw(g2, x, y);
    }

    public int getFontSize(String text, Rectangle r, Graphics g) {
        float fontSizeWidth = 40.0f;
        float fontSizeHeight = 40.0f;
        Font font = g.getFont().deriveFont(fontSizeWidth);
        int width = g.getFontMetrics(font).stringWidth(text);
        fontSizeWidth = (float) ((r.getWidth() / width) * fontSizeWidth);

        font = g.getFont().deriveFont(fontSizeHeight);
        int height = g.getFontMetrics(font).getHeight();
        fontSizeHeight = (float) ((r.getHeight() / height) * fontSizeHeight);

        double maxFontSize = Math.min(fontSizeWidth, fontSizeHeight);

        return (int) Math.min(maxFontSize - 4, 40);
    }

    private void drawGradientRectangle(Graphics g, Rectangle r, Color color) {

        int x = (int) Math.round(r.getX());
        int y = (int) Math.round(r.getY());
        int x2 = (int) Math.round(r.getX2());
        int y2 = (int) Math.round(r.getY2());
        Graphics2D g2 = (Graphics2D) g;

        if (color == null) {
            System.out.println("No color");
        }
        //draw the rectangle with a gradient
        Color color1 = color;
        Color color2 = color;//new Color(Math.max(0, color.getRed() - 40), Math.max(0, color.getGreen() - 40), Math.max(0, color.getBlue() - 40));

        GradientPaint rect = new GradientPaint(x, y, color1, x2, y2, color2, false);
        g2.setPaint(rect);
        Polygon p = new Polygon();
        p.addPoint(x, y);
        p.addPoint(x, y2);
        p.addPoint(x2, y2);
        p.addPoint(x2, y);

        g2.fillPolygon(p);
    }

    private Rectangle getRectWithinOutline(Rectangle r, int shrinkSize) {
//        int shrinkSize = getShrinkSize(depth);
        //draw outer outline
        int x1 = (int) Math.round(r.getX());
        int y1 = (int) Math.round(r.getY());
        int x2 = (int) Math.round(r.getX2());
        int y2 = (int) Math.round(r.getY2());
        int w = x2 - x1;
        int h = y2 - y1;

        int innerX = x1 + shrinkSize;
        int innerY = y1 + shrinkSize;
        int innerW = w - shrinkSize * 2;
        int innerH = h - shrinkSize * 2;

        Rectangle innerRectangle = new Rectangle(innerX, innerY, innerW, innerH);
        return innerRectangle;
    }

    //blend color with alpha manually
    private Color blendColor(Color grayColor, TreeMap tm, double alpha) {
        double gray = grayColor.getRed();
        Color tmColor = tm.getColor();
        int red = (int) Math.min(255, tmColor.getRed() * (1 - alpha) + gray * alpha);
        int green = (int) Math.min(255, tmColor.getGreen() * (1 - alpha) + gray * alpha);
        int blue = (int) Math.min(255, tmColor.getBlue() * (1 - alpha) + gray * alpha);
        Color newColor = new Color(red, green, blue);
        return newColor;
    }

    private boolean isPaleYellow(Color c) {
        if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 179) {
            return true;
        }
        return false;
    }

    private Color getOutlineColor(int height, int maxHeight, TreeMap tm) {
        Color[] colorPalette = ColorBrewer.Greys.getColorPalette(maxHeight + 1);
        if (isPaleYellow(tm.getColor())) {
            colorPalette[0] = new Color(240,240,240);

        } else {
            colorPalette[0] = blendColor(colorPalette[0], tm, 0.5);
        }
//        colorPalette[1] = blendColor(colorPalette[1], tm, 0.8);
        return colorPalette[height];
    }

    /**
     * Draws the outline of the rectangle. Moreover uses height differences to
     * illustrate the hierarchy. ParentR contains the inner surface area of the
     * parent
     *
     * @param g
     * @param parentR
     * @param r
     * @param isLeaf
     */
    private void drawRectOutline(Graphics g, Rectangle r, int tmHeight, TreeMap tm) {
        int rootHeight = newTreeMap.getHeight();

        int shrinkSize = getShrinkSize(rootHeight - tmHeight);
        //draw outer outline
        int x1 = (int) Math.round(r.getX());
        int y1 = (int) Math.round(r.getY());
        int x2 = (int) Math.round(r.getX2());
        int y2 = (int) Math.round(r.getY2());
        int w = x2 - x1;
        int h = y2 - y1;

        Rectangle innerR = getRectWithinOutline(r, shrinkSize);

        if (innerR.getHeight() > shrinkSize && innerR.getWidth() > shrinkSize) {
            //there is an interior

            //start drawing the outline
            Graphics2D g2 = (Graphics2D) g;
//            int tmHeight = rootHeight - depth;

            Color color = getOutlineColor(tmHeight, rootHeight, tm);
            g2.setColor(color);
            g2.fillRect(x1, y1, w, shrinkSize);
            g2.fillRect(x1, y1, shrinkSize, h);
            g2.fillRect(x2 - shrinkSize, y1, shrinkSize, h);
            g2.fillRect(x1, y2 - shrinkSize, w, shrinkSize);
//            g2.drawRect(Math.round((int) parentR.getX()), (int) parentR.getY(), (int) parentR.getWidth(), (int) parentR.getHeight());
        }
        //too small to draw an outline
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public void setDrawWeight(boolean drawWeightsEnabled) {
        this.drawWeightsEnabled = drawWeightsEnabled;
        System.out.println("drawWeightsEnabled = " + drawWeightsEnabled);
    }

    public void toIpe(String fileName) {
        if (newTreeMap == null) {
            return;
        }
        File outputFile = new File(fileName);
        System.out.println("outputFile.getAbsolutePath() = " + outputFile.getAbsolutePath());
        FileWriter fw;
        try {
            fw = new FileWriter(outputFile, false);
            fw.write(IpeExporter.getPreamble());

            for (TreeMap tm : newTreeMap.getAllLeafs()) {
                fw.write(IpeExporter.getRectangle(tm.getRectangle(), tm.getColor(), tm.getLabel()));
                fw.flush();
            }
            fw.write(IpeExporter.endIpe());
            fw.flush();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void toIpe() {
        toIpe("IpeExport.ipe");
    }

    public void drawWeights(Graphics g, double targetSize, double newX, double newY) {
        if (drawWeightsEnabled) {
            g.setColor(Color.red);
            g.drawString("" + Math.round(targetSize), (int) newX + 5, (int) newY + 25);
        }
    }

    public void updateUncertaintyVisParam(UncertaintyParams params) {
        this.params = params;
    }

    public void showLabels(boolean showLabels) {
        this.drawLabelsEnabled = showLabels;
    }

    void setDrawUncertainty(boolean selected) {
        this.drawUncertainty = selected;
    }

}
