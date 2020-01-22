package TreeMapGenerator.LocalChanges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import treemap.dataStructure.Rectangle;

/**
 * Represents a block in the treemap layout. Identified by {@code label}
 *
 * @author max
 */
public class Block {

    /**
     * The unique label of the block in the treemap
     */
    protected String label;
    /**
     * The targetSize that this block should have in the treemap.
     */
    protected double targetSize;

    /**
     * The position of this block. Can be null Area of rectangle does not have
     * to equal targetSize
     */
    public Rectangle rectangle;

    /**
     * The orderEquivalentMaximalSegments surrounding this block
     */
    protected OrderEquivalentMaximalSegment msLeft, msRight, msTop, msBottom;

    /**
     *
     * @param rectangle
     * @param label
     * @param size
     */
    public Block(Rectangle rectangle, String label, double targetSize) {
        this.rectangle = rectangle;
        this.label = label;
        this.targetSize = targetSize;
    }

    public String getLabel() {
        return label;
    }

    public double getTargetSize() {
        return targetSize;
    }

    public double getActualSize(double scaleFactor) {
        return this.rectangle.getArea() * scaleFactor;
    }

    /**
     * Rescales this block dependent on the four maximalsegments surrounding
     * this block
     *
     * @param scaleFactor The global scaling factor to convert from size in px
     * to the actual size
     */
    public void reScale(double scaleFactor) {
        //TODO verify that everything is updated correcly
        double x1 = msLeft.x1;
        double x2 = msRight.x1;
        double y1 = msTop.y1;
        double y2 = msBottom.y1;
        this.rectangle = new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    public OrderEquivalentMaximalSegment getMsLeft() {
        return msLeft;
    }

    public void setMsLeft(OrderEquivalentMaximalSegment msLeft) {
        this.msLeft = msLeft;
    }

    public OrderEquivalentMaximalSegment getMsRight() {
        return msRight;
    }

    public void setMsRight(OrderEquivalentMaximalSegment msRight) {
        this.msRight = msRight;
    }

    public OrderEquivalentMaximalSegment getMsTop() {
        return msTop;
    }

    public void setMsTop(OrderEquivalentMaximalSegment msTop) {
        this.msTop = msTop;
    }

    public OrderEquivalentMaximalSegment getMsBottom() {
        return msBottom;
    }

    public void setMsBottom(OrderEquivalentMaximalSegment msBottom) {
        this.msBottom = msBottom;
    }

    public void setTargetSize(double targetSize) {
        this.targetSize = targetSize;
    }

    @Override
    public boolean equals(Object obj) {
        //Maximal segments do not have to be equal as these are specified afterwards
        //and are uniquely determined by all blocks in cohesion.
        if (obj == null) {
            return false;
        }
        if (!Block.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        Block other = (Block) obj;
//        if (other.rectangle != rectangle || other.label != label || other.targetSize != targetSize || other.getActualSize(1) != getActualSize(1)) {
        //return false
        //}
        return label.equals(other.label);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.label);
//        hash = 97 * hash + (int) (Double.doubleToLongBits(this.targetSize) ^ (Double.doubleToLongBits(this.targetSize) >>> 32));
//        hash = 97 * hash + (int) (Double.doubleToLongBits(this.getActualSize(1)) ^ (Double.doubleToLongBits(this.getActualSize(1)) >>> 32));
//        hash = 97 * hash + Objects.hashCode(this.rectangle);
        return hash;
    }

    /**
     * Gets a maximal segment for which block b is the only block adjacent to it
     * on one side or null if it does not exist. Maximal segment is not allowed
     * to be the boundary
     *
     * @param boundarySegments the boundary segments
     * @return
     */
    public OrderEquivalentMaximalSegment getOneSided() {
        OrderEquivalentMaximalSegment oneSided = null;
        if (getMsBottom().isOneSidedWithBlock(this) == true && getMsBottom().adjacentBlocks.size() > 1) {
            oneSided = getMsBottom();
        } else if (getMsTop().isOneSidedWithBlock(this) == true && getMsTop().adjacentBlocks.size() > 1) {
            oneSided = getMsTop();
        } else if (getMsLeft().isOneSidedWithBlock(this) == true && getMsLeft().adjacentBlocks.size() > 1) {
            oneSided = getMsLeft();
        } else if (getMsRight().isOneSidedWithBlock(this) == true && getMsRight().adjacentBlocks.size() > 1) {
            oneSided = getMsRight();
        }
        return oneSided;
    }

    public static double getWeight(Set<Block> blocks) {
        double weight = 0;
        for (Block b : blocks) {
            weight += b.getTargetSize();
        }
        return weight;
    }

    /**
     * Returns a common block between {@code blockList1} and {@code blockList2}
     * or null if it does not exist
     *
     * @param blockList1
     * @param blockList2
     * @return
     */
    public static Block getCommonBlock(List<Block> blockList1, List<Block> blockList2) {
        for (Block b : blockList1) {
            if (blockList2.contains(b)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Returns true if each block in list1 is present in list2 and vise verca. A
     * block is present in both lists if and only if there exists a block in
     * both lists with the same labeling
     *
     * @param list1
     * @param list2
     * @return
     */
    public static boolean checkEqualBlockList(List<Block> list1, List<Block> list2) {
        //Check if all blocks in blockList1 of msNew are also present din blockList 1 of ms and visa verca
        if (list1.size() != list2.size()) {
            //sizes are not identical thus can't hold
            return false;
        }
        /**
         * as sizes are identical only have to iterate once, if all blocks
         * present in list1 are present in list2 then the lists are identical
         */
        for (Block b1 : list1) {
            boolean found = false;
            for (Block b2 : list2) {
                if (b1.getLabel().equals(b2.getLabel())) {
                    found = true;
                    break;

                }
            }
            //b1 is not present in b2
            if (found == false) {
                return false;
            }
        }
        //all blocks in b1 are present in b2 and they have the same size. Lists are identical
        return true;
    }

    /**
     * returns the block with the label equal to {@code label} or null if no
     * block in blocklist has this label
     *
     * @param blockList
     * @param label
     * @return
     */
    public static Block getBlockWithLabel(List<Block> blockList, String label) {
        for (Block b : blockList) {
            if (b.getLabel().equals(label)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Returns all blocks in blockList2 which have a block with a label that is
     * also present in blockList1
     *
     * @param adjacentBlockList1
     * @param newBlockList
     * @return
     */
    static List<Block> getEquivalentBlockList(List<Block> blockList1, List<Block> blockList2) {
        List<Block> equivalentBlocks = new ArrayList();
        for (Block b1 : blockList1) {
            String label1 = b1.getLabel();

            for (Block b2 : blockList2) {
                if (label1.equals(b2.getLabel())) {
                    equivalentBlocks.add(b2);
                    break;
                }
            }
        }
        return equivalentBlocks;
    }

}
