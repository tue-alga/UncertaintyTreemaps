package UserControl.Visualiser;

import TreeMapGenerator.ApproximationTreeMap;
import TreeMapGenerator.ApproximationTreeMapUncertainty;
import TreeMapGenerator.ApproximationTreeMapUncertaintyV3;
import TreeMapGenerator.HilbertMoore.HilbertTreeMap;
import TreeMapGenerator.HilbertMoore.MooreTreeMap;
import TreeMapGenerator.LocalChanges.TreeMapChangeGenerator;
import TreeMapGenerator.LocalMoves.LocalMoves;
import TreeMapGenerator.LocalMoves.LocalMovesUncertainty;
import TreeMapGenerator.Pivot.PivotByMiddle;
import TreeMapGenerator.Pivot.PivotBySize;
import TreeMapGenerator.Pivot.PivotBySplit;
import TreeMapGenerator.SliceAndDice;
import TreeMapGenerator.SpiralTreeMap;
import TreeMapGenerator.SpiralTreeMapLookAhead;
import TreeMapGenerator.SplitTreeMap;
import TreeMapGenerator.SplitTreeMapUncertainty;
import TreeMapGenerator.SquarifiedTreeMap;
import TreeMapGenerator.SquarifiedTreeMapLookAhead;
import TreeMapGenerator.SquarifiedTreeMapLookAheadUncertainty;
import TreeMapGenerator.SquarifiedTreeMapUncertainty;
import TreeMapGenerator.StripTreeMap;
import TreeMapGenerator.StripTreeMapLookAhead;
import TreeMapGenerator.StripTreeMapLookAheadUncertainty;
import TreeMapGenerator.StripTreeMapUncertainty;
import TreeMapGenerator.TreeMapGenerator;
import java.awt.Color;
import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import statistics.Baseline.BaseLineGenerator;
import statistics.Baseline.TreeMapReader;
import statistics.Stability.RelativeQuadrantStability;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.DataFileManager;
import treemap.DataFaciliation.DataFileManagerFast;
import treemap.DataFaciliation.Generators.RandomDataGenerator;
import treemap.DataFaciliation.Generators.RandomSequentialDataGenerator;
import treemap.DataFaciliation.Generators.RandomLogNormalDataGenerator;
import treemap.DataFaciliation.Generators.RandomLogNormalSequentialDataGenerator;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public final class GUI extends javax.swing.JFrame {

    private TreeMapVisualisation treeMapVisualisation;
    private Visualiser visualiser;

    //Variable indicating if a change in time was due to a simulation step
    private boolean simulationStep = false;

    //Whether we are currently running a simulation
    private boolean simulationRunning = false;

    //whether we are currently running an experiment
    private boolean experimentRunning = false;

    Timer simulationTimer = new Timer();

    //The time to wait after a simulation step
    private volatile int simulationSpeed = 500;
    private boolean useStored = false;

    //True when we have loaded rectangle file from an old generation
    private boolean showOldData = false;

    //Ensures that we only allows listeners to respond after full initialization.
    private boolean initialized = false;

    /**
     * Creates new form GUI
     */
    public GUI(Visualiser visualiser) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        this.visualiser = visualiser;
        initComponents();
        initTreeMapVisualistion();

        String[] facilitators = new String[]{"Random", "ExampleUncertainty", "DefaultExampleUncertainty", "slides", "trumpUncertainty", "cesUncertainty", "InfantDeath", "InfantDeathUncertainty", "Coffee", "CoffeeUncertainty", "testCase"};
        dataFacilitatorSelector.setModel(new DefaultComboBoxModel(facilitators));
        dataFacilitatorSelector.setSelectedIndex(7);
        setDataFacilitator("InfantDeathUncertainty");

        String[] generators = new String[]{"SliceAndDice", "PivotByMiddle", "PivotBySize", "PivotBySplitSize", "Strip", "StripUncertainty", "StripLookAhead", "StripLookAheadUncertainty", "Split", "SplitUncertainty", "Squarified", "SquarifiedUncertainty", "SquarifiedLookAhead", "SquarifiedLookAheadUncertainty", "Spiral", "SpiralLookAhead", "Moore", "Hilbert", "Approximation", "ApproximationUncertainty", "3.0-ApproximationUncertaintyV3", "5.0-ApproximationUncertaintyV3", "3.0%-ApproximationUncertaintyV3", "5.0%-ApproximationUncertaintyV3", "Incremental", "LocalMovesUncertainty", "NoMovesIncremental"};
        treeMapSelector.setModel(new DefaultComboBoxModel(generators));
        treeMapSelector.setSelectedIndex(15);
        setTreeMapGenerator("ApproximationUncertainty");

        String[] uncertainty = new String[]{"Bars", "CheckerBoard", "Diagonal", "DiagonalOutline"};
        uncertaintySelector.setModel(new DefaultComboBoxModel(uncertainty));
        uncertaintySelector.setSelectedIndex(2);

        setVisible(true);
        treeMapVisualisation.updateTreeMapRectangle();
        Rectangle treeMapRectangle = treeMapVisualisation.getTreemapRectangle();
        visualiser.setTreeMapRectangle(treeMapRectangle);
        initialized = true;

    }

    public void setDataFacilitator(String identifier) {
        int minItems = (int) minItemSpinner.getValue();
        int maxItems = (int) maxItemSpinner.getValue();
        int minDepth = (int) minDepthSpinner.getValue();
        int maxDepth = (int) maxDepthSpinner.getValue();
        int minSize = (int) minSizeSpinner.getValue();
        int maxSize = (int) maxSizeSpinner.getValue();
        int changeValue = (int) changeValueSpinner.getValue();
        int changeChance = (int) chanceChanceSpinner.getValue();
        int time = (int) timeSpinner.getValue();

        DataFacilitator df = null;
        switch (identifier) {
            case "Random":
                df = new RandomDataGenerator(minItems, maxItems, minDepth, maxDepth, minSize, maxSize);
                break;
//            case "SeqRandom":
//                df = new RandomSequentialDataGenerator(minItems, maxItems, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time);
//                break;
//            case "RandomLogNormal":
//                df = new RandomLogNormalDataGenerator(minItems, maxItems);
//                break;
//            case "SeqRandomLogNormal":
//                df = new RandomLogNormalSequentialDataGenerator(minItems, maxItems, time);
//                break;
            case "trumpUncertainty":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/trumpUncertainty.csv", true, true);
                break;
//            case "merchandise":
//                df = new DataFileManagerFast(new File("").getAbsolutePath() + "\\datasets\\MerchandiseTrade.csv", true, true);
//                break;
            case "cesUncertainty":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/cesTrimmed_2014Uncertainty.csv", true, true);
                break;
            case "InfantDeath":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/InfantDeath25Years.csv", false, true);
                break;
            case "InfantDeathUncertainty":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/InfantDeath25YearsUncertainty.csv", true, true);
//                System.out.println(df.getData(0).toJson());
                break;
            case "ExampleUncertainty":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/examplePropagation.csv", true, true);
                break;
            case "DefaultExampleUncertainty":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/examplePropagationDefault.csv", true, true);
                break;
            case "CoffeeUncertainty":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/UnSchemeCoffeeUncertaintyColored.csv", true, true);
//                System.out.println(df.getData(0).toJson());
                break;
            case "Coffee":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/UnSchemeCoffee.csv", false, true);
                break;
            case "testCase":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/uncertaintyTestCase.csv", true, true);
                break;
            case "slides":
                df = new DataFileManagerFast(new File("").getAbsolutePath() + "/datasets/RecheckedDatasets4-9-2019/slides.csv", true, true);
                break;
        }
        visualiser.setDataFacilitator(df);
    }

    public void setTreeMapGenerator(String identifier) {
        TreeMapGenerator tmg = null;
        switch (identifier) {
            case "SliceAndDice":
                tmg = new SliceAndDice();
                break;
            case "PivotByMiddle":
                tmg = new PivotByMiddle();
                break;
            case "PivotBySize":
                tmg = new PivotBySize();
                break;
            case "PivotBySplitSize":
                tmg = new PivotBySplit();
                break;
            case "Strip":
                tmg = new StripTreeMap();
                break;
            case "StripUncertainty":
                tmg = new StripTreeMapUncertainty();
                break;
            case "StripLookAhead":
                tmg = new StripTreeMapLookAhead();
                break;
            case "StripLookAheadUncertainty":
                tmg = new StripTreeMapLookAheadUncertainty();
                break;
            case "Split":
                tmg = new SplitTreeMap();
                break;
            case "SplitUncertainty":
                tmg = new SplitTreeMapUncertainty();
                break;
            case "Squarified":
                tmg = new SquarifiedTreeMap();
                break;
            case "SquarifiedUncertainty":
                tmg = new SquarifiedTreeMapUncertainty();
                break;
            case "SquarifiedLookAhead":
                tmg = new SquarifiedTreeMapLookAhead();
                break;
            case "SquarifiedLookAheadUncertainty":
                tmg = new SquarifiedTreeMapLookAheadUncertainty();
                break;
            case "Spiral":
                tmg = new SpiralTreeMap();
                break;
            case "SpiralLookAhead":
                tmg = new SpiralTreeMapLookAhead();
                break;
            case "Moore":
                tmg = new MooreTreeMap();
                break;
            case "Hilbert":
                tmg = new HilbertTreeMap();
                break;
            case "Approximation":
                tmg = new ApproximationTreeMap();
                break;
            case "ApproximationUncertainty":
                tmg = new ApproximationTreeMapUncertainty();
                break;
            case "3.0%-ApproximationUncertaintyV3":
                tmg = new ApproximationTreeMapUncertaintyV3(3.0, true);
                break;
            case "5.0%-ApproximationUncertaintyV3":
                tmg = new ApproximationTreeMapUncertaintyV3(5.0, true);
                break;
            case "3.0-ApproximationUncertaintyV3":
                tmg = new ApproximationTreeMapUncertaintyV3(3.0, false);
                break;
            case "5.0-ApproximationUncertaintyV3":
                tmg = new ApproximationTreeMapUncertaintyV3(5.0, false);
                break;
            case "Incremental":
                tmg = new LocalMoves(false);
                break;
            case "LocalMovesUncertainty":
                tmg = new LocalMovesUncertainty(false);
                break;
            case "NoMovesIncremental":
                tmg = new LocalMoves(true);
                break;
        }
        System.out.println(visualisationPanel.getSize());
        visualiser.setTreeMapGenerator(tmg);
        getNewTreeMap();
    }

    public void updateTreeMap(TreeMap treeMap) {
        treeMapVisualisation.updateTreeMap(treeMap);
        setAspectRatioBeforeMoves(treeMap.getAverageAspectRatio());

        int time = (int) timeSpinner.getValue();
        if ("F:\\Development\\Treemap\\datasets\\PopularNamesSinceBirth.csv".equals(visualiser.getDataFacilitator().getDataIdentifier()));
        {
            int year = 2014 - time;
//            Title.setText("" + year);
        }
    }

    private void initTreeMapVisualistion() {
        treeMapVisualisation = new TreeMapVisualisation();

        visualisationPanel.add(treeMapVisualisation);

        treeMapVisualisation.setBackground(Color.WHITE);
        treeMapVisualisation.setSize(visualisationPanel.getSize());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        visualisationPanel = new javax.swing.JPanel();
        dataFacilitatorSelector = new javax.swing.JComboBox();
        treeMapSelector = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        timeSpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        animationSpeedSlider = new javax.swing.JSlider();
        drawWeightCheckBox = new javax.swing.JCheckBox();
        minItemSpinner = new javax.swing.JSpinner();
        maxItemSpinner = new javax.swing.JSpinner();
        minItemsLabel = new javax.swing.JLabel();
        maxItemsLabel = new javax.swing.JLabel();
        minDepthLabel = new javax.swing.JLabel();
        minDepthSpinner = new javax.swing.JSpinner();
        maxDepthLabel = new javax.swing.JLabel();
        maxDepthSpinner = new javax.swing.JSpinner();
        minSizeLabel = new javax.swing.JLabel();
        maxSizeLabel = new javax.swing.JLabel();
        maxSizeSpinner = new javax.swing.JSpinner();
        minSizeSpinner = new javax.swing.JSpinner();
        changeValueLabel = new javax.swing.JLabel();
        changeValueSpinner = new javax.swing.JSpinner();
        chanceChanceLabel = new javax.swing.JLabel();
        chanceChanceSpinner = new javax.swing.JSpinner();
        orderEnabledCheckBox = new javax.swing.JCheckBox();
        orderLabelsEnabledCheckBox = new javax.swing.JCheckBox();
        simulationButton = new javax.swing.JButton();
        animationEnabledBox1 = new javax.swing.JCheckBox();
        jLabel16 = new javax.swing.JLabel();
        meanAspectRatio = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        simulationSpeedSlider = new javax.swing.JSlider();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        buttonShowNewGenerated = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0));
        jLabel19 = new javax.swing.JLabel();
        stabilityRelativeValue = new javax.swing.JLabel();
        useStoredButton = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        performMoveButton = new javax.swing.JButton();
        baseLineButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jCheckBoxUncertainty = new javax.swing.JCheckBox();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        baseSeperationSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        baseLuminanceSpinner = new javax.swing.JSpinner();
        luminanceAdditionSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        baseWidthSpinner = new javax.swing.JSpinner();
        widthAddSpinner = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        uncertaintySelector = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        uncertaintyAllPercentageValue = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        uncertaintyAllAreaValue = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        uncertaintyPercentageValue = new javax.swing.JLabel();
        uncertaintyAreaValue = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        sdTreemapButton = new javax.swing.JButton();
        drawUncertaintyCheckBox = new javax.swing.JCheckBox();
        sdAsDataButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        visualisationPanel.setBackground(new java.awt.Color(255, 0, 0));
        visualisationPanel.setName(""); // NOI18N
        visualisationPanel.setPreferredSize(new java.awt.Dimension(640, 320));
        visualisationPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                visualisationPanelComponentResized(evt);
            }
        });

        javax.swing.GroupLayout visualisationPanelLayout = new javax.swing.GroupLayout(visualisationPanel);
        visualisationPanel.setLayout(visualisationPanelLayout);
        visualisationPanelLayout.setHorizontalGroup(
            visualisationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1920, Short.MAX_VALUE)
        );
        visualisationPanelLayout.setVerticalGroup(
            visualisationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1080, Short.MAX_VALUE)
        );

        dataFacilitatorSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataFacilitatorSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dataFacilitatorSelectorItemStateChanged(evt);
            }
        });

        treeMapSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        treeMapSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                treeMapSelectorItemStateChanged(evt);
            }
        });

        jLabel2.setText("Time");

        jLabel3.setText("Generation Method");

        jLabel4.setText("Facilitator");

        timeSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        timeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                timeSpinnerStateChanged(evt);
            }
        });

        jLabel5.setText("Animation speed");

        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                animationSpeedSliderStateChanged(evt);
            }
        });

        drawWeightCheckBox.setText("Draw weights");
        drawWeightCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                drawWeightCheckBoxStateChanged(evt);
            }
        });
        drawWeightCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawWeightCheckBoxActionPerformed(evt);
            }
        });

        minItemSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, null, 1));
        minItemSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minItemSpinnerStateChanged(evt);
            }
        });

        maxItemSpinner.setModel(new javax.swing.SpinnerNumberModel(4, 1, null, 1));
        maxItemSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxItemSpinnerStateChanged(evt);
            }
        });

        minItemsLabel.setText("minItems");

        maxItemsLabel.setText("maxItems");

        minDepthLabel.setText("minDepth");

        minDepthSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, null, 1));
        minDepthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minDepthSpinnerStateChanged(evt);
            }
        });

        maxDepthLabel.setText("maxDepth");

        maxDepthSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, null, 1));
        maxDepthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxDepthSpinnerStateChanged(evt);
            }
        });

        minSizeLabel.setText("minSize");

        maxSizeLabel.setText("maxSize");

        maxSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(40, 0, null, 1));
        maxSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxSizeSpinnerStateChanged(evt);
            }
        });

        minSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        minSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minSizeSpinnerStateChanged(evt);
            }
        });

        changeValueLabel.setText("changeValue");

        changeValueSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 1, null, 1));
        changeValueSpinner.setAutoscrolls(true);
        changeValueSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                changeValueSpinnerStateChanged(evt);
            }
        });

        chanceChanceLabel.setText("chanceChance");

        chanceChanceSpinner.setModel(new javax.swing.SpinnerNumberModel(50, 0, null, 1));
        chanceChanceSpinner.setMinimumSize(new java.awt.Dimension(40, 20));
        chanceChanceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chanceChanceSpinnerStateChanged(evt);
            }
        });

        orderEnabledCheckBox.setText("Show order-equivalance relations");
        orderEnabledCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                orderEnabledCheckBoxStateChanged(evt);
            }
        });

        orderLabelsEnabledCheckBox.setText("Show order-equivalance labels");
        orderLabelsEnabledCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                orderLabelsEnabledCheckBoxStateChanged(evt);
            }
        });
        orderLabelsEnabledCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderLabelsEnabledCheckBoxActionPerformed(evt);
            }
        });

        simulationButton.setText("Start simulation");
        simulationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulationButtonActionPerformed(evt);
            }
        });

        animationEnabledBox1.setSelected(true);
        animationEnabledBox1.setText("Animation enabled");
        animationEnabledBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                animationEnabledBox1StateChanged(evt);
            }
        });
        animationEnabledBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                animationEnabledBox1ActionPerformed(evt);
            }
        });

        jLabel16.setText("Mean aspect ratio");

        meanAspectRatio.setText("Undefined");

        jLabel18.setText("Simulation speed");

        simulationSpeedSlider.setPaintTicks(true);
        simulationSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                simulationSpeedSliderStateChanged(evt);
            }
        });

        buttonShowNewGenerated.setText("Show new generation");
        buttonShowNewGenerated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShowNewGeneratedActionPerformed(evt);
            }
        });

        jLabel19.setText("StabilityRelative");

        stabilityRelativeValue.setText("Undefined");

        useStoredButton.setText("Use stored");
        useStoredButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                useStoredButtonStateChanged(evt);
            }
        });
        useStoredButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useStoredButtonActionPerformed(evt);
            }
        });

        jButton1.setText("Export");
        jButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jButton1StateChanged(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        performMoveButton.setText("Perform Move");
        performMoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performMoveButtonActionPerformed(evt);
            }
        });

        baseLineButton.setText("baseLine");
        baseLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseLineButtonActionPerformed(evt);
            }
        });

        jButton2.setText("load data");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCheckBoxUncertainty.setSelected(true);
        jCheckBoxUncertainty.setText("Show Uncertainty");

        baseSeperationSpinner.setModel(new javax.swing.SpinnerNumberModel(4, 1, null, 1));
        baseSeperationSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                baseSeperationSpinnerStateChanged(evt);
            }
        });

        jLabel1.setText("Base Seperation");

        jLabel7.setText("Base luminance");

        baseLuminanceSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 100.0d, 1.0d));
        baseLuminanceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                baseLuminanceSpinnerStateChanged(evt);
            }
        });

        luminanceAdditionSpinner.setModel(new javax.swing.SpinnerNumberModel(30.0d, -100.0d, 100.0d, 1.0d));
        luminanceAdditionSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                luminanceAdditionSpinnerStateChanged(evt);
            }
        });

        jLabel8.setText("Lum add");

        baseWidthSpinner.setModel(new javax.swing.SpinnerNumberModel(8.0d, 0.1d, null, 0.1d));
        baseWidthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                baseWidthSpinnerStateChanged(evt);
            }
        });

        widthAddSpinner.setModel(new javax.swing.SpinnerNumberModel(2, 2, null, 1));
        widthAddSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthAddSpinnerStateChanged(evt);
            }
        });

        jLabel9.setText("Base width");

        jLabel10.setText("Width add");

        uncertaintySelector.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        uncertaintySelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                uncertaintySelectorItemStateChanged(evt);
            }
        });

        jLabel20.setText("UncertaintyAll%");

        uncertaintyAllPercentageValue.setText("Undefined");

        jCheckBox1.setText("Show labels");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel21.setText("UncertaintyAllA");

        uncertaintyAllAreaValue.setText("Undefined");

        jLabel22.setText("Uncertainty%");

        uncertaintyPercentageValue.setText("Undefined");

        uncertaintyAreaValue.setText("Undefined");

        jLabel23.setText("UncertaintyA");

        sdTreemapButton.setText("SdTreemap");
        sdTreemapButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sdTreemapButtonActionPerformed(evt);
            }
        });

        drawUncertaintyCheckBox.setSelected(true);
        drawUncertaintyCheckBox.setText("DrawUncertainty");
        drawUncertaintyCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                drawUncertaintyCheckBoxStateChanged(evt);
            }
        });
        drawUncertaintyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawUncertaintyCheckBoxActionPerformed(evt);
            }
        });

        sdAsDataButton.setText("SdAsData");
        sdAsDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sdAsDataButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(visualisationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 1920, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(111, 315, Short.MAX_VALUE)
                        .addComponent(filler4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(treeMapSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel5)
                                    .addComponent(dataFacilitatorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(animationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(simulationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(performMoveButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addGap(29, 29, 29)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(minDepthLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(minDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(maxDepthLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxDepthSpinner))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(minSizeLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(minSizeSpinner))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(maxSizeLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxSizeSpinner))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(changeValueLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(changeValueSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(chanceChanceLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(chanceChanceSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(minItemsLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(minItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(maxItemsLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(maxItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(orderEnabledCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(orderLabelsEnabledCheckBox)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox1))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(buttonShowNewGenerated)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(simulationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(baseLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(6, 6, 6)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(drawWeightCheckBox)
                                        .addComponent(animationEnabledBox1)
                                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBoxUncertainty)
                                    .addComponent(useStoredButton)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(baseWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(widthAddSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(42, 42, 42)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(luminanceAdditionSpinner)
                                    .addComponent(baseSeperationSpinner)
                                    .addComponent(baseLuminanceSpinner)))
                            .addComponent(uncertaintySelector, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16)
                                    .addComponent(jLabel19)
                                    .addComponent(jLabel20))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(stabilityRelativeValue)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel22)
                                        .addGap(18, 18, 18)
                                        .addComponent(uncertaintyPercentageValue))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(uncertaintyAllPercentageValue)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel23)
                                        .addGap(18, 18, 18)
                                        .addComponent(uncertaintyAreaValue))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(meanAspectRatio)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel21)
                                        .addGap(18, 18, 18)
                                        .addComponent(uncertaintyAllAreaValue))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sdTreemapButton, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sdAsDataButton, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(drawUncertaintyCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(filler3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {chanceChanceLabel, changeValueLabel, maxDepthLabel, maxItemsLabel, maxSizeLabel, minDepthLabel, minItemsLabel, minSizeLabel});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {chanceChanceSpinner, changeValueSpinner, maxDepthSpinner, maxSizeSpinner, minSizeSpinner});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(filler3, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(visualisationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 1080, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(16, 16, 16)
                                                .addComponent(treeMapSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel4))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(76, 76, 76)
                                                .addComponent(dataFacilitatorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(minItemsLabel)
                                                    .addComponent(minItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(maxItemsLabel)
                                                    .addComponent(maxItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(4, 4, 4)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(minDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(minDepthLabel))))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(timeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(simulationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(performMoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(animationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel18)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(simulationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(maxDepthLabel)
                                                    .addComponent(maxDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(minSizeLabel)
                                                    .addComponent(minSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(maxSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(maxSizeLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(changeValueLabel)
                                                    .addComponent(changeValueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(chanceChanceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(chanceChanceLabel))
                                                .addGap(3, 3, 3)
                                                .addComponent(animationEnabledBox1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(drawWeightCheckBox)
                                                .addGap(0, 0, 0)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jButton2)
                                                    .addComponent(baseLineButton))))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(sdTreemapButton)
                                    .addComponent(sdAsDataButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(drawUncertaintyCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(uncertaintySelector, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(168, 168, 168)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonShowNewGenerated)
                            .addComponent(jButton1))
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(orderEnabledCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(orderLabelsEnabledCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel21)
                                .addComponent(uncertaintyAllAreaValue))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel16)
                                .addComponent(meanAspectRatio)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel22)
                                .addComponent(uncertaintyPercentageValue))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel19)
                                .addComponent(stabilityRelativeValue)))
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel23)
                                .addComponent(uncertaintyAreaValue))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel20)
                                .addComponent(uncertaintyAllPercentageValue)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxUncertainty)
                            .addComponent(baseSeperationSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(useStoredButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(baseLuminanceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(luminanceAdditionSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(baseWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(widthAddSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(0, 0, 0)
                .addComponent(filler4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {chanceChanceSpinner, changeValueSpinner, maxDepthSpinner, maxItemSpinner, maxSizeSpinner, minDepthSpinner, minItemSpinner, minSizeSpinner});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {chanceChanceLabel, changeValueLabel, maxDepthLabel, maxItemsLabel, maxSizeLabel, minDepthLabel, minItemsLabel, minSizeLabel});

        visualisationPanel.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void timeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_timeSpinnerStateChanged
        if (showOldData == true) {
            showLoadedData();
            return;
        }
        if (!simulationStep) {
            getNewTreeMap();
        }
        simulationStep = false;
    }//GEN-LAST:event_timeSpinnerStateChanged

    private void visualisationPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_visualisationPanelComponentResized
        if (treeMapVisualisation != null) {
            treeMapVisualisation.setSize(visualisationPanel.getSize());
            treeMapVisualisation.updateTreeMapRectangle();
            visualiser.setTreeMapRectangle(treeMapVisualisation.getTreemapRectangle());
            if (treeMapVisualisation.isShowingTreeMap()) {
                getNewTreeMap();
            }
        }
    }//GEN-LAST:event_visualisationPanelComponentResized

    private void treeMapSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_treeMapSelectorItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            setTreeMapGenerator((String) treeMapSelector.getSelectedItem());
        }
    }//GEN-LAST:event_treeMapSelectorItemStateChanged

    private void dataFacilitatorSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dataFacilitatorSelectorItemStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());        // TODO add your handling code here:
    }//GEN-LAST:event_dataFacilitatorSelectorItemStateChanged

    private void animationSpeedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_animationSpeedSliderStateChanged
        int sliderValue = animationSpeedSlider.getValue();
        treeMapVisualisation.setAnimationSpeed(sliderValue);
    }//GEN-LAST:event_animationSpeedSliderStateChanged

    private void drawWeightCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_drawWeightCheckBoxStateChanged

    }//GEN-LAST:event_drawWeightCheckBoxStateChanged


    private void minItemSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minItemSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_minItemSpinnerStateChanged

    private void maxItemSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxItemSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_maxItemSpinnerStateChanged

    private void minSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minSizeSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_minSizeSpinnerStateChanged

    private void maxDepthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxDepthSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_maxDepthSpinnerStateChanged

    private void minDepthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minDepthSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_minDepthSpinnerStateChanged

    private void maxSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxSizeSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_maxSizeSpinnerStateChanged

    private void changeValueSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeValueSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_changeValueSpinnerStateChanged

    private void chanceChanceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chanceChanceSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_chanceChanceSpinnerStateChanged

    private void orderEnabledCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_orderEnabledCheckBoxStateChanged
    }//GEN-LAST:event_orderEnabledCheckBoxStateChanged

    private void orderLabelsEnabledCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_orderLabelsEnabledCheckBoxStateChanged
    }//GEN-LAST:event_orderLabelsEnabledCheckBoxStateChanged

    private void simulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulationButtonActionPerformed
        if (visualiser == null || !visualiser.isInitialized() && treeMapVisualisation != null && !treeMapVisualisation.isShowingTreeMap()) {
            return;
        }
        if (simulationRunning) {
            stopSimulation();
            simulationButton.setText("Start simulation");
        } else {
            startSimulation();
            simulationButton.setText("Stop simulation");
        }
        simulationRunning = !simulationRunning;
    }//GEN-LAST:event_simulationButtonActionPerformed

    private Thread simulationThread;
    private volatile boolean simulationThreadStop;

    private void startSimulation() {
        //disallow changes to the timespinner while the simulation is running
        timeSpinner.setEnabled(false);
        int currentTime = (int) timeSpinner.getValue();
        //get a new initial treeMap
        //if (!treeMapVisualisation.isShowingTreeMap()) {
        visualiser.getTreeMap(currentTime, useStored, "");
//        }

        simulationThreadStop = false;
        //TODO start both of these after the animation has finished.
        simulationThread = new Thread(new Runnable() {

            @Override
            public void run() {
                runSimulation();
            }

        });

        simulationThread.start();
    }

    private void runSimulation() {
//        try {
        while (!simulationThreadStop) {
            //letting the spinner know that the change was coming from the simulation button
            simulationStep = true;
            int currentTime = (int) timeSpinner.getValue();

            int newTime = currentTime + 1;
            timeSpinner.setValue(newTime);

            System.out.print("\tnewTime = " + newTime + "\t");
            getNewTreeMap();
            treeMapVisualisation.toIpe("ipe//ipeFromTime_" + newTime + ".ipe");

//                while (treeMapVisualisation.treeMapRepaint == false) {
//                    Thread.sleep(50);
//                }
//                Thread.sleep(simulationSpeed);
//                while (treeMapVisualisation.treeMapRepaint == false) {
//                    Thread.sleep(50);
//                }
//                Thread.sleep(simulationSpeed);
        }
//        } 
//        catch (InterruptedException ex) {
//            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void stopSimulation() {
        simulationThreadStop = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    simulationThread.join();
                    timeSpinner.setEnabled(true);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    private void animationEnabledBox1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_animationEnabledBox1StateChanged
    }//GEN-LAST:event_animationEnabledBox1StateChanged

    private void simulationSpeedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_simulationSpeedSliderStateChanged
        int sliderValue = simulationSpeedSlider.getValue();
        //value is between 0 and 100
        simulationSpeed = (100 - sliderValue) * 50;


    }//GEN-LAST:event_simulationSpeedSliderStateChanged

    private void orderLabelsEnabledCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderLabelsEnabledCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_orderLabelsEnabledCheckBoxActionPerformed

    private void drawWeightCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawWeightCheckBoxActionPerformed
        treeMapVisualisation.setDrawWeight(drawWeightCheckBox.isSelected());
    }//GEN-LAST:event_drawWeightCheckBoxActionPerformed

    private void animationEnabledBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_animationEnabledBox1ActionPerformed
        treeMapVisualisation.setAnimationEnabled(animationEnabledBox1.isSelected());
    }//GEN-LAST:event_animationEnabledBox1ActionPerformed

    private void buttonShowNewGeneratedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShowNewGeneratedActionPerformed
        int currentTime = (int) timeSpinner.getValue();
        TreeMap tm = visualiser.getNewGeneratedTreeMap(currentTime);
        treeMapVisualisation.updateTreeMap(tm);
    }//GEN-LAST:event_buttonShowNewGeneratedActionPerformed

    private void useStoredButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_useStoredButtonStateChanged
        this.useStored = useStoredButton.isSelected();
        // TODO add your handling code here:
    }//GEN-LAST:event_useStoredButtonStateChanged

    private void useStoredButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useStoredButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useStoredButtonActionPerformed

    private void jButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jButton1StateChanged
        // TODO add your handling code here:


    }//GEN-LAST:event_jButton1StateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        treeMapVisualisation.toIpe();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void performMoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performMoveButtonActionPerformed
        if (visualiser != null && ((String) treeMapSelector.getSelectedItem()).equals("NoMovesIncremental")) {
            visualiser.performMove();
        }
    }//GEN-LAST:event_performMoveButtonActionPerformed

    private void baseLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseLineButtonActionPerformed
        if (visualiser.getCurrentTreeMap() != null) {
            BaseLineGenerator blg = new BaseLineGenerator();
            TreeMap currentTreemap = visualiser.getCurrentTreeMap();

            int currentTime = (int) timeSpinner.getValue();
            DataMap currentDm = visualiser.getDataFacilitator().getData(currentTime);
            DataMap newDm = visualiser.getDataFacilitator().getData(currentTime + 1);

            TreeMap baseLine = blg.generateBaseLine(currentTreemap, currentDm, newDm);
            visualiser.setTreeMap(baseLine);

            RelativeQuadrantStability rStab = new RelativeQuadrantStability();
            rStab.getStability(currentTreemap, baseLine);

            TreeMap newTm = visualiser.getNewGeneratedTreeMap(currentTime + 1);
            double score = rStab.getStability(currentTreemap, newTm);
            double baseScore = rStab.getStability(currentTreemap, baseLine);

            System.out.println("baseScore = " + baseScore + ";" + "score = " + score);

        }
    }//GEN-LAST:event_baseLineButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        showOldData = true;
        showLoadedData();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void baseSeperationSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_baseSeperationSpinnerStateChanged

        // TODO add your handling code here:
        updateUncertaintyVisualisation();
    }//GEN-LAST:event_baseSeperationSpinnerStateChanged

    private void baseWidthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_baseWidthSpinnerStateChanged
        // TODO add your handling code here:
        updateUncertaintyVisualisation();

    }//GEN-LAST:event_baseWidthSpinnerStateChanged

    private void widthAddSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthAddSpinnerStateChanged
        // TODO add your handling code here:
        updateUncertaintyVisualisation();

    }//GEN-LAST:event_widthAddSpinnerStateChanged

    private void baseLuminanceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_baseLuminanceSpinnerStateChanged
        // TODO add your handling code here:
        updateUncertaintyVisualisation();

    }//GEN-LAST:event_baseLuminanceSpinnerStateChanged

    private void luminanceAdditionSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_luminanceAdditionSpinnerStateChanged
        // TODO add your handling code here:
        updateUncertaintyVisualisation();

    }//GEN-LAST:event_luminanceAdditionSpinnerStateChanged

    private void uncertaintySelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_uncertaintySelectorItemStateChanged
        // TODO add your handling code here:
        updateUncertaintyVisualisation();
    }//GEN-LAST:event_uncertaintySelectorItemStateChanged

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        treeMapVisualisation.showLabels(jCheckBox1.isSelected());
        int currentTime = (int) timeSpinner.getValue();
        TreeMap tm = visualiser.getNewGeneratedTreeMap(currentTime);
        treeMapVisualisation.updateTreeMap(tm);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void sdTreemapButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdTreemapButtonActionPerformed
        if (visualiser.getCurrentTreeMap() != null) {
            TreeMap tm = visualiser.getCurrentTreeMap();

            //set sd as targetsize
            tm.setSdAsSize();

            //generate equivalent layout with sd as targetSize.
            TreeMapChangeGenerator tmcg = new TreeMapChangeGenerator(tm);
            TreeMap sdTreeMap = tmcg.fixPositions();

            visualiser.setTreeMap(sdTreeMap);

        }
    }//GEN-LAST:event_sdTreemapButtonActionPerformed

    private void drawUncertaintyCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_drawUncertaintyCheckBoxStateChanged
        // TODO add your handling code here:
        treeMapVisualisation.setDrawUncertainty(drawUncertaintyCheckBox.isSelected());

    }//GEN-LAST:event_drawUncertaintyCheckBoxStateChanged

    private void drawUncertaintyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawUncertaintyCheckBoxActionPerformed

        // TODO add your handling code here:
    }//GEN-LAST:event_drawUncertaintyCheckBoxActionPerformed

    private void sdAsDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sdAsDataButtonActionPerformed
        // TODO add your handling code here:
        visualiser.getDataFacilitator().useSdAsValue();

    }//GEN-LAST:event_sdAsDataButtonActionPerformed

    private void updateUncertaintyVisualisation() {
        if (!initialized) {
            return;
        }

        int baseSep = (int) baseSeperationSpinner.getValue();
        double baseLum = (double) baseLuminanceSpinner.getValue();
        double addLum = (double) luminanceAdditionSpinner.getValue();
        double baseWidth = (double) baseWidthSpinner.getValue();
        int addWidth = (int) widthAddSpinner.getValue();
        String uncertaintyType = (String) uncertaintySelector.getSelectedItem();
        System.out.println("uncertaintyType = " + uncertaintyType);
        UncertaintyParams params = new UncertaintyParams(baseSep, baseWidth, addWidth, baseLum, addLum, uncertaintyType);
        treeMapVisualisation.updateUncertaintyVisParam(params);

        int currentTime = (int) timeSpinner.getValue();
        TreeMap tm = visualiser.getNewGeneratedTreeMap(currentTime);
        treeMapVisualisation.updateTreeMap(tm);
    }

    private void showLoadedData() {
        TreeMapReader tmr = new TreeMapReader();
        int time = (int) timeSpinner.getValue();

        TreeMap tm = tmr.readTreeMap(new File("D:\\Development\\TreemapStability\\output-temp\\4MovesIncrementalLayout\\Hystrix.data\\t" + time + ".rect"));
        visualiser.setTreeMap(tm);
    }

    private void getNewTreeMap() {
        if (visualiser == null || !visualiser.isInitialized()) {
            return;
        }
        int time = (int) timeSpinner.getValue();
        visualiser.getTreeMap(time, useStored, "Test");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox animationEnabledBox1;
    private javax.swing.JSlider animationSpeedSlider;
    private javax.swing.JButton baseLineButton;
    private javax.swing.JSpinner baseLuminanceSpinner;
    private javax.swing.JSpinner baseSeperationSpinner;
    private javax.swing.JSpinner baseWidthSpinner;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonShowNewGenerated;
    private javax.swing.JLabel chanceChanceLabel;
    private javax.swing.JSpinner chanceChanceSpinner;
    private javax.swing.JLabel changeValueLabel;
    private javax.swing.JSpinner changeValueSpinner;
    private javax.swing.JComboBox dataFacilitatorSelector;
    private javax.swing.JCheckBox drawUncertaintyCheckBox;
    private javax.swing.JCheckBox drawWeightCheckBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBoxUncertainty;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner luminanceAdditionSpinner;
    private javax.swing.JLabel maxDepthLabel;
    private javax.swing.JSpinner maxDepthSpinner;
    private javax.swing.JSpinner maxItemSpinner;
    private javax.swing.JLabel maxItemsLabel;
    private javax.swing.JLabel maxSizeLabel;
    private javax.swing.JSpinner maxSizeSpinner;
    private javax.swing.JLabel meanAspectRatio;
    private javax.swing.JLabel minDepthLabel;
    private javax.swing.JSpinner minDepthSpinner;
    private javax.swing.JSpinner minItemSpinner;
    private javax.swing.JLabel minItemsLabel;
    private javax.swing.JLabel minSizeLabel;
    private javax.swing.JSpinner minSizeSpinner;
    private javax.swing.JCheckBox orderEnabledCheckBox;
    private javax.swing.JCheckBox orderLabelsEnabledCheckBox;
    private javax.swing.JButton performMoveButton;
    private javax.swing.JButton sdAsDataButton;
    private javax.swing.JButton sdTreemapButton;
    private javax.swing.JButton simulationButton;
    private javax.swing.JSlider simulationSpeedSlider;
    private javax.swing.JLabel stabilityRelativeValue;
    private javax.swing.JSpinner timeSpinner;
    private javax.swing.JComboBox treeMapSelector;
    private javax.swing.JLabel uncertaintyAllAreaValue;
    private javax.swing.JLabel uncertaintyAllPercentageValue;
    private javax.swing.JLabel uncertaintyAreaValue;
    private javax.swing.JLabel uncertaintyPercentageValue;
    private javax.swing.JComboBox<String> uncertaintySelector;
    private javax.swing.JCheckBox useStoredButton;
    private javax.swing.JPanel visualisationPanel;
    private javax.swing.JSpinner widthAddSpinner;
    // End of variables declaration//GEN-END:variables

    public void setStability(Map<String, Double> stabilities) {
        if (stabilities == null) {
//            stabilityNoModifierValue.setText("Undefined");
//            stabilitySizeValue.setText("Undefined");
//            stabilityDistanceValue.setText("Undefined");
//            stabilityMaxDistanceValue.setText("Undefined");
            stabilityRelativeValue.setText("Undefined");
            return;
        }

        if (stabilities.containsKey("uncertaintyAllPercentage")) {
            double modifierValue = stabilities.get("uncertaintyAllPercentage");
            String value = "" + (Math.floor(modifierValue * 100) / 100);
            //make sure it has 2 decimals
            int decimals = value.substring(value.indexOf(".") + 1).length();

            if (decimals == 1) {
                value += "0";
            }
            if (decimals == 0) {
                value += "00";
            }
            uncertaintyAllPercentageValue.setText("" + value);
        } else {
            uncertaintyAllPercentageValue.setText("Undefined");
        }

        if (stabilities.containsKey("uncertaintyAllArea")) {
            double modifierValue = stabilities.get("uncertaintyAllArea");
            String value = "" + (Math.floor(modifierValue * 100) / 100);
            //make sure it has 2 decimals
            int decimals = value.substring(value.indexOf(".") + 1).length();

            if (decimals == 1) {
                value += "0";
            }
            if (decimals == 0) {
                value += "00";
            }
            uncertaintyAllAreaValue.setText("" + value);
        } else {
            uncertaintyAllAreaValue.setText("Undefined");
        }
        if (stabilities.containsKey("uncertaintyPercentage")) {
            double modifierValue = stabilities.get("uncertaintyPercentage");
            String value = "" + (Math.floor(modifierValue * 100) / 100);
            //make sure it has 2 decimals
            int decimals = value.substring(value.indexOf(".") + 1).length();

            if (decimals == 1) {
                value += "0";
            }
            if (decimals == 0) {
                value += "00";
            }
            uncertaintyPercentageValue.setText("" + value);
        } else {
            uncertaintyPercentageValue.setText("Undefined");
        }
        if (stabilities.containsKey("uncertaintyArea")) {
            double modifierValue = stabilities.get("uncertaintyArea");
            String value = "" + (Math.floor(modifierValue * 100) / 100);
            //make sure it has 2 decimals
            int decimals = value.substring(value.indexOf(".") + 1).length();

            if (decimals == 1) {
                value += "0";
            }
            if (decimals == 0) {
                value += "00";
            }
            uncertaintyAreaValue.setText("" + value);
        } else {
            uncertaintyAreaValue.setText("Undefined");
        }

        if (stabilities.containsKey("stabilityRelative")) {
            double modifierValue = stabilities.get("stabilityRelative");
            String value = "" + (Math.floor(modifierValue * 100) / 100);
            //make sure it has 2 decimals
            int decimals = value.substring(value.indexOf(".") + 1).length();

            if (decimals == 1) {
                value += "0";
            }
            if (decimals == 0) {
                value += "00";
            }
            stabilityRelativeValue.setText("" + value);
        } else {
            stabilityRelativeValue.setText("Undefined");
        }

    }

    public void setAspectRatioBeforeMoves(double meanAr) {
        String value = "" + (Math.floor(meanAr * 100) / 100);
        //make sure it has 2 decimals
        int decimals = value.substring(value.indexOf(".") + 1).length();
        if (decimals == 1) {
            value += "0";
        }
        if (decimals == 0) {
            value += "00";
        }
        meanAspectRatio.setText("" + value);
    }

    public void setAspectRatioAfterMoves(double maxAspectRatio) {
        String value = "" + (Math.floor(maxAspectRatio * 100) / 100);
        //make sure it has 2 decimals
        int decimals = value.substring(value.indexOf(".") + 1).length();

        if (decimals == 1) {
            value += "0";
        }
        if (decimals == 0) {
            value += "00";
        }
        meanAspectRatio.setText("" + value);
    }

}
