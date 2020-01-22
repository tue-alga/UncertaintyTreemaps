package treemap.dataStructure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static utility.Precision.*;

/**
 *
 * @author max
 */
public class LineSegment {

    public double x1, x2, y1, y2;
    public boolean horizontal;
    public boolean vertical;

    /**
     * Creates the line segment and sets the orientation.
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     */
    public LineSegment(double x1, double x2, double y1, double y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        //it is either vertical or horizontal as we are making squares
        //TODO check for double precision errors here
        if (y1 == y2) {
            this.horizontal = true;
            this.vertical = false;
        } else {
            this.vertical = true;
            this.horizontal = false;
        }
    }

    /**
     * Returns the x-center of the line segment
     *
     * @return
     */
    public double getCenterX() {
        return (x1 + (x2 - x1) / 2);
    }

    /**
     * Returns the y-center of the line segment
     *
     * @return
     */
    public double getCenterY() {
        return (y1 + (y2 - y1) / 2);
    }

    /**
     * Returns the euclidian length of the line segment
     *
     * @return
     */
    public double getLength() {
        double xLength = Math.abs(x2 - x1);
        double yLength = Math.abs(y2 - y1);
        return Math.sqrt(xLength * xLength + yLength * yLength);
    }

    /**
     * Returns whether this segment partially overlaps {@code segment}
     *
     * @param segment
     * @return
     */
    public boolean partiallyOverlaps(LineSegment segment) {
        if (horizontal) {
            if ((eq(y1, segment.y1) && eq(y2, segment.y2))
                && ((geq(x1, segment.x1) && leq(x1, segment.x2))
                    || (geq(x2, segment.x1) && leq(x2, segment.x2)))) {
                return true;
            }
        } else //it is vertical
        {
            if ((eq(x1, segment.x1) && eq(x2, segment.x2))
                && ((geq(y1, segment.y1) && leq(y1, segment.y2))
                    || (geq(y2, segment.y1) && leq(y2, segment.y2)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the set of 4 line segment surrounding rectangle {@code r}
     *
     * @param r
     * @return
     */
    public static Set<LineSegment> getSegments(Rectangle r) {
        //add the four segments from the rectangle
        Set<LineSegment> segmentSet = new HashSet();
        double x = r.getX();
        double x2 = r.getX() + r.getWidth();
        double y = r.getY();
        double y2 = r.getY() + r.getHeight();

        LineSegment segment;
        segment = new LineSegment(x, x2, y, y);
        segmentSet.add(segment);
        segment = new LineSegment(x2, x2, y, y2);
        segmentSet.add(segment);
        segment = new LineSegment(x, x2, y2, y2);
        segmentSet.add(segment);
        segment = new LineSegment(x2, x2, y, y2);
        segmentSet.add(segment);

        return segmentSet;
    }

    /**
     * Returns all line segments formed by the rectangles of leaf treemaps in
     * {@code treemaps}
     *
     * @param treeMaps
     * @return
     */
    public static Set<LineSegment> getSegmentsFromLeafs(List<TreeMap> treeMaps) {
        Set<LineSegment> segmentSet = new HashSet();

        for (TreeMap tm : treeMaps) {
            if (!tm.hasChildren()) {
                Rectangle r = tm.getRectangle();
                segmentSet.addAll(getSegments(r));
            }
        }

        return segmentSet;
    }

    /**
     * Returns the segment formed by the bottom of rectangle {@code r}
     *
     * @param r
     * @return
     */
    public static LineSegment getBottomSegment(Rectangle r) {
        LineSegment ls = new LineSegment(r.getX(), r.getX2(), r.getY2(), r.getY2());
        return ls;
    }

    /**
     * Returns the segment formed by the top of rectangle {@code r}
     *
     * @param r
     * @return
     */
    public static LineSegment getTopSegment(Rectangle r) {
        LineSegment ls = new LineSegment(r.getX(), r.getX2(), r.getY(), r.getY());
        return ls;
    }

    /**
     * Returns the segment formed by the left of rectangle {@code r}
     *
     * @param r
     * @return
     */
    public static LineSegment getLeftSegment(Rectangle r) {
        LineSegment ls = new LineSegment(r.getX(), r.getX(), r.getY(), r.getY2());
        return ls;

    }

    /**
     * Returns the segment formed by the right of rectangle {@code r}
     *
     * @param r
     * @return
     */
    public static LineSegment getRightSegment(Rectangle r) {
        LineSegment ls = new LineSegment(r.getX2(), r.getX2(), r.getY(), r.getY2());
        return ls;

    }

    /**
     * Returns whether this segment intersect horizontalMs. Assumes that this
     * segment is a vertical segment
     *
     * @param horizontalMs
     * @return
     */
    public boolean intersects(LineSegment horizontalMs) {
        //this is a vertical segment
        LineSegment msHor = horizontalMs;
        LineSegment msVer = this;

        if (ge(msVer.x1, msHor.x1) && le(msVer.x1, msHor.x2)) {
            //vertical segment is between horizontal x1 and x2
            if (ge(msHor.y1, msVer.y1) && le(msHor.y1, msVer.y2)) {
                //horizontal segement is between vertical y1 and y2, thus intersect
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!LineSegment.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        LineSegment other = (LineSegment) obj;

        if (!eq(other.x1, x1) || !eq(other.x2, x2) || !eq(other.y1, y1) || !eq(other.y2, y2)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.x1) ^ (Double.doubleToLongBits(this.x1) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.x2) ^ (Double.doubleToLongBits(this.x2) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.y1) ^ (Double.doubleToLongBits(this.y1) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.y2) ^ (Double.doubleToLongBits(this.y2) >>> 32));
        return hash;
    }
}
