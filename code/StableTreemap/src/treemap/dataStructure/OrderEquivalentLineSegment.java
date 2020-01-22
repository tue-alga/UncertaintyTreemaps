package treemap.dataStructure;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author max
 */
public class OrderEquivalentLineSegment extends LineSegment {

    /**
     * The labels of the rectangle adjacent to the left of this segment. Can be
     * empty
     */
    public Set<String> labelsLeft;

    /**
     * The labels of the rectangle adjacent to the right of this segment. Can be
     * empty
     */
    public Set<String> labelsRight;

    /**
     * The labels of the rectangle adjacent to the top of this segment. Can be
     * empty
     */
    public Set<String> labelsTop;

    /**
     * The labels of the rectangle adjacent to the bottom of this segment. Can
     * be empty
     */
    public Set<String> labelsBottom;

    public OrderEquivalentLineSegment(double x1, double x2, double y1, double y2, Set<String> labelsLeft, Set<String> labelsRight, Set<String> labelsTop, Set<String> labelsBottom) {
        super(x1, x2, y1, y2);
        this.labelsLeft = labelsLeft;
        this.labelsRight = labelsRight;
        this.labelsTop = labelsTop;
        this.labelsBottom = labelsBottom;
        if (this.labelsLeft == null) {
            this.labelsLeft = new HashSet();
        }
        if (this.labelsRight == null) {
            this.labelsRight = new HashSet();
        }
        if (this.labelsTop == null) {
            this.labelsTop = new HashSet();
        }
        if (this.labelsBottom == null) {
            this.labelsBottom = new HashSet();
        }
    }

    public OrderEquivalentLineSegment(LineSegment ls, Set<String> labelsLeft, Set<String> labelsRight, Set<String> labelsTop, Set<String> labelsBottom) {
        super(ls.x1, ls.x2, ls.y1, ls.y2);
        this.labelsLeft = labelsLeft;
        this.labelsRight = labelsRight;
        this.labelsTop = labelsTop;
        this.labelsBottom = labelsBottom;
        if (this.labelsLeft == null) {
            this.labelsLeft = new HashSet();
        }
        if (this.labelsRight == null) {
            this.labelsRight = new HashSet();
        }
        if (this.labelsTop == null) {
            this.labelsTop = new HashSet();
        }
        if (this.labelsBottom == null) {
            this.labelsBottom = new HashSet();
        }
    }

    public OrderEquivalentLineSegment(LineSegment ls, String labelLeft, String labelRight, String labelTop, String labelBottom) {
        super(ls.x1, ls.x2, ls.y1, ls.y2);
        labelsLeft = new HashSet();
        if (labelLeft != null) {
            labelsLeft.add(labelLeft);
        }
        labelsRight = new HashSet();
        if (labelRight != null) {
            labelsRight.add(labelRight);
        }
        labelsTop = new HashSet();
        if (labelTop != null) {
            labelsTop.add(labelTop);
        }
        labelsBottom = new HashSet();
        if (labelBottom != null) {
            labelsBottom.add(labelBottom);
        }

    }

    /**
     * Returns a string consisting of a concatenation of the
     * top,bottom,left,right labels of this segment. This uniquely identifies
     * this segment in a treemap.
     *
     * @return
     */
    public String getLabelName() {
        String labelName = "{";
        for (String s : labelsTop) {
            labelName += ";" + s;
        }
        labelName += "}\n{";
        for (String s : labelsBottom) {
            labelName += ";" + s;
        }
        labelName += "}\n{";
        for (String s : labelsLeft) {
            labelName += ";" + s;
        }
        labelName += "}\n{";
        for (String s : labelsRight) {
            labelName += ";" + s;
        }
        labelName += "}";
        return labelName;
    }

    /**
     * Returns all labels of rectangles that this segment is adjacent too.
     *
     * @return
     */
    Set<String> getLabels() {
        Set<String> labels = new HashSet();
        labels.addAll(labelsBottom);
        labels.addAll(labelsTop);
        labels.addAll(labelsLeft);
        labels.addAll(labelsRight);
        return labels;
    }

}
