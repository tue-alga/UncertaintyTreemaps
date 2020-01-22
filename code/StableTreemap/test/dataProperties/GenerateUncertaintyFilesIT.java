/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataProperties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import treemap.DataFaciliation.DataFileManagerFast;

/**
 *
 * @author msondag
 */
public class GenerateUncertaintyFilesIT {

    public GenerateUncertaintyFilesIT() {
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
     * Test of getUncertainty method, of class GenerateUncertaintyFiles.
     */
    @Test
    public void testGetUncertainty() {
        System.out.println("getUncertainty");
        DataFileManagerFast df = new DataFileManagerFast("D:/Development/TreeMapUncertainty/datasets/RecheckedDatasets4-9-2019/uncertaintyTestCase.csv", false, true);
        GenerateUncertaintyFiles instance = new GenerateUncertaintyFiles();

        String result = instance.getUncertainty(df);
        //double are not precise so can't check for exact string
        double c1M = 3;
        double c1sd = 2;

        double c2M = 2.5;
        double c2sd = 0.5;

        double c3M = 2.5;
        double c3sd = 0.5;

        double c4M = 2.5;
        double c4sd = 1.5;

        double c5M = 2.5;
        double c5sd = 1.5;

        double h1M = 5.5;
        double h1sd = 2.5;

        double h2M = 7.5;
        double h2sd = 0.5;

        assertEquals(c1M, getMean("c1", result), 0.01);
        assertEquals(c1sd, getSd("c1", result), 0.01);

        assertEquals(c2M, getMean("c2", result), 0.01);
        assertEquals(c2sd, getSd("c2", result), 0.01);

        assertEquals(c3M, getMean("c3", result), 0.01);
        assertEquals(c3sd, getSd("c3", result), 0.01);

        assertEquals(c4M, getMean("c4", result), 0.01);
        assertEquals(c4sd, getSd("c4", result), 0.01);

        assertEquals(c5M, getMean("c5", result), 0.01);
        assertEquals(c5sd, getSd("c5", result), 0.01);

        assertEquals(h1M, getMean("h1", result), 0.01);
        assertEquals(h1sd, getSd("h1", result), 0.01);

        assertEquals(h2M, getMean("h2", result), 0.01);
        assertEquals(h2sd, getSd("h2", result), 0.01);        
    }

    private double getMean(String label, String uncertaintyString) {
        String[] rows = uncertaintyString.split("\n");
        for (String s : rows) {
            String[] values = s.split(",");
            if (values[0].equals(label)) {
                return Double.parseDouble(values[3]);
            }
        }
        fail("Label: " + label + " is not contained in uncertaintyString: " + uncertaintyString);
        throw new IllegalStateException();
    }

    private double getSd(String label, String uncertaintyString) {
        String[] rows = uncertaintyString.split("\n");
        for (String s : rows) {
            String[] values = s.split(",");
            if (values[0].equals(label)) {
                return Double.parseDouble(values[4]);
            }
        }
        fail("Label: " + label + "is not contained in uncertaintyString: " + uncertaintyString);
        throw new IllegalStateException();
    }
}
