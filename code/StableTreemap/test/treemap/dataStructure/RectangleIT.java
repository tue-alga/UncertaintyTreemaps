/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package treemap.dataStructure;

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
public class RectangleIT {

    public RectangleIT() {
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
     * Test of intersection method, of class Rectangle.
     */
    @Test
    public void testIntersection() {
        System.out.println("intersection");
        Rectangle r1 = new Rectangle(0,0,100,100);
        Rectangle r2 = new Rectangle(20,70,30,30);
        Rectangle expResult = new Rectangle(20,70,30,30);
        Rectangle result = r1.intersection(r2);
        testRectangleEqual(expResult, result);
        
        //invert should not matter
        result = r2.intersection(r1);
        testRectangleEqual(expResult, result);
        
        //partial overlap
        r2 = new Rectangle(50,50,100,100);
        expResult = new Rectangle(50,50,50,50);
        result = r1.intersection(r2);
        testRectangleEqual(expResult, result);
        
        //invert should not matter
        result = r2.intersection(r1);
        testRectangleEqual(expResult, result);
        
        //check decimals
        r2 = new Rectangle(50.3,50.3,100.1,100.1);
        expResult = new Rectangle(50.3,50.3,49.7,49.7);
        result = r1.intersection(r2);
        testRectangleEqual(expResult, result);
        
        //invert should not matter
        result = r2.intersection(r1);
        testRectangleEqual(expResult, result);
        
    }

    private void testRectangleEqual(Rectangle r1, Rectangle r2) {
        assertEquals(r1.getX(), r2.getX(), 0.001);
        assertEquals(r1.getX2(), r2.getX2(), 0.001);
        assertEquals(r1.getY(), r2.getY(), 0.001);
        assertEquals(r1.getY2(), r2.getY2(), 0.001);
    }
}
