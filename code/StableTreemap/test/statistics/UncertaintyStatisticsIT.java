/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package statistics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author msondag
 */
public class UncertaintyStatisticsIT {

    public UncertaintyStatisticsIT() {
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
     * Test of getPenalty method, of class UncertaintyStatistics.
     */
    @Test
    public void testGetPenalty() {
        System.out.println("getPenalty");
        Rectangle childR = new Rectangle(0.0, 0.0, 2.0, 2.0);
        Rectangle childSd = new Rectangle(0.0, 0.0, 2.0, 0.4);
        Rectangle parentSdR = new Rectangle(0.0, 0.0, 2.0, 0.3);
        UncertaintyStatistics instance = new UncertaintyStatistics(true);
        double expResult = 0.0;
        double result = instance.getPenalty(childR, childSd, parentSdR);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        parentSdR = new Rectangle(0.0, 0.0, 2.0, 0.5);
        expResult = 0.05;
        result = instance.getPenalty(childR, childSd, parentSdR);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of getOverlapScores method, of class UncertaintyStatistics.
     */
    @Test
    public void testGetOverlapScores() {
        System.out.println("getOverlapScores");
        TreeMap c11 = new TreeMap(new Rectangle(0, 0, 1, 1), "c11", Color.yellow, 1, 0.6, null);
        TreeMap c12 = new TreeMap(new Rectangle(1, 0, 1, 1), "c12", Color.yellow, 1, 0.6, null);
        TreeMap c13 = new TreeMap(new Rectangle(0, 1, 2, 1), "c13", Color.yellow, 2, 0.8, null);
        TreeMap c2 = new TreeMap(new Rectangle(0, 2, 2, 2), "c2", Color.yellow, 4, 2.5, null);
        TreeMap c1 = new TreeMap(new Rectangle(0, 0, 2, 2), "c1", Color.yellow, 4, 2, Arrays.asList(c11, c12, c13));
        TreeMap root = new TreeMap(new Rectangle(0, 0, 2, 4), "root", Color.yellow, 8, 3, Arrays.asList(c1, c2));

        UncertaintyStatistics instance = new UncertaintyStatistics(true);

        List<Double> result = instance.getOverlapScores(c11, c1);
        assertEquals(0.0,result.get(0), 0.001);
        
        result = instance.getOverlapScores(c12, c1);
        assertEquals(0.0,result.get(0), 0.001);
        
        result = instance.getOverlapScores(c13, c1);
        assertEquals(0.6,result.get(0), 0.001);
        
        result = instance.getOverlapScores(c2, root);
        assertEquals(0.125,result.get(0), 0.001);

        result = instance.getOverlapScores(c1, root);
        assertTrue(result.size() == 4);
        
        result = instance.getOverlapScores(root,null);
        assertTrue(result.size() == 5);

    }
//
//      /**
//     * Test of getOverlapScore method, of class UncertaintyStatistics.
//     */
//    @Test
//    public void testGetOverlapScore() {
//        System.out.println("getOverlapScore");
//        TreeMap c11 = new TreeMap(new Rectangle(0, 0, 1, 1), "c11", Color.yellow, 1, 0.6, null);
//        TreeMap c12 = new TreeMap(new Rectangle(1, 0, 1, 1), "c12", Color.yellow, 1, 0.6, null);
//        TreeMap c13 = new TreeMap(new Rectangle(0, 1, 2, 1), "c13", Color.yellow, 2, 0.8, null);
//        TreeMap c2 = new TreeMap(new Rectangle(0, 2, 2, 2), "c2", Color.yellow, 4, 2.5, null);
//        TreeMap c1 = new TreeMap(new Rectangle(0, 0, 2, 2), "c1", Color.yellow, 4, 2, Arrays.asList(c11, c12, c13));
//        TreeMap root = new TreeMap(new Rectangle(0, 0, 2, 4), "root", Color.yellow, 8, 3, Arrays.asList(c1, c2));
//        
//        
//        UncertaintyStatistics instance = new UncertaintyStatistics(true);
//        double expResult = (0.0+0.0+0.6+0.125+0.0);
//        double result = instance.getOverlapScore(root);
//        assertEquals(expResult, result, 0.001);
//    }
    
}
