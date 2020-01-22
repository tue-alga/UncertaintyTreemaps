package com.eduardovernier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreemapManager {

    private Package rootPackage;
    private List<List<Entity>> revisionList;
    private int revision = 0;

    // Later for the interactive version
    public TreemapManager(Rectangle canvas) {
        this.rootPackage = new Package("");
        this.rootPackage.setCanvas(canvas.x, canvas.y, canvas.width, canvas.height);
    }

    public TreemapManager(Rectangle canvas, List<List<Entity>> revisionList) {
        this.rootPackage = new Package("");
        this.rootPackage.setCanvas(canvas.x, canvas.y, canvas.width, canvas.height);
        this.revisionList = revisionList;

        for (Entity entity : revisionList.get(0)) {
            addEntity(entity, true);
        }
        MetricsCollector.init(revisionList.size());
        MetricsCollector.saveRectangles(rootPackage, revision, "");
    }

    public Package getRootPackage() {
        return rootPackage;
    }

    public int getRevision() {
        return revision;
    }

    public void addEntity(Entity entity, boolean forward) {
        if (entity.weight > 0) {
            rootPackage.addOrUpdateItem(new ArrayList<>(Arrays.asList(entity.id.split("/"))), entity.weight);
        } else {
            if (forward) {
                if (entity.weight == 0.0 && revision > 0) {
                    int index = revisionList.get(revision - 1).indexOf(entity);
                    Entity entityInLastRev = revisionList.get(revision - 1).get(index);
                    if (entityInLastRev.weight > 0) { // deletion
                        rootPackage.addOrUpdateItem(new ArrayList<>(Arrays.asList(entity.id.split("/"))), entity.weight);
                    }
                }
            } else {
                if (entity.weight == 0.0 && revision < revisionList.size()) {
                    int index = revisionList.get(revision + 1).indexOf(entity);
                    Entity entityInLastRev = revisionList.get(revision + 1).get(index);
                    if (entityInLastRev.weight > 0) { // deletion backwards -- addition
                        rootPackage.addOrUpdateItem(new ArrayList<>(Arrays.asList(entity.id.split("/"))), entity.weight);
                    }
                }
            }
        }
    }

    public void returnOneRevision() {
        rootPackage.updateAnimationCoords();
        if (revision > 0) {
            revision--;
            for (Entity entity : revisionList.get(revision)) {
                addEntity(entity, false);
            }
            for (Entity entity : revisionList.get(revision)) {
                addEntity(entity, false);
            }
        }
    }

    public void advanceOneRevision() {
        rootPackage.updateAnimationCoords();
        if (revision < revisionList.size() - 1) {
            revision++;
            for (Entity entity : revisionList.get(revision)) {
                addEntity(entity, true);
            }
            for (Entity entity : revisionList.get(revision)) {
                addEntity(entity, true);
            }
            MetricsCollector.saveRectangles(rootPackage, revision, "");
        }

    }
}
