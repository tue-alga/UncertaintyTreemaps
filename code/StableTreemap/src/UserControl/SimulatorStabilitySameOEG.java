package UserControl;

import TreeMapGenerator.LocalMoves.LocalMoves;
import TreeMapGenerator.TreeMapGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.Generators.RandomSequentialDataGenerator;
import treemap.ModelController;
import treemap.dataStructure.Rectangle;

/**
 * Tests the influence on the changes on the order-equivalence graph
 *
 * @author Max Sondag
 */

public class SimulatorStabilitySameOEG extends SimulatorMaster {

    public static void main(String args[]) {
        new SimulatorStabilitySameOEG();
    }

    public SimulatorStabilitySameOEG() {
        super();
        treeMapRectangle = new Rectangle(0, 0, 1920, 1080);
        runExperiments();
    }

    private void runExperiments() {
        long startingTime = System.currentTimeMillis();

        int timeSteps = 100;
        List<TreeMapGenerator> generators = getTreeMapGenerators();
        List<DataFacilitator> facilitators = getDataFacilitators();
        int maxRuns = 100;
        int totalExperiments = generators.size() * facilitators.size() * maxRuns;
        System.out.println("totalExperiments = " + totalExperiments);
        int experimentsDone = 0;
        for (TreeMapGenerator generator : generators) {
            System.out.println("starting with  with generator: " + generator.getClass().getName());

            for (DataFacilitator facilitator : facilitators) {
                modelController.newStatisticsFile(new File("experiment/" + generator.getClass().getName() + ";" + generator.getParamaterDescription()),false);

                System.out.println("facilitator.getClass().getName() = " + facilitator.getClass().getName());

                for (int run = 0; run < maxRuns; run++) {
                    //need to reinitialize the generator after every run to make sure it is not persistent
                    //for the local moves algorithms
                    generator = generator.reinitialize();

                    facilitator.reinitializeWithSeed(run);

                    Experiment e = new Experiment(facilitator, generator, timeSteps, this);
                    e.runExperiment();
                    experimentsDone++;
                    System.out.println("Run progress: " + run + "/" + maxRuns);

                    long currentTime = System.currentTimeMillis();
                    long timeDifference = currentTime - startingTime;
                    long timePerExperiment = timeDifference / experimentsDone;
                    long experimentsLeft = totalExperiments - experimentsDone;
                    long timeLeft = timePerExperiment * experimentsLeft;
                    System.out.println("Experiment progress: " + experimentsDone + "/" + totalExperiments);
                    System.out.println("Timeleft = " + timeLeft);
                    System.out.println("Expected time remaining in seconds = " + timeLeft / 1000);
                    System.out.println("Expected time remaining in minutes = " + timeLeft / 60000);
                    System.out.println("Expected time remaining in hours = " + timeLeft / 3600000);
                    System.out.println("\r\n");
                }
                modelController.closeStatisticsOutput();
                modelController = new ModelController(this);

            }

            System.out.println("Done with generator: " + generator.getClass().getName());
        }
        System.out.println("done!");
    }

    private List<TreeMapGenerator> getTreeMapGenerators() {
        List<TreeMapGenerator> generatorList = new ArrayList();
        TreeMapGenerator tmg = null;

        tmg = new LocalMoves(true);
        generatorList.add(tmg);

//        tmg = new moveTest(new File("experiment\\moves"));
//        generatorList.add(tmg);

        return generatorList;
    }

    private List<DataFacilitator> getDataFacilitators() {
        List<DataFacilitator> facilitators = new ArrayList();

        double changeChance = 100;
        int minItemsPerLevel = 5;
        int minDepth = 1;
        int maxDepth = 1;
        int minSize = 1;
        int time = 0;

        int maxItemsPerLevel = 50;
        int maxSize = 100;
        double changeValue = 5;
        int addRemoveChange = 0;

        List<Double> changeVals = new ArrayList();
        changeVals.add(5.0);
        changeVals.add(10.0);
        changeVals.add(25.0);
        changeVals.add(50.0);
        changeVals.add(100.0);
        for (double newChangeValue : changeVals) {
            RandomSequentialDataGenerator faciliator = new RandomSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, newChangeValue, changeChance, time, addRemoveChange, false, "changeValue");
            facilitators.add(faciliator);
        }

        return facilitators;
    }
}
