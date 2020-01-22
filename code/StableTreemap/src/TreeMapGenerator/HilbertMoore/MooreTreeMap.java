package TreeMapGenerator.HilbertMoore;

import static TreeMapGenerator.HilbertMoore.HilbertMooreTreeMap.Corner.*;
import static TreeMapGenerator.HilbertMoore.HilbertMooreTreeMap.CurveOrientation.*;
import TreeMapGenerator.TreeMapGenerator;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author max
 */
public class MooreTreeMap extends HilbertMooreTreeMap {

    @Override
    protected void layOutQuadrants(Quadrant q, HilbertMooreTreeMap.CurveOrientation orientation, HilbertMooreTreeMap.Corner corner) {

        //leafQuadrants do not have to be layed out further
        if (q.isLeafQuadrant()) {
            return;
        }

        Rectangle r = q.getPosition();
        if ((orientation == LEFTBOTTOMCLOCK && corner == NORTHWEST)
                || (orientation == LEFTBOTTOMCLOCK && corner == SOUTHWEST)
                || (orientation == RIGHTTOPCLOCK && corner == NORTHWEST)
                || (orientation == RIGHTTOPCLOCK && corner == SOUTHWEST)) {
            layoutRightBottomClockWise(q, r);
        }
        if ((orientation == LEFTBOTTOMCLOCK && corner == NORTHEAST)
                || (orientation == LEFTBOTTOMCLOCK && corner == SOUTHEAST)
                || (orientation == RIGHTTOPCLOCK && corner == NORTHEAST)
                || (orientation == RIGHTTOPCLOCK && corner == SOUTHEAST)) {
            layoutLeftTopClockWise(q, r);
        }
        if ((orientation == LEFTTOPCLOCK && corner == NORTHWEST)
                || (orientation == LEFTTOPCLOCK && corner == NORTHEAST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == NORTHWEST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == NORTHEAST)) {
            layoutLeftBottomClockWise(q, r);
        }
        if ((orientation == LEFTTOPCLOCK && corner == SOUTHWEST)
                || (orientation == LEFTTOPCLOCK && corner == SOUTHEAST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == SOUTHWEST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == SOUTHEAST)) {
            layoutRightTopClockWise(q, r);
        }

    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return this;
    }
}
