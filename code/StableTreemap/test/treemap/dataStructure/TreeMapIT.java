/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package treemap.dataStructure;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author msondag
 */
public class TreeMapIT {

    public TreeMapIT() {
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
     * Test of getSdRectangle method, of class TreeMap.
     */
    @Test
    public void testGetSdRectangle() {
        System.out.println("getSdRectangle");

        //test normal
        TreeMap instance = new TreeMap(new Rectangle(0, 0, 1, 1), "root", Color.yellow, 1, 0.2, null);
        Rectangle expResult = new Rectangle(0, 0.8, 1, 0.2);
        Rectangle result = instance.getSdRectangle();
        testRectangleEqual(expResult,result);
        //test overflow
        
        instance = new TreeMap(new Rectangle(0, 0, 1, 1), "root", Color.yellow, 1, 2, null);
        expResult = new Rectangle(0, 0, 1, 1);
        result = instance.getSdRectangle();
        testRectangleEqual(expResult,result);
    }

    
    private void testRectangleEqual(Rectangle r1, Rectangle r2){
        assertEquals(r1.getX(),r2.getX(),0.001);
        assertEquals(r1.getX2(),r2.getX2(),0.001);
        assertEquals(r1.getY(),r2.getY(),0.001);
        assertEquals(r1.getY2(),r2.getY2(),0.001);
    }
}
