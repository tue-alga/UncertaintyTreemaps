/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.naming.spi.DirStateFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import utility.Pair;
import static utility.Precision.eq;

/**
 *
 * @author msondag
 */
public class ApproximationTreeMapUncertaintyV3IT {

    public ApproximationTreeMapUncertaintyV3IT() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getValidAdditions method, of class
     * ApproximationTreeMapUncertaintyV3.
     */
    @Test
    public void testGetValidAdditions() {
        System.out.println("getValidAdditions");
        double maxRatio = 3.0;
        ApproximationTreeMapUncertaintyV3 instance = new ApproximationTreeMapUncertaintyV3(maxRatio,true);
        Rectangle inputR = new Rectangle(0, 0, 0.5, 1);

        //test whether too large ratio works
        //l1 = 6
        DataMap dm1 = new DataMap("dm1", 2, 1, null, Color.yellow);
        DataMap dm2 = new DataMap("dm2", 2, 1, null, Color.yellow);
        DataMap dm3 = new DataMap("dm3", 2, 1, null, Color.yellow);

        //l2 = 14
        DataMap dm4 = new DataMap("dm4", 3, 1, null, Color.yellow);
        DataMap dm5 = new DataMap("dm5", 8, 1, null, Color.yellow);

        List<DataMap> list1 = Arrays.asList(dm1, dm2, dm3);
        List<DataMap> list2 = Arrays.asList(dm4, dm5);

        List<DataMap> expResult = Arrays.asList(dm4);//dm6 is not allowed due to too large diffence in ratios
        List<DataMap> result = instance.getValidAdditions(list1, list2, maxRatio, inputR, true);
        assertEquals(expResult, result);

        //test whether too large split rectangles work
        //l1 = 6
        dm1 = new DataMap("dm1", 2, 1, null, Color.yellow);
        dm2 = new DataMap("dm2", 2, 1, null, Color.yellow);
        dm3 = new DataMap("dm3", 2, 1, null, Color.yellow);

        //l2 = 6
        dm4 = new DataMap("dm4", 3, 1, null, Color.yellow);
        dm5 = new DataMap("dm5", 3, 1, null, Color.yellow);

        list1 = Arrays.asList(dm1, dm2, dm3);
        list2 = Arrays.asList(dm4, dm5);

        expResult = new ArrayList();
        result = instance.getValidAdditions(list1, list2, maxRatio, inputR, false);
        assertEquals(expResult, result);

        //test break not allowed
        //l1 = 18
        dm1 = new DataMap("dm1", 3, 1, null, Color.yellow);
        dm2 = new DataMap("dm2", 6, 1, null, Color.yellow);
        dm3 = new DataMap("dm3", 9, 1, null, Color.yellow);

        //l2 = 19
        dm4 = new DataMap("dm4", 3, 1, null, Color.yellow);
        dm5 = new DataMap("dm5", 6, 1, null, Color.yellow);
        DataMap dm6 = new DataMap("dm6", 10, 1, null, Color.yellow);

        list1 = Arrays.asList(dm1, dm2, dm3);
        list2 = Arrays.asList(dm4, dm5, dm6);

        result = instance.getValidAdditions(list1, list2, maxRatio, inputR, true);
        assertTrue(result.contains(dm4));
        assertTrue(result.size() == 1);

        //test singleton not removable
        //l1 = 9
        dm1 = new DataMap("dm1", 3, 1, null, Color.yellow);
        dm2 = new DataMap("dm2", 3, 1, null, Color.yellow);
        dm3 = new DataMap("dm3", 3, 1, null, Color.yellow);

        //l2 = 9
        dm4 = new DataMap("dm4", 9, 1, null, Color.yellow);

        list1 = Arrays.asList(dm1, dm2, dm3);
        list2 = Arrays.asList(dm4);

        result = instance.getValidAdditions(list1, list2, maxRatio, inputR, true);
        assertTrue(result.isEmpty());

        result = instance.getValidAdditions(new ArrayList(), list1, maxRatio, inputR, true);
        assertTrue(result.isEmpty());

        result = instance.getValidAdditions(list1, new ArrayList(), maxRatio, inputR, true);
        assertTrue(result.isEmpty());
    }

    /**
     * Test of sortDataMapsIncreasing method, of class
     * ApproximationTreeMapUncertaintyV3.
     */
    @Test
    public void testSortDataMapsIncreasing() {
        System.out.println("sortDataMapsIncreasing");

        List<DataMap> dataMaps = getSimpleDataMapList2();
        ApproximationTreeMapUncertaintyV3 instance = new ApproximationTreeMapUncertaintyV3(3,true);
        instance.sortDataMapsIncreasing(dataMaps);

        double lastSize = Double.MIN_VALUE;
        for (DataMap dm : dataMaps) {
            assertTrue(lastSize <= dm.getTargetSize());
            lastSize = dm.getTargetSize();
        }
    }

    /**
     * Test of sortDataMapsDecreasing method, of class
     * ApproximationTreeMapUncertaintyV3.
     */
    @Test
    public void testSortDataMapsDecreasing() {
        System.out.println("sortDataMapsDecreasing");
        List<DataMap> dataMaps = getSimpleDataMapList2();
        ApproximationTreeMapUncertaintyV3 instance = new ApproximationTreeMapUncertaintyV3(3,true);
        instance.sortDataMapsIncreasing(dataMaps);
        instance.sortDataMapsDecreasing(dataMaps);

        double lastSize = Double.MAX_VALUE;
        for (DataMap dm : dataMaps) {
            assertTrue(lastSize >= dm.getTargetSize());
            lastSize = dm.getTargetSize();
        }
    }

    /**
     * Test of sortDataMapsDecreasingUncertainty method, of class
     * ApproximationTreeMapUncertaintyV3.
     */
    @Test
    public void testSortDataMapsDecreasingUncertainty() {
        System.out.println("sortDataMapsDecreasingUncertainty");
        List<DataMap> dataMaps = getSimpleDataMapList2();
        ApproximationTreeMapUncertaintyV3 instance = new ApproximationTreeMapUncertaintyV3(3,true);
        instance.sortDataMapsDecreasingUncertainty(dataMaps);

        double lastSize = Double.MAX_VALUE;
        for (DataMap dm : dataMaps) {
            assertTrue(lastSize >= dm.getSd());
            lastSize = dm.getSd();
        }
    }

    /**
     * Test of maxRatio method, of class
     * ApproximationTreeMapUncertaintyV3.
     */
    @Test
    public void testGetMaxRatio() {
        System.out.println("getMaxRatio");
        List<DataMap> dataMaps = getSimpleDataMapList2();
        ApproximationTreeMapUncertaintyV3 instance = new ApproximationTreeMapUncertaintyV3(3,true);

        //verify normal behaviour
        double result = instance.getMaxRatio(dataMaps);
        double expResult = 10.0 / 1.0;
        assertEquals(expResult, result, 0.001);

        //verify different order
        instance.sortDataMapsDecreasing(dataMaps);
        expResult = 4.0 / 1.0;
        result = instance.getMaxRatio(dataMaps);
        assertEquals(expResult, result, 0.001);

        //no element
        expResult = Double.MAX_VALUE;
        result = instance.getMaxRatio(new ArrayList());
        assertEquals(expResult, result, 0.001);

        //single element
        expResult = 1;
        result = instance.getMaxRatio(Arrays.asList(dataMaps.get(0)));
        assertEquals(expResult, result, 0.001);

    }

    /**
     * Test of getSplitRectangles method, of class
     * ApproximationTreeMapUncertaintyV3.
     */
    @Test
    public void testGetSplitRectangles() {
        System.out.println("getSplitRectangles");

        List<DataMap> l1 = getSimpleDataMapList();//size = 19
        List<DataMap> l2 = getSimpleDataMapList2(); //size = 116
        Rectangle inputR = new Rectangle(0, 0, 1, 1);
        boolean horizontalSplit = false;
        ApproximationTreeMapUncertaintyV3 instance = new ApproximationTreeMapUncertaintyV3(3,true);

        Rectangle expR1 = new Rectangle(0, 0, 19.0 / 45.0, 1);
        Rectangle expR2 = new Rectangle(19.0 / 45, 0, 26.0 / 45.0, 1);

        Pair<Rectangle, Rectangle> result = instance.getSplitRectangles(l1, l2, inputR, horizontalSplit);
        assertRectangleEqual(expR1, result.x);
        assertRectangleEqual(expR2, result.y);
    }

    private void assertRectangleEqual(Rectangle r1, Rectangle r2) {
        if (!eq(r1.getX(), r2.getX())) {
            fail();
        }
        if (!eq(r1.getY(), r2.getY())) {
            fail();
        }
        if (!eq(r1.getWidth(), r2.getWidth())) {
            fail();
        }
        if (!eq(r1.getHeight(), r2.getHeight())) {
            fail();
        }
    }

    private List<DataMap> getSimpleDataMapList() {
        //size = 19
        List<DataMap> list = new ArrayList();
        DataMap dm1 = new DataMap("dm1", 2, 1, null, Color.yellow);
        DataMap dm2 = new DataMap("dm2", 2, 1, null, Color.yellow);
        DataMap dm3 = new DataMap("dm3", 2, 1, null, Color.yellow);
        DataMap dm4 = new DataMap("dm4", 2, 1, null, Color.yellow);
        DataMap dm5 = new DataMap("dm5", 2, 1, null, Color.yellow);
        DataMap dm6 = new DataMap("dm6", 3, 1, null, Color.yellow);
        DataMap dm7 = new DataMap("dm7", 3, 1, null, Color.yellow);
        DataMap dm8 = new DataMap("dm8", 3, 1, null, Color.yellow);
        list.addAll(Arrays.asList(dm1, dm2, dm3, dm4, dm5, dm6, dm7, dm8));
        return list;
    }

    private List<DataMap> getSimpleDataMapList2() {
        //suze = 35
        List<DataMap> list = new ArrayList();
        DataMap dm1 = new DataMap("dm1", 10, 1, null, Color.yellow);
        DataMap dm2 = new DataMap("dm2", 1, 1, null, Color.yellow);
        DataMap dm3 = new DataMap("dm3", 6, 2, null, Color.yellow);
        DataMap dm4 = new DataMap("dm4", 4, 1, null, Color.yellow);
        DataMap dm5 = new DataMap("dm5", 5, 1, null, Color.yellow);
        list.addAll(Arrays.asList(dm1, dm2, dm3, dm4, dm5));
        return list;
    }

}
