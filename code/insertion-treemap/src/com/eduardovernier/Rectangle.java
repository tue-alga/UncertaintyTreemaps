package com.eduardovernier;

public class Rectangle {

    double x, y, width, height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getAspectRatio() {
        if (width == 0 || height == 0) {
            return 1;
        }
        return Math.min(width/height, height/width);
    }

    public void reposition(Rectangle oldCanvas, Rectangle newCanvas) {
        this.x = ((this.x - oldCanvas.x) / oldCanvas.width) * newCanvas.width + newCanvas.x;
        this.y = ((this.y - oldCanvas.y) / oldCanvas.height) * newCanvas.height + newCanvas.y;
        this.width = (newCanvas.width / oldCanvas.width) * this.width;
        this.height = (newCanvas.height / oldCanvas.height) * this.height;

        this.x = (Double.isNaN(this.x) || Double.isInfinite(this.x)) ? 0 : this.x;
        this.y = (Double.isNaN(this.y) || Double.isInfinite(this.y)) ? 0 : this.y;
        this.width = (Double.isNaN(this.width) || Double.isInfinite(this.width)) ? 0 : this.width;
        this.height = (Double.isNaN(this.height) || Double.isInfinite(this.height)) ? 0 : this.height;
    }

    @Override
    public String toString() {
        return "Rectangle{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
