package treemap.dataStructure;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Max Sondag
 */
public class Rectangle {

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Rectangle rectangle) {
        this.x = rectangle.x;
        this.y = rectangle.y;
        this.width = rectangle.width;
        this.height = rectangle.height;
    }

    public Rectangle(Rectangle r1, Rectangle r2) {

        double x1 = Math.min(r1.x, r2.x);
        double y1 = Math.min(r1.y, r2.y);
        double x2 = Math.max(r1.getX2(), r2.getX2());
        double y2 = Math.max(r1.getY2(), r2.getY2());

        this.x = x1;
        this.y = y1;
        this.width = x2 - x1;
        this.height = y2 - y1;
    }

    public double getArea() {
        return width * height;
    }

    public double getAspectRatio() {
        return Math.max(width / height, height / width);
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    @Override
    public String toString() {
        return "x: " + x + ";y: " + y + ";width:" + width + ";height" + height;
    }

    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    public double getX2() {
        return x + width;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    public double getY2() {
        return y + height;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }
    //</editor-fold>

    public Rectangle deepCopy() {
        Rectangle rectangleDeepCopy = new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        return rectangleDeepCopy;
    }

    /**
     * Returns a new rectangle which is smaller by shrinkAmount pixels on all
     * sides
     *
     * @param shrinkAmount
     * @return 
     */
    public Rectangle shrink(int shrinkAmount) {
        double newX = x + shrinkAmount;
        double newY = y + shrinkAmount;
        double newW = width - shrinkAmount * 2.0;
        double newH = height - shrinkAmount * 2.0;

        newX = Math.max(0, newX);
        newY = Math.max(0, newY);
        newW = Math.max(0, newW);
        newH = Math.max(0, newH);

        return new Rectangle(newX, newY, newW, newH);
    }

    /**
     * Substracts the smallest from the largest rectangle and returns the result
     * @param rectangle
     * @return 
     */
    public Rectangle intersection(Rectangle rectangle) {
        double newX =  Math.max(x, rectangle.getX());
        double newY = Math.max(y, rectangle.getY());

        double newX2 =  Math.min(getX2(), rectangle.getX2());
        double newY2 =  Math.min(getY2(), rectangle.getY2());
        double newW = newX2 - newX;
        double newH = newY2 - newY;

        if (newX2 < newX || newY2 < newY) {
            //empty intersection
            return new Rectangle(0.0, 0.0, 0.0, 0.0);
        }

        //construct clipped rectangle
        Rectangle clippedRectangle = new Rectangle(newX, newY, newW, newH);
        return clippedRectangle;
    }

    
    
   
    
}
