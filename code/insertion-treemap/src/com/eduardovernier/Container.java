package com.eduardovernier;

//  ---------
//  | C |   |
//  |---| R |
//  | B |   |
//  ---------
public class Container {

    String id;
    Kind kind;
    Double weight;
    Rectangle oldRectangle;
    Rectangle rectangle = new Rectangle(0, 0, 0, 0);
    Container central; // Container may contain another central inside it
    Container right;
    Container bottom;

    public Container(String id, Double weight, Kind kind) {
        this.id = id;
        this.weight = weight;
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "Container{" +
                "id='" + id + '\'' +
                ", weight=" + weight +
                '}';
    }

    public void saveOldRectangle() {
        this.oldRectangle = new Rectangle(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
    }

    public double getFullWeight() {
        double fullWeight = this.weight;
        if (central != null) {
            fullWeight += central.getFullWeight();
        }

        if (right != null) {
            fullWeight += right.getFullWeight();
        }

        if (bottom != null) {
            fullWeight += bottom.getFullWeight();
        }
        return fullWeight;
    }

    public double getCentralWeight() {
        if (central == null) {
            return weight;
        } else {
            return central.getFullWeight();
        }
    }

    public void computeTreemap() {

        if (right != null && bottom != null) {

            double baseWidth = this.rectangle.width;
            double baseHeight = this.rectangle.height;
            // C coordinates
            this.rectangle.width = ((this.getCentralWeight() + bottom.getFullWeight()) / (this.getCentralWeight() + bottom.getFullWeight() + right.getFullWeight())) * baseWidth;
            this.rectangle.height = (this.getCentralWeight() / (this.getCentralWeight() + bottom.getFullWeight())) * baseHeight;

            if (Double.isNaN(this.rectangle.height) || Double.isInfinite(this.rectangle.height)) {
                this.rectangle.height = 0;
            }

            if (Double.isNaN(this.rectangle.width) || Double.isInfinite(this.rectangle.width)) {
                this.rectangle.width = 0;
            }

            // B coordinates
            bottom.rectangle.x = this.rectangle.x;
            bottom.rectangle.width = this.rectangle.width;
            bottom.rectangle.y = this.rectangle.y + this.rectangle.height;
            bottom.rectangle.height = baseHeight - this.rectangle.height;

            // R coordinates
            right.rectangle.x = this.rectangle.x + this.rectangle.width;
            right.rectangle.width = baseWidth - this.rectangle.width;
            right.rectangle.y = this.rectangle.y;
            right.rectangle.height = baseHeight;

            right.computeTreemap();
            bottom.computeTreemap();

        } else if (right != null) {

            double baseWidth = this.rectangle.width;

            // C coordinates - Only the width changes
            this.rectangle.width = (this.getCentralWeight() / (this.getCentralWeight() + right.getFullWeight())) * baseWidth;
            if (Double.isNaN(this.rectangle.width) || Double.isInfinite(this.rectangle.width)) {
                this.rectangle.width = 0;
            }
            // R coordinates
            right.rectangle.x = this.rectangle.x + this.rectangle.width;
            right.rectangle.width = baseWidth - this.rectangle.width;
            right.rectangle.y = this.rectangle.y;
            right.rectangle.height = this.rectangle.height;

            right.computeTreemap();

        } else if (bottom != null) {

            double baseHeight = this.rectangle.height;

            // C coordinates - Only the height changes
            this.rectangle.height = (this.getCentralWeight() / (this.getCentralWeight() + bottom.getFullWeight())) * baseHeight;
            if (Double.isNaN(this.rectangle.height) || Double.isInfinite(this.rectangle.height)) {
                this.rectangle.height = 0;
            }

            // B coordinates
            bottom.rectangle.x = this.rectangle.x;
            bottom.rectangle.width = this.rectangle.width;
            bottom.rectangle.y = this.rectangle.y + this.rectangle.height;
            bottom.rectangle.height = baseHeight - this.rectangle.height;

            bottom.computeTreemap();
        }

        if (central != null) {
            central.rectangle = new Rectangle(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
            central.computeTreemap();
        }
    }

}
