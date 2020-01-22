package TreeMapGenerator.HilbertMoore;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;

/**
 * Class used to determine the best layout out of a variety of layouts
 *
 * @author max
 */
public class LayoutGenerator {

    public static Map<DataMap, Rectangle> getOptimalLayout(Rectangle treeMapRectangle, List<DataMap> itemList) {

        if (itemList.size() == 1) {
            Map<DataMap, Rectangle> mapping = new HashMap();
            mapping.put(itemList.get(0), treeMapRectangle);
            return mapping;
        }

        if (itemList.size() == 2) {
            Map<DataMap, Rectangle> mapping = new HashMap();
            double startX = treeMapRectangle.getX();
            double startY = treeMapRectangle.getY();
            double height = treeMapRectangle.getHeight();
            DataMap dm0 = itemList.get(0);
            DataMap dm1 = itemList.get(1);

            double width = dm0.getTargetSize() / (dm0.getTargetSize() + dm1.getTargetSize()) * treeMapRectangle.getWidth();
            Rectangle r = new Rectangle(startX, startY, width, height);
            mapping.put(dm0, r);

            startX += width;
            width = treeMapRectangle.getWidth() - width;
            r = new Rectangle(startX, startY, width, height);
            mapping.put(dm1, r);

            return mapping;
        }

        if (itemList.size() == 3) {
            Map<DataMap, Rectangle> snakeLayout = getSnakeLayout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most1Layout = getMost1Layout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most2Layout = getMost2Layout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most3Layout3Items = getMost3Layout3Items(treeMapRectangle, itemList);

            double snakeAspectRatio = getAverageAspectRatio(snakeLayout.values());
            double most1AspectRatio = getAverageAspectRatio(most1Layout.values());
            double most2AspectRatio = getAverageAspectRatio(most2Layout.values());
            double most3AspectRatio = getAverageAspectRatio(most3Layout3Items.values());

            double bestAspectRatio = Math.min(snakeAspectRatio, Math.min(most1AspectRatio, Math.min(most2AspectRatio, most3AspectRatio)));
            if (snakeAspectRatio <= bestAspectRatio) {
                return snakeLayout;
            }
            if (most1AspectRatio <= bestAspectRatio) {
                return most1Layout;
            }
            if (most2AspectRatio <= bestAspectRatio) {
                return most2Layout;
            }
            if (most3AspectRatio <= bestAspectRatio) {
                return most3Layout3Items;
            }

        }
        if (itemList.size() == 4) {
            Map<DataMap, Rectangle> snakeLayout = getSnakeLayout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most1Layout = getMost1Layout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most2Layout = getMost2Layout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most3Layout4Items = getMost3Layout4Items(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> most4Layout = getMost4Layout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> horizontalLayout = getHorizontalSplitLayout(treeMapRectangle, itemList);
            Map<DataMap, Rectangle> verticalLayout = getVerticalSplitLayout(treeMapRectangle, itemList);

            double snakeAspectRatio = getAverageAspectRatio(snakeLayout.values());
            double most1AspectRatio = getAverageAspectRatio(most1Layout.values());
            double most2AspectRatio = getAverageAspectRatio(most2Layout.values());
            double most3AspectRatio = getAverageAspectRatio(most3Layout4Items.values());
            double most4AspectRatio = getAverageAspectRatio(most4Layout.values());
            double horizontalAspectRatio = getAverageAspectRatio(horizontalLayout.values());
            double verticalAspectRatio = getAverageAspectRatio(verticalLayout.values());

            double bestAspectRatio
                    = Math.min(snakeAspectRatio,
                            Math.min(most1AspectRatio,
                                    Math.min(most2AspectRatio,
                                            Math.min(most3AspectRatio,
                                                    Math.min(most4AspectRatio,
                                                            Math.min(horizontalAspectRatio, verticalAspectRatio))))));
            if (snakeAspectRatio <= bestAspectRatio) {
                return snakeLayout;
            }
            if (most1AspectRatio <= bestAspectRatio) {
                return most1Layout;
            }
            if (most2AspectRatio <= bestAspectRatio) {
                return most2Layout;
            }
            if (most3AspectRatio <= bestAspectRatio) {
                return most3Layout4Items;
            }
            if (most4AspectRatio <= bestAspectRatio) {
                return most4Layout;
            }
            if (horizontalAspectRatio <= bestAspectRatio) {
                return horizontalLayout;
            }
            if (verticalAspectRatio <= bestAspectRatio) {
                return verticalLayout;
            }

        }

        return null;
    }

    private static double getAverageAspectRatio(Collection<Rectangle> rectangles) {
        double sumAspectRatio = 0;
        for (Rectangle r : rectangles) {
            sumAspectRatio += r.getAspectRatio();
        }
        //todo
        return sumAspectRatio / rectangles.size();
    }

    private static Map<DataMap, Rectangle> getSnakeLayout(Rectangle treeMapRectangle, List<DataMap> itemList) {
        assert (itemList.size() == 3 || itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;

        Map<DataMap, Rectangle> rectangleMapping = new HashMap();
        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double height = treeMapRectangle.getHeight();
        for (DataMap dm : itemList) {
            double width = dm.getTargetSize() / height * scaleFactor;
            Rectangle r = new Rectangle(startX, startY, width, height);
            rectangleMapping.put(dm, r);
            startX += width;
        }
        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getMost1Layout(Rectangle treeMapRectangle, List<DataMap> itemList) {
        assert (itemList.size() == 3 || itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double height = treeMapRectangle.getHeight();

        DataMap dm1 = itemList.get(0);
        double width = dm1.getTargetSize() / height * scaleFactor;

        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm1, r);

        startX = treeMapRectangle.getX() + width;
        startY = treeMapRectangle.getY();
        width = treeMapRectangle.getWidth() - width;

        for (int i = 1; i < itemList.size(); i++) {
            DataMap dmI = itemList.get(i);
            height = dmI.getTargetSize() / width * scaleFactor;

            r = new Rectangle(startX, startY, width, height);
            rectangleMapping.put(dmI, r);
            startY += height;
        }

        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getMost2Layout(Rectangle treeMapRectangle, List<DataMap> itemList) {

        assert (itemList.size() == 3 || itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;

        Map<DataMap, Rectangle> rectangleMapping = new HashMap();
        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double width = treeMapRectangle.getWidth();

        DataMap dm2 = itemList.get(1);
        double height = dm2.getTargetSize() / width * scaleFactor;
        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm2, r);

        startY += height;
        height = treeMapRectangle.getHeight() - height;
        DataMap dm1 = itemList.get(0);
        width = dm1.getTargetSize() / height * scaleFactor;
        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm1, r);

        startX += width;
        width = treeMapRectangle.getWidth() - width;
        for (int i = 2; i < itemList.size(); i++) {
            DataMap dmI = itemList.get(i);
            height = dmI.getTargetSize() / width * scaleFactor;
            r = new Rectangle(startX, startY, width, height);
            rectangleMapping.put(dmI, r);

            startY += height;
        }

        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getMost3Layout3Items(Rectangle treeMapRectangle, List<DataMap> itemList) {

        assert (itemList.size() == 3);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;

        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        double startY = treeMapRectangle.getY();
        double height = treeMapRectangle.getHeight();

        DataMap dm3 = itemList.get(2);
        double width = dm3.getTargetSize() / height * scaleFactor;
        double startX = treeMapRectangle.getX() + treeMapRectangle.getWidth() - width;

        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm3, r);

        startX = treeMapRectangle.getX();
        width = treeMapRectangle.getWidth() - width;

        startY = treeMapRectangle.getY() + treeMapRectangle.getHeight();
        for (int i = 0; i <= 1; i++) {
            DataMap dmI = itemList.get(i);
            height = dmI.getTargetSize() / width * scaleFactor;
            startY -= height;

            r = new Rectangle(startX, startY, width, height);
            rectangleMapping.put(dmI, r);
        }

        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getMost3Layout4Items(Rectangle treeMapRectangle, List<DataMap> itemList) {
        assert (itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double width = treeMapRectangle.getWidth();

        DataMap dm3 = itemList.get(2);
        double height = dm3.getTargetSize() / width * scaleFactor;

        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm3, r);

        startY += height;
        height = treeMapRectangle.getHeight() - height;

        DataMap dm4 = itemList.get(3);
        width = dm4.getTargetSize() / height * scaleFactor;
        startX = treeMapRectangle.getX() + treeMapRectangle.getWidth() - width;
        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm4, r);

        startX = treeMapRectangle.getX();
        width = treeMapRectangle.getWidth() - width;
        startY = treeMapRectangle.getY() + treeMapRectangle.getHeight();
        for (int i = 0; i <= 1; i++) {
            DataMap dmI = itemList.get(i);
            height = dmI.getTargetSize() / width * scaleFactor;
            startY -= height;

            r = new Rectangle(startX, startY, width, height);
            rectangleMapping.put(dmI, r);
        }

        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getMost4Layout(Rectangle treeMapRectangle, List<DataMap> itemList) {
        assert (itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        double startY = treeMapRectangle.getY();
        double height = treeMapRectangle.getHeight();

        DataMap dm4 = itemList.get(3);
        double width = dm4.getTargetSize() / height * scaleFactor;
        double startX = treeMapRectangle.getX() + treeMapRectangle.getWidth() - width;

        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm4, r);

        width = treeMapRectangle.getWidth() - width;
        startX = treeMapRectangle.getX();
        startY = treeMapRectangle.getY() + treeMapRectangle.getHeight();

        for (int i = 0; i <= 2; i++) {
            DataMap dmI = itemList.get(i);
            height = dmI.getTargetSize() / width * scaleFactor;
            startY -= height;

            r = new Rectangle(startX, startY, width, height);
            rectangleMapping.put(dmI, r);
        }

        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getHorizontalSplitLayout(Rectangle treeMapRectangle, List<DataMap> itemList) {
        assert (itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        DataMap dm1 = itemList.get(0);
        DataMap dm2 = itemList.get(1);
        DataMap dm3 = itemList.get(2);
        DataMap dm4 = itemList.get(3);

        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double height = (dm2.getTargetSize() + dm3.getTargetSize()) / totalSize * treeMapRectangle.getHeight();
        double width = dm2.getTargetSize() / height * scaleFactor;

        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm2, r);

        startX += width;
        width = treeMapRectangle.getWidth() - width;
        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm3, r);

        startX = treeMapRectangle.getX();
        startY += height;
        height = treeMapRectangle.getHeight() - height;
        width = dm1.getTargetSize() / height * scaleFactor;

        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm1, r);

        startX += width;
        width = treeMapRectangle.getWidth() - width;
        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm4, r);

        return rectangleMapping;
    }

    private static Map<DataMap, Rectangle> getVerticalSplitLayout(Rectangle treeMapRectangle, List<DataMap> itemList) {
        assert (itemList.size() == 4);

        double totalArea = treeMapRectangle.getArea();
        double totalSize = DataMap.getTotalSize(itemList);
        double scaleFactor = totalArea / totalSize;
        Map<DataMap, Rectangle> rectangleMapping = new HashMap();

        DataMap dm1 = itemList.get(0);
        DataMap dm2 = itemList.get(1);
        DataMap dm3 = itemList.get(2);
        DataMap dm4 = itemList.get(3);

        double startX = treeMapRectangle.getX();
        double startY = treeMapRectangle.getY();
        double width = (dm1.getTargetSize() + dm2.getTargetSize()) / totalSize * treeMapRectangle.getWidth();
        double height = dm2.getTargetSize() / width * scaleFactor;

        Rectangle r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm2, r);

        startY += height;
        height = treeMapRectangle.getHeight() - height;
        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm1, r);

        startY = treeMapRectangle.getY();
        startX += width;
        width = treeMapRectangle.getWidth() - width;
        height = dm3.getTargetSize() / width * scaleFactor;

        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm3, r);

        startY += height;
        height = treeMapRectangle.getHeight() - height;
        r = new Rectangle(startX, startY, width, height);
        rectangleMapping.put(dm4, r);

        return rectangleMapping;
    }

}
