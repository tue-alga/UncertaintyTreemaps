package TreeMapGenerator.HilbertMoore;

import static TreeMapGenerator.HilbertMoore.HilbertMooreTreeMap.Corner.*;
import static TreeMapGenerator.HilbertMoore.HilbertMooreTreeMap.CurveOrientation.*;
import TreeMapGenerator.TreeMapGenerator;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author max
 */
public class HilbertTreeMap extends HilbertMooreTreeMap {

    @Override
    protected void layOutQuadrants(Quadrant q, CurveOrientation orientation, Corner corner) {

        //leafQuadrants do not have to be layed out further
        if (q.isLeafQuadrant()) {
            return;
        }

        Rectangle r = q.getPosition();
        if ((orientation == LEFTBOTTOMCLOCK && corner == NORTHWEST)
                || (orientation == LEFTBOTTOMCLOCK && corner == NORTHEAST)
                || (orientation == LEFTBOTTOMCOUNTER && corner == SOUTHWEST)
                || (orientation == RIGHTTOPCOUNTER && corner == SOUTHEAST)) {
            layoutLeftBottomClockWise(q, r);
        }
        if ((orientation == LEFTTOPCLOCK && corner == NORTHEAST)
                || (orientation == LEFTTOPCLOCK && corner == SOUTHEAST)
                || (orientation == RIGHTBOTTOMCOUNTER && corner == SOUTHWEST)
                || (orientation == LEFTTOPCOUNTER && corner == NORTHWEST)) {
            layoutLeftTopClockWise(q, r);
        }
        if ((orientation == RIGHTTOPCLOCK && corner == SOUTHEAST)
                || (orientation == RIGHTTOPCLOCK && corner == SOUTHWEST)
                || (orientation == LEFTBOTTOMCOUNTER && corner == NORTHWEST)
                || (orientation == RIGHTTOPCOUNTER && corner == NORTHEAST)) {
            layoutRightTopClockWise(q, r);
        }
        if ((orientation == RIGHTBOTTOMCLOCK && corner == SOUTHWEST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == NORTHWEST)
                || (orientation == RIGHTBOTTOMCOUNTER && corner == SOUTHEAST)
                || (orientation == LEFTTOPCOUNTER && corner == NORTHEAST)) {
            layoutRightBottomClockWise(q, r);
        }
//counter
        if ((orientation == RIGHTBOTTOMCOUNTER && corner == NORTHWEST)
                || (orientation == RIGHTBOTTOMCOUNTER && corner == NORTHEAST)
                || (orientation == LEFTTOPCLOCK && corner == SOUTHWEST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == SOUTHEAST)) {
            layoutRightBottomCounterClockWise(q, r);
        }
        if ((orientation == LEFTBOTTOMCOUNTER && corner == NORTHEAST)
                || (orientation == LEFTBOTTOMCOUNTER && corner == SOUTHEAST)
                || (orientation == LEFTBOTTOMCLOCK && corner == SOUTHWEST)
                || (orientation == RIGHTTOPCLOCK && corner == NORTHWEST)) {
            layoutLeftBottomCounterClockWise(q, r);
        }
        if ((orientation == LEFTTOPCOUNTER && corner == SOUTHEAST)
                || (orientation == LEFTTOPCOUNTER && corner == SOUTHWEST)
                || (orientation == LEFTTOPCLOCK && corner == NORTHWEST)
                || (orientation == RIGHTBOTTOMCLOCK && corner == NORTHEAST)) {
            layoutLeftTopCounterClockWise(q, r);
        }
        if ((orientation == RIGHTTOPCOUNTER && corner == SOUTHWEST)
                || (orientation == RIGHTTOPCOUNTER && corner == NORTHWEST)
                || (orientation == LEFTBOTTOMCLOCK && corner == SOUTHEAST)
                || (orientation == RIGHTTOPCLOCK && corner == NORTHEAST)) {
            layoutRightTopCounterClockWise(q, r);
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
