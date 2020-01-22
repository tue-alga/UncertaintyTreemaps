package com.eduardovernier;

import java.util.ArrayList;
import java.util.List;

public class Package {

    String id; // Package name
    List<Package> packageList = new ArrayList<>(); // Children package
    List<Entity> entityList = new ArrayList<>(); // Used to check whether a change is a insertion, modification or deletion
    Treemap treemap = new Treemap(); // Each package holds a treemap

    public Package(String id) {
        this.id = id;
    }

    public void setCanvas(double x, double y, double width, double height) {
        this.treemap.setCanvas(x, y, width, height);
    }

    public void addOrUpdateItem(List<String> path, Double weight) {

        if (path.size() == 1) {
            // Adding a leaf
            String entityName = path.get(0);
            // Look for entity in entity list
            for (Entity entity : entityList) {
                if (entity.id.equals(entityName)) { // If found, update its value and recompute treemap
                    entity.weight = weight;
                    treemap.updateItem(entityName, weight);
                    updateTreemapCoords();
                    return;
                }
            }
            // Else add it to entity list and treemap
            entityList.add(new Entity(entityName, weight));
            treemap.addItem(entityName, weight, Kind.LEAF);
            updateTreemapCoords();
        } else {
            // Adding a package
            String packageName = path.remove(0);
            // Package already exists
            for (Package childPackage : packageList) {
                if (childPackage.id.equals(packageName)) {
                    childPackage.addOrUpdateItem(path, weight);
                    treemap.updateItem(packageName, childPackage.getWeight()); // Recompute treemap with updated value
                    updateTreemapCoords(); // Update this.children size
                    return;
                }
            }
            // Needs to be created
            Package newPackage = new Package(packageName);
            packageList.add(newPackage);
            treemap.addItem(packageName, weight, Kind.PARENT); // Recompute treemap with new value
            updateTreemapCoords(); // Update this.children size
            newPackage.addOrUpdateItem(path, weight);
        }
        // updateAnimationCoords();
    }

    private void updateTreemapCoords() {
        for (Package pack : packageList) {
            Container container = treemap.findContainer(treemap.root, pack.id);
            if (container != null) {
                Rectangle canvas = container.rectangle;
                pack.setCanvas(canvas.x, canvas.y, canvas.width, canvas.height);
                pack.updateTreemapCoords();
            }
        }
    }

    public void updateAnimationCoords() {
        treemap.saveRectangleState(treemap.root);
        treemap.saveOldCanvas();
        for (Package pack : packageList) {
            pack.updateAnimationCoords();
        }
    }

    public double getWeight() {
        double weight = 0;
        for (Entity entity : entityList) {
            weight += entity.weight;
        }
        for (Package childPackage : packageList) {
            weight += childPackage.getWeight();
        }
        return weight;
    }

    @Override
    public String toString() {
        return "Package{" +
                "id='" + id + '\'' +
                ", packageList=" + packageList +
                ", entityList=" + entityList +
                '}';
    }
}
