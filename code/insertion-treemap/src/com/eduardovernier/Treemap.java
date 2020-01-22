package com.eduardovernier;

public class Treemap {

    public Container root;
    public Rectangle oldCanvas; // For animation purposes
    public Rectangle canvas;

    public Treemap() {
        this.root = new Container("", 0.0, Kind.PARENT);
    }

    public void setCanvas(double x, double y, double width, double height) {

        Rectangle newCanvas = new Rectangle(x, y, width, height);
        // Transform all rectangles if there was a change in size
        if (canvas != null) {
            transformRectangles(root, this.canvas, newCanvas);
        }
        // Update canvas
        this.canvas = newCanvas;
    }

    public void saveOldCanvas() {
        this.oldCanvas = new Rectangle(this.canvas.x, this.canvas.y, this.canvas.width, this.canvas.height);
    }

    private void transformRectangles(Container container, Rectangle oldCanvas, Rectangle newCanvas) {

        container.rectangle.reposition(oldCanvas, newCanvas);

        if (container.central != null) {
            transformRectangles(container.central, oldCanvas, newCanvas);
        }
        if (container.right != null) {
            transformRectangles(container.right, oldCanvas, newCanvas);
        }
        if (container.bottom != null) {
            transformRectangles(container.bottom, oldCanvas, newCanvas);
        }
    }

    public void addItem(String id, double value, Kind kind) {

        if (root == null) {
            root = new Container(id, value, kind);
        } else {
            Container receiver = findWorstAspectRatioContainer(root);

            if (receiver.rectangle.width >= receiver.rectangle.height) {
                if (receiver.right == null) {
                    receiver.right = new Container(id, value, kind);
                    // System.out.println("Right insert " + receiver.right.id + " into " + receiver.id);
                } else {
                    if (receiver.central == null) {
                        receiver.central = new Container(receiver.id, receiver.weight, receiver.kind);
                        receiver.central.right = new Container(id, value, kind);
                        receiver.id = receiver.id + "_";
                        receiver.kind = Kind.HELPER;
                    } else {
                        if (receiver.central.right == null) {
                            receiver.central.right = new Container(id, value, kind);
                        } else {
                            // System.out.print("WEIRD CENTRAL RIGHT INSERT. ");
                            Container temp = receiver.central.right;
                            receiver.central.right = new Container(id, value, kind);
                            receiver.central.right.right = temp;
                        }
                    }
                    // System.out.println("Special Right insert " + receiver.central.right.id + " into " + receiver.central);
                }
            } else {
                if (receiver.bottom == null) {
                    receiver.bottom = new Container(id, value, kind);
                    // System.out.println("Bottom insert " + receiver.bottom.id + " into " + receiver.id);
                } else {
                    if (receiver.central == null) {
                        receiver.central = new Container(receiver.id, receiver.weight, receiver.kind);
                        receiver.central.bottom = new Container(id, value, kind);
                        receiver.id = receiver.id + "_";
                        receiver.kind = Kind.HELPER;
                    } else {
                        if (receiver.central.bottom == null) {
                            receiver.central.bottom = new Container(id, value, kind);
                        } else {
                            // System.out.print("WEIRD CENTRAL BOTTOM INSERT. ");
                            Container temp = receiver.central.bottom;
                            receiver.central.bottom = new Container(id, value, kind);
                            receiver.central.bottom.bottom = temp;
                        }
                    }
                    // System.out.println("Special Bottom insert " + receiver.central.bottom.id + " into " + receiver.central);
                }
            }
        }

        root.rectangle = new Rectangle(canvas.x, canvas.y, canvas.width, canvas.height); // Reset canvas with parent measurements
        root.computeTreemap(); // Now that the layout reignign data structure was updated, recompute rectangles dimensions
    }

    public void saveRectangleState(Container container) {

        if (container.rectangle != null) {
            container.saveOldRectangle();
        }
        if (container.central != null) {
            saveRectangleState(container.central);
        }
        if (container.right != null) {
            saveRectangleState(container.right);
        }
        if (container.bottom != null) {
            saveRectangleState(container.bottom);
        }
    }

    public void updateItem(String id, Double weight) {
        // No change to the layout reigning data structure is needed,
        // just update the container weight and recompute the treemap
        Container container = findContainer(root, id);
        container.weight = weight;
        if (weight == 0.0) {
            container.oldRectangle = null;
        }
        root.rectangle = new Rectangle(canvas.x, canvas.y, canvas.width, canvas.height);
        root.computeTreemap();
    }

    public Container findContainer(Container container, String itemId) {

        if (container.id.equals(itemId)) {
            return container;
        } else {
            Container found = null;
            if (container.central != null) {
                Container temp = findContainer(container.central, itemId);
                if (temp != null) {
                    found = temp;
                }
            }
            if (container.right != null) {
                Container temp = findContainer(container.right, itemId);
                if (temp != null) {
                    found = temp;
                }
            }
            if (container.bottom != null) {
                Container temp = findContainer(container.bottom, itemId);
                if (temp != null) {
                    found = temp;
                }
            }
            return found;
        }
    }

    private Container findWorstAspectRatioContainer(Container container) {
        // Find worst aspect ratio container inside the argument container
        Container bestCandidate = container;
        double worstAR = container.rectangle.getAspectRatio();

        if (container.central != null) {
            Container temp = findWorstAspectRatioContainer(container.central);
            if (temp != null && temp.rectangle.getAspectRatio() < worstAR) {
                bestCandidate = temp;
                worstAR = temp.rectangle.getAspectRatio();
            }
        }

        if (container.right != null) {
            Container temp = findWorstAspectRatioContainer(container.right);
            if (temp != null && temp.rectangle.getAspectRatio() < worstAR) {
                bestCandidate = temp;
                worstAR = temp.rectangle.getAspectRatio();
            }
        }

        if (container.bottom != null) {
            Container temp = findWorstAspectRatioContainer(container.bottom);
            if (temp != null && temp.rectangle.getAspectRatio() < worstAR) {
                bestCandidate = temp;
            }
        }
        return bestCandidate;
    }
}
